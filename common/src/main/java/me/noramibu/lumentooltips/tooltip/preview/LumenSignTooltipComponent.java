package me.noramibu.lumentooltips.tooltip.preview;

import java.util.Arrays;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
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
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jspecify.annotations.Nullable;

final class LumenSignTooltipComponent implements TooltipComponent, ClientTooltipComponent {
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
    this.hanging = block instanceof HangingSignBlock;
    this.model =
        this.hanging
            ? null
            : StandingSignRenderer.createSignModel(
                Minecraft.getInstance().getEntityModels(),
                this.woodType,
                PlainSignBlock.Attachment.WALL);
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
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    int previewWidth = getWidth(font);
    int previewHeight = getHeight(font);
    int renderX = x + (width - previewWidth) / 2;
    if (this.hanging) {
      Identifier texture =
          Identifier.withDefaultNamespace(
              "textures/gui/hanging_signs/" + this.woodType.name() + ".png");
      int size = Math.min(previewWidth, previewHeight);
      graphics.blit(
          RenderPipelines.GUI_TEXTURED,
          texture,
          renderX + (previewWidth - size) / 2,
          y,
          0,
          0,
          size,
          size,
          16,
          16);
    } else if (this.model != null) {
      graphics.sign(
          this.model,
          previewHeight * 0.65F,
          this.woodType,
          renderX,
          y,
          renderX + previewWidth,
          y + previewHeight);
    }
    drawText(font, graphics, renderX + previewWidth / 2.0F, y + previewHeight * 0.52F);
  }

  private void drawText(
      Font font, GuiGraphicsExtractor graphics, float centerX, float centerY) {
    float scale = switch (this.config.density) {
      case COMPACT -> 0.55F;
      case VANILLA -> 0.65F;
      case COMFORTABLE -> 0.75F;
    };
    graphics.pose().pushMatrix();
    graphics.pose().translate(centerX, centerY);
    graphics.pose().scale(scale, scale);
    int maxWidth = Math.round((getWidth(font) - 32) / scale);
    int startY = -this.lines.length * 5;
    for (int index = 0; index < this.lines.length; index++) {
      FormattedCharSequence line = font.split(this.lines[index], maxWidth).getFirst();
      graphics.centeredText(font, line, 0, startY + index * 10, this.textColor);
    }
    graphics.pose().popMatrix();
  }

  private static boolean hasText(SignText text) {
    return Arrays.stream(text.getMessages(false)).anyMatch(line -> !line.getString().isBlank());
  }
}
