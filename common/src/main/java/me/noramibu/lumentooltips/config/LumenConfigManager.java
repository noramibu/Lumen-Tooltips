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
      LumenConfig loaded = GSON.fromJson(reader, LumenConfig.class);
      apply(loaded, SaveMode.MEMORY);
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
        Files.move(
            tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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
    return current.copy();
  }

  private static LumenConfig validate(LumenConfig config) {
    LumenConfig safe = Objects.requireNonNullElseGet(config, LumenConfig::new);
    safe.schemaVersion = CURRENT_SCHEMA_VERSION;
    safe.controls =
        Objects.requireNonNullElseGet(safe.controls, LumenConfig.ControlConfig::new);
    if (safe.controls.detailsMode == null) {
      safe.controls.detailsMode = HoldMode.KEY;
    }
    if (safe.controls.detailsMode == HoldMode.SHIFT) {
      safe.controls.detailsMode = HoldMode.KEY;
      safe.controls.detailsKey = LumenInputBinding.LEFT_SHIFT;
    } else if (safe.controls.detailsMode == HoldMode.ALT) {
      safe.controls.detailsMode = HoldMode.KEY;
      safe.controls.detailsKey = LumenInputBinding.LEFT_ALT;
    }
    safe.controls.detailsKey =
        LumenInputBinding.normalize(safe.controls.detailsKey, LumenInputBinding.LEFT_SHIFT);
    safe.controls.itemEditorKey =
        LumenInputBinding.normalize(
            safe.controls.itemEditorKey, LumenInputBinding.CONTROL_SPACE);
    safe.modules =
        Objects.requireNonNullElseGet(safe.modules, LumenConfig.ModuleConfig::new);
    safe.modules.durability =
        Objects.requireNonNullElseGet(
            safe.modules.durability, LumenConfig.DurabilityConfig::new);
    safe.modules.food =
        Objects.requireNonNullElseGet(safe.modules.food, LumenConfig.FoodConfig::new);
    safe.modules.enchantments =
        Objects.requireNonNullElseGet(
            safe.modules.enchantments, LumenConfig.EnchantmentConfig::new);
    safe.modules.comparison =
        Objects.requireNonNullElseGet(
            safe.modules.comparison, LumenConfig.ComparisonConfig::new);
    safe.modules.navigation =
        Objects.requireNonNullElseGet(
            safe.modules.navigation, LumenConfig.NavigationConfig::new);
    safe.modules.itemEditor =
        Objects.requireNonNullElseGet(
            safe.modules.itemEditor, LumenConfig.ItemEditorConfig::new);
    safe.modules.safety =
        Objects.requireNonNullElseGet(safe.modules.safety, LumenConfig.SafetyConfig::new);
    safe.modules.tooltip =
        Objects.requireNonNullElseGet(
            safe.modules.tooltip, LumenConfig.TooltipConfig::new);
    safe.modules.tooltip.ignoredHiddenComponents =
        Objects.requireNonNullElseGet(
            safe.modules.tooltip.ignoredHiddenComponents, LinkedHashSet::new);
    safe.modules.tooltip.ignoredHiddenComponents.removeIf(Objects::isNull);
    safe.modules.preview =
        Objects.requireNonNullElseGet(
            safe.modules.preview, LumenConfig.PreviewConfig::new);
    if (safe.modules.itemEditor.target == null) {
      safe.modules.itemEditor.target = ItemEditorStorageTarget.FIRST_AVAILABLE;
    }
    safe.modules.itemEditor.saveKey =
        LumenInputBinding.normalize(
            safe.modules.itemEditor.saveKey, LumenInputBinding.CONTROL_S);
    safe.modules.itemEditor.pageNumber =
        Math.clamp(
            safe.modules.itemEditor.pageNumber,
            1,
            LumenConfig.ItemEditorConfig.MAX_PAGE_NUMBER);
    safe.modules.itemEditor.pageName =
        Objects.requireNonNullElse(
            safe.modules.itemEditor.pageName, LumenConfig.ItemEditorConfig.DEFAULT_PAGE_NAME);
    if (safe.modules.itemEditor.pageName.length()
        > LumenConfig.ItemEditorConfig.MAX_PAGE_NAME_LENGTH) {
      safe.modules.itemEditor.pageName =
          safe.modules.itemEditor.pageName.substring(
              0, LumenConfig.ItemEditorConfig.MAX_PAGE_NAME_LENGTH);
    }
    if (safe.modules.preview.activation == null) {
      safe.modules.preview.activation = HoldMode.KEY;
    }
    if (safe.modules.preview.activation == HoldMode.SHIFT) {
      safe.modules.preview.activation = HoldMode.KEY;
      safe.modules.preview.key = LumenInputBinding.LEFT_SHIFT;
    } else if (safe.modules.preview.activation == HoldMode.ALT) {
      safe.modules.preview.activation = HoldMode.KEY;
      safe.modules.preview.key = LumenInputBinding.LEFT_ALT;
    }
    if (safe.modules.preview.activation == HoldMode.ADVANCED) {
      safe.modules.preview.activation = HoldMode.KEY;
    }
    safe.modules.preview.key =
        LumenInputBinding.normalize(safe.modules.preview.key, LumenInputBinding.LEFT_SHIFT);
    safe.modules.preview.openKey =
        LumenInputBinding.normalize(safe.modules.preview.openKey, LumenInputBinding.LEFT_ALT);
    safe.modules.durability.warningPercent =
        Math.clamp(safe.modules.durability.warningPercent, 1, 99);
    safe.modules.durability.dangerPercent =
        Math.clamp(safe.modules.durability.dangerPercent, 1, 99);
    if (safe.modules.durability.dangerPercent > safe.modules.durability.warningPercent) {
      safe.modules.durability.dangerPercent = safe.modules.durability.warningPercent;
    }
    safe.modules.tooltip.maxWidth = Math.clamp(safe.modules.tooltip.maxWidth, 0, 16_384);
    safe.modules.tooltip.scrollStep = Math.clamp(safe.modules.tooltip.scrollStep, 4, 64);
    safe.modules.safety.maxCharacters =
        Math.clamp(safe.modules.safety.maxCharacters, 256, 65_536);
    safe.modules.safety.maxTranslationDepth =
        Math.clamp(safe.modules.safety.maxTranslationDepth, 8, 256);
    safe.modules.safety.maxTranslationVisits =
        Math.clamp(safe.modules.safety.maxTranslationVisits, 64, 8192);
    safe.modules.preview.displayYaw = Math.clamp(safe.modules.preview.displayYaw, -180, 180);
    safe.modules.preview.displayPitch = Math.clamp(safe.modules.preview.displayPitch, -90, 90);
    return safe;
  }
}
