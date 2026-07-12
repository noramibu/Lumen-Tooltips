package me.noramibu.lumentooltips.tooltip.preview;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public final class LumenTooltipPreview {
  private static final int CONTAINER_COLUMNS = 9;
  private static final int CONTAINER_ROWS = 3;
  private static final int ITEM_DETAIL_COLUMNS = 8;
  private static final int GENERIC_CONTAINER_COLOR = 0xFFFFFFFF;
  private static final int CROSSBOW_COLOR = 0xFF78A6C8;

  private static Level cachedLevel;
  private static Language cachedLanguage;
  private static LumenConfig.PreviewConfig cachedConfig;
  private static ItemStack cachedStack = ItemStack.EMPTY;
  private static Optional<TooltipComponent> cachedPreview = Optional.empty();
  private static boolean cachedTextFiltering;

  private LumenTooltipPreview() {}

  public static boolean supports(ItemStack stack) {
    LumenConfig.PreviewConfig config = LumenConfigManager.current().modules.preview;
    if (!config.enabled) {
      return false;
    }
    return config.itemDetails && supportsItemDetails(stack, config)
        || LumenEntityPreviewResolver.supports(stack, config)
        || LumenContainerContents.hasStoredContainerItems(stack)
            && (LumenContainerContents.isShulker(stack) ? config.shulkers : config.containers)
        || config.bundles && LumenContainerContents.hasBundleItems(stack);
  }

  public static Optional<TooltipComponent> create(ItemStack stack) {
    LumenConfig config = LumenConfigManager.current();
    LumenConfig.PreviewConfig preview = config.modules.preview;
    if (!preview.enabled
        || !preview.activation.isActive(TooltipFlag.NORMAL, preview.key)) {
      return Optional.empty();
    }
    Minecraft minecraft = Minecraft.getInstance();
    Language language = Language.getInstance();
    boolean textFiltering = minecraft.isTextFilteringEnabled();
    if (minecraft.level == cachedLevel
        && language == cachedLanguage
        && preview == cachedConfig
        && textFiltering == cachedTextFiltering
        && ItemStack.isSameItemSameComponents(stack, cachedStack)) {
      return cachedPreview;
    }
    cachedLevel = minecraft.level;
    cachedLanguage = language;
    cachedConfig = preview;
    cachedStack = stack.copy();
    cachedTextFiltering = textFiltering;
    return cachedPreview = createUncached(stack, preview);
  }

  private static Optional<TooltipComponent> createUncached(
      ItemStack stack, LumenConfig.PreviewConfig preview) {
    return (preview.itemDetails
            ? createItemDetails(stack, preview)
            : Optional.<TooltipComponent>empty())
        .or(
            () ->
                LumenEntityPreviewResolver.create(stack, preview)
                    .flatMap(entity -> LumenEntityTooltipComponent.create(entity, preview)))
        .or(() -> createContainerPreview(stack, preview))
        .or(() -> createBundlePreview(stack, preview));
  }

  public static Optional<TooltipComponent> configSample(String path) {
    return switch (path) {
      case "modules.preview.openContainers",
          "modules.preview.shulkers",
          "modules.preview.containers" -> Optional.of(configContainerSample());
      case "modules.preview.bundles" -> Optional.of(configBundleSample());
      case "modules.preview.openBooks", "modules.preview.books" ->
          Optional.of(
              new LumenBookTooltipComponent(
                  "",
                  3,
                  Component.translatable(
                          "screen.lumen_tooltips.config.preview.book_text")
                      .getString()));
      case "modules.preview.crossbows" -> Optional.of(configCrossbowSample());
      case "modules.preview.fireworks" -> Optional.of(configFireworkSample());
      default -> Optional.empty();
    };
  }

  private static Optional<TooltipComponent> createContainerPreview(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    boolean shulker = LumenContainerContents.isShulker(stack);
    if (shulker ? !config.shulkers : !config.containers) {
      return Optional.empty();
    }
    return LumenContainerContents.storedContainerItems(stack)
        .map(
            stored ->
                containerPreview(
                    stored,
                    shulker
                        ? LumenContainerContents.shulkerColor(stack)
                        : GENERIC_CONTAINER_COLOR));
  }

  private static Optional<TooltipComponent> createBundlePreview(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    if (!config.bundles) {
      return Optional.empty();
    }
    BundleContents contents =
        stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
    if (contents.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new LumenBundleTooltipComponent(contents));
  }

  private static boolean supportsItemDetails(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    return config.crossbows && stack.getItem() instanceof CrossbowItem
        || config.books && stack.get(DataComponents.WRITTEN_BOOK_CONTENT) != null
        || config.fireworks
            && (stack.get(DataComponents.FIREWORKS) != null
                || stack.get(DataComponents.FIREWORK_EXPLOSION) != null);
  }

  private static Optional<TooltipComponent> createItemDetails(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    Optional<TooltipComponent> preview =
        (config.books
                ? LumenBookTooltipComponent.create(stack)
                : Optional.<TooltipComponent>empty())
            .or(
            () ->
                config.fireworks
                    ? LumenFireworkTooltipComponent.create(stack)
                    : Optional.empty());
    if (preview.isPresent()) {
      return preview;
    }
    if (!config.crossbows || !(stack.getItem() instanceof CrossbowItem)) {
      return Optional.empty();
    }
    ChargedProjectiles projectiles =
        stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
    if (projectiles.isEmpty()) {
      return Optional.empty();
    }
    List<ItemStack> items = projectiles.itemCopies();
    int visible = Math.min(ITEM_DETAIL_COLUMNS, items.size());
    return Optional.of(
        new LumenContainerTooltipComponent(
            items.subList(0, visible).toArray(ItemStack[]::new),
            visible,
            1,
            CROSSBOW_COLOR,
            items.size() - visible));
  }

  public static boolean suppressContainerText(ItemStack stack, TooltipFlag flag) {
    LumenConfig.PreviewConfig config = LumenConfigManager.current().modules.preview;
    return config.enabled
        && config.activation.isActive(flag, config.key)
        && LumenContainerContents.hasStoredContainerItems(stack)
        && (LumenContainerContents.isShulker(stack) ? config.shulkers : config.containers);
  }

  private static TooltipComponent containerPreview(List<ItemStack> stored, int color) {
    ItemStack[] items = new ItemStack[CONTAINER_COLUMNS * CONTAINER_ROWS];
    Arrays.fill(items, ItemStack.EMPTY);
    int hidden = 0;
    for (int index = 0; index < stored.size(); index++) {
      ItemStack stack = stored.get(index);
      if (index < items.length) {
        items[index] = stack;
      } else if (!stack.isEmpty()) {
        hidden++;
      }
    }
    return new LumenContainerTooltipComponent(
        items, CONTAINER_COLUMNS, CONTAINER_ROWS, color, hidden);
  }

  private static TooltipComponent configBundleSample() {
    BundleContents.Mutable contents = new BundleContents.Mutable(BundleContents.EMPTY);
    contents.tryInsert(new ItemStack(Items.DIAMOND, 8));
    contents.tryInsert(new ItemStack(Items.BREAD, 16));
    contents.tryInsert(new ItemStack(Items.ENDER_PEARL, 4));
    return new LumenBundleTooltipComponent(contents.toImmutable());
  }

  private static TooltipComponent configContainerSample() {
    return containerPreview(
        List.of(
            new ItemStack(Items.DIAMOND, 12),
            new ItemStack(Items.GOLDEN_APPLE, 3),
            new ItemStack(Items.ENDER_PEARL, 16),
            new ItemStack(Items.TORCH, 64)),
        GENERIC_CONTAINER_COLOR);
  }

  private static TooltipComponent configCrossbowSample() {
    return new LumenContainerTooltipComponent(
        new ItemStack[] {new ItemStack(Items.ARROW, 3), new ItemStack(Items.FIREWORK_ROCKET)},
        2,
        1,
        CROSSBOW_COLOR,
        0);
  }

  private static TooltipComponent configFireworkSample() {
    return new LumenFireworkTooltipComponent(
        2,
        List.of(
            new FireworkExplosion(
                FireworkExplosion.Shape.STAR,
                IntList.of(0x55FF55, 0x5555FF, 0xFF55FF),
                IntList.of(0xFFFF55),
                true,
                true)));
  }
}
