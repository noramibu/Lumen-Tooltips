package me.noramibu.lumentooltips.config;

import java.util.List;
import java.util.stream.Stream;
import me.noramibu.lumentooltips.client.LumenItemEditor;
import net.minecraft.core.registries.BuiltInRegistries;

public final class LumenOptionRegistry {
  private static final String ITEM_EDITOR_KEY_PATH = "controls.itemEditorKey";
  private static final String ITEM_EDITOR_STORAGE_PREFIX = "modules.itemEditor.";
  private static final List<ConfigOption> OPTIONS =
      List.of(
          ConfigOption.holdMode(
              "controls.detailsMode",
              config -> config.controls.detailsMode,
              (config, value) -> config.controls.detailsMode = value),
          ConfigOption.keyBind(
              "controls.detailsKey",
              config -> config.controls.detailsKey,
              (config, value) -> config.controls.detailsKey = value),
          ConfigOption.keyBind(
              ITEM_EDITOR_KEY_PATH,
              config -> config.controls.itemEditorKey,
              (config, value) -> config.controls.itemEditorKey = value),
          ConfigOption.keyBind(
              "modules.itemEditor.saveKey",
              config -> config.modules.itemEditor.saveKey,
              (config, value) -> config.modules.itemEditor.saveKey = value),
          ConfigOption.itemEditorTarget(
              "modules.itemEditor.target",
              config -> config.modules.itemEditor.target,
              (config, value) -> config.modules.itemEditor.target = value),
          ConfigOption.number(
              "modules.itemEditor.pageNumber",
              1,
              LumenConfig.ItemEditorConfig.MAX_PAGE_NUMBER,
              config -> config.modules.itemEditor.pageNumber,
              (config, value) -> config.modules.itemEditor.pageNumber = value),
          ConfigOption.text(
              "modules.itemEditor.pageName",
              LumenConfig.ItemEditorConfig.MAX_PAGE_NAME_LENGTH,
              config -> config.modules.itemEditor.pageName,
              (config, value) -> config.modules.itemEditor.pageName = value),
          ConfigOption.toggle(
              "modules.itemEditor.createPage",
              config -> config.modules.itemEditor.createPage,
              (config, value) -> config.modules.itemEditor.createPage = value),
          ConfigOption.toggle(
              "modules.itemEditor.showFeedback",
              config -> config.modules.itemEditor.showFeedback,
              (config, value) -> config.modules.itemEditor.showFeedback = value),
          ConfigOption.toggle(
              "modules.safety.translationCrashFix",
              config -> config.modules.safety.translationCrashFix,
              (config, value) -> config.modules.safety.translationCrashFix = value),
          ConfigOption.toggle(
              "modules.safety.globalComponentVisitGuard",
              config -> config.modules.safety.globalComponentVisitGuard,
              (config, value) -> config.modules.safety.globalComponentVisitGuard = value),
          ConfigOption.integer(
              "modules.safety.maxCharacters",
              256,
              65_536,
              256,
              config -> config.modules.safety.maxCharacters,
              (config, value) -> config.modules.safety.maxCharacters = value),
          ConfigOption.integer(
              "modules.safety.maxTranslationDepth",
              8,
              256,
              8,
              config -> config.modules.safety.maxTranslationDepth,
              (config, value) -> config.modules.safety.maxTranslationDepth = value),
          ConfigOption.integer(
              "modules.safety.maxTranslationVisits",
              64,
              8192,
              64,
              config -> config.modules.safety.maxTranslationVisits,
              (config, value) -> config.modules.safety.maxTranslationVisits = value),
          ConfigOption.toggle(
              "modules.durability.showPercent",
              config -> config.modules.durability.showPercent,
              (config, value) -> config.modules.durability.showPercent = value),
          ConfigOption.toggle(
              "modules.durability.useColors",
              config -> config.modules.durability.useColors,
              (config, value) -> config.modules.durability.useColors = value),
          ConfigOption.durabilityPalette(
              "modules.durability.palette",
              config -> config.modules.durability.palette,
              (config, value) -> config.modules.durability.palette = value),
          ConfigOption.percent(
              "modules.durability.warningPercent",
              1,
              99,
              5,
              config -> config.modules.durability.warningPercent,
              (config, value) -> config.modules.durability.warningPercent = value),
          ConfigOption.percent(
              "modules.durability.dangerPercent",
              1,
              99,
              5,
              config -> config.modules.durability.dangerPercent,
              (config, value) -> config.modules.durability.dangerPercent = value),
          ConfigOption.toggle(
              "modules.food.enabled",
              config -> config.modules.food.enabled,
              (config, value) -> config.modules.food.enabled = value),
          ConfigOption.toggle(
              "modules.food.showHunger",
              config -> config.modules.food.showHunger,
              (config, value) -> config.modules.food.showHunger = value),
          ConfigOption.toggle(
              "modules.food.showSaturation",
              config -> config.modules.food.showSaturation,
              (config, value) -> config.modules.food.showSaturation = value),
          ConfigOption.toggle(
              "modules.food.showEffects",
              config -> config.modules.food.showEffects,
              (config, value) -> config.modules.food.showEffects = value),
          ConfigOption.toggle(
              "modules.enchantments.enabled",
              config -> config.modules.enchantments.enabled,
              (config, value) -> config.modules.enchantments.enabled = value),
          ConfigOption.toggle(
              "modules.enchantments.decimalLevels",
              config -> config.modules.enchantments.decimalLevels,
              (config, value) -> config.modules.enchantments.decimalLevels = value),
          ConfigOption.toggle(
              "modules.comparison.enabled",
              config -> config.modules.comparison.enabled,
              (config, value) -> config.modules.comparison.enabled = value),
          ConfigOption.toggle(
              "modules.navigation.enabled",
              config -> config.modules.navigation.enabled,
              (config, value) -> config.modules.navigation.enabled = value),
          ConfigOption.toggle(
              "modules.navigation.maps",
              config -> config.modules.navigation.maps,
              (config, value) -> config.modules.navigation.maps = value),
          ConfigOption.toggle(
              "modules.navigation.compasses",
              config -> config.modules.navigation.compasses,
              (config, value) -> config.modules.navigation.compasses = value),
          ConfigOption.toggle(
              "modules.extraStatistics.enabled",
              config -> config.modules.extraStatistics.enabled,
              (config, value) -> config.modules.extraStatistics.enabled = value),
          ConfigOption.holdMode(
              "modules.extraStatistics.activation",
              config -> config.modules.extraStatistics.activation,
              (config, value) -> config.modules.extraStatistics.activation = value),
          ConfigOption.keyBind(
              "modules.extraStatistics.key",
              config -> config.modules.extraStatistics.key,
              (config, value) -> config.modules.extraStatistics.key = value),
          ConfigOption.toggle(
              "modules.extraStatistics.useSeconds",
              config -> config.modules.extraStatistics.useSeconds,
              (config, value) -> config.modules.extraStatistics.useSeconds = value),
          ConfigOption.toggle(
              "modules.extraStatistics.fuelTime",
              config -> config.modules.extraStatistics.fuelTime,
              (config, value) -> config.modules.extraStatistics.fuelTime = value),
          ConfigOption.toggle(
              "modules.extraStatistics.compostChance",
              config -> config.modules.extraStatistics.compostChance,
              (config, value) -> config.modules.extraStatistics.compostChance = value),
          ConfigOption.toggle(
              "modules.extraStatistics.useCooldown",
              config -> config.modules.extraStatistics.useCooldown,
              (config, value) -> config.modules.extraStatistics.useCooldown = value),
          ConfigOption.toggle(
              "modules.extraStatistics.enchantability",
              config -> config.modules.extraStatistics.enchantability,
              (config, value) -> config.modules.extraStatistics.enchantability = value),
          ConfigOption.toggle(
              "modules.extraStatistics.repairCost",
              config -> config.modules.extraStatistics.repairCost,
              (config, value) -> config.modules.extraStatistics.repairCost = value),
          ConfigOption.toggle(
              "modules.extraStatistics.blockHardness",
              config -> config.modules.extraStatistics.blockHardness,
              (config, value) -> config.modules.extraStatistics.blockHardness = value),
          ConfigOption.toggle(
              "modules.extraStatistics.blastResistance",
              config -> config.modules.extraStatistics.blastResistance,
              (config, value) -> config.modules.extraStatistics.blastResistance = value),
          ConfigOption.toggle(
              "modules.extraStatistics.enchantmentPower",
              config -> config.modules.extraStatistics.enchantmentPower,
              (config, value) -> config.modules.extraStatistics.enchantmentPower = value),
          ConfigOption.toggle(
              "modules.extraStatistics.miningLevel",
              config -> config.modules.extraStatistics.miningLevel,
              (config, value) -> config.modules.extraStatistics.miningLevel = value),
          ConfigOption.toggle(
              "modules.extraStatistics.miningSpeed",
              config -> config.modules.extraStatistics.miningSpeed,
              (config, value) -> config.modules.extraStatistics.miningSpeed = value),
          ConfigOption.toggle(
              "modules.extraStatistics.modName",
              config -> config.modules.extraStatistics.modName,
              (config, value) -> config.modules.extraStatistics.modName = value),
          ConfigOption.toggle(
              "modules.tooltip.edgeFix",
              config -> config.modules.tooltip.edgeFix,
              (config, value) -> config.modules.tooltip.edgeFix = value),
          ConfigOption.toggle(
              "modules.tooltip.scrollLongTooltips",
              config -> config.modules.tooltip.scrollLongTooltips,
              (config, value) -> config.modules.tooltip.scrollLongTooltips = value),
          ConfigOption.toggle(
              "modules.tooltip.showControlHints",
              config -> config.modules.tooltip.showControlHints,
              (config, value) -> config.modules.tooltip.showControlHints = value),
          ConfigOption.toggle(
              "modules.tooltip.showPreviewHint",
              config -> config.modules.tooltip.showPreviewHint,
              (config, value) -> config.modules.tooltip.showPreviewHint = value),
          ConfigOption.toggle(
              "modules.tooltip.showOpenContainerHint",
              config -> config.modules.tooltip.showOpenContainerHint,
              (config, value) -> config.modules.tooltip.showOpenContainerHint = value),
          ConfigOption.toggle(
              "modules.tooltip.showOpenBookHint",
              config -> config.modules.tooltip.showOpenBookHint,
              (config, value) -> config.modules.tooltip.showOpenBookHint = value),
          ConfigOption.toggle(
              "modules.tooltip.showEditItemHint",
              config -> config.modules.tooltip.showEditItemHint,
              (config, value) -> config.modules.tooltip.showEditItemHint = value),
          ConfigOption.toggle(
              "modules.tooltip.showSaveItemHint",
              config -> config.modules.tooltip.showSaveItemHint,
              (config, value) -> config.modules.tooltip.showSaveItemHint = value),
          ConfigOption.integer(
              "modules.tooltip.maxWidth",
              0,
              16_384,
              20,
              config -> config.modules.tooltip.maxWidth,
              (config, value) -> config.modules.tooltip.maxWidth = value),
          ConfigOption.integer(
              "modules.tooltip.scrollStep",
              4,
              64,
              2,
              config -> config.modules.tooltip.scrollStep,
              (config, value) -> config.modules.tooltip.scrollStep = value),
          ConfigOption.toggle(
              "modules.preview.enabled",
              config -> config.modules.preview.enabled,
              (config, value) -> config.modules.preview.enabled = value),
          ConfigOption.previewDensity(
              "modules.preview.density",
              config -> config.modules.preview.density,
              (config, value) -> config.modules.preview.density = value),
          ConfigOption.toggle(
              "modules.preview.accents",
              config -> config.modules.preview.accents,
              (config, value) -> config.modules.preview.accents = value),
          ConfigOption.toggle(
              "modules.preview.reducedMotion",
              config -> config.modules.preview.reducedMotion,
              (config, value) -> config.modules.preview.reducedMotion = value),
          ConfigOption.inputHoldMode(
              "modules.preview.activation",
              config -> config.modules.preview.activation,
              (config, value) -> config.modules.preview.activation = value),
          ConfigOption.keyBind(
              "modules.preview.key",
              config -> config.modules.preview.key,
              (config, value) -> config.modules.preview.key = value),
          ConfigOption.toggle(
              "modules.preview.openContainers",
              config -> config.modules.preview.openContainers,
              (config, value) -> config.modules.preview.openContainers = value),
          ConfigOption.toggle(
              "modules.preview.openBooks",
              config -> config.modules.preview.openBooks,
              (config, value) -> config.modules.preview.openBooks = value),
          ConfigOption.keyBind(
              "modules.preview.openKey",
              config -> config.modules.preview.openKey,
              (config, value) -> config.modules.preview.openKey = value),
          ConfigOption.toggle(
              "modules.preview.nestedNavigation",
              config -> config.modules.preview.nestedNavigation,
              (config, value) -> config.modules.preview.nestedNavigation = value),
          ConfigOption.toggle(
              "modules.preview.shulkers",
              config -> config.modules.preview.shulkers,
              (config, value) -> config.modules.preview.shulkers = value),
          ConfigOption.toggle(
              "modules.preview.containers",
              config -> config.modules.preview.containers,
              (config, value) -> config.modules.preview.containers = value),
          ConfigOption.containerMode(
              "modules.preview.containerMode",
              config -> config.modules.preview.containerMode,
              (config, value) -> config.modules.preview.containerMode = value),
          ConfigOption.toggle(
              "modules.preview.showContainerTitle",
              config -> config.modules.preview.showContainerTitle,
              (config, value) -> config.modules.preview.showContainerTitle = value),
          ConfigOption.toggle(
              "modules.preview.showContainerCounts",
              config -> config.modules.preview.showContainerCounts,
              (config, value) -> config.modules.preview.showContainerCounts = value),
          ConfigOption.percent(
              "modules.preview.containerTintPercent",
              0,
              100,
              5,
              config -> config.modules.preview.containerTintPercent,
              (config, value) -> config.modules.preview.containerTintPercent = value),
          ConfigOption.toggle(
              "modules.preview.bundles",
              config -> config.modules.preview.bundles,
              (config, value) -> config.modules.preview.bundles = value),
          ConfigOption.toggle(
              "modules.preview.maps",
              config -> config.modules.preview.maps,
              (config, value) -> config.modules.preview.maps = value),
          ConfigOption.toggle(
              "modules.preview.banners",
              config -> config.modules.preview.banners,
              (config, value) -> config.modules.preview.banners = value),
          ConfigOption.toggle(
              "modules.preview.decoratedPots",
              config -> config.modules.preview.decoratedPots,
              (config, value) -> config.modules.preview.decoratedPots = value),
          ConfigOption.toggle(
              "modules.preview.potions",
              config -> config.modules.preview.potions,
              (config, value) -> config.modules.preview.potions = value),
          ConfigOption.toggle(
              "modules.preview.enderChest",
              config -> config.modules.preview.enderChest,
              (config, value) -> config.modules.preview.enderChest = value),
          ConfigOption.toggle(
              "modules.preview.paintings",
              config -> config.modules.preview.paintings,
              (config, value) -> config.modules.preview.paintings = value),
          ConfigOption.toggle(
              "modules.preview.playerHeads",
              config -> config.modules.preview.playerHeads,
              (config, value) -> config.modules.preview.playerHeads = value),
          ConfigOption.toggle(
              "modules.preview.signs",
              config -> config.modules.preview.signs,
              (config, value) -> config.modules.preview.signs = value),
          ConfigOption.toggle(
              "modules.preview.itemDetails",
              config -> config.modules.preview.itemDetails,
              (config, value) -> config.modules.preview.itemDetails = value),
          ConfigOption.toggle(
              "modules.preview.books",
              config -> config.modules.preview.books,
              (config, value) -> config.modules.preview.books = value),
          ConfigOption.toggle(
              "modules.preview.fireworks",
              config -> config.modules.preview.fireworks,
              (config, value) -> config.modules.preview.fireworks = value),
          ConfigOption.toggle(
              "modules.preview.entities",
              config -> config.modules.preview.entities,
              (config, value) -> config.modules.preview.entities = value),
          ConfigOption.toggle(
              "modules.preview.areaEffectClouds",
              config -> config.modules.preview.areaEffectClouds,
              (config, value) -> config.modules.preview.areaEffectClouds = value),
          ConfigOption.toggle(
              "modules.preview.displayEntities",
              config -> config.modules.preview.displayEntities,
              (config, value) -> config.modules.preview.displayEntities = value),
          ConfigOption.toggle(
              "modules.preview.itemFrames",
              config -> config.modules.preview.itemFrames,
              (config, value) -> config.modules.preview.itemFrames = value),
          ConfigOption.integer(
              "modules.preview.displayYaw",
              -180,
              180,
              5,
              config -> config.modules.preview.displayYaw,
              (config, value) -> config.modules.preview.displayYaw = value),
          ConfigOption.integer(
              "modules.preview.displayPitch",
              -90,
              90,
              5,
              config -> config.modules.preview.displayPitch,
              (config, value) -> config.modules.preview.displayPitch = value),
          ConfigOption.toggle(
              "modules.preview.spawnEggs",
              config -> config.modules.preview.spawnEggs,
              (config, value) -> config.modules.preview.spawnEggs = value),
          ConfigOption.toggle(
              "modules.preview.mobBuckets",
              config -> config.modules.preview.mobBuckets,
              (config, value) -> config.modules.preview.mobBuckets = value),
          ConfigOption.toggle(
              "modules.preview.spawners",
              config -> config.modules.preview.spawners,
              (config, value) -> config.modules.preview.spawners = value));
  private static final List<ConfigOption> ACTIVE_OPTIONS =
      Stream.concat(OPTIONS.stream(), tooltipFlagOptions())
          .filter(
              option ->
                  LumenItemEditor.isAvailable()
                      || !ITEM_EDITOR_KEY_PATH.equals(option.path()))
          .filter(
              option ->
                  LumenItemEditor.isAvailable()
                      || !option.path().startsWith(ITEM_EDITOR_STORAGE_PREFIX))
          .toList();

  private LumenOptionRegistry() {}

  private static Stream<ConfigOption> tooltipFlagOptions() {
    return Stream.concat(
        Stream.of(
            ConfigOption.toggle(
                "modules.tooltipFlags.ignoreHideTooltip",
                config -> config.modules.tooltip.ignoreHideTooltip,
                (config, value) -> config.modules.tooltip.ignoreHideTooltip = value)),
        BuiltInRegistries.DATA_COMPONENT_TYPE.keySet().stream()
            .filter(id -> "minecraft".equals(id.getNamespace()))
            .sorted()
            .map(id -> hiddenComponentOption(id.toString())));
  }

  private static ConfigOption hiddenComponentOption(String id) {
    return ConfigOption.hiddenComponent(
        id,
        config -> config.modules.tooltip.ignoredHiddenComponents.contains(id),
        (config, value) -> {
          config.modules.tooltip.ignoredHiddenComponents.remove(id);
          if (value) {
            config.modules.tooltip.ignoredHiddenComponents.add(id);
          }
        });
  }

  public static List<ConfigOption> options() {
    return ACTIVE_OPTIONS;
  }

  public static boolean isVisible(ConfigOption option, LumenConfig config) {
    LumenConfig.PreviewConfig preview = config.modules.preview;
    if (option.path().startsWith("modules.extraStatistics.")
        && !option.path().equals("modules.extraStatistics.enabled")
        && !config.modules.extraStatistics.enabled) {
      return false;
    }
    return switch (option.path()) {
      case "controls.detailsKey" -> config.controls.detailsMode == HoldMode.KEY;
      case "modules.itemEditor.pageNumber" ->
          config.modules.itemEditor.target == ItemEditorStorageTarget.PAGE_NUMBER;
      case "modules.itemEditor.pageName" ->
          config.modules.itemEditor.target == ItemEditorStorageTarget.PAGE_NAME
              || config.modules.itemEditor.target == ItemEditorStorageTarget.FIRST_AVAILABLE
                  && config.modules.itemEditor.createPage;
      case "modules.itemEditor.createPage" ->
          config.modules.itemEditor.target != ItemEditorStorageTarget.PAGE_NUMBER;
      case "modules.durability.warningPercent", "modules.durability.dangerPercent" ->
          config.modules.durability.useColors;
      case "modules.durability.palette" -> config.modules.durability.useColors;
      case "modules.food.showHunger", "modules.food.showSaturation", "modules.food.showEffects" ->
          config.modules.food.enabled;
      case "modules.navigation.maps", "modules.navigation.compasses" ->
          config.modules.navigation.enabled;
      case "modules.extraStatistics.key" ->
          config.modules.extraStatistics.activation == HoldMode.KEY;
      case "modules.tooltip.scrollStep" -> config.modules.tooltip.scrollLongTooltips;
      case "modules.tooltip.showPreviewHint" ->
          config.modules.tooltip.showControlHints
              && preview.enabled
              && preview.activation == HoldMode.KEY;
      case "modules.tooltip.showOpenContainerHint" ->
          config.modules.tooltip.showControlHints
              && preview.enabled
              && preview.openContainers;
      case "modules.tooltip.showOpenBookHint" ->
          config.modules.tooltip.showControlHints && preview.enabled && preview.openBooks;
      case "modules.tooltip.showEditItemHint", "modules.tooltip.showSaveItemHint" ->
          config.modules.tooltip.showControlHints && LumenItemEditor.isAvailable();
      case "modules.safety.globalComponentVisitGuard",
          "modules.safety.maxCharacters",
          "modules.safety.maxTranslationDepth",
          "modules.safety.maxTranslationVisits" ->
          config.modules.safety.translationCrashFix;
      case "modules.preview.activation",
          "modules.preview.density",
          "modules.preview.accents",
          "modules.preview.reducedMotion",
          "modules.preview.openContainers",
          "modules.preview.openBooks",
          "modules.preview.shulkers",
          "modules.preview.containers",
          "modules.preview.bundles",
          "modules.preview.maps",
          "modules.preview.banners",
          "modules.preview.decoratedPots",
          "modules.preview.potions",
          "modules.preview.enderChest",
          "modules.preview.paintings",
          "modules.preview.playerHeads",
          "modules.preview.signs",
          "modules.preview.itemDetails",
          "modules.preview.entities" -> preview.enabled;
      case "modules.preview.key" ->
          preview.enabled && preview.activation == HoldMode.KEY;
      case "modules.preview.openKey" ->
          preview.enabled && (preview.openContainers || preview.openBooks);
      case "modules.preview.nestedNavigation" -> preview.enabled && preview.openContainers;
      case "modules.preview.containerMode",
          "modules.preview.showContainerTitle",
          "modules.preview.showContainerCounts" ->
          preview.enabled && (preview.shulkers || preview.containers || preview.enderChest);
      case "modules.preview.containerTintPercent" ->
          preview.enabled
              && preview.accents
              && (preview.shulkers || preview.containers || preview.enderChest);
      case "modules.preview.books",
          "modules.preview.fireworks" -> preview.enabled && preview.itemDetails;
      case "modules.preview.displayYaw", "modules.preview.displayPitch" ->
          preview.enabled && preview.entities && preview.displayEntities;
      case "modules.preview.spawnEggs",
          "modules.preview.areaEffectClouds",
          "modules.preview.displayEntities",
          "modules.preview.itemFrames",
          "modules.preview.mobBuckets",
          "modules.preview.spawners" -> preview.enabled && preview.entities;
      default -> true;
    };
  }
}
