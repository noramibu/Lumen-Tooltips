package me.noramibu.lumentooltips.mixin;

import java.util.Optional;
import me.noramibu.lumentooltips.tooltip.LumenTextGuard;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Component.class)
public interface ComponentMixin {
    @ModifyVariable(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At("HEAD"),
            argsOnly = true,
            order = 900)
    private <T> FormattedText.ContentConsumer<T> lumenTooltips$guardPlainText(FormattedText.ContentConsumer<T> output) {
        return LumenTextGuard.guardComponent(output);
    }

    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void lumenTooltips$inspectPlainText(
            FormattedText.ContentConsumer<T> output, CallbackInfoReturnable<Optional<T>> callbackInfo) {
        Component component = (Component) this;
        if (LumenTextGuard.shouldInspect(output, component) && !LumenTextGuard.inspect(component)) {
            callbackInfo.setReturnValue(LumenTextGuard.reject(output));
        }
    }

    @ModifyVariable(
            method =
                    "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            argsOnly = true,
            order = 900)
    private <T> FormattedText.StyledContentConsumer<T> lumenTooltips$guardStyledText(
            FormattedText.StyledContentConsumer<T> output) {
        return LumenTextGuard.guardComponent(output);
    }

    @Inject(
            method =
                    "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void lumenTooltips$inspectStyledText(
            FormattedText.StyledContentConsumer<T> output,
            Style style,
            CallbackInfoReturnable<Optional<T>> callbackInfo) {
        Component component = (Component) this;
        if (LumenTextGuard.shouldInspect(output, component) && !LumenTextGuard.inspect(component)) {
            callbackInfo.setReturnValue(LumenTextGuard.reject(output, style));
        }
    }
}
