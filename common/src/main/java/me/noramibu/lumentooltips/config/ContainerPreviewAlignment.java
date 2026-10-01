package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum ContainerPreviewAlignment {
    LEFT,
    CENTER,
    RIGHT;

    public Component displayName() {
        return Component.translatable(
                "config.lumen_tooltips.value.container_alignment." + ConfigOption.serializedName(this));
    }
}
