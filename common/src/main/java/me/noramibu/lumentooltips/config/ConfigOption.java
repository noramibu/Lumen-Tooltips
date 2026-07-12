package me.noramibu.lumentooltips.config;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

public record ConfigOption(
    String path,
    String languageKey,
    OptionControl control,
    String defaultValue,
    Function<LumenConfig, String> getter,
    BiConsumer<LumenConfig, String> setter,
    int minValue,
    int maxValue,
    int step,
    List<String> cycleValues) {
  private static final String HIDDEN_COMPONENT_KEY =
      "config.lumen_tooltips.modules.tooltip_flags.hidden_component";
  private static final String HIDDEN_COMPONENT_PATH = "modules.tooltipFlags.hiddenComponents.";
  private static final LumenConfig DEFAULTS = new LumenConfig();

  public ConfigOption {
    cycleValues = List.copyOf(cycleValues);
  }

  public static ConfigOption toggle(
      String path,
      Function<LumenConfig, Boolean> getter,
      BiConsumer<LumenConfig, Boolean> setter) {
    boolean defaultValue = getter.apply(DEFAULTS);
    return create(
        path,
        OptionControl.TOGGLE,
        Boolean.toString(defaultValue),
        config -> Boolean.toString(getter.apply(config)),
        (config, value) -> setter.accept(config, Boolean.parseBoolean(value)),
        0,
        1,
        1,
        List.of());
  }

  public static ConfigOption hiddenComponent(
      String id,
      Function<LumenConfig, Boolean> getter,
      BiConsumer<LumenConfig, Boolean> setter) {
    return toggle(HIDDEN_COMPONENT_PATH + id, getter, setter);
  }

  public static ConfigOption integer(
      String path,
      int minValue,
      int maxValue,
      int step,
      Function<LumenConfig, Integer> getter,
      BiConsumer<LumenConfig, Integer> setter) {
    return numeric(
        path, OptionControl.INTEGER, minValue, maxValue, step, getter, setter);
  }

  public static ConfigOption number(
      String path,
      int minValue,
      int maxValue,
      Function<LumenConfig, Integer> getter,
      BiConsumer<LumenConfig, Integer> setter) {
    return numeric(path, OptionControl.NUMBER, minValue, maxValue, 1, getter, setter);
  }

  public static ConfigOption percent(
      String path,
      int minValue,
      int maxValue,
      int step,
      Function<LumenConfig, Integer> getter,
      BiConsumer<LumenConfig, Integer> setter) {
    return numeric(
        path, OptionControl.PERCENT, minValue, maxValue, step, getter, setter);
  }

  private static ConfigOption numeric(
      String path,
      OptionControl control,
      int minValue,
      int maxValue,
      int step,
      Function<LumenConfig, Integer> getter,
      BiConsumer<LumenConfig, Integer> setter) {
    return create(
        path,
        control,
        Integer.toString(getter.apply(DEFAULTS)),
        config -> Integer.toString(getter.apply(config)),
        (config, value) ->
            setter.accept(config, Math.clamp(Integer.parseInt(value), minValue, maxValue)),
        minValue,
        maxValue,
        step,
        List.of());
  }

  public static ConfigOption keyBind(
      String path,
      Function<LumenConfig, String> getter,
      BiConsumer<LumenConfig, String> setter) {
    String defaultValue = getter.apply(DEFAULTS);
    return create(
        path,
        OptionControl.KEY_BIND,
        defaultValue,
        config -> LumenInputBinding.normalize(getter.apply(config), defaultValue),
        (config, value) -> setter.accept(config, LumenInputBinding.normalize(value, defaultValue)),
        0,
        0,
        1,
        List.of());
  }

  public static ConfigOption text(
      String path,
      int maxLength,
      Function<LumenConfig, String> getter,
      BiConsumer<LumenConfig, String> setter) {
    String defaultValue = getter.apply(DEFAULTS);
    return create(
        path,
        OptionControl.TEXT,
        defaultValue,
        getter,
        (config, value) ->
            setter.accept(
                config, value.substring(0, Math.min(value.length(), maxLength))),
        0,
        maxLength,
        1,
        List.of());
  }

  public static ConfigOption itemEditorTarget(
      String path,
      Function<LumenConfig, ItemEditorStorageTarget> getter,
      BiConsumer<LumenConfig, ItemEditorStorageTarget> setter) {
    ItemEditorStorageTarget defaultValue = getter.apply(DEFAULTS);
    return create(
        path,
        OptionControl.CYCLE,
        defaultValue.serializedName(),
        config -> getter.apply(config).serializedName(),
        (config, value) ->
            setter.accept(config, ItemEditorStorageTarget.byName(value, defaultValue)),
        0,
        0,
        1,
        List.of("first_available", "page_number", "page_name"));
  }

  public static ConfigOption holdMode(
      String path,
      Function<LumenConfig, HoldMode> getter,
      BiConsumer<LumenConfig, HoldMode> setter) {
    return holdMode(path, getter, setter, List.of("always", "key", "advanced"));
  }

  public static ConfigOption inputHoldMode(
      String path,
      Function<LumenConfig, HoldMode> getter,
      BiConsumer<LumenConfig, HoldMode> setter) {
    return holdMode(path, getter, setter, List.of("always", "key"));
  }

  private static ConfigOption holdMode(
      String path,
      Function<LumenConfig, HoldMode> getter,
      BiConsumer<LumenConfig, HoldMode> setter,
      List<String> cycleValues) {
    HoldMode defaultValue = getter.apply(DEFAULTS);
    return create(
        path,
        OptionControl.CYCLE,
        defaultValue.serializedName(),
        config -> getter.apply(config).serializedName(),
        (config, value) -> setter.accept(config, HoldMode.byName(value, defaultValue)),
        0,
        0,
        1,
        cycleValues);
  }

  private static ConfigOption create(
      String path,
      OptionControl control,
      String defaultValue,
      Function<LumenConfig, String> getter,
      BiConsumer<LumenConfig, String> setter,
      int minValue,
      int maxValue,
      int step,
      List<String> cycleValues) {
    return new ConfigOption(
        path,
        languageKey(path),
        control,
        defaultValue,
        getter,
        setter,
        minValue,
        maxValue,
        step,
        cycleValues);
  }

  public Component title() {
    return translated("");
  }

  public Component description() {
    return translated(".desc");
  }

  private Component translated(String suffix) {
    String key = this.languageKey + suffix;
    return this.path.startsWith(HIDDEN_COMPONENT_PATH)
        ? Component.translatable(key, this.path.substring(HIDDEN_COMPONENT_PATH.length()))
        : Component.translatable(key);
  }

  @Nullable
  public Component preview(LumenConfig config) {
    if (this.path.startsWith("modules.durability.")) {
      return durabilityPreview(config);
    }
    if (this.path.startsWith("modules.food.")) {
      return foodPreview(config);
    }
    if (this.path.startsWith("modules.enchantments.")) {
      return enchantmentPreview(config);
    }
    if (this.path.startsWith("modules.navigation.")) {
      return navigationPreview(config, this.path.endsWith("compasses"));
    }
    return switch (this.path) {
      case "modules.comparison.enabled" -> comparisonPreview(config);
      case "modules.tooltip.showControlHints" -> controlHintPreview(config);
      case "modules.safety.translationCrashFix", "modules.safety.globalComponentVisitGuard" ->
          safetyPreview(config, this.path.endsWith("globalComponentVisitGuard"));
      default -> null;
    };
  }

  public boolean matchesSearch(String normalizedQuery) {
    return searchText().contains(normalizedQuery);
  }

  public String getAsString(LumenConfig config) {
    return this.getter.apply(config);
  }

  public Component valueComponent(LumenConfig config) {
    Component title = title();
    String value = getAsString(config);
    return switch (this.control) {
      case TOGGLE -> {
        boolean enabled = Boolean.parseBoolean(value);
        yield CommonComponents.optionNameValue(
            title,
            colored(
                enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
                enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
      }
      case INTEGER -> {
        boolean automatic = "0".equals(value);
        yield CommonComponents.optionNameValue(
            title,
            colored(
                automatic
                    ? Component.translatable("config.lumen_tooltips.value.auto")
                    : Component.literal(value),
                automatic ? ChatFormatting.GREEN : ChatFormatting.AQUA));
      }
      case NUMBER, TEXT ->
          CommonComponents.optionNameValue(
              title, colored(Component.literal(value), ChatFormatting.AQUA));
      case PERCENT ->
          CommonComponents.optionNameValue(
              title, colored(Component.literal(value + "%"), ChatFormatting.AQUA));
      case CYCLE ->
          CommonComponents.optionNameValue(
              title,
              colored(
                  this.path.equals("modules.itemEditor.target")
                      ? ItemEditorStorageTarget.byName(
                              value, ItemEditorStorageTarget.FIRST_AVAILABLE)
                          .displayName()
                      : HoldMode.byName(value, HoldMode.ALWAYS).displayName(),
                  ChatFormatting.YELLOW));
      case KEY_BIND ->
          CommonComponents.optionNameValue(
              title,
              colored(
                  LumenInputBinding.displayName(value),
                  LumenInputBinding.UNBOUND.equals(value)
                      ? ChatFormatting.RED
                      : ChatFormatting.YELLOW));
    };
  }

  private static Component colored(Component component, ChatFormatting color) {
    return component.copy().withStyle(color);
  }

  private static Component durabilityPreview(LumenConfig config) {
    int percent = 42;
    MutableComponent durability = Component.translatable("item.durability", 656, 1561);
    if (config.modules.durability.showPercent) {
      durability.append(" (" + percent + "%)");
    }
    if (config.modules.durability.useColors) {
      durability.withStyle(
          percent <= config.modules.durability.dangerPercent
              ? ChatFormatting.RED
              : percent <= config.modules.durability.warningPercent
                  ? ChatFormatting.YELLOW
                  : ChatFormatting.GREEN);
    }
    return Component.translatable("item.minecraft.diamond_pickaxe").append("\n").append(durability);
  }

  private static Component enchantmentPreview(LumenConfig config) {
    MutableComponent preview = Component.translatable("item.minecraft.diamond_sword");
    Component level =
        config.modules.enchantments.decimalLevels
            ? Component.literal("5")
            : Component.translatable("enchantment.level.5");
    Component enchantment =
        Component.translatable("enchantment.minecraft.sharpness")
            .append(" ")
            .append(level)
            .withStyle(ChatFormatting.GRAY);
    preview.append("\n").append(enchantment);
    if (config.modules.enchantments.enabled) {
      preview
          .append("\n")
          .append(
              Component.translatable(
                      "tooltip.lumen_tooltips.enchantment_description",
                      Component.translatable("enchantment.minecraft.sharpness"),
                      Component.translatable("enchantment.minecraft.sharpness.desc"))
                  .withStyle(ChatFormatting.GRAY));
    }
    return preview;
  }

  private static Component foodPreview(LumenConfig config) {
    MutableComponent preview = Component.translatable("item.minecraft.suspicious_stew");
    if (!config.modules.food.enabled) {
      return preview;
    }
    if (config.modules.food.showHunger) {
      preview
          .append("\n")
          .append(
              Component.translatable("screen.lumen_tooltips.config.preview.hunger", 6)
                  .withStyle(ChatFormatting.RED));
    }
    if (config.modules.food.showSaturation) {
      preview
          .append("\n")
          .append(
              Component.translatable("screen.lumen_tooltips.config.preview.saturation", "7.2")
                  .withStyle(ChatFormatting.GOLD));
    }
    if (config.modules.food.showEffects) {
      preview
          .append("\n")
          .append(
              Component.translatable("effect.minecraft.night_vision")
                  .append(" (0:05)")
                  .withStyle(ChatFormatting.BLUE));
    }
    return preview;
  }

  private static Component comparisonPreview(LumenConfig config) {
    MutableComponent preview = Component.translatable("item.minecraft.diamond_chestplate");
    if (config.modules.comparison.enabled) {
      preview
          .append("\n")
          .append(
              Component.translatable(
                      "tooltip.lumen_tooltips.compared_with",
                      Component.translatable("item.minecraft.iron_chestplate"))
                  .withStyle(ChatFormatting.GRAY))
          .append("\n")
          .append(
              Component.literal("+2 ")
                  .append(Component.translatable("attribute.name.armor"))
                  .withStyle(ChatFormatting.GREEN));
    }
    return preview;
  }

  private static Component navigationPreview(LumenConfig config, boolean compass) {
    MutableComponent preview =
        Component.translatable(
            compass ? "item.minecraft.compass" : "item.minecraft.filled_map");
    if (config.modules.navigation.enabled
        && (compass ? config.modules.navigation.compasses : config.modules.navigation.maps)) {
      preview
          .append("\n")
          .append(
              compass
                  ? Component.translatable("tooltip.lumen_tooltips.navigation.distance", 128)
                  : Component.translatable("tooltip.lumen_tooltips.navigation.map_scale", 2));
    }
    return preview;
  }

  private static Component controlHintPreview(LumenConfig config) {
    MutableComponent preview = Component.translatable("block.minecraft.shulker_box");
    if (config.modules.tooltip.showControlHints) {
      preview
          .append("\n")
          .append(
              Component.translatable(
                      "tooltip.lumen_tooltips.control_hint",
                      LumenInputBinding.displayName(config.modules.preview.key),
                      Component.translatable("tooltip.lumen_tooltips.action.preview"))
                  .withStyle(ChatFormatting.DARK_GRAY));
    }
    return preview;
  }

  private static Component safetyPreview(LumenConfig config, boolean global) {
    boolean enabled =
        config.modules.safety.translationCrashFix
            && (!global || config.modules.safety.globalComponentVisitGuard);
    return Component.translatable("screen.lumen_tooltips.config.preview.unsafe_item")
        .append("\n")
        .append(
            Component.translatable(
                    enabled
                        ? "tooltip.lumen_tooltips.unsafe_text"
                        : "screen.lumen_tooltips.config.preview.expanded_text")
                .withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GRAY));
  }

  public void setFromString(LumenConfig config, String value) {
    this.setter.accept(config, value);
  }

  public void cycle(LumenConfig config) {
    switch (this.control) {
      case TOGGLE ->
          this.setter.accept(
              config, Boolean.toString(!Boolean.parseBoolean(getAsString(config))));
      case INTEGER, NUMBER, PERCENT -> cycleInteger(config);
      case CYCLE -> cycleValue(config);
      case KEY_BIND -> this.setter.accept(config, this.defaultValue);
      case TEXT -> {}
    }
  }

  private String searchText() {
    return String.join(" ", this.path, title().getString(), description().getString())
        .toLowerCase(Locale.ROOT);
  }

  private void cycleInteger(LumenConfig config) {
    int current = Integer.parseInt(getAsString(config));
    int next = current + this.step;
    if (next > this.maxValue) {
      next = this.minValue;
    }
    this.setter.accept(config, Integer.toString(next));
  }

  private void cycleValue(LumenConfig config) {
    String current = getAsString(config).toLowerCase(Locale.ROOT);
    int index = this.cycleValues.indexOf(current);
    int nextIndex = index < 0 ? 0 : (index + 1) % this.cycleValues.size();
    this.setter.accept(config, this.cycleValues.get(nextIndex));
  }

  private static String languageKey(String path) {
    if (path.startsWith(HIDDEN_COMPONENT_PATH)) {
      return HIDDEN_COMPONENT_KEY;
    }
    return "config.lumen_tooltips."
        + path.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
  }

}
