package me.noramibu.lumentooltips.config;

import java.util.LinkedHashSet;
import java.util.Set;

public final class LumenConfig {
    public int schemaVersion = 1;
    public ControlConfig controls = new ControlConfig();
    public ModuleConfig modules = new ModuleConfig();

    public static final class ControlConfig {
        public HoldMode detailsMode = HoldMode.KEY;
        public String detailsKey = LumenInputBinding.LEFT_SHIFT;
        public String itemEditorKey = LumenInputBinding.CONTROL_SPACE;
    }

    public static final class ModuleConfig {
        public DurabilityConfig durability = new DurabilityConfig();
        public FoodConfig food = new FoodConfig();
        public EnchantmentConfig enchantments = new EnchantmentConfig();
        public ComparisonConfig comparison = new ComparisonConfig();
        public NavigationConfig navigation = new NavigationConfig();
        public ExtraStatisticsConfig extraStatistics = new ExtraStatisticsConfig();
        public ItemEditorConfig itemEditor = new ItemEditorConfig();
        public SafetyConfig safety = new SafetyConfig();
        public TooltipConfig tooltip = new TooltipConfig();
        public PreviewConfig preview = new PreviewConfig();
        public StatisticsConfig statistics = new StatisticsConfig();
    }

    public static final class StatisticsConfig {
        public boolean enabled = true;
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
    }

    public static final class DurabilityConfig {
        public boolean showPercent = true;
        public boolean useColors = true;
        public DurabilityPalette palette = DurabilityPalette.DEFAULT;
        public int warningPercent = 50;
        public int dangerPercent = 25;
    }

    public static final class FoodConfig {
        public boolean enabled = true;
        public boolean showHunger = true;
        public boolean showSaturation = true;
        public boolean showEffects = true;
    }

    public static final class EnchantmentConfig {
        public boolean enabled = true;
        public boolean decimalLevels = false;
    }

    public static final class ComparisonConfig {
        public boolean enabled = true;
    }

    public static final class NavigationConfig {
        public boolean enabled = true;
        public boolean maps = true;
        public boolean compasses = true;
    }

    public static final class ExtraStatisticsConfig {
        public boolean enabled = false;
        public HoldMode activation = HoldMode.ALWAYS;
        public String key = LumenInputBinding.LEFT_SHIFT;
        public boolean useSeconds = true;
        public boolean fuelTime = true;
        public boolean compostChance = true;
        public boolean useCooldown = true;
        public boolean enchantability = true;
        public boolean repairCost = true;
        public boolean blockHardness = true;
        public boolean blastResistance = true;
        public boolean enchantmentPower = true;
        public boolean miningLevel = true;
        public boolean miningSpeed = true;
        public boolean modName = true;
    }

    public static final class SafetyConfig {
        public boolean translationCrashFix = true;
        public boolean globalComponentVisitGuard = true;
        public boolean textLengthLimit = true;
        public int maxCharacters = 8192;
        public int maxTranslationDepth = 64;
        public int maxTranslationVisits = 2048;
    }

    public static final class TooltipConfig {
        public boolean edgeFix = true;
        public boolean scrollLongTooltips = true;
        public boolean showControlHints = true;
        public boolean showPreviewHint = true;
        public boolean showOpenContainerHint = true;
        public boolean showOpenBookHint = true;
        public boolean showEditItemHint = true;
        public boolean showSaveItemHint = true;
        public boolean ignoreHideTooltip = false;
        public Set<String> ignoredHiddenComponents = new LinkedHashSet<>();
        public int maxWidth = 0;
        public int scrollStep = 18;
    }

    public static final class PreviewConfig {
        public boolean enabled = true;
        public PreviewDensity density = PreviewDensity.VANILLA;
        public boolean accents = true;
        public boolean reducedMotion = false;
        public HoldMode activation = HoldMode.KEY;
        public String key = LumenInputBinding.LEFT_SHIFT;
        public boolean openContainers = true;
        public boolean openBooks = true;
        public String openKey = LumenInputBinding.LEFT_ALT;
        public boolean shulkers = true;
        public boolean containers = true;
        public ContainerPreviewMode containerMode = ContainerPreviewMode.FULL;
        public boolean showContainerTitle = false;
        public boolean showContainerCounts = true;
        public int containerTintPercent = 55;
        public boolean bundles = true;
        public boolean maps = true;
        public boolean banners = true;
        public boolean decoratedPots = true;
        public boolean potions = false;
        public boolean enderChest = true;
        public boolean paintings = true;
        public boolean playerHeads = true;
        public boolean signs = true;
        public boolean nestedNavigation = true;
        public boolean itemDetails = true;
        public boolean books = true;
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
    }
}
