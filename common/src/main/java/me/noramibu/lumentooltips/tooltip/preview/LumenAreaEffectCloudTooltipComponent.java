package me.noramibu.lumentooltips.tooltip.preview;

import java.util.Locale;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.mixin.ParticleEngineAccessor;
import me.noramibu.lumentooltips.mixin.SingleQuadParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.alchemy.PotionContents;

final class LumenAreaEffectCloudTooltipComponent
    implements TooltipComponent, ClientTooltipComponent {
  private static final int MAX_PARTICLES = 40;
  private static final float[][] PARTICLE_POSITIONS = createParticlePositions();
  private final AreaEffectCloud cloud;
  private final LumenConfig.PreviewConfig config;
  private TextureAtlasSprite particleSprite;
  private boolean particleResolved;

  LumenAreaEffectCloudTooltipComponent(
      AreaEffectCloud cloud, LumenConfig.PreviewConfig config) {
    this.cloud = cloud;
    this.config = config;
  }

  @Override
  public int getHeight(Font font) {
    return LumenPreviewStyle.entityHeight(this.config) + 8;
  }

  @Override
  public int getWidth(Font font) {
    return LumenPreviewStyle.entityWidth(this.config) + 16;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    int centerX = x + getWidth(font) / 2;
    int centerY = y + 28;
    int radiusX = Math.clamp(Math.round(this.cloud.getRadius() * 7.0F), 14, 34);
    int radiusY = Math.max(5, radiusX / 3);
    PotionContents potion = this.cloud.get(DataComponents.POTION_CONTENTS);
    fillEllipse(graphics, centerX, centerY + 3, radiusX + 2, radiusY);

    Minecraft minecraft = Minecraft.getInstance();
    float time =
        this.config.reducedMotion
            ? 0.0F
            : minecraft.level == null
            ? 0.0F
            : minecraft.level.getGameTime()
                + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    int particleCount = particleCount(this.cloud.getRadius(), this.cloud.isWaiting());
    int particleRadiusX = this.cloud.isWaiting() ? Math.min(3, radiusX) : radiusX;
    int particleRadiusY = this.cloud.isWaiting() ? 1 : radiusY;
    ParticleOptions options = this.cloud.getParticle();
    int color =
        options instanceof ColorParticleOption colored
            ? ARGB.colorFromFloat(
                colored.getAlpha(), colored.getRed(), colored.getGreen(), colored.getBlue())
            : -1;
    TextureAtlasSprite sprite = particleSprite(minecraft);
    float[] positions = PARTICLE_POSITIONS[particleCount];
    for (int index = 0; index < particleCount; index++) {
      int offset = index * 3;
      double age = (time * 0.04 + index * 0.37) % 1.0;
      int particleX = centerX + (int) (positions[offset] * particleRadiusX);
      int particleY =
          centerY
              + (int) (positions[offset + 1] * particleRadiusY)
              - (int) (age * 6.0);
      int particleSize = positions[offset + 2] > 0.65F ? 6 : 5;
      int renderedColor =
          this.cloud.isWaiting()
                  && options.getType() == ParticleTypes.ENTITY_EFFECT
                  && index % 2 == 0
              ? -1
              : color;
      int particleColor =
          ARGB.multiplyAlpha(renderedColor, (float) ((1.0 - age) * 0.375 + 0.375));
      if (sprite == null) {
        graphics.fill(particleX, particleY, particleX + 2, particleY + 2, particleColor);
      } else {
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            sprite,
            particleX - particleSize / 2,
            particleY - particleSize / 2,
            particleSize,
            particleSize,
            particleColor);
      }
    }

    graphics.text(
        font,
        Component.translatable(
            "tooltip.lumen_tooltips.area_effect_cloud.radius",
            String.format(Locale.ROOT, "%.1f", this.cloud.getRadius())),
        x + 4,
        y + getHeight(font) - 19,
        0xFFCCCCCC);
    graphics.text(font, effectName(potion), x + 4, y + getHeight(font) - 10, 0xFFAAAAAA);
  }

  private TextureAtlasSprite particleSprite(Minecraft minecraft) {
    if (!this.particleResolved && minecraft.level != null) {
      this.particleResolved = true;
      Particle particle =
          ((ParticleEngineAccessor) minecraft.particleEngine)
              .lumenTooltips$makeParticle(this.cloud.getParticle(), 0, 0, 0, 0, 0, 0);
      if (particle instanceof SingleQuadParticle quad) {
        this.particleSprite = ((SingleQuadParticleAccessor) quad).lumenTooltips$getSprite();
      }
    }
    return this.particleSprite;
  }

  private static void fillEllipse(
      GuiGraphicsExtractor graphics,
      int centerX,
      int centerY,
      int radiusX,
      int radiusY) {
    for (int offsetY = -radiusY; offsetY <= radiusY; offsetY++) {
      int span = ellipseSpan(radiusX, radiusY, offsetY);
      graphics.fill(
          centerX - span,
          centerY + offsetY,
          centerX + span + 1,
          centerY + offsetY + 1,
          0x20000000);
    }
  }

  private static int ellipseSpan(int radiusX, int radiusY, int offsetY) {
    double normalizedY = offsetY / (double) radiusY;
    return (int) Math.round(radiusX * Math.sqrt(Math.max(0.0, 1.0 - normalizedY * normalizedY)));
  }

  private static int particleCount(float radius, boolean waiting) {
    return waiting
        ? 2
        : Math.clamp((int) Math.ceil(Math.PI * radius * radius), 1, MAX_PARTICLES);
  }

  private static float[][] createParticlePositions() {
    float[][] positions = new float[MAX_PARTICLES + 1][];
    for (int count = 1; count <= MAX_PARTICLES; count++) {
      positions[count] = new float[count * 3];
      for (int index = 0; index < count; index++) {
        double angle = index * 2.399963229728653;
        float distance = (float) Math.sqrt((index + 0.5) / count);
        int offset = index * 3;
        positions[count][offset] = (float) Math.cos(angle) * distance;
        positions[count][offset + 1] = (float) Math.sin(angle) * distance;
        positions[count][offset + 2] = distance;
      }
    }
    return positions;
  }

  private static Component effectName(PotionContents potion) {
    if (potion != null) {
      for (MobEffectInstance effect : potion.getAllEffects()) {
        return effect.getEffect().value().getDisplayName();
      }
    }
    return Component.translatable("effect.none");
  }
}
