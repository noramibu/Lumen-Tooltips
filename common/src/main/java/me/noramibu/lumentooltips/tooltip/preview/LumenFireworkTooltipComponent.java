package me.noramibu.lumentooltips.tooltip.preview;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

record LumenFireworkTooltipComponent(
    int flightDuration,
    List<FireworkExplosion> explosions,
    LumenConfig.PreviewConfig config,
    Identifier style)
    implements TooltipComponent, ClientTooltipComponent {
  private static final int ROW_HEIGHT = 10;
  private static final int MAX_ROWS = 4;
  private static final int SHAPE_WIDTH = 62;
  private static final int SWATCH_SIZE = 5;
  private static final int SWATCH_GAP = 2;
  private static final int EXPLOSION_INTERVAL = 2;
  private static final int MIN_SPARK_LIFETIME = 48;
  private static final int SPARK_LIFETIME_RANGE = 12;
  private static final int MAX_SPARK_LIFETIME = MIN_SPARK_LIFETIME + SPARK_LIFETIME_RANGE;
  private static final float SPARK_FRICTION = 0.91F;
  private static final float SPARK_GRAVITY = 0.004F;
  private static final float SHAPE_SCALE = 4.5F;
  private static final float BURST_SCALE = 2.6F;
  private static final float[] FRICTION_POWERS = powers(SPARK_FRICTION);
  private static final float[] FADE_POWERS = powers(0.8F);
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final double[][] STAR_POINTS = {
    {0.0, 1.0},
    {0.3455, 0.309},
    {0.9511, 0.309},
    {0.3795918367346939, -0.12653061224489795},
    {0.6122448979591837, -0.8040816326530612},
    {0.0, -0.35918367346938773}
  };
  private static final double[][] CREEPER_POINTS = {
    {0.0, 0.2},
    {0.2, 0.2},
    {0.2, 0.6},
    {0.6, 0.6},
    {0.6, 0.2},
    {0.2, 0.2},
    {0.2, 0.0},
    {0.4, 0.0},
    {0.4, -0.6},
    {0.2, -0.6},
    {0.2, -0.4},
    {0.0, -0.4}
  };
  private static final double[][] SMALL_BALL_VELOCITIES = ballVelocities(0.25, 2, 2);
  private static final double[][] LARGE_BALL_VELOCITIES = ballVelocities(0.5, 4, 3);
  private static final double[][] BURST_VELOCITIES = burstVelocities();

  LumenFireworkTooltipComponent(int flightDuration, List<FireworkExplosion> explosions) {
    this(
        flightDuration,
        explosions,
        LumenConfigManager.current().modules.preview,
        null);
  }

  static Optional<TooltipComponent> create(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    Fireworks fireworks = stack.get(DataComponents.FIREWORKS);
    if (fireworks != null) {
      return Optional.of(
          new LumenFireworkTooltipComponent(
              fireworks.flightDuration(),
              fireworks.explosions(),
              config,
              stack.get(DataComponents.TOOLTIP_STYLE)));
    }
    FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);
    return explosion == null
        ? Optional.empty()
        : Optional.of(
            new LumenFireworkTooltipComponent(
                -1, List.of(explosion), config, stack.get(DataComponents.TOOLTIP_STYLE)));
  }

  @Override
  public int getHeight(Font font) {
    return padding() * 3
        + font.lineHeight
        + simulationHeight()
        + Math.min(MAX_ROWS, this.explosions.size()) * ROW_HEIGHT;
  }

  @Override
  public int getWidth(Font font) {
    return panelWidth();
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
        panelWidth(),
        getHeight(font),
        this.style);
    Component header =
        this.flightDuration >= 0
            ? Component.translatable(
                "tooltip.lumen_tooltips.firework.summary",
                durationText(this.flightDuration),
                this.explosions.size())
            : Component.translatable("tooltip.lumen_tooltips.firework.star");
    graphics.text(
        font,
        font.plainSubstrByWidth(header.getString(), panelWidth() - padding() * 2),
        x + padding(),
        y + padding(),
        TEXT_COLOR);

    int simulationY = y + padding() + font.lineHeight;
    drawSimulation(graphics, x + panelWidth() / 2, simulationY);
    int visible = Math.min(MAX_ROWS, this.explosions.size());
    int rowY = simulationY + simulationHeight() + padding();
    for (int index = 0; index < visible; index++) {
      drawExplosion(font, graphics, this.explosions.get(index), x + padding(), rowY);
      rowY += ROW_HEIGHT;
    }
  }

  private void drawSimulation(GuiGraphicsExtractor graphics, int centerX, int top) {
    Minecraft minecraft = Minecraft.getInstance();
    float time =
        this.config.reducedMotion
            ? (this.explosions.isEmpty()
                ? flightTicks(this.flightDuration) * 0.65F
                : flightTicks(this.flightDuration) + 8.0F)
            : minecraft.level == null
            ? 0.0F
            : minecraft.level.getGameTime()
                + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    int ascentTicks = flightTicks(this.flightDuration);
    int explosionTicks =
        this.explosions.isEmpty()
            ? 0
            : (this.explosions.size() - 1) * EXPLOSION_INTERVAL + MAX_SPARK_LIFETIME;
    int cycleTicks = Math.max(1, ascentTicks + explosionTicks);
    float cycle = time % cycleTicks;
    int centerY = top + simulationHeight() / 2;
    if (cycle < ascentTicks) {
      drawRocket(graphics, centerX, centerY, top + simulationHeight() - 3, cycle, ascentTicks);
      return;
    }
    float explosionTime = cycle - ascentTicks;
    float burstLift = ascentTicks == 0 ? 0.0F : (0.05F + 0.04F * ascentTicks) * 0.5F;
    for (int index = 0; index < this.explosions.size(); index++) {
      float age = explosionTime - index * EXPLOSION_INTERVAL;
      if (age >= 0.0F && age < MAX_SPARK_LIFETIME) {
        drawExplosion(
            graphics, this.explosions.get(index), centerX, centerY, age, index, burstLift);
      }
    }
  }

  private static void drawRocket(
      GuiGraphicsExtractor graphics,
      int centerX,
      int endY,
      int startY,
      float age,
      int lifetime) {
    for (int trail = 5; trail >= 1; trail--) {
      float trailAge = Math.max(0.0F, age - trail * 0.75F);
      float progress = rocketProgress(trailAge, lifetime);
      int x = centerX + Math.round((float) Math.sin(trailAge * 0.7F) * 1.5F);
      int y = Math.round(Mth.lerp(progress, startY, endY));
      graphics.fill(x, y, x + 1, y + 1, (190 - trail * 25) << 24 | 0xFFAA33);
    }
    float progress = rocketProgress(age, lifetime);
    int x = centerX + Math.round((float) Math.sin(age * 0.7F) * 1.5F);
    int y = Math.round(Mth.lerp(progress, startY, endY));
    graphics.fill(x - 1, y - 2, x + 2, y + 1, 0xFFFFFFFF);
  }

  private void drawExplosion(
      GuiGraphicsExtractor graphics,
      FireworkExplosion explosion,
      int centerX,
      int centerY,
      float age,
      int explosionIndex,
      float burstLift) {
    if (age < 4.0F) {
      float flashSize = 3.55F * Mth.sin((age + 1.0F) * 0.25F * (float) Math.PI);
      int radius = Math.max(1, Math.round(Math.abs(flashSize)));
      int flashAlpha = Math.round(255 * Mth.clamp(0.6F - age * 0.125F, 0.0F, 1.0F));
      graphics.fill(
          centerX - radius,
          centerY - radius,
          centerX + radius + 1,
          centerY + radius + 1,
          ARGB.color(
              flashAlpha,
              explosion.colors().isEmpty() ? 0x1D1D21 : explosion.colors().getInt(0)));
    }
    SparkFrame frame =
        new SparkFrame(
            age,
            explosionIndex,
            dampedDistance(age),
            gravityDrop(age),
            (float) Math.pow(0.8, age - (int) age));
    switch (explosion.shape()) {
      case SMALL_BALL ->
          drawBall(graphics, explosion, centerX, centerY, frame, SMALL_BALL_VELOCITIES);
      case LARGE_BALL ->
          drawBall(graphics, explosion, centerX, centerY, frame, LARGE_BALL_VELOCITIES);
      case STAR ->
          drawShape(graphics, explosion, centerX, centerY, frame, STAR_POINTS, false);
      case CREEPER ->
          drawShape(graphics, explosion, centerX, centerY, frame, CREEPER_POINTS, true);
      case BURST ->
          drawBurst(graphics, explosion, centerX, centerY, frame, burstLift);
    }
  }

  private static void drawBall(
      GuiGraphicsExtractor graphics,
      FireworkExplosion explosion,
      int centerX,
      int centerY,
      SparkFrame frame,
      double[][] velocities) {
    for (int index = 0; index < velocities.length; index++) {
      double[] velocity = velocities[index];
      drawSpark(
          graphics,
          explosion,
          centerX,
          centerY,
          velocity[0],
          velocity[1],
          velocity[2],
          index,
          frame,
          SHAPE_SCALE);
    }
  }

  private static void drawShape(
      GuiGraphicsExtractor graphics,
      FireworkExplosion explosion,
      int centerX,
      int centerY,
      SparkFrame frame,
      double[][] points,
      boolean flat) {
    int particle = 0;
    drawSpark(
        graphics,
        explosion,
        centerX,
        centerY,
        points[0][0] * 0.5,
        points[0][1] * 0.5,
        0.0,
        particle++,
        frame,
        SHAPE_SCALE);
    double baseAngle = random(frame.explosionIndex * 4099 + 31) * Math.PI;
    for (int angleStep = 0; angleStep < 3; angleStep++) {
      double angle = baseAngle + angleStep * Math.PI * (flat ? 0.034 : 0.34);
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      for (int segment = 1; segment < points.length; segment++) {
        for (int step = 1; step <= 4; step++) {
          double amount = step * 0.25;
          double velocityX =
              Mth.lerp(amount, points[segment - 1][0], points[segment][0]) * 0.5;
          double velocityY =
              Mth.lerp(amount, points[segment - 1][1], points[segment][1]) * 0.5;
          double velocityZ = velocityX * sin;
          velocityX *= cos;
          for (int flip = -1; flip <= 1; flip += 2) {
            drawSpark(
                graphics,
                explosion,
                centerX,
                centerY,
                velocityX * flip,
                velocityY,
                velocityZ * flip,
                particle++,
                frame,
                SHAPE_SCALE);
          }
        }
      }
    }
  }

  private static void drawBurst(
      GuiGraphicsExtractor graphics,
      FireworkExplosion explosion,
      int centerX,
      int centerY,
      SparkFrame frame,
      float lift) {
    for (int index = 0; index < BURST_VELOCITIES.length; index++) {
      double[] velocity = BURST_VELOCITIES[index];
      drawSpark(
          graphics,
          explosion,
          centerX,
          centerY,
          velocity[0],
          lift + velocity[1],
          velocity[2],
          index,
          frame,
          BURST_SCALE);
    }
  }

  private static void drawSpark(
      GuiGraphicsExtractor graphics,
      FireworkExplosion explosion,
      int centerX,
      int centerY,
      double velocityX,
      double velocityY,
      double velocityZ,
      int index,
      SparkFrame frame,
      float scale) {
    int seed = frame.explosionIndex * 4099 + index * 17;
    int lifetime = sparkLifetime(seed);
    if (frame.age >= lifetime || !visible(explosion.hasTwinkle(), frame.age, lifetime)) {
      return;
    }
    if (explosion.hasTrail()) {
      int spawnAge = Math.min((int) frame.age, lifetime / 2 - 1);
      if ((spawnAge + lifetime) % 2 != 0) {
        spawnAge--;
      }
      for (int trail = 0; trail < 4 && spawnAge > 0; trail++, spawnAge -= 2) {
        float elapsed = frame.age - spawnAge;
        int trailLifetime = sparkLifetime(seed + trail + 1);
        float trailAge = trailLifetime / 2.0F + elapsed;
        if (trailAge < trailLifetime && visible(explosion.hasTwinkle(), trailAge, trailLifetime)) {
          drawSparkPoint(
              graphics,
              centerX,
              centerY,
              velocityX,
              velocityY,
              velocityZ,
              dampedDistance(spawnAge),
              gravityDrop(spawnAge) + gravityDrop(elapsed),
              scale,
              ARGB.multiplyAlpha(
                  sparkColor(
                      explosion, seed, trailAge, trailLifetime, frame.fadePartial),
                  0.7F),
              1);
        }
      }
    }
    drawSparkPoint(
        graphics,
        centerX,
        centerY,
        velocityX,
        velocityY,
        velocityZ,
        frame.distance,
        frame.drop,
        scale,
        sparkColor(explosion, seed, frame.age, lifetime, frame.fadePartial),
        2);
  }

  private static void drawSparkPoint(
      GuiGraphicsExtractor graphics,
      int centerX,
      int centerY,
      double velocityX,
      double velocityY,
      double velocityZ,
      float distance,
      float drop,
      float scale,
      int color,
      int size) {
    int x =
        centerX
            + Math.round((float) ((velocityX + velocityZ * 0.35) * distance * scale));
    int y =
        centerY
            - Math.round((float) (velocityY * distance * scale))
            + Math.round(drop * scale);
    graphics.fill(x, y, x + size, y + size, color);
  }

  private static int sparkColor(
      FireworkExplosion explosion, int seed, float age, int lifetime, float fadePartial) {
    IntList colors = explosion.colors();
    int color =
        colors.isEmpty()
            ? 0x1D1D21
            : colors.getInt(Math.floorMod(Mth.murmurHash3Mixer(seed), colors.size()));
    IntList fades = explosion.fadeColors();
    int halfLife = lifetime / 2;
    if (!fades.isEmpty() && age > halfLife) {
      float fade = 1.0F - FADE_POWERS[(int) age - halfLife] * fadePartial;
      color =
          ARGB.srgbLerp(
              fade,
              color,
              fades.getInt(Math.floorMod(Mth.murmurHash3Mixer(seed + 1), fades.size())));
    }
    return ARGB.color(Math.round(255 * sparkAlpha(age, lifetime)), color);
  }

  private static boolean visible(boolean twinkle, float age, int lifetime) {
    return !twinkle
        || age < lifetime / 3.0F
        || ((int) age + lifetime) / 3 % 2 == 0;
  }

  private static float sparkAlpha(float age, int lifetime) {
    return age > lifetime / 2.0F
        ? 1.0F - (age - lifetime / 2.0F) / lifetime
        : 0.99F;
  }

  private static int sparkLifetime(int seed) {
    return MIN_SPARK_LIFETIME
        + Math.floorMod(Mth.murmurHash3Mixer(seed), SPARK_LIFETIME_RANGE);
  }

  private static float dampedDistance(float age) {
    int ticks = (int) age;
    float power = FRICTION_POWERS[ticks];
    return (1.0F - power) / (1.0F - SPARK_FRICTION) + (age - ticks) * power;
  }

  private static float gravityDrop(float age) {
    int ticks = (int) age;
    float power = FRICTION_POWERS[ticks];
    float distance = (1.0F - power) / (1.0F - SPARK_FRICTION);
    float partial = age - ticks;
    return SPARK_GRAVITY
        / (1.0F - SPARK_FRICTION)
        * (ticks - SPARK_FRICTION * distance + partial * (1.0F - power * SPARK_FRICTION));
  }

  private static float rocketProgress(float age, int lifetime) {
    return rocketHeight(age) / rocketHeight(lifetime);
  }

  private static float rocketHeight(float age) {
    int ticks = (int) age;
    float partial = age - ticks;
    return 0.05F * ticks
        + 0.02F * ticks * (ticks + 1)
        + partial * (0.05F + 0.04F * (ticks + 1));
  }

  private static double[][] ballVelocities(double speed, int steps, int stride) {
    List<double[]> velocities = new ArrayList<>();
    int particle = 0;
    for (int yStep = -steps; yStep <= steps; yStep++) {
      for (int xStep = -steps; xStep <= steps; xStep++) {
        for (int zStep = -steps; zStep <= steps; zStep++) {
          int index = particle++;
          if (index % stride == 0) {
            int seed = index * 17;
            double x = xStep + triangular(seed) * 0.5;
            double y = yStep + triangular(seed + 2) * 0.5;
            double z = zStep + triangular(seed + 4) * 0.5;
            double length =
                Math.sqrt(x * x + y * y + z * z) / speed + gaussian(seed + 6) * 0.05;
            velocities.add(new double[] {x / length, y / length, z / length});
          }
          if (yStep != -steps && yStep != steps && xStep != -steps && xStep != steps) {
            zStep += steps * 2 - 1;
          }
        }
      }
    }
    return velocities.toArray(double[][]::new);
  }

  private static double[][] burstVelocities() {
    double[][] velocities = new double[70][3];
    double offsetX = gaussian(73) * 0.05;
    double offsetZ = gaussian(75) * 0.05;
    for (int index = 0; index < velocities.length; index++) {
      int seed = 73 + index * 17;
      velocities[index] =
          new double[] {
            gaussian(seed) * 0.15 + offsetX,
            random(seed + 2) * 0.5,
            gaussian(seed + 4) * 0.15 + offsetZ
          };
    }
    return velocities;
  }

  private static float[] powers(float base) {
    float[] powers = new float[MAX_SPARK_LIFETIME + 1];
    powers[0] = 1.0F;
    for (int index = 1; index < powers.length; index++) {
      powers[index] = powers[index - 1] * base;
    }
    return powers;
  }

  private static double triangular(int seed) {
    return random(seed) - random(seed + 1);
  }

  private static double gaussian(int seed) {
    return Math.sqrt(-2.0 * Math.log(Math.max(0.000001, random(seed))))
        * Math.cos(Math.TAU * random(seed + 1));
  }

  private static float random(int seed) {
    return (Mth.murmurHash3Mixer(seed) >>> 8) * 0x1.0p-24F;
  }

  private void drawExplosion(
      Font font,
      GuiGraphicsExtractor graphics,
      FireworkExplosion explosion,
      int x,
      int y) {
    String shape = font.plainSubstrByWidth(explosion.shape().getName().getString(), SHAPE_WIDTH);
    graphics.text(font, shape, x, y, TEXT_COLOR);
    int swatchX = x + SHAPE_WIDTH + SWATCH_GAP;
    int maxX = x + panelWidth() - padding() * 2;
    swatchX = drawSwatches(graphics, explosion.colors(), swatchX, y + 2, maxX);
    if (!explosion.fadeColors().isEmpty() && swatchX + font.width(">") < maxX) {
      graphics.text(font, ">", swatchX, y, TEXT_COLOR);
      swatchX += font.width(">") + SWATCH_GAP;
      drawSwatches(graphics, explosion.fadeColors(), swatchX, y + 2, maxX);
    }
  }

  private static int drawSwatches(
      GuiGraphicsExtractor graphics, IntList colors, int x, int y, int maxX) {
    for (int index = 0; index < colors.size() && x + SWATCH_SIZE <= maxX; index++) {
      graphics.fill(
          x, y, x + SWATCH_SIZE, y + SWATCH_SIZE, 0xFF000000 | colors.getInt(index));
      x += SWATCH_SIZE + SWATCH_GAP;
    }
    return x;
  }

  private static float averageFlightSeconds(int flightDuration) {
    return (10.0F * (1 + flightDuration) + 5.5F) / 20.0F;
  }

  private static int flightTicks(int flightDuration) {
    return flightDuration < 0 ? 0 : Math.round(averageFlightSeconds(flightDuration) * 20.0F);
  }

  private static String durationText(int flightDuration) {
    float rounded = Math.round(averageFlightSeconds(flightDuration) * 10.0F) / 10.0F;
    return rounded == Math.round(rounded)
        ? Integer.toString(Math.round(rounded))
        : Float.toString(rounded);
  }

  private int panelWidth() {
    return switch (this.config.density) {
      case COMPACT -> 160;
      case VANILLA -> 180;
      case COMFORTABLE -> 200;
    };
  }

  private int simulationHeight() {
    return switch (this.config.density) {
      case COMPACT -> 56;
      case VANILLA -> 64;
      case COMFORTABLE -> 72;
    };
  }

  private int padding() {
    return LumenPreviewStyle.padding(this.config);
  }

  private record SparkFrame(
      float age, int explosionIndex, float distance, float drop, float fadePartial) {}
}
