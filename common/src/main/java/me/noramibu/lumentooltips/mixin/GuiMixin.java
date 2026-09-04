package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.tooltip.LumenTextGuard;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Hud.class)
public abstract class GuiMixin {
    @ModifyArg(
            method = {
                "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
                "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V"
            },
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/chat/MutableComponent;append(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"))
    private Component lumenTooltips$guardSelectedItemName(Component name) {
        return LumenTextGuard.protect(name);
    }
}
