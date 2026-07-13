package me.noramibu.lumentooltips.tooltip.preview;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.noramibu.lumentooltips.config.ContainerPreviewMode;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;

public final class LumenTooltipPreview {
  private static final int CONTAINER_COLUMNS = 9;
  private static final int CONTAINER_ROWS = 3;
  private static final int GENERIC_CONTAINER_COLOR = 0xFFFFFFFF;

  private static Level cachedLevel;
  private static Language cachedLanguage;
  private static LumenConfig.PreviewConfig cachedConfig;
  private static ItemStack cachedStack = ItemStack.EMPTY;
  private static Optional<TooltipComponent> cachedPreview = Optional.empty();
  private static boolean cachedTextFiltering;
  private static long cachedEnderChestRevision;

  private LumenTooltipPreview() {}

  public static boolean supports(ItemStack stack) {
    LumenConfig.PreviewConfig config = LumenConfigManager.current().modules.preview;
    if (!config.enabled) {
      return false;
    }
    return config.itemDetails && supportsItemDetails(stack, config)
        || config.maps && stack.get(DataComponents.MAP_ID) != null
        || supportsVisualItem(stack, config)
        || config.potions && stack.get(DataComponents.POTION_CONTENTS) != null
        || config.signs && isSign(stack)
        || config.enderChest && stack.is(Items.ENDER_CHEST) && LumenEnderChestMemory.isKnown()
        || LumenEntityPreviewResolver.supports(stack, config)
        || LumenContainerContents.hasStoredContainerItems(stack)
            && (LumenContainerContents.isShulker(stack) ? config.shulkers : config.containers)
        || config.bundles && LumenContainerContents.hasBundleItems(stack);
  }

  public static boolean isPotionPreviewActive(ItemStack stack) {
    LumenConfig.PreviewConfig config = LumenConfigManager.current().modules.preview;
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    return config.enabled
        && config.potions
        && config.activation.isActive(currentFlag(), config.key)
        && contents != null
        && contents.hasEffects();
  }

  public static Optional<TooltipComponent> create(ItemStack stack) {
    LumenConfig config = LumenConfigManager.current();
    LumenConfig.PreviewConfig preview = config.modules.preview;
    if (!preview.enabled
        || !preview.activation.isActive(currentFlag(), preview.key)) {
      return Optional.empty();
    }
    Minecraft minecraft = Minecraft.getInstance();
    Language language = Language.getInstance();
    boolean textFiltering = minecraft.isTextFilteringEnabled();
    long enderChestRevision = LumenEnderChestMemory.revision();
    if (minecraft.level == cachedLevel
        && language == cachedLanguage
        && preview == cachedConfig
        && textFiltering == cachedTextFiltering
        && enderChestRevision == cachedEnderChestRevision
        && ItemStack.isSameItemSameComponents(stack, cachedStack)) {
      return cachedPreview;
    }
    cachedLevel = minecraft.level;
    cachedLanguage = language;
    cachedConfig = preview;
    cachedStack = stack.copy();
    cachedTextFiltering = textFiltering;
    cachedEnderChestRevision = enderChestRevision;
    return cachedPreview = createUncached(stack, preview);
  }

  private static TooltipFlag currentFlag() {
    return Minecraft.getInstance().options.advancedItemTooltips
        ? TooltipFlag.ADVANCED
        : TooltipFlag.NORMAL;
  }

  private static Optional<TooltipComponent> createUncached(
      ItemStack stack, LumenConfig.PreviewConfig preview) {
    return (preview.itemDetails
            ? createItemDetails(stack, preview)
            : Optional.<TooltipComponent>empty())
        .or(
            () ->
                preview.maps
                    ? LumenMapTooltipComponent.create(stack, preview)
                    : Optional.empty())
        .or(() -> createVisualItemPreview(stack, preview))
        .or(
            () ->
                preview.potions
                    ? LumenPotionTooltipComponent.create(stack, preview)
                    : Optional.empty())
        .or(
            () ->
                preview.signs ? createSignPreview(stack, preview) : Optional.empty())
        .or(() -> createEnderChestPreview(stack, preview))
        .or(
            () ->
                LumenEntityPreviewResolver.create(stack, preview)
                    .flatMap(entity -> LumenEntityTooltipComponent.create(entity, preview)))
        .or(() -> createContainerPreview(stack, preview))
        .or(() -> createBundlePreview(stack, preview));
  }

  public static Optional<TooltipComponent> configSample(
      String path, LumenConfig config) {
    LumenConfig.PreviewConfig preview = config.modules.preview;
    return switch (path) {
      case "modules.preview.shulkers",
          "modules.preview.containers",
          "modules.preview.accents",
          "modules.preview.containerMode",
          "modules.preview.showContainerTitle",
          "modules.preview.showContainerCounts",
          "modules.preview.containerTintPercent" -> Optional.of(configContainerSample(preview));
      case "modules.preview.bundles" -> Optional.of(configBundleSample());
      case "modules.preview.playerHeads" ->
          Optional.of(
              new LumenItemVisualTooltipComponent(
                  new ItemStack(Items.PLAYER_HEAD), preview));
      case "modules.preview.books" ->
          LumenBookTooltipComponent.create(
              Component.translatable("screen.lumen_tooltips.config.preview.book_text"), 3);
      case "modules.preview.density",
          "modules.preview.fireworks",
          "modules.preview.reducedMotion" ->
          Optional.of(configFireworkSample(preview));
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
        .map(stored -> containerPreview(stack, stored, shulker, config));
  }

  private static Optional<TooltipComponent> createEnderChestPreview(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    return config.enderChest && stack.is(Items.ENDER_CHEST) && LumenEnderChestMemory.isKnown()
        ? Optional.of(
            containerPreview(
                stack,
                LumenEnderChestMemory.items(),
                false,
                config,
                0xFF284060))
        : Optional.empty();
  }

  private static Optional<TooltipComponent> createVisualItemPreview(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    if (config.banners && stack.getItem() instanceof BannerItem banner) {
      return Optional.of(new LumenBannerTooltipComponent(stack, banner, config));
    }
    if (config.banners && stack.is(Items.SHIELD)) {
      return Optional.of(
          new LumenItemVisualTooltipComponent(stack, config));
    }
    if (config.decoratedPots && stack.is(Items.DECORATED_POT)) {
      return Optional.of(new LumenItemVisualTooltipComponent(stack, config));
    }
    if (config.playerHeads && stack.is(Items.PLAYER_HEAD)) {
      return Optional.of(new LumenItemVisualTooltipComponent(stack, config));
    }
    return config.paintings
            && stack.is(Items.PAINTING)
            && !hasPaintingEntity(stack)
        ? Optional.of(new LumenItemVisualTooltipComponent(stack, config))
        : Optional.empty();
  }

  private static Optional<TooltipComponent> createSignPreview(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    return isSign(stack) ? LumenSignTooltipComponent.create(stack, config) : Optional.empty();
  }

  private static boolean isSign(ItemStack stack) {
    return stack.getItem() instanceof BlockItem blockItem
        && blockItem.getBlock() instanceof SignBlock;
  }

  private static boolean hasPaintingEntity(ItemStack stack) {
    if (stack.get(DataComponents.PAINTING_VARIANT) != null) {
      return true;
    }
    TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
    return data != null && data.type() == EntityType.PAINTING;
  }

  private static boolean supportsVisualItem(ItemStack stack, LumenConfig.PreviewConfig config) {
    return config.banners
            && (stack.getItem() instanceof BannerItem
                || stack.is(Items.SHIELD))
        || config.decoratedPots && stack.is(Items.DECORATED_POT)
        || config.playerHeads && stack.is(Items.PLAYER_HEAD)
        || config.paintings
            && stack.is(Items.PAINTING)
            && !hasPaintingEntity(stack);
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
    return config.books
            && (stack.get(DataComponents.WRITTEN_BOOK_CONTENT) != null
                || stack.get(DataComponents.WRITABLE_BOOK_CONTENT) != null)
        || config.fireworks
            && (stack.get(DataComponents.FIREWORKS) != null
                || stack.get(DataComponents.FIREWORK_EXPLOSION) != null);
  }

  private static Optional<TooltipComponent> createItemDetails(
      ItemStack stack, LumenConfig.PreviewConfig config) {
    return (config.books
            ? LumenBookTooltipComponent.create(stack)
            : Optional.<TooltipComponent>empty())
        .or(
            () ->
                config.fireworks
                    ? LumenFireworkTooltipComponent.create(stack, config)
                    : Optional.empty());
  }

  public static boolean suppressContainerText(ItemStack stack, TooltipFlag flag) {
    LumenConfig.PreviewConfig config = LumenConfigManager.current().modules.preview;
    return config.enabled
        && config.activation.isActive(flag, config.key)
        && LumenContainerContents.hasStoredContainerItems(stack)
        && (LumenContainerContents.isShulker(stack) ? config.shulkers : config.containers);
  }

  private static TooltipComponent containerPreview(
      ItemStack source,
      List<ItemStack> stored,
      boolean shulker,
      LumenConfig.PreviewConfig config) {
    return containerPreview(
        source,
        stored,
        shulker,
        config,
        shulker ? LumenContainerContents.shulkerColor(source) : GENERIC_CONTAINER_COLOR);
  }

  private static TooltipComponent containerPreview(
      ItemStack source,
      List<ItemStack> stored,
      boolean shulker,
      LumenConfig.PreviewConfig config,
      int color) {
    List<ItemStack> visibleItems =
        config.containerMode == ContainerPreviewMode.COMPACT ? compact(stored) : stored;
    int columns =
        config.containerMode == ContainerPreviewMode.COMPACT
            ? Math.min(CONTAINER_COLUMNS, Math.max(1, visibleItems.size()))
            : CONTAINER_COLUMNS;
    int rows =
        config.containerMode == ContainerPreviewMode.COMPACT
            ? Math.max(1, Math.min(CONTAINER_ROWS, (visibleItems.size() + columns - 1) / columns))
            : CONTAINER_ROWS;
    ItemStack[] items = new ItemStack[columns * rows];
    Arrays.fill(items, ItemStack.EMPTY);
    int hidden = 0;
    for (int index = 0; index < visibleItems.size(); index++) {
      ItemStack stack = visibleItems.get(index);
      if (index < items.length) {
        items[index] = stack;
      } else if (!stack.isEmpty()) {
        hidden++;
      }
    }
    return new LumenContainerTooltipComponent(
        items,
        color,
        hidden,
        config.showContainerTitle ? source.getHoverName() : null,
        config);
  }

  private static List<ItemStack> compact(List<ItemStack> stored) {
    List<ItemStack> compact = new java.util.ArrayList<>();
    for (ItemStack stack : stored) {
      if (stack.isEmpty()) {
        continue;
      }
      ItemStack existing =
          compact.stream()
              .filter(item -> ItemStack.isSameItemSameComponents(item, stack))
              .findFirst()
              .orElse(null);
      if (existing == null) {
        compact.add(stack.copy());
      } else {
        existing.grow(stack.getCount());
      }
    }
    return compact;
  }

  private static TooltipComponent configBundleSample() {
    BundleContents.Mutable contents = new BundleContents.Mutable(BundleContents.EMPTY);
    contents.tryInsert(new ItemStack(Items.DIAMOND, 8));
    contents.tryInsert(new ItemStack(Items.BREAD, 16));
    contents.tryInsert(new ItemStack(Items.ENDER_PEARL, 4));
    return new LumenBundleTooltipComponent(contents.toImmutable());
  }

  private static TooltipComponent configContainerSample(LumenConfig.PreviewConfig config) {
    ItemStack source = new ItemStack(Items.CHEST);
    return containerPreview(
        source,
        List.of(
            new ItemStack(Items.DIAMOND, 12),
            new ItemStack(Items.GOLDEN_APPLE, 3),
            new ItemStack(Items.ENDER_PEARL, 16),
            new ItemStack(Items.DIAMOND, 8),
            new ItemStack(Items.TORCH, 64)),
        false,
        config);
  }

  private static TooltipComponent configFireworkSample(LumenConfig.PreviewConfig config) {
    return new LumenFireworkTooltipComponent(
        2,
        List.of(
            new FireworkExplosion(
                FireworkExplosion.Shape.STAR,
                IntList.of(0x55FF55, 0x5555FF, 0xFF55FF),
                IntList.of(0xFFFF55),
                true,
                true)),
        config,
        null);
  }
}
