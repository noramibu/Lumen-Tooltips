package me.noramibu.lumentooltips.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public final class LumenScreenOpener {
    private static boolean configPending;

    private LumenScreenOpener() {}

    public static void openConfig(Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof ChatScreen) {
            configPending = true;
            return;
        }
        minecraft.setScreenAndShow(new LumenConfigScreen(parent));
    }

    public static void openPending() {
        if (!configPending) {
            return;
        }
        configPending = false;
        Minecraft.getInstance().setScreenAndShow(new LumenConfigScreen(null));
    }
}
