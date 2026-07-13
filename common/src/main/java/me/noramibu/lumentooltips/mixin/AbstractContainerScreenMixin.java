package me.noramibu.lumentooltips.mixin;

import java.util.ArrayList;
import java.util.List;
import me.noramibu.lumentooltips.client.LumenItemEditor;
import me.noramibu.lumentooltips.client.screen.LumenContainerOpener;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.config.LumenInputBinding;
import me.noramibu.lumentooltips.tooltip.LumenTooltipAppender;
import me.noramibu.lumentooltips.tooltip.preview.LumenEnderChestMemory;
import me.noramibu.lumentooltips.tooltip.preview.LumenTooltipPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen
    implements MenuAccess<T> {
  @Shadow
  protected Slot hoveredSlot;

  protected AbstractContainerScreenMixin(Component title) {
    super(title);
  }

  @Shadow
  public abstract T getMenu();

  @Shadow
  protected abstract List<Component> getTooltipFromContainerItem(ItemStack stack);

  @Inject(method = "removed", at = @At("HEAD"))
  private void lumenTooltips$captureEnderChest(CallbackInfo callbackInfo) {
    LumenEnderChestMemory.capture(getMenu(), this.title);
  }

  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void lumenTooltips$handleHoveredItem(
      KeyEvent event, CallbackInfoReturnable<Boolean> callbackInfo) {
    if (this.hoveredSlot == null
        || this.hoveredSlot.getItem().isEmpty()
        || !getMenu().getCarried().isEmpty()) {
      return;
    }
    if (lumenTooltips$saveToItemEditor(event)
        || lumenTooltips$openItemEditor(event)
        || LumenContainerOpener.tryOpen(this.hoveredSlot.getItem(), event)) {
      callbackInfo.setReturnValue(true);
    }
  }

  @Redirect(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getTooltipFromContainerItem(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
  private List<Component> lumenTooltips$appendItemEditorHint(
      AbstractContainerScreen<?> screen, ItemStack stack) {
    List<Component> tooltip = getTooltipFromContainerItem(stack);
    boolean potionPreview = LumenTooltipPreview.isPotionPreviewActive(stack);
    if (potionPreview) {
      tooltip = new ArrayList<>(tooltip);
      tooltip.removeIf(
          line ->
              line.getContents() instanceof TranslatableContents contents
                  && contents.getKey().startsWith("itemGroup."));
    }
    LumenConfig config = LumenConfigManager.current();
    if (!config.modules.tooltip.showControlHints
        || !getMenu().getCarried().isEmpty()
        || !LumenItemEditor.isAvailable()) {
      return tooltip;
    }

    if (!potionPreview) {
      tooltip = new ArrayList<>(tooltip);
    }
    Minecraft minecraft = Minecraft.getInstance();
    Slot inventorySlot = lumenTooltips$backingSlot();
    String editKey = config.controls.itemEditorKey;
    if (config.modules.tooltip.showEditItemHint
        && minecraft.player != null
        && inventorySlot != null
        && inventorySlot.container == minecraft.player.getInventory()
        && !LumenInputBinding.UNBOUND.equals(editKey)) {
      tooltip.add(
          LumenTooltipAppender.controlHint(
              editKey, "tooltip.lumen_tooltips.action.edit_item"));
    }
    String saveKey = config.modules.itemEditor.saveKey;
    if (config.modules.tooltip.showSaveItemHint
        && !LumenInputBinding.UNBOUND.equals(saveKey)) {
      tooltip.add(
          LumenTooltipAppender.controlHint(
              saveKey, "tooltip.lumen_tooltips.action.save_item"));
    }
    return tooltip;
  }

  @Unique
  private boolean lumenTooltips$saveToItemEditor(KeyEvent event) {
    return !(this.getFocused() instanceof EditBox text && text.canConsumeInput())
        && LumenItemEditor.trySaveToStorage(this.hoveredSlot.getItem(), event);
  }

  @Unique
  private boolean lumenTooltips$openItemEditor(KeyEvent event) {
    Minecraft minecraft = Minecraft.getInstance();
    Slot inventorySlot = lumenTooltips$backingSlot();
    if (!LumenItemEditor.isAvailable()
        || minecraft.player == null
        || inventorySlot == null
        || inventorySlot.container != minecraft.player.getInventory()
        || !LumenInputBinding.matches(
            LumenConfigManager.current().controls.itemEditorKey, event)) {
      return false;
    }
    return LumenItemEditor.openInventorySlot(inventorySlot.getContainerSlot());
  }

  @Unique
  private Slot lumenTooltips$backingSlot() {
    return this.hoveredSlot instanceof CreativeSlotWrapperAccessor wrapper
        ? wrapper.lumenTooltips$getTarget()
        : this.hoveredSlot;
  }
}
