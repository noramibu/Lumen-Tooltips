package me.noramibu.lumentooltips.fabric;

import dev.faststats.Metrics;
import dev.faststats.fabric.FabricContext;
import me.noramibu.lumentooltips.LumenTooltips;
import me.noramibu.lumentooltips.client.FabricItemEditorApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.ComposterBlock;

public final class FabricLumenTooltips implements ClientModInitializer {
  private final FabricContext fastStats =
      new FabricContext.Factory(LumenTooltips.MOD_ID, "52824245513b7378c46a48dd8982008b")
          .metrics(Metrics.Factory::create)
          .create();

  @Override
  public void onInitializeClient() {
    FabricLoader loader = FabricLoader.getInstance();
    LumenTooltips.init(
        loader.getConfigDir(),
        new LumenTooltips.Platform(
            namespace ->
                loader
                    .getModContainer(namespace)
                    .map(mod -> mod.getMetadata().getName())
                    .orElse(namespace),
            stack -> {
              var level = Minecraft.getInstance().level;
              return level == null ? 0 : level.fuelValues().burnDuration(stack);
            },
            stack -> ComposterBlock.COMPOSTABLES.getFloat(stack.getItem()),
            block -> block.getExplosionResistance()));
    loader
        .getModContainer("itemeditor")
        .filter(mod -> supportsItemEditorApi(mod.getMetadata().getVersion()))
        .ifPresent(mod -> FabricItemEditorApi.install());
  }

  private static boolean supportsItemEditorApi(Version version) {
    String value = version.getFriendlyString();
    int marker = value.lastIndexOf('b');
    try {
      return marker >= 0 && Integer.parseInt(value.substring(marker + 1)) >= 19;
    } catch (NumberFormatException ignored) {
      return false;
    }
  }
}
