package me.noramibu.lumentooltips.tooltip.preview;

import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

final class LumenBannerTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private final BannerFlagModel flag =
      new BannerFlagModel(
          Minecraft.getInstance()
              .getEntityModels()
              .bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
  private final BannerPatternLayers patterns;
  private final BannerItem banner;
  private final LumenConfig.PreviewConfig config;

  LumenBannerTooltipComponent(
      ItemStack stack, BannerItem banner, LumenConfig.PreviewConfig config) {
    this.patterns =
        stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
    this.banner = banner;
    this.config = config;
  }

  @Override
  public int getHeight(Font font) {
    return switch (this.config.density) {
      case COMPACT -> 64;
      case VANILLA -> 76;
      case COMFORTABLE -> 88;
    };
  }

  @Override
  public int getWidth(Font font) {
    return switch (this.config.density) {
      case COMPACT -> 36;
      case VANILLA -> 44;
      case COMFORTABLE -> 52;
    };
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    int renderX = x + (width - getWidth(font)) / 2;
    graphics.bannerPattern(
        this.flag,
        this.banner.getColor(),
        this.patterns,
        renderX,
        y,
        renderX + getWidth(font),
        y + getHeight(font));
  }
}
