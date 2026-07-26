package me.noramibu.lumentooltips.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.tooltip.LumenTextGuard;
import me.noramibu.lumentooltips.tooltip.layout.LumenTooltipLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
  @Shadow private @Nullable Runnable deferredTooltip;

  @Shadow
  public abstract int guiWidth();

  @Shadow
  public abstract void renderTooltip(
      Font font,
      List<ClientTooltipComponent> lines,
      int x,
      int y,
      ClientTooltipPositioner positioner,
      @Nullable Identifier background);

  @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At("HEAD"), cancellable = true)
  private void lumenTooltips$wrapComponentTooltip(
      Font font,
      List<Component> lines,
      Optional<TooltipComponent> image,
      int x,
      int y,
      @Nullable Identifier background,
      CallbackInfo callbackInfo) {
    List<Component> safeLines = LumenTextGuard.protectTooltip(lines);
    List<FormattedCharSequence> wrapped =
        LumenTooltipLayout.wrapTextIfNeeded(font, safeLines, guiWidth());
    if (wrapped == null && safeLines == lines) {
      return;
    }
    List<FormattedCharSequence> visualLines =
        wrapped == null
            ? safeLines.stream().map(Component::getVisualOrderText).toList()
            : wrapped;
    List<ClientTooltipComponent> components =
        new ArrayList<>(visualLines.stream().map(ClientTooltipComponent::create).toList());
    image.ifPresent(
        component ->
            components.add(
                components.isEmpty() ? 0 : 1, ClientTooltipComponent.create(component)));
    if (this.deferredTooltip == null) {
      this.deferredTooltip =
          () ->
              renderTooltip(
                  font, components, x, y, DefaultTooltipPositioner.INSTANCE, background);
    }
    callbackInfo.cancel();
  }

  @Inject(method = "renderDeferredElements", at = @At("RETURN"))
  private void lumenTooltips$finishTooltipFrame(CallbackInfo callbackInfo) {
    LumenTooltipLayout.finishFrame();
  }
}
