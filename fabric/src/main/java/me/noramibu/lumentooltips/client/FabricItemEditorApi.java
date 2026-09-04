package me.noramibu.lumentooltips.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.noramibu.itemeditor.api.ItemEditorApi;
import me.noramibu.itemeditor.api.ItemEditorStorageApi;
import me.noramibu.itemeditor.api.ItemEditorStorageApi.StoragePage;
import me.noramibu.itemeditor.api.ItemEditorStorageApi.StoreResult;
import me.noramibu.lumentooltips.client.LumenItemEditor.Page;
import me.noramibu.lumentooltips.client.LumenItemEditor.SaveOptions;
import me.noramibu.lumentooltips.client.LumenItemEditor.SaveOutcome;
import me.noramibu.lumentooltips.client.LumenItemEditor.SaveStatus;
import net.minecraft.world.item.ItemStack;

public final class FabricItemEditorApi implements LumenItemEditor.Api {
    private FabricItemEditorApi() {}

    public static void install() {
        LumenItemEditor.install(new FabricItemEditorApi());
    }

    @Override
    public boolean openInventorySlot(int slot) {
        return ItemEditorApi.openPlayerInventorySlot(slot);
    }

    @Override
    public CompletableFuture<SaveOutcome> save(ItemStack stack, SaveOptions options) {
        return switch (options.target()) {
            case FIRST_AVAILABLE -> saveFirstAvailable(stack, options);
            case PAGE_NUMBER -> savePageNumber(stack, options.pageNumber());
            case PAGE_NAME -> savePageName(stack, options);
        };
    }

    private static CompletableFuture<SaveOutcome> saveFirstAvailable(ItemStack stack, SaveOptions options) {
        return ItemEditorStorageApi.listPages().thenCompose(pages -> tryPages(pages, 0, stack)
                .thenCompose(outcome -> outcome.status() == SaveStatus.PAGE_FULL && options.createPage()
                        ? createAndSave(options.pageName(), stack)
                        : CompletableFuture.completedFuture(outcome)));
    }

    private static CompletableFuture<SaveOutcome> tryPages(List<StoragePage> pages, int index, ItemStack stack) {
        if (index >= pages.size()) {
            return CompletableFuture.completedFuture(SaveOutcome.failed(SaveStatus.PAGE_FULL));
        }
        return add(pages.get(index), stack)
                .thenCompose(outcome ->
                        outcome.status() == SaveStatus.PAGE_FULL || outcome.status() == SaveStatus.PAGE_NOT_FOUND
                                ? tryPages(pages, index + 1, stack)
                                : CompletableFuture.completedFuture(outcome));
    }

    private static CompletableFuture<SaveOutcome> savePageNumber(ItemStack stack, int pageNumber) {
        return ItemEditorStorageApi.findPageByNumber(pageNumber)
                .thenCompose(page -> page.isPresent()
                        ? add(page.get(), stack)
                        : CompletableFuture.completedFuture(SaveOutcome.failed(SaveStatus.PAGE_NOT_FOUND)));
    }

    private static CompletableFuture<SaveOutcome> savePageName(ItemStack stack, SaveOptions options) {
        return ItemEditorStorageApi.searchPages(options.pageName()).thenCompose(pages -> {
            StoragePage page = pages.stream()
                    .filter(candidate -> candidate.plainName().equalsIgnoreCase(options.pageName()))
                    .findFirst()
                    .orElse(null);
            if (page != null) {
                return add(page, stack);
            }
            return options.createPage()
                    ? createAndSave(options.pageName(), stack)
                    : CompletableFuture.completedFuture(SaveOutcome.failed(SaveStatus.PAGE_NOT_FOUND));
        });
    }

    private static CompletableFuture<SaveOutcome> createAndSave(String pageName, ItemStack stack) {
        return ItemEditorStorageApi.createPage(pageName).thenCompose(page -> add(page, stack));
    }

    private static CompletableFuture<SaveOutcome> add(StoragePage page, ItemStack stack) {
        Page summary = new Page(page.id(), page.number(), page.plainName());
        return ItemEditorStorageApi.addToFirstEmptySlot(page.id(), stack).thenApply(result -> outcome(result, summary));
    }

    private static SaveOutcome outcome(StoreResult result, Page page) {
        SaveStatus status =
                switch (result.status()) {
                    case SAVED -> SaveStatus.SAVED;
                    case PAGE_NOT_FOUND -> SaveStatus.PAGE_NOT_FOUND;
                    case PAGE_FULL -> SaveStatus.PAGE_FULL;
                    case INVALID_ITEM -> SaveStatus.INVALID_ITEM;
                };
        return new SaveOutcome(status, status == SaveStatus.SAVED ? page : null, result.slot());
    }
}
