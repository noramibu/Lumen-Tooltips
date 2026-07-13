package me.noramibu.lumentooltips.tooltip.preview;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LumenEnderChestMemory {
  private static final int SIZE = 27;

  private static Level level;
  private static List<ItemStack> items;
  private static long revision;

  private LumenEnderChestMemory() {}

  public static void capture(AbstractContainerMenu menu, Component title) {
    if (!(title.getContents() instanceof TranslatableContents contents)
        || !"container.enderchest".equals(contents.getKey())
        || menu.slots.size() < SIZE) {
      return;
    }
    try {
      if (menu.getType() != MenuType.GENERIC_9x3) {
        return;
      }
    } catch (UnsupportedOperationException exception) {
      return;
    }
    List<ItemStack> captured = new ArrayList<>(SIZE);
    for (int index = 0; index < SIZE; index++) {
      captured.add(menu.slots.get(index).getItem().copy());
    }
    level = Minecraft.getInstance().level;
    items = List.copyOf(captured);
    revision++;
  }

  public static boolean isKnown() {
    validateLevel();
    return items != null;
  }

  public static List<ItemStack> items() {
    validateLevel();
    return items == null ? List.of() : items.stream().map(ItemStack::copy).toList();
  }

  public static long revision() {
    validateLevel();
    return revision;
  }

  private static void validateLevel() {
    if (level != Minecraft.getInstance().level && (level != null || items != null)) {
      level = null;
      items = null;
      revision++;
    }
  }
}
