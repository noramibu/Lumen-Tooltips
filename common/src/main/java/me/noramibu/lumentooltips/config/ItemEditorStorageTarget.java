package me.noramibu.lumentooltips.config;

import net.minecraft.network.chat.Component;

public enum ItemEditorStorageTarget {
  FIRST_AVAILABLE,
  PAGE_NUMBER,
  PAGE_NAME;

  public Component displayName() {
    return Component.translatable(
        "config.lumen_tooltips.value.item_editor_target." + ConfigOption.serializedName(this));
  }
}
