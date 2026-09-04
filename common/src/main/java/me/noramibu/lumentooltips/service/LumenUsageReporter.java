package me.noramibu.lumentooltips.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.noramibu.lumentooltips.LumenTooltips;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public final class LumenUsageReporter {
    private static final String STARTUP_URL = "http://webhook.noramibu.me:26900/webhook/Lumen%20Tooltips/Startup";
    private static final String ACTIVE_URL = "http://webhook.noramibu.me:26900/webhook/Lumen%20Tooltips/Active";
    private static final String KEY_RESOURCE = "/lumen_tooltips_webhook.json";
    private static final long STARTUP_DELAY_SECONDS = 5;
    private static final long ACTIVE_INTERVAL_MINUTES = 5;
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STARTUP_API_KEY = webhookKey("startup");
    private static final String ACTIVE_API_KEY = webhookKey("active");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, LumenTooltips.MOD_ID + "-usage-reporter");
        thread.setDaemon(true);
        return thread;
    });

    private static boolean started;

    private LumenUsageReporter() {}

    public static void initialize() {
        if (started) {
            return;
        }
        started = true;
        logStatus();
        EXECUTOR.schedule(() -> send(STARTUP_URL, STARTUP_API_KEY), STARTUP_DELAY_SECONDS, TimeUnit.SECONDS);
        EXECUTOR.scheduleAtFixedRate(
                () -> send(ACTIVE_URL, ACTIVE_API_KEY),
                ACTIVE_INTERVAL_MINUTES,
                ACTIVE_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
    }

    public static boolean enabled() {
        return LumenConfigManager.current().modules.statistics.enabled;
    }

    public static void logStatus() {
        if (enabled()) {
            LOGGER.info("[Lumen Tooltips] Usage statistics are enabled. Sends a ping to a remote server for"
                    + " activity and version checks. Run /lumen statistics to disable them.");
        } else {
            LOGGER.info("[Lumen Tooltips] Usage statistics are disabled. Run /lumen statistics to enable them.");
        }
    }

    private static void send(String url, String apiKey) {
        if (!enabled() || apiKey.isBlank()) {
            return;
        }
        try {
            var user = Minecraft.getInstance().getUser();
            if (user == null) {
                return;
            }
            JsonObject payload = new JsonObject();
            payload.addProperty("minecraft-version", LumenTooltips.minecraftVersion());
            payload.addProperty("mod-version", LumenTooltips.buildNumber(LumenTooltips.modVersion()));
            payload.addProperty("username", user.getName());
            payload.addProperty("uuid", user.getProfileId().toString());

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();
            HTTP_CLIENT
                    .sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ignored -> null);
        } catch (RuntimeException ignored) {
        }
    }

    private static String webhookKey(String name) {
        try (InputStream stream = LumenUsageReporter.class.getResourceAsStream(KEY_RESOURCE)) {
            if (stream == null) {
                return "";
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject keys = GSON.fromJson(reader, JsonObject.class);
                return keys != null && keys.has(name) ? keys.get(name).getAsString() : "";
            }
        } catch (Exception ignored) {
            return "";
        }
    }
}
