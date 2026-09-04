package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.tooltip.layout.LumenTooltipLayout;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultTooltipPositioner.class)
public abstract class DefaultTooltipPositionerMixin {
    @Inject(method = "positionTooltip", at = @At("RETURN"), cancellable = true)
    private void lumenTooltips$positionTooltip(
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            int tooltipWidth,
            int tooltipHeight,
            CallbackInfoReturnable<Vector2ic> callbackInfo) {
        callbackInfo.setReturnValue(LumenTooltipLayout.position(
                callbackInfo.getReturnValue(), screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight));
    }
}
