package me.noramibu.lumentooltips.tooltip.preview;

import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

final class LumenMapTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private static final Identifier MAP_BACKGROUND =
      Identifier.withDefaultNamespace("textures/map/map_background.png");

  private final MapId id;
  private final MapItemSavedData data;
  private final MapRenderState renderState = new MapRenderState();
  private final LumenConfig.PreviewConfig config;
  private final Identifier style;

  private LumenMapTooltipComponent(
      MapId id, MapItemSavedData data, LumenConfig.PreviewConfig config, Identifier style) {
    this.id = id;
    this.data = data;
    this.config = config;
    this.style = style;
  }

  static Optional<TooltipComponent> create(ItemStack stack, LumenConfig.PreviewConfig config) {
    Minecraft minecraft = Minecraft.getInstance();
    MapId id = stack.get(DataComponents.MAP_ID);
    MapItemSavedData data =
        id == null || minecraft.level == null ? null : minecraft.level.getMapData(id);
    return data == null
        ? Optional.empty()
        : Optional.of(
            new LumenMapTooltipComponent(
                id, data, config, stack.get(DataComponents.TOOLTIP_STYLE)));
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
    int panelSize = getWidth(font);
    LumenPreviewStyle.drawPanel(graphics, x, y, panelSize, panelSize, this.style);
    int padding = padding();
    int size = size();
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        MAP_BACKGROUND,
        x + padding,
        y + padding,
        0,
        0,
        size,
        size,
        size,
        size);
    float scale = size / 128.0F;
    graphics.pose().pushMatrix();
    graphics.pose().translate(x + padding, y + padding);
    graphics.pose().scale(scale, scale);
    Minecraft.getInstance()
        .getMapRenderer()
        .extractRenderState(this.id, this.data, this.renderState);
    graphics.map(this.renderState);
    graphics.pose().popMatrix();
  }

  private int size() {
    return LumenPreviewStyle.mapSize(this.config);
  }

  private int padding() {
    return LumenPreviewStyle.padding(this.config);
  }
}
