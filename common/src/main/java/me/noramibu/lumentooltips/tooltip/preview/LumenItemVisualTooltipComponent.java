package me.noramibu.lumentooltips.tooltip.preview;

import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

final class LumenItemVisualTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private final ItemStack stack;
  private final LumenConfig.PreviewConfig config;

  LumenItemVisualTooltipComponent(ItemStack stack, LumenConfig.PreviewConfig config) {
    this.stack = stack.copy();
    this.config = config;
  }

  @Override
  public int getHeight(Font font) {
    return size() + padding() * 2;
  }

  @Override
  public int getWidth(Font font) {
    return getHeight(font);
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    int renderX = x + (width - getWidth(font)) / 2;
    float centerX = renderX + getWidth(font) / 2.0F;
    float centerY = y + getHeight(font) / 2.0F;
    float scale = size() / 16.0F;
    graphics.pose().pushMatrix();
    graphics.pose().translate(centerX, centerY);
    graphics.pose().rotate((float) Math.toRadians(-20.0));
    graphics.pose().translate(-centerX, -centerY);
    graphics.pose().translate(renderX + padding(), y + padding());
    graphics.pose().scale(scale, scale);
    graphics.item(this.stack, 0, 0, 0);
    graphics.pose().popMatrix();
  }

  private int size() {
    return switch (this.config.density) {
      case COMPACT -> 40;
      case VANILLA -> 48;
      case COMFORTABLE -> 56;
    };
  }

  private int padding() {
    return LumenPreviewStyle.padding(this.config) + 2;
  }
}
