package me.noramibu.itemeditor.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.item.ItemStack;

public final class ItemEditorStorageApi {
  private ItemEditorStorageApi() {}

  public static CompletableFuture<List<StoragePage>> listPages() {
    throw new UnsupportedOperationException();
  }

  public static CompletableFuture<StoragePage> createPage(String name) {
    throw new UnsupportedOperationException();
  }

  public static CompletableFuture<Optional<StoragePage>> findPageByNumber(int number) {
    throw new UnsupportedOperationException();
  }

  public static CompletableFuture<List<StoragePage>> searchPages(String query) {
    throw new UnsupportedOperationException();
  }

  public static CompletableFuture<StoreResult> addToFirstEmptySlot(
      String pageId, ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  public record StoragePage(String id, int number, String name, String plainName, int itemCount) {}

  public record StoreResult(Status status, int slot) {}

  public enum Status {
    SAVED,
    PAGE_NOT_FOUND,
    PAGE_FULL,
    INVALID_ITEM
  }
}
