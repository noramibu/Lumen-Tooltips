package me.noramibu.lumentooltips.mixin;

import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.tooltip.LumenTooltipAppender;
import me.noramibu.lumentooltips.tooltip.preview.LumenTooltipPreview;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStack.class, priority = 1500)
public abstract class ItemStackTooltipMixin {
  @ModifyVariable(method = "getTooltipLines", at = @At("STORE"), ordinal = 0)
  private TooltipDisplay lumenTooltips$showConfiguredComponents(TooltipDisplay display) {
    LumenConfig.TooltipConfig config = LumenConfigManager.current().modules.tooltip;
    if (!config.ignoreHideTooltip && config.ignoredHiddenComponents.isEmpty()) {
      return display;
    }
    TooltipDisplay visible =
        config.ignoreHideTooltip && display.hideTooltip()
            ? new TooltipDisplay(false, display.hiddenComponents())
            : display;
    for (DataComponentType<?> component : display.hiddenComponents()) {
      if (config.ignoredHiddenComponents.contains(
          BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component).toString())) {
        visible = visible.withHidden(component, false);
      }
    }
    return visible;
  }

  @Inject(method = "getTooltipLines", at = @At("RETURN"))
  @SuppressWarnings("DataFlowIssue")
  private void lumenTooltips$appendTooltip(
      Item.TooltipContext context,
      Player player,
      TooltipFlag flag,
      CallbackInfoReturnable<List<Component>> callbackInfo) {
    LumenTooltipAppender.append(
        context, (ItemStack) (Object) this, player, callbackInfo.getReturnValue(), flag);
  }

  @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
  private void lumenTooltips$getTooltipImage(
      CallbackInfoReturnable<Optional<TooltipComponent>> callbackInfo) {
    Optional<TooltipComponent> preview = LumenTooltipPreview.create((ItemStack) (Object) this);
    if (preview.isPresent()) {
      callbackInfo.setReturnValue(preview);
    }
  }
}
