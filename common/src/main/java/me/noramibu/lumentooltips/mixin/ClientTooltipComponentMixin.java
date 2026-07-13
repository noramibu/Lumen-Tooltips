package me.noramibu.lumentooltips.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
  @Inject(method = "create(Lnet/minecraft/util/FormattedCharSequence;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
  private static void lumenTooltips$createText(
      FormattedCharSequence text,
      CallbackInfoReturnable<ClientTooltipComponent> callbackInfo) {
    if (text instanceof ClientTooltipComponent clientComponent) {
      callbackInfo.setReturnValue(clientComponent);
    }
  }

  @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
  private static void lumenTooltips$create(
      TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> callbackInfo) {
    if (component instanceof ClientTooltipComponent clientComponent) {
      callbackInfo.setReturnValue(clientComponent);
    }
  }
}
