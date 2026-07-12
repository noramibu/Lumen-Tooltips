package me.noramibu.lumentooltips.mixin;

import me.noramibu.lumentooltips.client.screen.LumenScreenOpener;
import me.noramibu.lumentooltips.command.LumenClientCommand;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
  @Inject(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V"), cancellable = true)
  private void lumenTooltips$handleCommand(
      String input, boolean addToRecentChat, CallbackInfo callbackInfo) {
    if (LumenClientCommand.tryHandle(input)) {
      callbackInfo.cancel();
    }
  }

  @Inject(method = "keyPressed", at = @At("RETURN"))
  private void lumenTooltips$openPendingConfig(
      CallbackInfoReturnable<Boolean> callbackInfo) {
    LumenScreenOpener.openPending();
  }
}
