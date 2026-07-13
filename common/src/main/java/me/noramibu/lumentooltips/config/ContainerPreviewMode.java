package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum ContainerPreviewMode {
  COMPACT,
  FULL;

  public Component displayName() {
    return Component.translatable(
        "config.lumen_tooltips.value.container_mode." + ConfigOption.serializedName(this));
  }
}
