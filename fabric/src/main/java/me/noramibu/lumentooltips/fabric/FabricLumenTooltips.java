package me.noramibu.lumentooltips.fabric;

import me.noramibu.lumentooltips.LumenTooltips;
import me.noramibu.lumentooltips.client.FabricItemEditorApi;
import me.noramibu.lumentooltips.tooltip.LumenItemStatistics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

public final class FabricLumenTooltips implements ClientModInitializer {
    private static final String ITEM_EDITOR_MOD_ID = "itemeditor";
    private static final int ITEM_EDITOR_API_BUILD = 19;

    @Override
    public void onInitializeClient() {
        FabricLoader loader = FabricLoader.getInstance();
        LumenTooltips.init(
                loader.getConfigDir(),
                new LumenTooltips.Platform(
                        namespace -> loader.getModContainer(namespace)
                                .map(mod -> mod.getMetadata().getName())
                                .orElse(namespace),
                        LumenItemStatistics::fuelTime,
                        LumenItemStatistics::compostChance,
                        block -> block.getExplosionResistance(),
                        () -> modVersion(loader, LumenTooltips.MOD_ID),
                        () -> modVersion(loader, "minecraft")));
        loader.getModContainer(ITEM_EDITOR_MOD_ID)
                .filter(mod -> supportsItemEditorApi(mod.getMetadata().getVersion()))
                .ifPresent(mod -> FabricItemEditorApi.install());
    }

    private static String modVersion(FabricLoader loader, String modId) {
        return loader.getModContainer(modId)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("");
    }

    private static boolean supportsItemEditorApi(Version version) {
        return LumenTooltips.buildNumber(version.getFriendlyString()) >= ITEM_EDITOR_API_BUILD;
    }
}
