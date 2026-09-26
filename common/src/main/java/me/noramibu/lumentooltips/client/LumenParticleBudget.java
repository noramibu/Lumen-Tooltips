package me.noramibu.lumentooltips.client;

import me.noramibu.lumentooltips.config.LumenConfig;

public final class LumenParticleBudget {
    private double tokens;
    private long updated;
    private int capacity;
    private int windowMillis;

    public int allow(int count, LumenConfig.ParticleSafetyConfig config, long now) {
        if (!config.enabled) {
            capacity = 0;
            return count;
        }
        if (count < 0) return -1;
        int requested = Math.max(1, count);
        int allowed = config.limitPerPacket ? Math.min(requested, config.maxPerPacket) : requested;
        if (config.limitRate) {
            if (capacity != config.maxPerWindow || windowMillis != config.windowMillis) {
                capacity = config.maxPerWindow;
                windowMillis = config.windowMillis;
                tokens = capacity;
            } else {
                tokens = Math.min(
                        capacity,
                        tokens + Math.max(0L, now - updated) * (double) capacity / (windowMillis * 1_000_000L));
            }
            updated = now;
            allowed = Math.min(allowed, (int) tokens);
        } else {
            capacity = 0;
        }
        if (allowed == 0 || config.dropOversized && allowed < requested) return -1;
        if (config.limitRate) tokens -= allowed;
        return count == 0 ? 0 : allowed;
    }
}
