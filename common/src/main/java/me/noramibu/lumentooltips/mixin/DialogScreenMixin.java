package me.noramibu.lumentooltips.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DialogScreen.class)
public abstract class DialogScreenMixin {
    @WrapMethod(method = "packControlsIntoColumns")
    private static LayoutElement lumenTooltips$limitColumns(
            List<? extends LayoutElement> controls, int columns, Operation<LayoutElement> original) {
        if ((columns < 1 || columns > Math.max(64, controls.size()))
                && !controls.isEmpty()
                && controls.getFirst() instanceof Button button) {
            button.setMessage(Component.translatable("screen.lumen_tooltips.dialog.columns_limited")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return original.call(controls, Math.clamp(columns, 1, Math.max(1, controls.size())));
    }
}
