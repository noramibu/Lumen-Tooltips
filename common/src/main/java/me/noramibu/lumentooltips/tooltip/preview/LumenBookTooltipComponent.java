package me.noramibu.lumentooltips.tooltip.preview;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

record LumenBookTooltipComponent(String author, int pageCount, String preview)
    implements TooltipComponent, ClientTooltipComponent {
  private static final int WIDTH = 180;
  private static final int PADDING = 4;
  private static final int HEAD_SIZE = 18;
  private static final int MAX_PREVIEW_LENGTH = 240;
  private static final int MAX_CACHE_SIZE = 128;
  private static final int PANEL_ACCENT = 0xFFD6B56C;
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final int PREVIEW_COLOR = 0xFFB8B8B8;
  private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
  private static final PlayerSkin DEFAULT_SKIN = DefaultPlayerSkin.getDefaultSkin();
  private static final Map<String, CompletableFuture<PlayerSkin>> SKINS =
      new ConcurrentHashMap<>();

  static Optional<TooltipComponent> create(ItemStack stack) {
    WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
    if (content == null) {
      return Optional.empty();
    }
    List<Component> pages =
        content.getPages(Minecraft.getInstance().isTextFilteringEnabled());
    String preview = pages.isEmpty() ? "" : previewText(pages.getFirst());
    return Optional.of(
        new LumenBookTooltipComponent(content.author(), pages.size(), preview));
  }

  @Override
  public int getHeight(Font font) {
    return PADDING * 2 + Math.max(HEAD_SIZE, font.lineHeight * 3 + PADDING);
  }

  @Override
  public int getWidth(Font font) {
    return WIDTH;
  }

  @Override
  public boolean showTooltipWithItemInHand() {
    return true;
  }

  @Override
  public void extractImage(
      Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
    LumenContainerTooltipComponent.drawPanel(
        graphics, x, y, WIDTH, getHeight(font), PANEL_ACCENT);
    PlayerFaceExtractor.extractRenderState(
        graphics,
        skin(this.author),
        x + PADDING,
        y + PADDING,
        HEAD_SIZE);

    int textX = x + PADDING * 2 + HEAD_SIZE;
    int textY = y + PADDING;
    graphics.text(
        font,
        Component.translatable("tooltip.lumen_tooltips.book.pages", this.pageCount),
        textX,
        textY,
        TEXT_COLOR);
    if (this.preview.isEmpty()) {
      return;
    }
    List<FormattedCharSequence> lines =
        font.split(Component.literal(this.preview), WIDTH - PADDING * 3 - HEAD_SIZE);
    for (int index = 0; index < Math.min(2, lines.size()); index++) {
      graphics.text(
          font,
          lines.get(index),
          textX,
          textY + font.lineHeight * (index + 1) + PADDING,
          PREVIEW_COLOR);
    }
  }

  private static PlayerSkin skin(String author) {
    Minecraft minecraft = Minecraft.getInstance();
    ClientPacketListener connection = minecraft.getConnection();
    PlayerInfo online = connection == null ? null : connection.getPlayerInfoIgnoreCase(author);
    if (online != null) {
      return online.getSkin();
    }
    if (!validAuthor(author)) {
      return DEFAULT_SKIN;
    }
    String key = author.toLowerCase(Locale.ROOT);
    CompletableFuture<PlayerSkin> cached = SKINS.get(key);
    if (cached != null) {
      return cached.getNow(DEFAULT_SKIN);
    }
    if (SKINS.size() >= MAX_CACHE_SIZE) {
      return DEFAULT_SKIN;
    }
    return SKINS.computeIfAbsent(key, ignored -> load(minecraft, author)).getNow(DEFAULT_SKIN);
  }

  private static boolean validAuthor(String author) {
    return author != null && PLAYER_NAME.matcher(author).matches();
  }

  private static CompletableFuture<PlayerSkin> load(Minecraft minecraft, String author) {
    ProfileResolver profiles = minecraft.services().profileResolver();
    SkinManager skins = minecraft.getSkinManager();
    return CompletableFuture.supplyAsync(() -> profiles.fetchByName(author), Util.ioPool())
        .thenCompose(
            profile -> {
              if (profile.isEmpty()) {
                return CompletableFuture.completedFuture(DEFAULT_SKIN);
              }
              GameProfile resolved = profile.get();
              return skins.get(resolved)
                  .thenApply(skin -> skin.orElseGet(() -> DefaultPlayerSkin.get(resolved)));
            })
        .exceptionally(ignored -> DEFAULT_SKIN);
  }

  private static String previewText(Component page) {
    String text = page.getString().replace('\n', ' ').replace('\r', ' ').trim();
    return text.length() <= MAX_PREVIEW_LENGTH
        ? text
        : text.substring(0, MAX_PREVIEW_LENGTH);
  }
}
