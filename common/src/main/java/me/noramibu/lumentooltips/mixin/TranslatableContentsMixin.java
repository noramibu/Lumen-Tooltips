package me.noramibu.lumentooltips.mixin;

import java.util.Optional;
import me.noramibu.lumentooltips.tooltip.LumenTextGuard;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @ModifyVariable(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At("HEAD"),
            argsOnly = true,
            order = 900)
    private <T> FormattedText.ContentConsumer<T> lumenTooltips$guardPlainText(FormattedText.ContentConsumer<T> output) {
        return LumenTextGuard.guard(output);
    }

    @ModifyVariable(
            method =
                    "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            argsOnly = true,
            order = 900)
    private <T> FormattedText.StyledContentConsumer<T> lumenTooltips$guardStyledText(
            FormattedText.StyledContentConsumer<T> output) {
        return LumenTextGuard.guard(output);
    }

    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void lumenTooltips$limitPlainText(
            FormattedText.ContentConsumer<T> output, CallbackInfoReturnable<Optional<T>> callbackInfo) {
        if (!LumenTextGuard.enterTranslation(output)) {
            callbackInfo.setReturnValue(LumenTextGuard.reject(output));
        }
    }

    @Inject(
            method =
                    "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void lumenTooltips$limitStyledText(
            FormattedText.StyledContentConsumer<T> output,
            Style style,
            CallbackInfoReturnable<Optional<T>> callbackInfo) {
        if (!LumenTextGuard.enterTranslation(output)) {
            callbackInfo.setReturnValue(LumenTextGuard.reject(output, style));
        }
    }

    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At("RETURN"))
    private <T> void lumenTooltips$finishPlainText(
            FormattedText.ContentConsumer<T> output, CallbackInfoReturnable<Optional<T>> callbackInfo) {
        LumenTextGuard.exitTranslation(output);
    }

    @Inject(
            method =
                    "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("RETURN"))
    private <T> void lumenTooltips$finishStyledText(
            FormattedText.StyledContentConsumer<T> output,
            Style style,
            CallbackInfoReturnable<Optional<T>> callbackInfo) {
        LumenTextGuard.exitTranslation(output);
    }
}
