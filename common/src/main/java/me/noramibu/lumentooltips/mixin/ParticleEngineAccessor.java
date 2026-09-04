package me.noramibu.lumentooltips.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {
    @Invoker("makeParticle")
    @Nullable
    <T extends ParticleOptions> Particle lumenTooltips$makeParticle(
            T options, double x, double y, double z, double velocityX, double velocityY, double velocityZ);
}
