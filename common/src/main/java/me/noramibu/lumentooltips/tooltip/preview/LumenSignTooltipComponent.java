package me.noramibu.lumentooltips.tooltip.preview;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jspecify.annotations.Nullable;

final class LumenSignTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private static final float SIGN_BOX_HEIGHT = 102.0F;
  private static final float SIGN_SCALE = 62.500004F;
  private static final float SIGN_TEXT_SCALE = 0.9765628F;
  private static final float SIGN_TEXT_OFFSET = 24.0F;
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
  private final Model.@Nullable Simple model;

  private LumenSignTooltipComponent(
      SignBlock block, SignText text, LumenConfig.PreviewConfig config) {
    this.lines = text.getMessages(Minecraft.getInstance().isTextFilteringEnabled());
    this.textColor =
        text.hasGlowingText()
            ? text.getColor().getTextColor()
            : AbstractSignRenderer.getDarkColor(text);
    this.config = config;
    this.woodType = SignBlock.getWoodType(block);
    this.hanging =
        block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock;
    this.model =
        this.hanging
            ? null
            : SignRenderer.createSignModel(
                Minecraft.getInstance().getEntityModels(), this.woodType, false);
  }

  static Optional<TooltipComponent> create(ItemStack stack, LumenConfig.PreviewConfig config) {
    if (!(stack.getItem() instanceof BlockItem blockItem)
        || !(blockItem.getBlock() instanceof SignBlock block)) {
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
    SignText text = hasText(sign.getFrontText()) ? sign.getFrontText() : sign.getBackText();
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
  public void renderImage(
      Font font, int x, int y, int width, int height, GuiGraphics graphics) {
    int previewWidth = getWidth(font);
    int previewHeight = getHeight(font);
    int renderX = x + (width - previewWidth) / 2;
    if (this.hanging) {
      Identifier texture =
          Identifier.withDefaultNamespace(
              "textures/gui/hanging_signs/" + this.woodType.name() + ".png");
      int size = Math.min(previewWidth, previewHeight);
      int textureX = renderX + (previewWidth - size) / 2;
      graphics.blit(
          RenderPipelines.GUI_TEXTURED, texture, textureX, y, 0, 0, size, size, 16, 16);
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
    float factor = previewHeight / SIGN_BOX_HEIGHT;
    if (this.model != null) {
      graphics.submitSignRenderState(
          this.model,
          SIGN_SCALE * factor,
          this.woodType,
          renderX,
          y,
          renderX + previewWidth,
          y + previewHeight);
    }
    drawText(
        font,
        graphics,
        renderX + previewWidth / 2.0F,
        y + SIGN_TEXT_OFFSET * factor,
        SIGN_TEXT_SCALE * factor,
        SIGN_LINE_HEIGHT,
        SIGN_MAX_LINE_WIDTH);
  }

  private void drawText(
      Font font,
      GuiGraphics graphics,
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
      graphics.drawString(
          font,
          line,
          -font.width(line) / 2,
          startY + index * lineHeight,
          this.textColor,
          false);
    }
    graphics.pose().popMatrix();
  }

  private static boolean hasText(SignText text) {
    return Arrays.stream(text.getMessages(false)).anyMatch(line -> !line.getString().isBlank());
  }
}
