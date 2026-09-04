package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.client.screen.LumenScreenOpener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void lumenTooltips$addConfigButton(CallbackInfo callbackInfo) {
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.lumen_tooltips.config.open"),
                        button -> LumenScreenOpener.openConfig(this))
                .bounds(8, 8, 100, 20)
                .build());
    }
}
