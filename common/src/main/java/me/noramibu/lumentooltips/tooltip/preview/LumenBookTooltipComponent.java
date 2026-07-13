package me.noramibu.lumentooltips.tooltip.preview;

import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.joml.Matrix3x2fStack;

record LumenBookTooltipComponent(
    Component page, LumenConfig.PreviewConfig config, Identifier style)
    implements TooltipComponent, ClientTooltipComponent {
  private static final Identifier BACKGROUND = Identifier.parse("textures/gui/book.png");

  static Optional<TooltipComponent> create(ItemStack stack) {
    boolean filtered = Minecraft.getInstance().isTextFilteringEnabled();
    WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
    if (writable != null) {
      List<Filterable<String>> pages = writable.pages();
      return pages.isEmpty()
          ? Optional.empty()
          : create(
              Component.literal(pages.getFirst().get(filtered)),
              pages.size(),
              stack,
              LumenConfigManager.current().modules.preview);
    }
    WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
    if (written == null) {
      return Optional.empty();
    }
    List<Component> pages = written.getPages(filtered);
    return pages.isEmpty()
        ? Optional.empty()
        : create(
            pages.getFirst(),
            pages.size(),
            stack,
            LumenConfigManager.current().modules.preview);
  }

  static Optional<TooltipComponent> create(Component page, int pageCount) {
    return create(
        page,
        pageCount,
        ItemStack.EMPTY,
        LumenConfigManager.current().modules.preview);
  }

  private static Optional<TooltipComponent> create(
      Component page,
      int pageCount,
      ItemStack stack,
      LumenConfig.PreviewConfig config) {
    return Optional.of(
        new LumenBookTooltipComponent(
            page.copy()
                .append(" ")
                .append(
                    Component.translatable("tooltip.lumen_tooltips.book.pages", pageCount)
                        .withStyle(ChatFormatting.GRAY)),
            config,
            stack.get(DataComponents.TOOLTIP_STYLE)));
  }

  @Override
  public int getHeight(Font font) {
    return Math.round(134 * scale());
  }

  @Override
  public int getWidth(Font font) {
    return Math.round(128 * scale());
  }

  @Override
  public boolean showTooltipWithItemInHand() {
    return true;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    LumenPreviewStyle.drawPanel(
        graphics,
        x,
        y,
        getWidth(font),
        getHeight(font),
        this.style);
    Matrix3x2fStack pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(x, y);
    pose.scale(scale(), scale());
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 0, 0, 128, 128, 179, 179);
    pose.translate(26, 12);
    pose.scale(0.7f, 0.7f);
    int lineY = 0;
    for (FormattedCharSequence line : font.split(this.page, 112)) {
      graphics.text(font, line, 0, lineY, 0xFF000000, false);
      lineY += 8;
    }
    pose.popMatrix();
  }

  private float scale() {
    return switch (this.config.density) {
      case COMPACT -> 0.85F;
      case VANILLA -> 1.0F;
      case COMFORTABLE -> 1.12F;
    };
  }
}
