package dev.faststats.neoforge.compat.v26_1;

import dev.faststats.neoforge.compat.CompatibilityLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class LumenCompatibilityLayer implements CompatibilityLayer {
  private volatile boolean onlineMode;
  private volatile int playerCount;

  public LumenCompatibilityLayer() {
    NeoForge.EVENT_BUS.addListener(this::update);
  }

  private void update(ClientTickEvent.Post event) {
    Minecraft client = Minecraft.getInstance();
    onlineMode = client.getUser().getXuid().isPresent() && !client.isOfflineDeveloperMode();
    ClientPacketListener connection = client.getConnection();
    playerCount =
        connection != null
            ? connection.getOnlinePlayers().size()
            : client.getSingleplayerServer() != null
                ? client.getSingleplayerServer().getPlayerCount()
                : client.player == null ? 0 : 1;
  }

  @Override
  public void initServer() {}

  @Override
  public boolean clientOnlineMode() {
    return onlineMode;
  }

  @Override
  public int clientPlayerCount() {
    return playerCount;
  }

  @Override
  public boolean serverOnlineMode() {
    return false;
  }

  @Override
  public int serverPlayerCount() {
    return 0;
  }

  @Override
  public Environment getEnvironment() {
    return Environment.CLIENT;
  }
}
