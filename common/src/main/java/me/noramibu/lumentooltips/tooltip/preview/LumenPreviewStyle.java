package me.noramibu.lumentooltips.tooltip.preview;

import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

final class LumenPreviewStyle {
  private static final Identifier BACKGROUND =
      Identifier.withDefaultNamespace("tooltip/background");
  private static final Identifier FRAME = Identifier.withDefaultNamespace("tooltip/frame");

  private LumenPreviewStyle() {}

  static int slotSize(LumenConfig.PreviewConfig config) {
    return switch (config.density) {
      case COMPACT -> 16;
      case VANILLA -> 18;
      case COMFORTABLE -> 20;
    };
  }

  static int padding(LumenConfig.PreviewConfig config) {
    return switch (config.density) {
      case COMPACT -> 4;
      case VANILLA -> 6;
      case COMFORTABLE -> 8;
    };
  }

  static int entityWidth(LumenConfig.PreviewConfig config) {
    return switch (config.density) {
      case COMPACT -> 72;
      case VANILLA -> 80;
      case COMFORTABLE -> 96;
    };
  }

  static int entityHeight(LumenConfig.PreviewConfig config) {
    return switch (config.density) {
      case COMPACT -> 56;
      case VANILLA -> 64;
      case COMFORTABLE -> 76;
    };
  }

  static int mapSize(LumenConfig.PreviewConfig config) {
    return switch (config.density) {
      case COMPACT -> 96;
      case VANILLA -> 128;
      case COMFORTABLE -> 144;
    };
  }

  static void drawPanel(
      GuiGraphicsExtractor graphics,
      int x,
      int y,
      int width,
      int height,
      @Nullable Identifier style) {
    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED,
        sprite(style, "_background", BACKGROUND),
        x,
        y,
        width,
        height);
    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED, sprite(style, "_frame", FRAME), x, y, width, height);
  }

  static int blend(int base, int tint, int percent) {
    int amount = Math.clamp(percent, 0, 100);
    int inverse = 100 - amount;
    int red = ((base >> 16 & 255) * inverse + (tint >> 16 & 255) * amount) / 100;
    int green = ((base >> 8 & 255) * inverse + (tint >> 8 & 255) * amount) / 100;
    int blue = ((base & 255) * inverse + (tint & 255) * amount) / 100;
    return 0xFF000000 | red << 16 | green << 8 | blue;
  }

  private static Identifier sprite(
      @Nullable Identifier style, String suffix, Identifier fallback) {
    return style == null
        ? fallback
        : Identifier.fromNamespaceAndPath(
            style.getNamespace(), "tooltip/" + style.getPath() + suffix);
  }
}
