package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.client.LumenItemEditor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class StorageLinkMixin {
    @Inject(method = "clickCommandAction", at = @At("HEAD"), cancellable = true)
    private static void lumenTooltips$openStorage(LocalPlayer player, String command, Screen parent, CallbackInfo ci) {
        if (LumenItemEditor.handleStorageLink(command)) {
            ci.cancel();
        }
    }
}
