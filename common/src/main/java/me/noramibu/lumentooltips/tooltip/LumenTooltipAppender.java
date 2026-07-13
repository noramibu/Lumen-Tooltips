package me.noramibu.lumentooltips.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import me.noramibu.lumentooltips.client.screen.LumenContainerOpener;
import me.noramibu.lumentooltips.config.HoldMode;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.config.LumenInputBinding;
import me.noramibu.lumentooltips.tooltip.preview.LumenContainerContents;
import me.noramibu.lumentooltips.tooltip.preview.LumenEnderChestMemory;
import me.noramibu.lumentooltips.tooltip.preview.LumenTooltipPreview;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class LumenTooltipAppender {
  private static final String ENCHANTMENT_LEVEL_PREFIX = "enchantment.level.";

  private LumenTooltipAppender() {}

  public static void append(
      Item.TooltipContext context,
      ItemStack stack,
      Player player,
      List<Component> tooltip,
      TooltipFlag flag) {
    if (stack == null || stack.isEmpty()) {
      return;
    }
    if (LumenTooltipPreview.suppressContainerText(stack, flag)) {
      tooltip.removeIf(LumenTooltipAppender::isContainerSummary);
    }
    LumenConfig config = LumenConfigManager.current();
    suppressPotionText(context, stack, tooltip);
    enhanceDurability(stack.getMaxDamage(), stack.getDamageValue(), tooltip, config);
    appendFood(context, stack, tooltip, flag, config);
    LumenExtraStatistics.append(stack, tooltip, flag, config.modules.extraStatistics);
    useDecimalEnchantmentLevels(tooltip, config);
    appendEnchantments(stack, tooltip, flag, config);
    if (config.modules.comparison.enabled
        && config.controls.detailsMode.isActive(flag, config.controls.detailsKey)) {
      LumenEquipmentComparison.append(stack, player, tooltip);
    }
    addBookAuthorHead(stack, tooltip, flag, config);
    if (config.modules.navigation.enabled) {
      LumenNavigationTooltip.append(stack, player, tooltip, config.modules.navigation);
    }
    appendEnderChestNotice(stack, tooltip, flag, config.modules.preview);
    if (config.modules.tooltip.showControlHints) {
      appendControlHints(stack, tooltip, config);
    }
  }

  private static void suppressPotionText(
      Item.TooltipContext context,
      ItemStack stack,
      List<Component> tooltip) {
    if (!LumenTooltipPreview.isPotionPreviewActive(stack)) {
      return;
    }
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    List<Component> vanilla = new ArrayList<>();
    PotionContents.addPotionTooltip(
        contents.getAllEffects(),
        vanilla::add,
        stack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F),
        context.tickRate());
    int start = Collections.indexOfSubList(tooltip, vanilla);
    if (start >= 0) {
      tooltip.subList(start, start + vanilla.size()).clear();
    }
  }

  private static void appendEnderChestNotice(
      ItemStack stack,
      List<Component> tooltip,
      TooltipFlag flag,
      LumenConfig.PreviewConfig config) {
    if (config.enabled
        && config.enderChest
        && stack.is(Items.ENDER_CHEST)
        && config.activation.isActive(flag, config.key)
        && !LumenEnderChestMemory.isKnown()) {
      tooltip.add(
          Component.translatable("tooltip.lumen_tooltips.ender_chest_unknown")
              .withStyle(ChatFormatting.GRAY));
    }
  }

  private static void addBookAuthorHead(
      ItemStack stack, List<Component> tooltip, TooltipFlag flag, LumenConfig config) {
    LumenConfig.PreviewConfig preview = config.modules.preview;
    WrittenBookContent book = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
    if (book == null
        || !preview.enabled
        || !preview.itemDetails
        || !preview.books
        || !preview.activation.isActive(flag, preview.key)) {
      return;
    }
    for (int index = 0; index < tooltip.size(); index++) {
      Component line = tooltip.get(index);
      if (line.getContents() instanceof TranslatableContents contents
          && "book.byAuthor".equals(contents.getKey())) {
        tooltip.set(index, new LumenBookAuthorTooltipComponent(line, book.author()));
        return;
      }
    }
  }

  private static void appendControlHints(
      ItemStack stack, List<Component> tooltip, LumenConfig config) {
    LumenConfig.PreviewConfig preview = config.modules.preview;
    LumenConfig.TooltipConfig hints = config.modules.tooltip;
    if (!preview.enabled) {
      return;
    }
    if (hints.showPreviewHint
        && preview.activation == HoldMode.KEY
        && !LumenInputBinding.UNBOUND.equals(preview.key)
        && !LumenInputBinding.isDown(preview.key)
        && LumenTooltipPreview.supports(stack)) {
      tooltip.add(
          controlHint(preview.key, "tooltip.lumen_tooltips.action.preview"));
    }
    String openAction =
        hints.showOpenContainerHint
                && preview.openContainers
                && LumenContainerOpener.isContainer(stack)
            ? "tooltip.lumen_tooltips.action.open_container"
            : hints.showOpenBookHint
                    && preview.openBooks
                    && LumenContainerOpener.isBook(stack)
                ? "tooltip.lumen_tooltips.action.open_book"
                : null;
    if (!LumenInputBinding.UNBOUND.equals(preview.openKey)
        && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>
        && openAction != null) {
      tooltip.add(controlHint(preview.openKey, openAction));
    }
  }

  public static Component controlHint(String key, String actionKey) {
    Style hintStyle = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
    Component keyName =
        LumenInputBinding.displayName(key)
            .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
    return Component.translatable(
            "tooltip.lumen_tooltips.control_hint",
            keyName,
            Component.translatable(actionKey).setStyle(hintStyle))
        .setStyle(hintStyle);
  }

  private static void enhanceDurability(
      int maxDamage, int damage, List<Component> tooltip, LumenConfig config) {
    if ((!config.modules.durability.showPercent && !config.modules.durability.useColors)
        || maxDamage <= 0) {
      return;
    }
    int remaining = maxDamage - damage;
    int percent = Math.round(remaining * 100.0f / maxDamage);
    for (int index = 0; index < tooltip.size(); index++) {
      Component line = tooltip.get(index);
      if (!(line.getContents() instanceof TranslatableContents contents)
          || !"item.durability".equals(contents.getKey())) {
        continue;
      }
      MutableComponent enhanced = line.copy();
      if (config.modules.durability.showPercent) {
        enhanced.append(" (" + percent + "%)");
      }
      if (config.modules.durability.useColors) {
        enhanced.withStyle(style -> style.withColor(durabilityColor(percent, config)));
      }
      tooltip.set(index, enhanced);
      return;
    }
  }

  private static void appendFood(
      Item.TooltipContext context,
      ItemStack stack,
      List<Component> tooltip,
      TooltipFlag flag,
      LumenConfig config) {
    if (!config.modules.food.enabled) {
      return;
    }
    FoodProperties food = stack.get(DataComponents.FOOD);
    if (food == null) {
      return;
    }
    Consumable consumable = stack.get(DataComponents.CONSUMABLE);
    if (config.modules.food.showHunger || config.modules.food.showSaturation) {
      tooltip.removeIf(LumenTooltipAppender::isAppleSkinFoodOverlay);
      tooltip.add(
          new LumenFoodTooltipComponent(
              food.nutrition(),
              food.saturation(),
              isRotten(consumable),
              config.modules.food.showHunger,
              config.modules.food.showSaturation));
    }
    appendFoodEffects(context, stack, tooltip, flag, config);
  }

  private static boolean isAppleSkinFoodOverlay(Component component) {
    return component
        .getClass()
        .getName()
        .equals("squeek.appleskin.client.TooltipOverlayHandler$FoodOverlayTextComponent");
  }

  private static boolean isContainerSummary(Component component) {
    if (!(component.getContents() instanceof TranslatableContents contents)) {
      return false;
    }
    return switch (contents.getKey()) {
      case "item.container.item_count", "item.container.more_items" -> true;
      default -> false;
    };
  }

  private static boolean isRotten(Consumable consumable) {
    if (consumable == null) {
      return false;
    }
    for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
      if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect effects) {
        for (MobEffectInstance effect : effects.effects()) {
          if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static void appendFoodEffects(
      Item.TooltipContext context,
      ItemStack stack,
      List<Component> tooltip,
      TooltipFlag flag,
      LumenConfig config) {
    if (!config.modules.food.showEffects
        || !config.controls.detailsMode.isActive(flag, config.controls.detailsKey)) {
      return;
    }
    List<MobEffectInstance> effects = new ArrayList<>();
    SuspiciousStewEffects stewEffects = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
    if (stewEffects != null && !flag.isCreative()) {
      for (SuspiciousStewEffects.Entry effect : stewEffects.effects()) {
        effects.add(effect.createEffectInstance());
      }
    }
    Consumable consumable = stack.get(DataComponents.CONSUMABLE);
    if (consumable != null) {
      for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
        if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect applyEffects) {
          effects.addAll(applyEffects.effects());
        }
      }
    }
    if (!effects.isEmpty()) {
      PotionContents.addPotionTooltip(effects, tooltip::add, 1.0F, context.tickRate());
    }
  }

  private static void appendEnchantments(
      ItemStack stack, List<Component> tooltip, TooltipFlag flag, LumenConfig config) {
    if (!config.modules.enchantments.enabled
        || !config.controls.detailsMode.isActive(flag, config.controls.detailsKey)) {
      return;
    }
    Set<Holder<Enchantment>> enchantments =
        new LinkedHashSet<>(
            stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).keySet());
    enchantments.addAll(
        stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet());
    for (Holder<Enchantment> enchantment : enchantments) {
      enchantment
          .unwrapKey()
          .map(key -> key.identifier().toLanguageKey("enchantment") + ".desc")
          .filter(I18n::exists)
          .ifPresent(
              descriptionKey ->
                  tooltip.add(
                      Component.translatable(
                              "tooltip.lumen_tooltips.enchantment_description",
                              enchantment.value().description(),
                              Component.translatable(descriptionKey))
                          .withStyle(ChatFormatting.GRAY)));
    }
  }

  private static void useDecimalEnchantmentLevels(List<Component> tooltip, LumenConfig config) {
    if (!config.modules.enchantments.decimalLevels) {
      return;
    }
    for (int lineIndex = 0; lineIndex < tooltip.size(); lineIndex++) {
      Component line = tooltip.get(lineIndex);
      for (int siblingIndex = 0; siblingIndex < line.getSiblings().size(); siblingIndex++) {
        Component level = decimalEnchantmentLevel(line.getSiblings().get(siblingIndex));
        if (level != null) {
          MutableComponent copy = line.copy();
          copy.getSiblings().set(siblingIndex, level);
          tooltip.set(lineIndex, copy);
          break;
        }
      }
    }
  }

  private static Component decimalEnchantmentLevel(Component component) {
    if (!(component.getContents() instanceof TranslatableContents contents)
        || !contents.getKey().startsWith(ENCHANTMENT_LEVEL_PREFIX)) {
      return null;
    }
    try {
      int level = Integer.parseInt(contents.getKey().substring(ENCHANTMENT_LEVEL_PREFIX.length()));
      return Component.literal(Integer.toString(level)).withStyle(component.getStyle());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static int durabilityColor(int percent, LumenConfig config) {
    boolean colorblind =
        config.modules.durability.palette
            == me.noramibu.lumentooltips.config.DurabilityPalette.COLORBLIND;
    if (percent <= config.modules.durability.dangerPercent) {
      return colorblind ? 0xD55E00 : 0xFF5555;
    }
    if (percent <= config.modules.durability.warningPercent) {
      return colorblind ? 0xE69F00 : 0xFFFF55;
    }
    return colorblind ? 0x0072B2 : 0x55FF55;
  }

}
