package me.noramibu.lumentooltips.tooltip.preview;

import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.properties.WoodType;

final class LumenSignTooltipComponent implements TooltipComponent, ClientTooltipComponent {
    private static final float SIGN_TEXT_SCALE = 0.9765628F;
    private static final int SIGN_TEXTURE_WIDTH = 24;
    private static final int SIGN_TEXTURE_HEIGHT = 12;
    private static final int SIGN_TEXTURE_FILE_HEIGHT = 26;
    private static final float HANGING_SIZE = 72.0F;
    private static final float HANGING_TEXT_OFFSET = 49.0F;
    private static final int SIGN_LINE_HEIGHT = 10;
    private static final int SIGN_MAX_LINE_WIDTH = 90;
    private static final int HANGING_LINE_HEIGHT = 9;
    private static final int HANGING_MAX_LINE_WIDTH = 60;

    private final Component[] lines;
    private final int textColor;
    private final LumenConfig.PreviewConfig config;
    private final WoodType woodType;
    private final boolean hanging;

    private LumenSignTooltipComponent(SignBlock block, SignText text, LumenConfig.PreviewConfig config) {
        this.lines = text.getMessages(Minecraft.getInstance().isTextFilteringEnabled())
                .toArray(Component[]::new);
        this.textColor =
                text.hasGlowingText() ? text.getColor().getTextColor() : AbstractSignRenderer.getDarkColor(text);
        this.config = config;
        this.woodType = SignBlock.getWoodType(block);
        this.hanging = block instanceof HangingSignBlock;
    }

    static Optional<TooltipComponent> create(ItemStack stack, LumenConfig.PreviewConfig config) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof SignBlock block)) {
            return Optional.empty();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return Optional.empty();
        }
        BlockEntity entity = block.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        if (!(entity instanceof SignBlockEntity sign)) {
            return Optional.empty();
        }
        sign.setLevel(minecraft.level);
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            try {
                data.loadInto(sign, minecraft.level.registryAccess());
            } catch (RuntimeException exception) {
                return Optional.empty();
            }
        }
        sign.applyComponentsFromItemStack(stack);
        SignText text = hasText(sign.getText(SignTextSlot.FRONT))
                ? sign.getText(SignTextSlot.FRONT)
                : sign.getText(SignTextSlot.BACK);
        return Optional.of(new LumenSignTooltipComponent(block, text, config));
    }

    @Override
    public int getHeight(Font font) {
        return switch (this.config.density) {
            case COMPACT -> 72;
            case VANILLA -> 84;
            case COMFORTABLE -> 96;
        };
    }

    @Override
    public int getWidth(Font font) {
        return switch (this.config.density) {
            case COMPACT -> 96;
            case VANILLA -> 112;
            case COMFORTABLE -> 128;
        };
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        int previewWidth = getWidth(font);
        int previewHeight = getHeight(font);
        int renderX = x + (width - previewWidth) / 2;
        if (this.hanging) {
            Identifier texture =
                    Identifier.withDefaultNamespace("textures/gui/hanging_signs/" + this.woodType.name() + ".png");
            int size = Math.min(previewWidth, previewHeight);
            int textureX = renderX + (previewWidth - size) / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, textureX, y, 0, 0, size, size, 16, 16);
            float factor = size / HANGING_SIZE;
            drawText(
                    font,
                    graphics,
                    textureX + size / 2.0F,
                    y + HANGING_TEXT_OFFSET * factor,
                    factor,
                    HANGING_LINE_HEIGHT,
                    HANGING_MAX_LINE_WIDTH);
            return;
        }
        Identifier texture = Identifier.withDefaultNamespace("textures/gui/signs/" + this.woodType.name() + ".png");
        int signWidth = Math.min(previewWidth, previewHeight * 2);
        int signHeight = signWidth / 2;
        int signX = renderX + (previewWidth - signWidth) / 2;
        int signY = y + (previewHeight - signHeight) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                signX,
                signY,
                0,
                0,
                signWidth,
                signHeight,
                SIGN_TEXTURE_WIDTH,
                SIGN_TEXTURE_HEIGHT,
                SIGN_TEXTURE_WIDTH,
                SIGN_TEXTURE_FILE_HEIGHT);
        float factor = signWidth / 96.0F;
        drawText(
                font,
                graphics,
                signX + signWidth / 2.0F,
                signY + signHeight / 2.0F,
                SIGN_TEXT_SCALE * factor,
                SIGN_LINE_HEIGHT,
                SIGN_MAX_LINE_WIDTH);
    }

    private void drawText(
            Font font,
            GuiGraphicsExtractor graphics,
            float centerX,
            float centerY,
            float scale,
            int lineHeight,
            int maxLineWidth) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(scale, scale);
        int startY = -this.lines.length * lineHeight / 2;
        for (int index = 0; index < this.lines.length; index++) {
            List<FormattedCharSequence> split = font.split(this.lines[index], maxLineWidth);
            if (split.isEmpty()) {
                continue;
            }
            FormattedCharSequence line = split.getFirst();
            graphics.text(font, line, -font.width(line) / 2, startY + index * lineHeight, this.textColor, false);
        }
        graphics.pose().popMatrix();
    }

    private static boolean hasText(SignText text) {
        return text.getMessages(false).stream()
                .anyMatch(line -> !line.getString().isBlank());
    }
}
