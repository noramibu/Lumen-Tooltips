package me.noramibu.lumentooltips.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;

public final class LumenParticleEffects {
    private final List<List<WeakReference<Particle>>> active =
            List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    public int group(ParticleType<?> type) {
        if (type == ParticleTypes.ELDER_GUARDIAN) return 0;
        if (type == ParticleTypes.EXPLOSION_EMITTER) return 1;
        if (type == ParticleTypes.GUST_EMITTER_LARGE || type == ParticleTypes.GUST_EMITTER_SMALL) return 2;
        return -1;
    }

    public int limit(int group, LumenConfig.ParticleSafetyConfig config) {
        if (!config.enabled) return -1;
        return switch (group) {
            case 0 -> config.limitElderGuardians ? config.maxElderGuardians : -1;
            case 1 -> config.limitExplosionEmitters ? config.maxExplosionEmitters : -1;
            case 2 -> config.limitGustEmitters ? config.maxGustEmitters : -1;
            default -> -1;
        };
    }

    public boolean hasSpace(int group, int limit) {
        var particles = active.get(group);
        particles.removeIf(reference -> {
            Particle particle = reference.get();
            return particle == null || !particle.isAlive();
        });
        return particles.size() < limit;
    }

    public void track(int group, Particle particle) {
        if (particle != null) active.get(group).add(new WeakReference<>(particle));
    }

    public void clear() {
        active.forEach(List::clear);
    }
}
