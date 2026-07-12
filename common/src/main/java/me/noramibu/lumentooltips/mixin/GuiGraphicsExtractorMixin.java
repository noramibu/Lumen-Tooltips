package me.noramibu.lumentooltips.mixin;

import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.tooltip.layout.LumenTooltipLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
  @Shadow
  public abstract int guiWidth();

  @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At("HEAD"), cancellable = true)
  @SuppressWarnings("DataFlowIssue")
  private void lumenTooltips$wrapComponentTooltip(
      Font font,
      List<Component> lines,
      Optional<TooltipComponent> image,
      int x,
      int y,
      Identifier background,
      CallbackInfo callbackInfo) {
    List<FormattedCharSequence> wrapped =
        LumenTooltipLayout.wrapTextIfNeeded(font, lines, guiWidth());
    if (wrapped == null) {
      return;
    }
    ((GuiGraphicsExtractor) (Object) this)
        .setTooltipForNextFrame(
            font, wrapped, image, DefaultTooltipPositioner.INSTANCE, x, y, false, background);
    callbackInfo.cancel();
  }

  @Inject(method = "extractDeferredElements", at = @At("RETURN"))
  private void lumenTooltips$finishTooltipFrame(CallbackInfo callbackInfo) {
    LumenTooltipLayout.finishFrame();
  }
}
