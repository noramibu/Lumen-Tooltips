package me.noramibu.lumentooltips.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class LumenChat {
    private LumenChat() {}

    public static MutableComponent message(String key, Object... arguments) {
        return Component.empty()
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("message.lumen_tooltips.prefix").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(key, arguments));
    }
}
