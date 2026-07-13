package me.noramibu.lumentooltips.tooltip.preview;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public final class LumenBundleTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private static final Identifier SLOT =
      Identifier.withDefaultNamespace("container/bundle/slot_background");
  private static final Identifier SELECTED_BACK =
      Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
  private static final Identifier SELECTED_FRONT =
      Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
  private static final Identifier BAR_BORDER =
      Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
  private static final Identifier BAR_FILL =
      Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
  private static final Identifier BAR_FULL =
      Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
  private static final Component BUNDLE_FULL = Component.translatable("item.minecraft.bundle.full");
  private static final int COLUMNS = 8;
  private static final int SLOT_SIZE = 24;
  private static final int PADDING = 8;
  private static final int BAR_WIDTH = 94;
  private static final int BAR_HEIGHT = 13;

  private final List<ItemStack> items;
  private final int rows;
  private final int selectedIndex;
  private final float fullness;

  public LumenBundleTooltipComponent(BundleContents contents) {
    this.items = contents.itemCopyStream().toList();
    this.rows = Math.max(1, (this.items.size() + COLUMNS - 1) / COLUMNS);
    this.selectedIndex = contents.getSelectedItemIndex();
    this.fullness = contents.weight().result().map(Number::floatValue).orElse(0.0F);
  }

  @Override
  public int getHeight(Font font) {
    return PADDING * 2 + this.rows * SLOT_SIZE + BAR_HEIGHT + 4;
  }

  @Override
  public int getWidth(Font font) {
    return PADDING * 2 + COLUMNS * SLOT_SIZE;
  }

  @Override
  public boolean showTooltipWithItemInHand() {
    return true;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    for (int index = 0; index < this.items.size(); index++) {
      ItemStack item = this.items.get(index);
      if (item.isEmpty()) {
        continue;
      }
      int slotX = x + PADDING + index % COLUMNS * SLOT_SIZE;
      int slotY = y + PADDING + index / COLUMNS * SLOT_SIZE;
      boolean selected = index == this.selectedIndex;
      graphics.blitSprite(
          RenderPipelines.GUI_TEXTURED,
          selected ? SELECTED_BACK : SLOT,
          slotX,
          slotY,
          SLOT_SIZE,
          SLOT_SIZE);
      int itemOffset = 4;
      graphics.item(item, slotX + itemOffset, slotY + itemOffset, 0);
      graphics.itemDecorations(font, item, slotX + itemOffset, slotY + itemOffset);
      if (selected) {
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            SELECTED_FRONT,
            slotX,
            slotY,
            SLOT_SIZE,
            SLOT_SIZE);
      }
    }
    drawSelectedItemTooltip(font, graphics, x, y, width);
    drawProgressBar(
        font,
        graphics,
        x + (getWidth(font) - BAR_WIDTH) / 2,
        y + getHeight(font) - BAR_HEIGHT - 4);
  }

  private void drawSelectedItemTooltip(
      Font font, GuiGraphicsExtractor graphics, int x, int y, int width) {
    if (this.selectedIndex < 0 || this.selectedIndex >= this.items.size()) {
      return;
    }
    ItemStack item = this.items.get(this.selectedIndex);
    Component name = item.getStyledHoverName();
    int nameWidth = font.width(name);
    graphics.tooltip(
        font,
        List.of(ClientTooltipComponent.create(name.getVisualOrderText())),
        x + width / 2 - 12 - nameWidth / 2,
        y - 37,
        DefaultTooltipPositioner.INSTANCE,
        item.get(DataComponents.TOOLTIP_STYLE));
  }

  private void drawProgressBar(
      Font font, GuiGraphicsExtractor graphics, int x, int y) {
    float fullness = Math.clamp(this.fullness, 0.0F, 1.0F);
    int fillWidth = Math.round(fullness * BAR_WIDTH);
    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED,
        fullness >= 1.0F ? BAR_FULL : BAR_FILL,
        x + 1,
        y,
        fillWidth,
        BAR_HEIGHT);
    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED, BAR_BORDER, x, y, BAR_WIDTH, BAR_HEIGHT);
    Component label =
        fullness >= 1.0F
            ? BUNDLE_FULL
            : Component.literal(Math.round(fullness * 100.0F) + "%");
    graphics.centeredText(font, label, x + BAR_WIDTH / 2, y + 3, CommonColors.WHITE);
  }

}
