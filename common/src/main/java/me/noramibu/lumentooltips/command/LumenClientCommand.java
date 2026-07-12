package me.noramibu.lumentooltips.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.noramibu.lumentooltips.client.screen.LumenScreenOpener;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class LumenClientCommand {
  private static final String USAGE_KEY = "command.lumen_tooltips.error.usage";

  private LumenClientCommand() {}

  public static void register(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
    registerAlias(dispatcher, "lumen");
    registerAlias(dispatcher, "tooltips");
  }

  public static boolean tryHandle(String input) {
    String trimmed = input.startsWith("/") ? input.substring(1).trim() : input.trim();
    if (trimmed.isEmpty()) {
      return false;
    }
    String[] parts = trimmed.split("\\s+", 3);
    if (!"lumen".equals(parts[0]) && !"tooltips".equals(parts[0])) {
      return false;
    }
    if (parts.length == 1 || parts.length == 2 && "config".equals(parts[1])) {
      LumenScreenOpener.openConfig(Minecraft.getInstance().screen);
      feedback("command.lumen_tooltips.config.opened");
    } else if (parts.length == 2 && "reload".equals(parts[1])) {
      LumenConfigManager.load();
      feedback("command.lumen_tooltips.reload.success");
    } else {
      feedback(USAGE_KEY);
    }
    return true;
  }

  private static void registerAlias(
      CommandDispatcher<ClientSuggestionProvider> dispatcher, String alias) {
    dispatcher.register(command(alias).then(command("config")).then(command("reload")));
  }

  private static LiteralArgumentBuilder<ClientSuggestionProvider> command(String name) {
    return LiteralArgumentBuilder.<ClientSuggestionProvider>literal(name).executes(context -> 1);
  }

  private static void feedback(String key) {
    Minecraft.getInstance().gui.getChat().addClientSystemMessage(Component.translatable(key));
  }
}
