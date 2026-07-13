package me.noramibu.lumentooltips.tooltip.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

final class LumenPotionTooltipComponent implements TooltipComponent, ClientTooltipComponent {
  private static final int ICON_SIZE = 18;

  private final List<MobEffectInstance> effects;
  private final float durationScale;
  private final LumenConfig.PreviewConfig config;
  private final Identifier style;

  private LumenPotionTooltipComponent(
      List<MobEffectInstance> effects,
      float durationScale,
      LumenConfig.PreviewConfig config,
      Identifier style) {
    this.effects = effects;
    this.durationScale = durationScale;
    this.config = config;
    this.style = style;
  }

  static Optional<TooltipComponent> create(ItemStack stack, LumenConfig.PreviewConfig config) {
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents == null) {
      return Optional.empty();
    }
    List<MobEffectInstance> effects = new ArrayList<>();
    contents.getAllEffects().forEach(effects::add);
    return effects.isEmpty()
        ? Optional.empty()
        : Optional.of(
            new LumenPotionTooltipComponent(
                effects,
                stack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F),
                config,
                stack.get(DataComponents.TOOLTIP_STYLE)));
  }

  @Override
  public int getHeight(Font font) {
    return padding() * 2 + this.effects.size() * rowHeight();
  }

  @Override
  public int getWidth(Font font) {
    int textWidth = 0;
    for (MobEffectInstance effect : this.effects) {
      textWidth = Math.max(textWidth, font.width(effectName(effect)));
      textWidth = Math.max(textWidth, font.width(duration(effect)));
    }
    return padding() * 2 + ICON_SIZE + 4 + textWidth;
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
    int rowY = y + padding();
    for (MobEffectInstance effect : this.effects) {
      graphics.blitSprite(
          RenderPipelines.GUI_TEXTURED,
          Gui.getMobEffectSprite(effect.getEffect()),
          x + padding(),
          rowY + (rowHeight() - ICON_SIZE) / 2,
          ICON_SIZE,
          ICON_SIZE);
      int textX = x + padding() + ICON_SIZE + 4;
      graphics.text(font, effectName(effect), textX, rowY, 0xFFFFFFFF);
      graphics.text(font, duration(effect), textX, rowY + font.lineHeight, 0xFFAAAAAA);
      rowY += rowHeight();
    }
  }

  private Component effectName(MobEffectInstance effect) {
    Component name = effect.getEffect().value().getDisplayName();
    return effect.getAmplifier() == 0
        ? name
        : name.copy()
            .append(CommonComponents.SPACE)
            .append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
  }

  private Component duration(MobEffectInstance effect) {
    Minecraft minecraft = Minecraft.getInstance();
    float tickrate = minecraft.level == null ? 20.0F : minecraft.level.tickRateManager().tickrate();
    return MobEffectUtil.formatDuration(effect, this.durationScale, tickrate);
  }

  private int padding() {
    return LumenPreviewStyle.padding(this.config);
  }

  private int rowHeight() {
    return switch (this.config.density) {
      case COMPACT -> 19;
      case VANILLA -> 22;
      case COMFORTABLE -> 25;
    };
  }
}
