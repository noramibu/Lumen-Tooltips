package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum PreviewDensity {
  COMPACT,
  VANILLA,
  COMFORTABLE;

  public Component displayName() {
    return Component.translatable(
        "config.lumen_tooltips.value.preview_density." + ConfigOption.serializedName(this));
  }
}
