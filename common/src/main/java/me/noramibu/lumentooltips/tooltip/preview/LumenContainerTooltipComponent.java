package me.noramibu.lumentooltips.tooltip.preview;

import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class LumenContainerTooltipComponent
    implements TooltipComponent, ClientTooltipComponent {
  private static final Identifier TEXTURE =
      Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png");
  private static final int WIDTH = 176;
  private static final int FULL_HEIGHT = 76;
  private static final int COMPACT_HEIGHT = 67;
  private static final int COMPACT_HEADER_HEIGHT = 6;
  private static final int COMPACT_CONTENT_Y = 15;
  private static final int COLUMNS = 9;

  private final ItemStack[] items;
  private final int accent;
  private final int hiddenItems;
  private final Component title;
  private final LumenConfig.PreviewConfig config;

  LumenContainerTooltipComponent(
      ItemStack[] items,
      int accent,
      int hiddenItems,
      @Nullable Component title,
      LumenConfig.PreviewConfig config) {
    this.items = items;
    this.accent = accent;
    this.hiddenItems = Math.max(0, hiddenItems);
    this.title = title;
    this.config = config;
  }

  @Override
  public int getHeight(Font font) {
    return this.title == null ? COMPACT_HEIGHT : FULL_HEIGHT;
  }

  @Override
  public int getWidth(Font font) {
    return WIDTH;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    int panelX = x + Math.max(0, (width - WIDTH) / 2);
    int tint =
        this.config.accents
            ? LumenPreviewStyle.blend(
                0xFFFFFFFF, this.accent, this.config.containerTintPercent)
            : 0xFFFFFFFF;
    int itemY;
    if (this.title == null) {
      graphics.blit(
          RenderPipelines.GUI_TEXTURED,
          TEXTURE,
          panelX,
          y,
          0,
          0,
          WIDTH,
          COMPACT_HEADER_HEIGHT,
          256,
          256,
          tint);
      graphics.blit(
          RenderPipelines.GUI_TEXTURED,
          TEXTURE,
          panelX,
          y + COMPACT_HEADER_HEIGHT,
          0,
          COMPACT_CONTENT_Y,
          WIDTH,
          FULL_HEIGHT - COMPACT_CONTENT_Y,
          256,
          256,
          tint);
      itemY = y + COMPACT_HEADER_HEIGHT + 18 - COMPACT_CONTENT_Y;
    } else {
      graphics.blit(
          RenderPipelines.GUI_TEXTURED,
          TEXTURE,
          panelX,
          y,
          0,
          0,
          WIDTH,
          FULL_HEIGHT,
          256,
          256,
          tint);
      graphics.text(font, this.title, panelX + 8, y + 6, 0xFF404040, false);
      itemY = y + 18;
    }
    for (int index = 0; index < this.items.length; index++) {
      ItemStack item = this.items[index];
      if (item.isEmpty()) {
        continue;
      }
      int itemX = panelX + 8 + index % COLUMNS * 18;
      int rowY = itemY + index / COLUMNS * 18;
      graphics.item(item, itemX, rowY, 0);
      if (this.config.showContainerCounts) {
        graphics.itemDecorations(font, item, itemX, rowY);
      }
    }
    if (this.hiddenItems > 0) {
      graphics.text(
          font, "+" + this.hiddenItems, panelX + 155, itemY + 42, 0xFFFFFFFF, true);
    }
  }
}
