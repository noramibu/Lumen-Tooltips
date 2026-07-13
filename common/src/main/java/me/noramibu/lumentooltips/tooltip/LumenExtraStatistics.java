package me.noramibu.lumentooltips.tooltip;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.noramibu.lumentooltips.LumenTooltips;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.Enchantable;

final class LumenExtraStatistics {
  private static final float MAX_REASONABLE_MINING_SPEED = 1_000_000.0F;

  private LumenExtraStatistics() {}

  static void append(
      ItemStack stack,
      List<Component> tooltip,
      TooltipFlag flag,
      LumenConfig.ExtraStatisticsConfig config) {
    if (!config.enabled || !config.activation.isActive(flag, config.key)) {
      return;
    }
    if (config.fuelTime) {
      appendFuelTime(stack, tooltip, config.useSeconds);
    }
    if (config.compostChance) {
      float chance = LumenTooltips.compostChance(stack);
      addPositive(tooltip, "compost_chance", chance * 100.0F, "%");
    }
    if (config.useCooldown) {
      UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
      if (cooldown != null) {
        add(
            tooltip,
            "use_cooldown",
            duration(cooldown.ticks(), cooldown.seconds(), config.useSeconds));
      }
    }
    if (config.enchantability) {
      Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
      if (enchantable != null) {
        add(tooltip, "enchantability", Integer.toString(enchantable.value()));
      }
    }
    if (config.repairCost) {
      int cost = stack.getOrDefault(DataComponents.REPAIR_COST, 0);
      if (cost > 0) {
        add(tooltip, "repair_cost", Integer.toString(cost));
      }
    }
    appendBlockStatistics(stack, tooltip, config);
    appendToolStatistics(stack, tooltip, config);
    if (config.modName) {
      String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
      String modName = LumenTooltips.modName(namespace);
      add(tooltip, "mod_name", modName.equals(namespace) ? humanize(namespace) : modName);
    }
  }

  private static void appendFuelTime(
      ItemStack stack, List<Component> tooltip, boolean seconds) {
    int ticks = LumenTooltips.fuelTime(stack);
    if (ticks > 0) {
      add(tooltip, "fuel_time", duration(ticks, ticks / 20.0F, seconds));
    }
  }

  private static void appendBlockStatistics(
      ItemStack stack,
      List<Component> tooltip,
      LumenConfig.ExtraStatisticsConfig config) {
    if (!(config.blockHardness || config.blastResistance || config.enchantmentPower)
        || !(stack.getItem() instanceof BlockItem blockItem)) {
      return;
    }
    var block = blockItem.getBlock();
    if (config.blockHardness) {
      float hardness = block.defaultDestroyTime();
      add(
          tooltip,
          "block_hardness",
          hardness < 0.0F
              ? Component.translatable("tooltip.lumen_tooltips.extra.unbreakable")
              : Component.literal(format(hardness)));
    }
    if (config.blastResistance) {
      add(tooltip, "blast_resistance", format(LumenTooltips.blastResistance(block)));
    }
    if (config.enchantmentPower
        && block.defaultBlockState().is(BlockTags.ENCHANTMENT_POWER_PROVIDER)) {
      add(tooltip, "enchantment_power", "1");
    }
  }

  private static void appendToolStatistics(
      ItemStack stack,
      List<Component> tooltip,
      LumenConfig.ExtraStatisticsConfig config) {
    if (!(config.miningLevel || config.miningSpeed) || !isMiningTool(stack)) {
      return;
    }
    Tool tool = stack.get(DataComponents.TOOL);
    if (tool == null) {
      return;
    }
    if (config.miningLevel) {
      miningLevel(tool).ifPresent(level -> add(tooltip, "mining_level", level));
    }
    if (config.miningSpeed) {
      float speed = tool.defaultMiningSpeed();
      for (Tool.Rule rule : tool.rules()) {
        if (rule.speed().isPresent()) {
          float ruleSpeed = rule.speed().get();
          if (Float.isFinite(ruleSpeed) && ruleSpeed < MAX_REASONABLE_MINING_SPEED) {
            speed = Math.max(speed, ruleSpeed);
          }
        }
      }
      addPositive(tooltip, "mining_speed", speed, "x");
    }
  }

  private static Optional<Component> miningLevel(Tool tool) {
    for (Tool.Rule rule : tool.rules()) {
      if (!rule.correctForDrops().orElse(true)) {
        Optional<Component> level =
            rule.blocks()
            .unwrapKey()
            .map(key -> key.location().getPath())
            .filter(path -> path.startsWith("incorrect_for_") && path.endsWith("_tool"))
            .map(path -> path.substring(14, path.length() - 5))
            .map(LumenExtraStatistics::miningLevelName);
        if (level.isPresent()) {
          return level;
        }
      }
    }
    return Optional.empty();
  }

  private static boolean isMiningTool(ItemStack stack) {
    return stack.is(ItemTags.PICKAXES)
        || stack.is(ItemTags.AXES)
        || stack.is(ItemTags.SHOVELS)
        || stack.is(ItemTags.HOES);
  }

  private static void addPositive(
      List<Component> tooltip, String key, float value, String suffix) {
    if (value > 0.0F) {
      add(tooltip, key, format(value) + suffix);
    }
  }

  private static void add(List<Component> tooltip, String key, String value) {
    add(tooltip, key, Component.literal(value));
  }

  private static void add(List<Component> tooltip, String key, Component value) {
    tooltip.add(
        Component.translatable("tooltip.lumen_tooltips.extra." + key, value)
            .withStyle(ChatFormatting.DARK_GRAY));
  }

  private static Component duration(int ticks, float seconds, boolean useSeconds) {
    return Component.translatable(
        useSeconds
            ? "tooltip.lumen_tooltips.extra.seconds"
            : "tooltip.lumen_tooltips.extra.ticks",
        useSeconds ? format(seconds) : Integer.toString(ticks));
  }

  private static String format(float value) {
    int hundredths = Math.round(value * 100.0F);
    return hundredths % 100 == 0
        ? Integer.toString(hundredths / 100)
        : hundredths % 10 == 0
            ? String.format(Locale.ROOT, "%.1f", hundredths / 100.0F)
            : String.format(Locale.ROOT, "%.2f", hundredths / 100.0F);
  }

  private static Component miningLevelName(String level) {
    String key = "tooltip.lumen_tooltips.extra.mining_level." + level;
    return I18n.exists(key) ? Component.translatable(key) : Component.literal(humanize(level));
  }

  private static String humanize(String value) {
    String words = value.replace('_', ' ');
    return words.isEmpty()
        ? value
        : Character.toUpperCase(words.charAt(0)) + words.substring(1);
  }
}
