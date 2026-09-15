package me.noramibu.lumentooltips.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Objects;
import me.noramibu.lumentooltips.LumenTooltips;
import org.slf4j.Logger;

public final class LumenConfigManager {
    private static final int CURRENT_SCHEMA_VERSION = 1;
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FILE_NAME = LumenTooltips.MOD_ID + ".json";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static Path configPath = Path.of("config", FILE_NAME);
    private static LumenConfig current = validate(new LumenConfig());

    private LumenConfigManager() {}

    public static LumenConfig current() {
        return current;
    }

    public static void load(Path configDirectory) {
        configPath = configDirectory.resolve(FILE_NAME);
        load();
    }

    public static void load() {
        Path path = configPath;
        if (!Files.exists(path)) {
            apply(new LumenConfig(), SaveMode.DISK);
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            apply(GSON.fromJson(reader, LumenConfig.class), SaveMode.MEMORY);
        } catch (IOException | JsonSyntaxException exception) {
            LOGGER.warn("Could not load Lumen Tooltips config from {}", path, exception);
            apply(new LumenConfig(), SaveMode.MEMORY);
        }
    }

    public static void save() {
        save(current);
    }

    private static void save(LumenConfig config) {
        Path path = configPath;
        Path tempPath = path.resolveSibling(FILE_NAME + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(tempPath)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.error("Could not save Lumen Tooltips config to {}", path, exception);
        }
    }

    public static void apply(LumenConfig config, SaveMode saveMode) {
        current = validate(config);
        if (saveMode == SaveMode.DISK) {
            save(current);
        }
    }

    public static LumenConfig editingCopy() {
        return GSON.fromJson(GSON.toJson(current), LumenConfig.class);
    }

    private static LumenConfig validate(LumenConfig config) {
        LumenConfig safe = Objects.requireNonNullElseGet(config, LumenConfig::new);
        safe.schemaVersion = CURRENT_SCHEMA_VERSION;
        safe.controls = Objects.requireNonNullElseGet(safe.controls, LumenConfig.ControlConfig::new);
        safe.modules = Objects.requireNonNullElseGet(safe.modules, LumenConfig.ModuleConfig::new);
        validateControls(safe.controls);
        validateModules(safe.modules);
        return safe;
    }

    private static void validateModules(LumenConfig.ModuleConfig modules) {
        modules.durability = Objects.requireNonNullElseGet(modules.durability, LumenConfig.DurabilityConfig::new);
        modules.food = Objects.requireNonNullElseGet(modules.food, LumenConfig.FoodConfig::new);
        modules.enchantments = Objects.requireNonNullElseGet(modules.enchantments, LumenConfig.EnchantmentConfig::new);
        modules.comparison = Objects.requireNonNullElseGet(modules.comparison, LumenConfig.ComparisonConfig::new);
        modules.navigation = Objects.requireNonNullElseGet(modules.navigation, LumenConfig.NavigationConfig::new);
        modules.extraStatistics =
                Objects.requireNonNullElseGet(modules.extraStatistics, LumenConfig.ExtraStatisticsConfig::new);
        modules.itemEditor = Objects.requireNonNullElseGet(modules.itemEditor, LumenConfig.ItemEditorConfig::new);
        modules.safety = Objects.requireNonNullElseGet(modules.safety, LumenConfig.SafetyConfig::new);
        modules.tooltip = Objects.requireNonNullElseGet(modules.tooltip, LumenConfig.TooltipConfig::new);
        modules.preview = Objects.requireNonNullElseGet(modules.preview, LumenConfig.PreviewConfig::new);
        modules.statistics = Objects.requireNonNullElseGet(modules.statistics, LumenConfig.StatisticsConfig::new);

        validateTooltip(modules.tooltip);
        validateDurability(modules.durability);
        validatePreview(modules.preview);
        validateExtraStatistics(modules.extraStatistics);
        validateItemEditor(modules.itemEditor);
        validateSafety(modules.safety);
    }

    private static void validateControls(LumenConfig.ControlConfig controls) {
        controls.detailsMode = Objects.requireNonNullElse(controls.detailsMode, HoldMode.KEY);
        if (controls.detailsMode == HoldMode.SHIFT) {
            controls.detailsMode = HoldMode.KEY;
            controls.detailsKey = LumenInputBinding.LEFT_SHIFT;
        } else if (controls.detailsMode == HoldMode.ALT) {
            controls.detailsMode = HoldMode.KEY;
            controls.detailsKey = LumenInputBinding.LEFT_ALT;
        }
        controls.detailsKey = LumenInputBinding.normalize(controls.detailsKey, LumenInputBinding.LEFT_SHIFT);
        controls.itemEditorKey = LumenInputBinding.normalize(controls.itemEditorKey, LumenInputBinding.CONTROL_SPACE);
    }

    private static void validateTooltip(LumenConfig.TooltipConfig tooltip) {
        tooltip.ignoredHiddenComponents =
                Objects.requireNonNullElseGet(tooltip.ignoredHiddenComponents, LinkedHashSet::new);
        tooltip.ignoredHiddenComponents.removeIf(Objects::isNull);
        tooltip.maxWidth = Math.clamp(tooltip.maxWidth, 0, 16_384);
        tooltip.scrollStep = Math.clamp(tooltip.scrollStep, 4, 64);
    }

    private static void validateDurability(LumenConfig.DurabilityConfig durability) {
        durability.palette = Objects.requireNonNullElse(durability.palette, DurabilityPalette.DEFAULT);
        durability.warningPercent = Math.clamp(durability.warningPercent, 1, 99);
        durability.dangerPercent = Math.clamp(durability.dangerPercent, 1, 99);
        if (durability.dangerPercent > durability.warningPercent) {
            durability.dangerPercent = durability.warningPercent;
        }
    }

    private static void validatePreview(LumenConfig.PreviewConfig preview) {
        preview.density = Objects.requireNonNullElse(preview.density, PreviewDensity.VANILLA);
        preview.containerMode = Objects.requireNonNullElse(preview.containerMode, ContainerPreviewMode.FULL);
        preview.activation = Objects.requireNonNullElse(preview.activation, HoldMode.KEY);
        if (preview.activation == HoldMode.SHIFT) {
            preview.activation = HoldMode.KEY;
            preview.key = LumenInputBinding.LEFT_SHIFT;
        } else if (preview.activation == HoldMode.ALT) {
            preview.activation = HoldMode.KEY;
            preview.key = LumenInputBinding.LEFT_ALT;
        }
        if (preview.activation == HoldMode.ADVANCED) {
            preview.activation = HoldMode.KEY;
        }
        preview.key = LumenInputBinding.normalize(preview.key, LumenInputBinding.LEFT_SHIFT);
        preview.openKey = LumenInputBinding.normalize(preview.openKey, LumenInputBinding.LEFT_ALT);
        preview.displayYaw = Math.clamp(preview.displayYaw, -180, 180);
        preview.displayPitch = Math.clamp(preview.displayPitch, -90, 90);
        preview.containerTintPercent = Math.clamp(preview.containerTintPercent, 0, 100);
    }

    private static void validateExtraStatistics(LumenConfig.ExtraStatisticsConfig extraStatistics) {
        extraStatistics.activation = Objects.requireNonNullElse(extraStatistics.activation, HoldMode.ALWAYS);
        extraStatistics.key = LumenInputBinding.normalize(extraStatistics.key, LumenInputBinding.LEFT_SHIFT);
    }

    private static void validateItemEditor(LumenConfig.ItemEditorConfig itemEditor) {
        itemEditor.target = Objects.requireNonNullElse(itemEditor.target, ItemEditorStorageTarget.FIRST_AVAILABLE);
        itemEditor.saveKey = LumenInputBinding.normalize(itemEditor.saveKey, LumenInputBinding.CONTROL_S);
        itemEditor.pageNumber = Math.clamp(itemEditor.pageNumber, 1, LumenConfig.ItemEditorConfig.MAX_PAGE_NUMBER);
        itemEditor.pageName =
                Objects.requireNonNullElse(itemEditor.pageName, LumenConfig.ItemEditorConfig.DEFAULT_PAGE_NAME);
        if (itemEditor.pageName.length() > LumenConfig.ItemEditorConfig.MAX_PAGE_NAME_LENGTH) {
            itemEditor.pageName = itemEditor.pageName.substring(0, LumenConfig.ItemEditorConfig.MAX_PAGE_NAME_LENGTH);
        }
    }

    private static void validateSafety(LumenConfig.SafetyConfig safety) {
        safety.maxCharacters = Math.clamp(safety.maxCharacters, 256, 65_536);
        safety.maxTranslationDepth = Math.clamp(safety.maxTranslationDepth, 8, 256);
        safety.maxTranslationVisits = Math.clamp(safety.maxTranslationVisits, 64, 8192);
    }
}
