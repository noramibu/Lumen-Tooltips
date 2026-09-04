package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.tooltip.layout.LumenTooltipLayout;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void lumenTooltips$scrollTooltip(
            long window, double horizontalAmount, double verticalAmount, CallbackInfo callbackInfo) {
        if (LumenTooltipLayout.scroll(verticalAmount)) {
            callbackInfo.cancel();
        }
    }
}
