package me.noramibu.lumentooltips.client.screen;

import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.config.LumenInputBinding;
import me.noramibu.lumentooltips.tooltip.preview.LumenContainerContents;
import me.noramibu.lumentooltips.tooltip.preview.LumenEnderChestMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LumenContainerOpener {
  private LumenContainerOpener() {}

  public static boolean tryOpen(ItemStack stack, KeyEvent event) {
    LumenConfig.PreviewConfig previewConfig = LumenConfigManager.current().modules.preview;
    if (!previewConfig.enabled
        || !LumenInputBinding.matches(previewConfig.openKey, event)) {
      return false;
    }
    return previewConfig.openContainers && open(stack)
        || previewConfig.openBooks && openBook(stack);
  }

  public static boolean isBook(ItemStack stack) {
    return stack.get(DataComponents.WRITTEN_BOOK_CONTENT) != null
        || stack.get(DataComponents.WRITABLE_BOOK_CONTENT) != null;
  }

  public static boolean isContainer(ItemStack stack) {
    return LumenContainerContents.isOpenable(stack)
        || previewConfig().enderChest
            && stack.is(Items.ENDER_CHEST)
            && LumenEnderChestMemory.isKnown();
  }

  private static boolean open(ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    Minecraft minecraft = Minecraft.getInstance();
    LumenConfig.PreviewConfig config = previewConfig();
    Screen parent = minecraft.screen;
    if (parent instanceof LumenContainerScreen && !config.nestedNavigation) {
      return false;
    }
    Inventory playerInventory =
        minecraft.player == null ? null : minecraft.player.getInventory();
    var items =
        config.enderChest
                && stack.is(Items.ENDER_CHEST)
                && LumenEnderChestMemory.isKnown()
            ? LumenEnderChestMemory.items()
            : LumenContainerContents.openableItems(stack).orElse(null);
    if (playerInventory == null
        || items == null) {
      return false;
    }
    minecraft.setScreen(new LumenContainerScreen(stack.copy(), items, playerInventory, parent));
    return true;
  }

  private static LumenConfig.PreviewConfig previewConfig() {
    return LumenConfigManager.current().modules.preview;
  }

  private static boolean openBook(ItemStack stack) {
    BookViewScreen.BookAccess book = BookViewScreen.BookAccess.fromItem(stack);
    if (book == null) {
      return false;
    }
    Minecraft.getInstance().setScreen(new BookViewScreen(book));
    return true;
  }
}
