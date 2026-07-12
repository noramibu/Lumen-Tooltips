package me.noramibu.lumentooltips.config;

import java.util.LinkedHashSet;
import java.util.Set;

public final class LumenConfig {
  public int schemaVersion = 1;
  public ControlConfig controls = new ControlConfig();
  public ModuleConfig modules = new ModuleConfig();

  public LumenConfig copy() {
    LumenConfig copy = new LumenConfig();
    copy.schemaVersion = this.schemaVersion;
    copy.controls = this.controls.copy();
    copy.modules = this.modules.copy();
    return copy;
  }

  public static final class ControlConfig {
    public HoldMode detailsMode = HoldMode.KEY;
    public String detailsKey = LumenInputBinding.LEFT_SHIFT;
    public String itemEditorKey = LumenInputBinding.CONTROL_SPACE;

    private ControlConfig copy() {
      ControlConfig copy = new ControlConfig();
      copy.detailsMode = this.detailsMode;
      copy.detailsKey = this.detailsKey;
      copy.itemEditorKey = this.itemEditorKey;
      return copy;
    }
  }

  public static final class ModuleConfig {
    public DurabilityConfig durability = new DurabilityConfig();
    public FoodConfig food = new FoodConfig();
    public EnchantmentConfig enchantments = new EnchantmentConfig();
    public ComparisonConfig comparison = new ComparisonConfig();
    public NavigationConfig navigation = new NavigationConfig();
    public ItemEditorConfig itemEditor = new ItemEditorConfig();
    public SafetyConfig safety = new SafetyConfig();
    public TooltipConfig tooltip = new TooltipConfig();
    public PreviewConfig preview = new PreviewConfig();

    private ModuleConfig copy() {
      ModuleConfig copy = new ModuleConfig();
      copy.durability = this.durability.copy();
      copy.food = this.food.copy();
      copy.enchantments = this.enchantments.copy();
      copy.comparison = this.comparison.copy();
      copy.navigation = this.navigation.copy();
      copy.itemEditor = this.itemEditor.copy();
      copy.safety = this.safety.copy();
      copy.tooltip = this.tooltip.copy();
      copy.preview = this.preview.copy();
      return copy;
    }
  }

  public static final class ItemEditorConfig {
    public static final int MAX_PAGE_NUMBER = 10_000;
    public static final int MAX_PAGE_NAME_LENGTH = 80;
    public static final String DEFAULT_PAGE_NAME = "Lumen Tooltips";

    public String saveKey = LumenInputBinding.CONTROL_S;
    public ItemEditorStorageTarget target = ItemEditorStorageTarget.FIRST_AVAILABLE;
    public int pageNumber = 1;
    public String pageName = DEFAULT_PAGE_NAME;
    public boolean createPage = true;
    public boolean showFeedback = true;

    private ItemEditorConfig copy() {
      ItemEditorConfig copy = new ItemEditorConfig();
      copy.saveKey = this.saveKey;
      copy.target = this.target;
      copy.pageNumber = this.pageNumber;
      copy.pageName = this.pageName;
      copy.createPage = this.createPage;
      copy.showFeedback = this.showFeedback;
      return copy;
    }
  }

  public static final class DurabilityConfig {
    public boolean showPercent = true;
    public boolean useColors = true;
    public int warningPercent = 50;
    public int dangerPercent = 25;

    private DurabilityConfig copy() {
      DurabilityConfig copy = new DurabilityConfig();
      copy.showPercent = this.showPercent;
      copy.useColors = this.useColors;
      copy.warningPercent = this.warningPercent;
      copy.dangerPercent = this.dangerPercent;
      return copy;
    }
  }

  public static final class FoodConfig {
    public boolean enabled = true;
    public boolean showHunger = true;
    public boolean showSaturation = true;
    public boolean showEffects = true;

    private FoodConfig copy() {
      FoodConfig copy = new FoodConfig();
      copy.enabled = this.enabled;
      copy.showHunger = this.showHunger;
      copy.showSaturation = this.showSaturation;
      copy.showEffects = this.showEffects;
      return copy;
    }
  }

  public static final class EnchantmentConfig {
    public boolean enabled = true;
    public boolean decimalLevels = false;

    private EnchantmentConfig copy() {
      EnchantmentConfig copy = new EnchantmentConfig();
      copy.enabled = this.enabled;
      copy.decimalLevels = this.decimalLevels;
      return copy;
    }
  }

  public static final class ComparisonConfig {
    public boolean enabled = true;

    private ComparisonConfig copy() {
      ComparisonConfig copy = new ComparisonConfig();
      copy.enabled = this.enabled;
      return copy;
    }
  }

  public static final class NavigationConfig {
    public boolean enabled = true;
    public boolean maps = true;
    public boolean compasses = true;

    private NavigationConfig copy() {
      NavigationConfig copy = new NavigationConfig();
      copy.enabled = this.enabled;
      copy.maps = this.maps;
      copy.compasses = this.compasses;
      return copy;
    }
  }

  public static final class SafetyConfig {
    public boolean translationCrashFix = true;
    public boolean globalComponentVisitGuard = true;
    public int maxCharacters = 8192;
    public int maxTranslationDepth = 64;
    public int maxTranslationVisits = 2048;

    private SafetyConfig copy() {
      SafetyConfig copy = new SafetyConfig();
      copy.translationCrashFix = this.translationCrashFix;
      copy.globalComponentVisitGuard = this.globalComponentVisitGuard;
      copy.maxCharacters = this.maxCharacters;
      copy.maxTranslationDepth = this.maxTranslationDepth;
      copy.maxTranslationVisits = this.maxTranslationVisits;
      return copy;
    }
  }

  public static final class TooltipConfig {
    public boolean edgeFix = true;
    public boolean scrollLongTooltips = true;
    public boolean showControlHints = true;
    public boolean ignoreHideTooltip = false;
    public Set<String> ignoredHiddenComponents = new LinkedHashSet<>();
    public int maxWidth = 0;
    public int scrollStep = 18;

    private TooltipConfig copy() {
      TooltipConfig copy = new TooltipConfig();
      copy.edgeFix = this.edgeFix;
      copy.scrollLongTooltips = this.scrollLongTooltips;
      copy.showControlHints = this.showControlHints;
      copy.ignoreHideTooltip = this.ignoreHideTooltip;
      copy.ignoredHiddenComponents = new LinkedHashSet<>(this.ignoredHiddenComponents);
      copy.maxWidth = this.maxWidth;
      copy.scrollStep = this.scrollStep;
      return copy;
    }
  }

  public static final class PreviewConfig {
    public boolean enabled = true;
    public HoldMode activation = HoldMode.KEY;
    public String key = LumenInputBinding.LEFT_SHIFT;
    public boolean openContainers = true;
    public boolean openBooks = true;
    public String openKey = LumenInputBinding.LEFT_ALT;
    public boolean shulkers = true;
    public boolean containers = true;
    public boolean bundles = true;
    public boolean itemDetails = true;
    public boolean books = true;
    public boolean crossbows = true;
    public boolean fireworks = true;
    public boolean entities = true;
    public boolean areaEffectClouds = true;
    public boolean displayEntities = true;
    public boolean itemFrames = true;
    public int displayYaw = 30;
    public int displayPitch = -15;
    public boolean spawnEggs = true;
    public boolean mobBuckets = true;
    public boolean spawners = true;

    private PreviewConfig copy() {
      PreviewConfig copy = new PreviewConfig();
      copy.enabled = this.enabled;
      copy.activation = this.activation;
      copy.key = this.key;
      copy.openContainers = this.openContainers;
      copy.openBooks = this.openBooks;
      copy.openKey = this.openKey;
      copy.shulkers = this.shulkers;
      copy.containers = this.containers;
      copy.bundles = this.bundles;
      copy.itemDetails = this.itemDetails;
      copy.books = this.books;
      copy.crossbows = this.crossbows;
      copy.fireworks = this.fireworks;
      copy.entities = this.entities;
      copy.areaEffectClouds = this.areaEffectClouds;
      copy.displayEntities = this.displayEntities;
      copy.itemFrames = this.itemFrames;
      copy.displayYaw = this.displayYaw;
      copy.displayPitch = this.displayPitch;
      copy.spawnEggs = this.spawnEggs;
      copy.mobBuckets = this.mobBuckets;
      copy.spawners = this.spawners;
      return copy;
    }
  }
}
