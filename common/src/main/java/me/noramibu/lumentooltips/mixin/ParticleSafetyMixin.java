package me.noramibu.lumentooltips.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import java.util.Locale;
import me.noramibu.lumentooltips.client.LumenChat;
import me.noramibu.lumentooltips.client.LumenParticleBudget;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ParticleSafetyMixin {
    @Unique
    private final LumenParticleBudget lumenTooltips$particleBudget = new LumenParticleBudget();

    @Unique
    private long lumenTooltips$lastParticleWarning = Long.MIN_VALUE;

    @Inject(
            method = "handleParticleEvent",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                            shift = At.Shift.AFTER),
            cancellable = true)
    private void lumenTooltips$limitParticles(
            ClientboundLevelParticlesPacket packet,
            CallbackInfo callback,
            @Share("lumenTooltips$particleCount") LocalIntRef particleCount) {
        Minecraft minecraft = Minecraft.getInstance();
        var config = LumenConfigManager.current().modules.particleSafety;
        long now = System.nanoTime();
        int allowed = lumenTooltips$particleBudget.allow(packet.getCount(), config, now);
        if (config.enabled
                && allowed != packet.getCount()
                && config.showWarnings
                && (lumenTooltips$lastParticleWarning == Long.MIN_VALUE
                        || now - lumenTooltips$lastParticleWarning >= config.warningCooldownSeconds * 1_000_000_000L)
                && minecraft.player != null) {
            minecraft
                    .gui
                    .hud
                    .getChat()
                    .addClientSystemMessage(LumenChat.message(
                                    "message.lumen_tooltips.particle_limited",
                                    Component.literal(String.format(
                                                    Locale.ROOT,
                                                    "%.1f, %.1f, %.1f",
                                                    packet.getX(),
                                                    packet.getY(),
                                                    packet.getZ()))
                                            .withStyle(ChatFormatting.YELLOW),
                                    Component.literal(String.format(Locale.ROOT, "%,d", Math.max(1, packet.getCount())))
                                            .withStyle(ChatFormatting.YELLOW),
                                    Component.literal(String.format(Locale.ROOT, "%,d", Math.max(0, allowed)))
                                            .withStyle(ChatFormatting.GREEN))
                            .withStyle(ChatFormatting.GOLD));
            lumenTooltips$lastParticleWarning = now;
        }
        particleCount.set(allowed);
        if (config.enabled && allowed < 0) callback.cancel();
    }

    @ModifyExpressionValue(
            method = "handleParticleEvent",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/protocol/game/ClientboundLevelParticlesPacket;getCount()I"))
    private int lumenTooltips$boundedParticleCount(
            int original, @Share("lumenTooltips$particleCount") LocalIntRef particleCount) {
        return Math.min(original, particleCount.get());
    }
}
