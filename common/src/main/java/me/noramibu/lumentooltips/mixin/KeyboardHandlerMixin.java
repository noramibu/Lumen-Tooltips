package me.noramibu.lumentooltips.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import me.noramibu.lumentooltips.client.LumenItemEditor;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void lumenTooltips$releaseSaveKey(long window, int action, KeyEvent event, CallbackInfo callbackInfo) {
        if (action == InputConstants.RELEASE) {
            LumenItemEditor.keyReleased(event);
        }
    }
}
