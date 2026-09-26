package me.noramibu.lumentooltips.client;

import com.mojang.logging.LogUtils;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import me.noramibu.lumentooltips.config.ItemEditorStorageTarget;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.config.LumenInputBinding;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class LumenItemEditor {
    private static final String STORAGE_LINK_PREFIX = "/lumen storage ";
    private static final LinkedHashMap<String, SaveOutcome> STORAGE_LINKS = new LinkedHashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean SAVE_KEY_DOWN = new AtomicBoolean();
    private static final AtomicBoolean SAVING = new AtomicBoolean();
    private static volatile Api api;
    private static volatile String activeSaveKey = LumenInputBinding.UNBOUND;

    private LumenItemEditor() {}

    static void install(Api integration) {
        api = integration;
    }

    public static boolean isAvailable() {
        return api != null;
    }

    public static boolean trySaveToStorage(ItemStack stack, KeyEvent event) {
        LumenConfig.ItemEditorConfig config = LumenConfigManager.current().modules.itemEditor;
        return !LumenInputBinding.UNBOUND.equals(config.saveKey)
                && LumenInputBinding.matches(config.saveKey, event)
                && saveToStorage(stack, config);
    }

    public static boolean openInventorySlot(int slot) {
        return api != null && api.openInventorySlot(slot);
    }

    private static boolean saveToStorage(ItemStack stack, LumenConfig.ItemEditorConfig config) {
        if (api == null || stack == null || stack.isEmpty() || config == null) {
            return false;
        }
        if (!SAVE_KEY_DOWN.compareAndSet(false, true)) {
            return true;
        }
        activeSaveKey = config.saveKey;
        SaveOptions options = SaveOptions.from(config);
        if (!SAVING.compareAndSet(false, true)) {
            if (options.showFeedback) {
                feedback("item_editor.storage.busy", ChatFormatting.YELLOW);
            }
            return true;
        }
        ItemStack saved = stack.copy();
        api.save(saved, options).whenComplete((outcome, error) -> {
            SAVING.set(false);
            Minecraft.getInstance().execute(() -> finishSave(saved, options.showFeedback, outcome, error));
        });
        return true;
    }

    public static void keyReleased(KeyEvent event) {
        if (SAVE_KEY_DOWN.get() && LumenInputBinding.includes(activeSaveKey, event)) {
            SAVE_KEY_DOWN.set(false);
            activeSaveKey = LumenInputBinding.UNBOUND;
        }
    }

    public static boolean handleStorageLink(String command) {
        if (!command.startsWith(STORAGE_LINK_PREFIX)) {
            return false;
        }
        SaveOutcome outcome = STORAGE_LINKS.get(command.substring(STORAGE_LINK_PREFIX.length()));
        if (api == null || outcome == null) {
            feedback("item_editor.storage.open_failed", ChatFormatting.RED);
        } else {
            api.openStorage(outcome)
                    .whenComplete((opened, error) -> Minecraft.getInstance().execute(() -> {
                        if (error != null || !Boolean.TRUE.equals(opened)) {
                            feedback("item_editor.storage.open_failed", ChatFormatting.RED);
                            if (error != null) {
                                LOGGER.warn("Could not open Item Editor storage", error);
                            }
                        }
                    }));
        }
        return true;
    }

    private static void finishSave(ItemStack stack, boolean showFeedback, SaveOutcome outcome, Throwable error) {
        if (error != null || outcome == null) {
            LOGGER.warn("Could not save item to Item Editor storage", error);
            if (showFeedback) {
                feedback("item_editor.storage.failed", ChatFormatting.RED, stack.getHoverName());
            }
            return;
        }
        if (!showFeedback) {
            return;
        }
        switch (outcome.status) {
            case SAVED, DUPLICATE -> {
                boolean saved = outcome.status == SaveStatus.SAVED;
                Component page = outcome.page.plainName.isBlank()
                        ? Component.translatable("message.lumen_tooltips.item_editor.storage.page", outcome.page.number)
                        : Component.translatable(
                                "message.lumen_tooltips.item_editor.storage.named_page",
                                outcome.page.plainName,
                                outcome.page.number);
                String token = UUID.randomUUID().toString();
                STORAGE_LINKS.put(token, outcome);
                if (STORAGE_LINKS.size() > 100) {
                    STORAGE_LINKS.pollFirstEntry();
                }
                showFeedback(LumenChat.message(
                                "message.lumen_tooltips.item_editor.storage." + (saved ? "saved" : "duplicate"),
                                stack.getHoverName(),
                                page,
                                outcome.slot + 1)
                        .withStyle(style -> style.withColor(saved ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
                                .withClickEvent(new ClickEvent.RunCommand(STORAGE_LINK_PREFIX + token))
                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable(
                                        "message.lumen_tooltips.item_editor.storage.open_hint")))));
            }
            case PAGE_NOT_FOUND -> feedback("item_editor.storage.page_not_found", ChatFormatting.RED);
            case PAGE_FULL -> feedback("item_editor.storage.full", ChatFormatting.RED);
            case INVALID_ITEM -> feedback("item_editor.storage.failed", ChatFormatting.RED, stack.getHoverName());
        }
    }

    private static void feedback(String suffix, ChatFormatting color, Object... arguments) {
        showFeedback(
                LumenChat.message("message.lumen_tooltips." + suffix, arguments).withStyle(color));
    }

    private static void showFeedback(Component message) {
        Minecraft.getInstance().gui.getChat().addMessage(message);
    }

    interface Api {
        boolean openInventorySlot(int slot);

        CompletableFuture<SaveOutcome> save(ItemStack stack, SaveOptions options);

        CompletableFuture<Boolean> openStorage(SaveOutcome outcome);
    }

    enum SaveStatus {
        SAVED,
        DUPLICATE,
        PAGE_NOT_FOUND,
        PAGE_FULL,
        INVALID_ITEM
    }

    record Page(String id, int number, String plainName) {}

    record SaveOutcome(SaveStatus status, Page page, int slot, String itemId) {
        SaveOutcome(SaveStatus status, Page page, int slot) {
            this(status, page, slot, null);
        }

        static SaveOutcome failed(SaveStatus status) {
            return new SaveOutcome(status, null, -1);
        }
    }

    record SaveOptions(
            ItemEditorStorageTarget target, int pageNumber, String pageName, boolean createPage, boolean showFeedback) {
        private static SaveOptions from(LumenConfig.ItemEditorConfig config) {
            return new SaveOptions(
                    config.target == null ? ItemEditorStorageTarget.FIRST_AVAILABLE : config.target,
                    config.pageNumber,
                    config.pageName == null ? "" : config.pageName.trim(),
                    config.createPage,
                    config.showFeedback);
        }
    }
}
