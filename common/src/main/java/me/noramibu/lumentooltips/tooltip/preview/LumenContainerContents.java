package me.noramibu.lumentooltips.tooltip.preview;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class LumenContainerContents {
  private LumenContainerContents() {}

  public static Optional<List<ItemStack>> openableItems(ItemStack stack) {
    Optional<List<ItemStack>> containerItems = storedContainerItems(stack);
    if (containerItems.isPresent()) {
      return containerItems;
    }
    BundleContents contents =
        stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
    if (contents.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(contents.itemCopyStream().toList());
  }

  public static boolean isOpenable(ItemStack stack) {
    return hasStoredContainerItems(stack) || hasBundleItems(stack);
  }

  public static Optional<List<ItemStack>> storedContainerItems(ItemStack stack) {
    ItemContainerContents container =
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    if (!container.nonEmptyItems().iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(container.stream().toList());
  }

  static boolean hasStoredContainerItems(ItemStack stack) {
    return stack
        .getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
        .nonEmptyItems()
        .iterator()
        .hasNext();
  }

  static boolean hasBundleItems(ItemStack stack) {
    return !stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).isEmpty();
  }

  public static boolean isShulker(ItemStack stack) {
    return stack.getItem() instanceof BlockItem blockItem
        && blockItem.getBlock() instanceof ShulkerBoxBlock;
  }

  public static int shulkerColor(ItemStack stack) {
    if (stack.getItem() instanceof BlockItem blockItem
        && blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBoxBlock
        && shulkerBoxBlock.getColor() != null) {
      return 0xFF000000 | shulkerBoxBlock.getColor().getTextureDiffuseColor();
    }
    return 0xFFC8C8C8;
  }
}
