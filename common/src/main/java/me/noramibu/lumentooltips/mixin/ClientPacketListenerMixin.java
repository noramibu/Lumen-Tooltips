package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.command.LumenClientCommand;
import me.noramibu.lumentooltips.tooltip.LumenNavigationTooltip;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
  @Inject(method = "handleCommands", at = @At("TAIL"))
  @SuppressWarnings("DataFlowIssue")
  private void lumenTooltips$registerClientCommands(CallbackInfo callbackInfo) {
    LumenClientCommand.register(((ClientPacketListener) (Object) this).getCommands());
  }

  @Inject(method = "handleMapItemData", at = @At("TAIL"))
  private void lumenTooltips$invalidateMapStats(
      ClientboundMapItemDataPacket packet, CallbackInfo callbackInfo) {
    LumenNavigationTooltip.invalidateMap(packet.mapId());
  }
}
