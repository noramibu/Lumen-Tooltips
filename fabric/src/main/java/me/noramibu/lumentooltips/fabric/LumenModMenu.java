package me.noramibu.lumentooltips.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.noramibu.lumentooltips.client.screen.LumenConfigScreen;

public final class LumenModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<LumenConfigScreen> getModConfigScreenFactory() {
        return LumenConfigScreen::new;
    }
}
