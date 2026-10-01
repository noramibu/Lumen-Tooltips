package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum ContainerStacking {
    OFF,
    ON,
    ON_PLUS;

    public Component displayName() {
        return Component.translatable(
                "config.lumen_tooltips.value.container_stacking." + ConfigOption.serializedName(this));
    }
}
