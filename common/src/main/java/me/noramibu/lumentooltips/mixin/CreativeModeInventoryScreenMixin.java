package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.client.screen.LumenContainerOpener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin
    extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
  protected CreativeModeInventoryScreenMixin(
      CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
  private void lumenTooltips$openHoveredItem(
      KeyEvent event, CallbackInfoReturnable<Boolean> callbackInfo) {
    if (this.hoveredSlot != null
        && this.hoveredSlot.hasItem()
        && this.menu.getCarried().isEmpty()
        && LumenContainerOpener.tryOpen(this.hoveredSlot.getItem(), event)) {
      callbackInfo.setReturnValue(true);
    }
  }
}
