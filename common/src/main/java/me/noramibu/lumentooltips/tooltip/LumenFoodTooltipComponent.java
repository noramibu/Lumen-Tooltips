package me.noramibu.lumentooltips.tooltip;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.joml.Matrix3x2fStack;

public final class LumenFoodTooltipComponent
    implements Component, FormattedCharSequence, ClientTooltipComponent {
  private static final Identifier ICONS =
      Identifier.fromNamespaceAndPath("lumen_tooltips", "textures/food_icons.png");
  private static final Identifier FOOD_EMPTY =
      Identifier.withDefaultNamespace("hud/food_empty");
  private static final Identifier FOOD_HALF =
      Identifier.withDefaultNamespace("hud/food_half");
  private static final Identifier FOOD_FULL =
      Identifier.withDefaultNamespace("hud/food_full");
  private static final Identifier FOOD_EMPTY_HUNGER =
      Identifier.withDefaultNamespace("hud/food_empty_hunger");
  private static final Identifier FOOD_HALF_HUNGER =
      Identifier.withDefaultNamespace("hud/food_half_hunger");
  private static final Identifier FOOD_FULL_HUNGER =
      Identifier.withDefaultNamespace("hud/food_full_hunger");

  private final int hunger;
  private final float saturation;
  private final boolean rotten;
  private final boolean showHunger;
  private final boolean showSaturation;
  private final int hungerIcons;
  private final int saturationIcons;
  private final String hungerText;
  private final String saturationText;

  public LumenFoodTooltipComponent(
      int hunger,
      float saturation,
      boolean rotten,
      boolean showHunger,
      boolean showSaturation) {
    this.hunger = hunger;
    this.saturation = saturation;
    this.rotten = rotten;
    this.showHunger = showHunger;
    this.showSaturation = showSaturation;

    int hungerIcons = (int) Math.ceil(Math.abs(hunger) / 2.0F);
    this.hungerText = hungerIcons > 10 ? "x" + Integer.signum(hunger) * hungerIcons : null;
    this.hungerIcons = hungerIcons > 10 ? 1 : hungerIcons;

    int saturationIcons = (int) Math.ceil(Math.abs(saturation) / 2.0F);
    this.saturationText =
        saturationIcons > 10 || saturationIcons == 0
            ? "x" + (saturation < 0.0F ? -saturationIcons : saturationIcons)
            : null;
    this.saturationIcons = saturationIcons > 10 || saturationIcons == 0 ? 1 : saturationIcons;
  }

  @Override
  public Style getStyle() {
    return Style.EMPTY;
  }

  @Override
  public ComponentContents getContents() {
    return PlainTextContents.EMPTY;
  }

  @Override
  public List<Component> getSiblings() {
    return List.of();
  }

  @Override
  public FormattedCharSequence getVisualOrderText() {
    return this;
  }

  @Override
  public boolean accept(FormattedCharSink visitor) {
    return true;
  }

  @Override
  public int getHeight(Font font) {
    return 12;
  }

  @Override
  public int getWidth(Font font) {
    int hungerWidth =
        this.showHunger ? this.hungerIcons * 9 + textWidth(font, this.hungerText) : 0;
    int saturationWidth =
        this.showSaturation
            ? this.saturationIcons * 7 + textWidth(font, this.saturationText)
            : 0;
    return hungerWidth
        + saturationWidth
        + (this.showHunger && this.showSaturation ? 2 : 0);
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    if (this.showHunger) {
      drawHunger(font, graphics, x, y);
    }
    if (this.showSaturation) {
      int hungerWidth =
          this.showHunger ? this.hungerIcons * 9 + textWidth(font, this.hungerText) + 2 : 0;
      drawSaturation(font, graphics, x + hungerWidth, y + 1);
    }
  }

  private void drawHunger(Font font, GuiGraphicsExtractor graphics, int startX, int y) {
    int x = startX + (this.hungerIcons - 1) * 9;
    int hunger = Math.abs(this.hunger);
    for (int value = 0; value < this.hungerIcons * 2; value += 2) {
      graphics.blitSprite(
          RenderPipelines.GUI_TEXTURED,
          this.rotten ? FOOD_EMPTY_HUNGER : FOOD_EMPTY,
          x,
          y,
          9,
          9);
      if (hunger > value) {
        boolean half = hunger - 1 == value;
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            this.rotten
                ? half ? FOOD_HALF_HUNGER : FOOD_FULL_HUNGER
                : half ? FOOD_HALF : FOOD_FULL,
            x,
            y,
            9,
            9);
      }
      x -= 9;
    }
    drawSmallText(font, graphics, this.hungerText, x + 18, y + 2);
  }

  private void drawSaturation(Font font, GuiGraphicsExtractor graphics, int startX, int y) {
    int x = startX + (this.saturationIcons - 1) * 7;
    float saturation = Math.abs(this.saturation);
    for (int value = 0; value < this.saturationIcons * 2; value += 2) {
      float iconValue = (saturation - value) / 2.0F;
      int textureX = saturationTextureX(iconValue);
      int color = saturation <= value ? 0x80FFFFFF : 0xFFFFFFFF;
      graphics.blit(
          RenderPipelines.GUI_TEXTURED,
          ICONS,
          x,
          y,
          textureX,
          this.saturation >= 0.0F ? 27 : 34,
          7,
          7,
          256,
          256,
          color);
      x -= 7;
    }
    drawSmallText(font, graphics, this.saturationText, x + 14, y + 1);
  }

  private static int textWidth(Font font, String text) {
    return text == null ? 0 : font.width(text);
  }

  private static int saturationTextureX(float value) {
    if (value >= 1.0F) {
      return 21;
    }
    if (value > 0.5F) {
      return 14;
    }
    if (value > 0.25F) {
      return 7;
    }
    return value > 0.0F ? 0 : 28;
  }

  private static void drawSmallText(
      Font font, GuiGraphicsExtractor graphics, String text, int x, int y) {
    if (text == null) {
      return;
    }
    Matrix3x2fStack pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(x, y);
    pose.scale(0.75F, 0.75F);
    graphics.text(font, text, 2, 1, 0xFFAAAAAA);
    pose.popMatrix();
  }
}
