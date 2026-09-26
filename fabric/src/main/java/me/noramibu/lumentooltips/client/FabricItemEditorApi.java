package me.noramibu.lumentooltips.client;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.noramibu.itemeditor.api.ItemEditorApi;
import me.noramibu.itemeditor.api.ItemEditorStorageApi;
import me.noramibu.itemeditor.api.ItemEditorStorageApi.DuplicateMatch;
import me.noramibu.itemeditor.api.ItemEditorStorageApi.StoragePage;
import me.noramibu.lumentooltips.client.LumenItemEditor.Page;
import me.noramibu.lumentooltips.client.LumenItemEditor.SaveOptions;
import me.noramibu.lumentooltips.client.LumenItemEditor.SaveOutcome;
import me.noramibu.lumentooltips.client.LumenItemEditor.SaveStatus;
import me.noramibu.lumentooltips.config.ItemEditorStorageTarget;
import net.minecraft.world.item.ItemStack;

public final class FabricItemEditorApi implements LumenItemEditor.Api {
    private boolean uniqueStorage;

    private FabricItemEditorApi(boolean uniqueStorage) {
        this.uniqueStorage = uniqueStorage;
    }

    public static void install(boolean uniqueStorage) {
        LumenItemEditor.install(new FabricItemEditorApi(uniqueStorage));
    }

    @Override
    public CompletableFuture<Boolean> openStorage(SaveOutcome outcome) {
        try {
            return outcome.itemId() == null
                    ? ItemEditorStorageApi.openStorageSlot(outcome.page().id(), outcome.slot())
                    : ItemEditorStorageApi.openStorageItem(outcome.itemId());
        } catch (NoSuchMethodError missingApi) {
            return CompletableFuture.failedFuture(missingApi);
        }
    }

    @Override
    public boolean openInventorySlot(int slot) {
        return ItemEditorApi.openPlayerInventorySlot(slot);
    }

    @Override
    public CompletableFuture<SaveOutcome> save(ItemStack stack, SaveOptions options) {
        if (uniqueStorage) {
            try {
                return ItemEditorStorageApi.findDuplicate(stack)
                        .thenCompose(match -> match.isPresent()
                                ? CompletableFuture.completedFuture(duplicate(match.get()))
                                : saveToTarget(stack, options));
            } catch (NoSuchMethodError missingApi) {
                uniqueStorage = false;
                LogUtils.getLogger().warn("[Lumen Tooltips] Update Item Editor to enable duplicate storage checks.");
            }
        }
        return saveToTarget(stack, options);
    }

    private CompletableFuture<SaveOutcome> saveToTarget(ItemStack stack, SaveOptions options) {
        if (options.target() == ItemEditorStorageTarget.FIRST_AVAILABLE) {
            return saveFirstAvailable(stack, options);
        }
        boolean byName = options.target() == ItemEditorStorageTarget.PAGE_NAME;
        var lookup = byName
                ? ItemEditorStorageApi.searchPages(options.pageName()).thenApply(pages -> pages.stream()
                        .filter(page -> page.plainName().equalsIgnoreCase(options.pageName()))
                        .findFirst())
                : ItemEditorStorageApi.findPageByNumber(options.pageNumber());
        return lookup.thenCompose(page -> {
            if (page.isPresent()) {
                return add(page.get(), stack);
            }
            return byName && options.createPage()
                    ? createAndSave(options.pageName(), stack)
                    : CompletableFuture.completedFuture(SaveOutcome.failed(SaveStatus.PAGE_NOT_FOUND));
        });
    }

    private CompletableFuture<SaveOutcome> saveFirstAvailable(ItemStack stack, SaveOptions options) {
        return ItemEditorStorageApi.listPages().thenCompose(pages -> tryPages(pages, 0, stack)
                .thenCompose(outcome -> outcome.status() == SaveStatus.PAGE_FULL && options.createPage()
                        ? createAndSave(options.pageName(), stack)
                        : CompletableFuture.completedFuture(outcome)));
    }

    private CompletableFuture<SaveOutcome> tryPages(List<StoragePage> pages, int index, ItemStack stack) {
        if (index >= pages.size()) {
            return CompletableFuture.completedFuture(SaveOutcome.failed(SaveStatus.PAGE_FULL));
        }
        return add(pages.get(index), stack)
                .thenCompose(outcome ->
                        outcome.status() == SaveStatus.PAGE_FULL || outcome.status() == SaveStatus.PAGE_NOT_FOUND
                                ? tryPages(pages, index + 1, stack)
                                : CompletableFuture.completedFuture(outcome));
    }

    private CompletableFuture<SaveOutcome> createAndSave(String pageName, ItemStack stack) {
        return ItemEditorStorageApi.createPage(pageName).thenCompose(page -> add(page, stack));
    }

    private CompletableFuture<SaveOutcome> add(StoragePage page, ItemStack stack) {
        Page summary = new Page(page.id(), page.number(), page.plainName());
        if (uniqueStorage) {
            return ItemEditorStorageApi.addIfAbsent(page.id(), stack).thenApply(result -> {
                if (result.status() == ItemEditorStorageApi.UniqueStoreStatus.DUPLICATE) {
                    return duplicate(result.duplicate().orElseThrow());
                }
                return outcome(result.status().name(), result.slot(), summary);
            });
        }
        return ItemEditorStorageApi.addToFirstEmptySlot(page.id(), stack)
                .thenApply(result -> outcome(result.status().name(), result.slot(), summary));
    }

    private static SaveOutcome duplicate(DuplicateMatch match) {
        return new SaveOutcome(
                SaveStatus.DUPLICATE, new Page(match.pageId(), match.pageNumber(), ""), match.slot(), match.itemId());
    }

    private static SaveOutcome outcome(String name, int slot, Page page) {
        SaveStatus status = SaveStatus.valueOf(name);
        return new SaveOutcome(status, status == SaveStatus.SAVED ? page : null, slot);
    }
}
