package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum DurabilityPalette {
  DEFAULT,
  COLORBLIND;

  public Component displayName() {
    return Component.translatable(
        "config.lumen_tooltips.value.durability_palette." + ConfigOption.serializedName(this));
  }
}
