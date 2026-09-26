package me.noramibu.lumentooltips.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Locale;
import me.noramibu.lumentooltips.client.LumenChat;
import me.noramibu.lumentooltips.client.LumenParticleEffects;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Unique
    private final LumenParticleEffects lumenTooltips$effects = new LumenParticleEffects();

    @Unique
    private long lumenTooltips$lastEffectWarning = Long.MIN_VALUE;

    @WrapMethod(method = "createParticle")
    private Particle lumenTooltips$limitEffects(
            ParticleOptions options,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            Operation<Particle> original) {
        var config = LumenConfigManager.current().modules.particleSafety;
        int group = lumenTooltips$effects.group(options.getType());
        int limit = lumenTooltips$effects.limit(group, config);
        if (limit >= 0 && !lumenTooltips$effects.hasSpace(group, limit)) {
            long now = System.nanoTime();
            Minecraft minecraft = Minecraft.getInstance();
            if (config.showWarnings
                    && minecraft.player != null
                    && (lumenTooltips$lastEffectWarning == Long.MIN_VALUE
                            || now - lumenTooltips$lastEffectWarning
                                    >= config.warningCooldownSeconds * 1_000_000_000L)) {
                minecraft
                        .gui
                        .getChat()
                        .addClientSystemMessage(LumenChat.message(
                                        "message.lumen_tooltips.particle_effect_limited",
                                        Component.translatable(
                                                        switch (group) {
                                                            case 0 -> "screen.lumen_tooltips.config.particle_guardians";
                                                            case 1 ->
                                                                "screen.lumen_tooltips.config.particle_explosions";
                                                            default -> "screen.lumen_tooltips.config.particle_gusts";
                                                        })
                                                .withStyle(ChatFormatting.YELLOW),
                                        Component.literal(String.format(Locale.ROOT, "%.1f, %.1f, %.1f", x, y, z))
                                                .withStyle(ChatFormatting.YELLOW),
                                        limit)
                                .withStyle(ChatFormatting.GOLD));
                lumenTooltips$lastEffectWarning = now;
            }
            return null;
        }
        Particle particle = original.call(options, x, y, z, velocityX, velocityY, velocityZ);
        if (limit >= 0) lumenTooltips$effects.track(group, particle);
        return particle;
    }

    @Inject(method = "clearParticles", at = @At("HEAD"))
    private void lumenTooltips$clearEffects(CallbackInfo callback) {
        lumenTooltips$effects.clear();
        lumenTooltips$lastEffectWarning = Long.MIN_VALUE;
    }
}
