package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum DurabilityPalette {
    DEFAULT,
    COLORBLIND;

    public int color(int percent, LumenConfig.DurabilityConfig config) {
        if (percent <= config.dangerPercent) {
            return this == COLORBLIND ? 0xD55E00 : 0xFF5555;
        }
        if (percent <= config.warningPercent) {
            return this == COLORBLIND ? 0xE69F00 : 0xFFFF55;
        }
        return this == COLORBLIND ? 0x0072B2 : 0x55FF55;
    }

    public Component displayName() {
        return Component.translatable(
                "config.lumen_tooltips.value.durability_palette." + ConfigOption.serializedName(this));
    }
}
