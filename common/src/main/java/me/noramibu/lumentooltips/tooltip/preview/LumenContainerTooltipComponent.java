package me.noramibu.lumentooltips.tooltip.preview;

import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class LumenContainerTooltipComponent implements TooltipComponent, ClientTooltipComponent {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png");
    private static final int BORDER = 7;
    private static final int SLOT_SIZE = 18;
    private static final int RIGHT_BORDER_X = 169;
    private static final int BOTTOM_Y = 159;

    private final ItemStack[] items;
    private final int columns;
    private final int rows;
    private final int accent;
    private final int hiddenItems;
    private final Component title;
    private final LumenConfig.PreviewConfig config;

    LumenContainerTooltipComponent(
            ItemStack[] items,
            int columns,
            int accent,
            int hiddenItems,
            @Nullable Component title,
            LumenConfig.PreviewConfig config) {
        this.items = items;
        this.columns = columns;
        this.rows = Math.max(1, (items.length + columns - 1) / columns);
        this.accent = accent;
        this.hiddenItems = Math.max(0, hiddenItems);
        this.title = title;
        this.config = config;
    }

    @Override
    public int getHeight(Font font) {
        return headerHeight() + this.rows * SLOT_SIZE + BORDER;
    }

    @Override
    public int getWidth(Font font) {
        return BORDER * 2 + this.columns * SLOT_SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        int panelWidth = getWidth(font);
        int availableSpace = Math.max(0, width - panelWidth);
        int panelX = x
                + switch (this.config.containerAlignment) {
                    case LEFT -> 0;
                    case CENTER -> availableSpace / 2;
                    case RIGHT -> availableSpace;
                };
        int tint = this.config.accents
                ? LumenPreviewStyle.blend(0xFFFFFFFF, this.accent, this.config.containerTintPercent)
                : 0xFFFFFFFF;
        if (this.title == null) {
            drawStrip(graphics, panelX, y, 0, 6, tint);
            drawStrip(graphics, panelX, y + 6, 15, 2, tint);
        } else {
            drawStrip(graphics, panelX, y, 0, headerHeight(), tint);
        }
        for (int row = 0; row < this.rows; row++) {
            drawStrip(graphics, panelX, y + headerHeight() + row * SLOT_SIZE, 17, SLOT_SIZE, tint);
        }
        drawStrip(graphics, panelX, y + headerHeight() + this.rows * SLOT_SIZE, BOTTOM_Y, BORDER, tint);
        if (this.title != null) {
            graphics.drawString(
                    font,
                    Language.getInstance().getVisualOrder(font.substrByWidth(this.title, panelWidth - 16)),
                    panelX + 8,
                    y + 6,
                    0xFF404040,
                    false);
        }
        int itemY = y + headerHeight() + 1;
        for (int index = 0; index < this.items.length; index++) {
            ItemStack item = this.items[index];
            if (item.isEmpty()) {
                continue;
            }
            int itemX = panelX + BORDER + 1 + index % this.columns * SLOT_SIZE;
            int rowY = itemY + index / this.columns * SLOT_SIZE;
            graphics.renderItem(item, itemX, rowY, 0);
            if (this.config.showContainerCounts) {
                graphics.renderItemDecorations(font, item, itemX, rowY);
            }
        }
        if (this.hiddenItems > 0) {
            String overflow = "+" + this.hiddenItems;
            graphics.drawString(
                    font,
                    overflow,
                    panelX + panelWidth - BORDER - font.width(overflow),
                    itemY + this.rows * SLOT_SIZE - 12,
                    0xFFFFFFFF,
                    true);
        }
    }

    private int headerHeight() {
        return this.title == null ? 8 : 17;
    }

    private void drawStrip(GuiGraphics graphics, int x, int y, int sourceY, int height, int tint) {
        int contentWidth = this.columns * SLOT_SIZE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, sourceY, BORDER, height, 256, 256, tint);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x + BORDER,
                y,
                BORDER,
                sourceY,
                contentWidth,
                height,
                256,
                256,
                tint);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x + BORDER + contentWidth,
                y,
                RIGHT_BORDER_X,
                sourceY,
                BORDER,
                height,
                256,
                256,
                tint);
    }
}
