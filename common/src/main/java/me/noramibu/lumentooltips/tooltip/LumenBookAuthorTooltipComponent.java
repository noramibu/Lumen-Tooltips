package me.noramibu.lumentooltips.tooltip;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;

public final class LumenBookAuthorTooltipComponent
    implements Component, FormattedCharSequence, ClientTooltipComponent {
  private static final int HEAD_SIZE = 10;
  private static final int GAP = 2;
  private static final int MAX_CACHE_SIZE = 128;
  private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
  private static final PlayerSkin DEFAULT_SKIN = DefaultPlayerSkin.getDefaultSkin();
  private static final Map<String, CompletableFuture<PlayerSkin>> SKINS =
      new ConcurrentHashMap<>();

  private final Component text;
  private final String author;

  public LumenBookAuthorTooltipComponent(Component text, String author) {
    this.text = text;
    this.author = author;
  }

  @Override
  public Style getStyle() {
    return this.text.getStyle();
  }

  @Override
  public ComponentContents getContents() {
    return this.text.getContents();
  }

  @Override
  public List<Component> getSiblings() {
    return this.text.getSiblings();
  }

  @Override
  public FormattedCharSequence getVisualOrderText() {
    return this;
  }

  @Override
  public boolean accept(FormattedCharSink visitor) {
    return this.text.getVisualOrderText().accept(visitor);
  }

  @Override
  public int getHeight(Font font) {
    return Math.max(font.lineHeight, HEAD_SIZE);
  }

  @Override
  public int getWidth(Font font) {
    return font.width(this.text) + GAP + HEAD_SIZE;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    graphics.text(font, this.text, x, y, 0xFFFFFFFF);
    PlayerFaceExtractor.extractRenderState(
        graphics, skin(this.author), x + font.width(this.text) + GAP, y, HEAD_SIZE);
  }

  private static PlayerSkin skin(String author) {
    Minecraft minecraft = Minecraft.getInstance();
    PlayerInfo online =
        minecraft.getConnection() == null
            ? null
            : minecraft.getConnection().getPlayerInfoIgnoreCase(author);
    if (online != null) {
      return online.getSkin();
    }
    if (!PLAYER_NAME.matcher(author).matches()) {
      return DEFAULT_SKIN;
    }
    String key = author.toLowerCase(Locale.ROOT);
    if (SKINS.size() >= MAX_CACHE_SIZE && !SKINS.containsKey(key)) {
      return DEFAULT_SKIN;
    }
    return SKINS.computeIfAbsent(key, ignored -> load(minecraft, author)).getNow(DEFAULT_SKIN);
  }

  private static CompletableFuture<PlayerSkin> load(Minecraft minecraft, String author) {
    ProfileResolver profiles = minecraft.services().profileResolver();
    return CompletableFuture.supplyAsync(() -> profiles.fetchByName(author), Util.ioPool())
        .thenCompose(
            profile -> {
              if (profile.isEmpty()) {
                return CompletableFuture.completedFuture(DEFAULT_SKIN);
              }
              GameProfile resolved = profile.get();
              return minecraft
                  .getSkinManager()
                  .get(resolved)
                  .thenApply(skin -> skin.orElseGet(() -> DefaultPlayerSkin.get(resolved)));
            })
        .exceptionally(ignored -> DEFAULT_SKIN);
  }
}
