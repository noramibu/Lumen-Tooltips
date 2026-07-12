package me.noramibu.lumentooltips.tooltip.preview;

import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.Blocks;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class LumenEntityTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private static final int WIDTH = 80;
  private static final int HEIGHT = 64;
  private static final int PADDING = 6;
  private static final float MAX_SCALE = 48.0F;
  private static final float MODEL_MARGIN = 0.9F;
  private static final float HORSE_MODEL_MARGIN = 0.7F;
  private static final float HORSE_CENTER_Y_OFFSET = 0.3F;
  private static final float ANIMATION_SPEED = 0.5F;

  private final Entity entity;
  private final int displayYaw;
  private final int displayPitch;

  private LumenEntityTooltipComponent(Entity entity, LumenConfig.PreviewConfig config) {
    this.entity = entity;
    this.displayYaw = config.displayYaw;
    this.displayPitch = config.displayPitch;
  }

  static Optional<TooltipComponent> create(Entity entity, LumenConfig.PreviewConfig config) {
    if (entity instanceof AreaEffectCloud cloud) {
      return config.areaEffectClouds
          ? Optional.of(new LumenAreaEffectCloudTooltipComponent(cloud))
          : Optional.empty();
    }
    return entity instanceof Display && !config.displayEntities
            || entity instanceof ItemFrame && !config.itemFrames
        ? Optional.empty()
        : Optional.of(new LumenEntityTooltipComponent(entity, config));
  }

  @Override
  public int getHeight(Font font) {
    return HEIGHT;
  }

  @Override
  public int getWidth(Font font) {
    return WIDTH;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null) {
      return;
    }
    int renderX = x + (width - WIDTH) / 2;
    if (drawEndPortal(graphics, renderX, y)) {
      return;
    }
    if (this.entity.isInvisible() && this.entity.isCustomNameVisible()) {
      drawVisibleName(font, graphics, renderX, y);
      return;
    }
    long gameTime = minecraft.level.getGameTime();
    float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    float animation = animationTime(gameTime, partialTick);
    this.entity.tickCount = (int) animation;
    EntityRenderState state;
    try {
      state =
          minecraft
              .getEntityRenderDispatcher()
              .extractEntity(this.entity, animation - this.entity.tickCount);
    } catch (RuntimeException exception) {
      return;
    }

    state.lightCoords = 15728880;
    state.shadowPieces.clear();
    state.outlineColor = 0;
    if (state instanceof TntRenderState tntState && this.entity instanceof PrimedTnt tnt) {
      tntState.fuseRemainingInTicks =
          loopingFuse(tnt.getFuse(), gameTime, partialTick);
    }
    float scale;
    float centerY;
    if (state instanceof LivingEntityRenderState livingState) {
      livingState.bodyRot = animation * 3.0F % 360.0F;
      livingState.yRot = 0.0F;
      livingState.xRot = 0.0F;
      livingState.boundingBoxWidth /= livingState.scale;
      livingState.boundingBoxHeight /= livingState.scale;
      livingState.scale = 1.0F;
      boolean horse = this.entity instanceof AbstractHorse;
      scale =
          scaleFor(
              livingState.boundingBoxWidth,
              livingState.boundingBoxHeight,
              horse ? HORSE_MODEL_MARGIN : MODEL_MARGIN);
      centerY =
          livingState.boundingBoxHeight / 2.0F
              + (horse ? HORSE_CENTER_Y_OFFSET : 0.0625F);
    } else if (state instanceof DisplayEntityRenderState displayState) {
      fixDisplayOrientation(displayState, this.displayYaw, this.displayPitch);
      scale = this.entity instanceof Display.TextDisplay ? 24.0F : 36.0F;
      centerY = this.entity instanceof Display.TextDisplay ? 0.0F : 0.5F;
    } else if (state instanceof ItemFrameRenderState itemFrameState) {
      itemFrameState.direction = Direction.NORTH;
      scale = scaleFor(state.boundingBoxWidth, state.boundingBoxHeight);
      centerY = 0.25F;
    } else {
      scale = scaleFor(state.boundingBoxWidth, state.boundingBoxHeight);
      centerY = state.boundingBoxHeight / 2.0F;
    }
    graphics.entity(
        state,
        scale,
        new Vector3f(0.0F, centerY, 0.0F),
        new Quaternionf().rotateZ((float) Math.PI),
        null,
        renderX,
        y,
        renderX + WIDTH,
        y + HEIGHT);
  }

  private boolean drawEndPortal(GuiGraphicsExtractor graphics, int x, int y) {
    if (!(this.entity instanceof FallingBlockEntity fallingBlock)) {
      return false;
    }
    var block = fallingBlock.getBlockState().getBlock();
    if (block != Blocks.END_PORTAL && block != Blocks.END_GATEWAY) {
      return false;
    }
    var textures = Minecraft.getInstance().getTextureManager();
    var sky = textures.getTexture(AbstractEndPortalRenderer.END_SKY_LOCATION);
    var portal = textures.getTexture(AbstractEndPortalRenderer.END_PORTAL_LOCATION);
    int inset = PADDING;
    graphics.fill(
        block == Blocks.END_GATEWAY ? RenderPipelines.END_GATEWAY : RenderPipelines.END_PORTAL,
        TextureSetup.doubleTexture(
            sky.getTextureView(), sky.getSampler(), portal.getTextureView(), portal.getSampler()),
        x + inset,
        y + inset,
        x + WIDTH - inset,
        y + HEIGHT - inset);
    return true;
  }

  private void drawVisibleName(
      Font font, GuiGraphicsExtractor graphics, int x, int y) {
    var name = this.entity.getCustomName();
    if (name == null) {
      return;
    }
    var lines = font.split(name, WIDTH - PADDING * 2);
    int count = Math.min(lines.size(), (HEIGHT - PADDING * 2) / font.lineHeight);
    int lineY = y + (HEIGHT - count * font.lineHeight) / 2;
    for (int index = 0; index < count; index++) {
      graphics.centeredText(font, lines.get(index), x + WIDTH / 2, lineY, 0xFFFFFFFF);
      lineY += font.lineHeight;
    }
  }

  private static float animationTime(long gameTime, float partialTick) {
    return ((gameTime % 1_000_000L) + partialTick) * ANIMATION_SPEED;
  }

  private static float loopingFuse(int fuse, long gameTime, float partialTick) {
    int duration = Math.max(1, fuse);
    return duration - ((gameTime % duration) + partialTick) % duration;
  }

  private static void fixDisplayOrientation(
      DisplayEntityRenderState state, float yaw, float pitch) {
    Display.RenderState renderState = state.renderState;
    if (renderState == null) {
      return;
    }
    state.renderState =
        new Display.RenderState(
            renderState.transformation(),
            Display.BillboardConstraints.FIXED,
            renderState.brightnessOverride(),
            renderState.shadowRadius(),
            renderState.shadowStrength(),
            renderState.glowColorOverride());
    state.entityYRot = yaw;
    state.entityXRot = pitch;
  }

  private static float scaleFor(float width, float height) {
    return scaleFor(width, height, MODEL_MARGIN);
  }

  private static float scaleFor(float width, float height, float margin) {
    return margin
        * Math.min(
            MAX_SCALE,
            Math.min((WIDTH - PADDING * 2) / width, (HEIGHT - PADDING * 2) / height));
  }
}
