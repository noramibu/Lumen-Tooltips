package me.noramibu.lumentooltips.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.noramibu.lumentooltips.client.LumenItemEditor;
import me.noramibu.lumentooltips.config.ConfigOption;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.config.LumenInputBinding;
import me.noramibu.lumentooltips.config.LumenOptionRegistry;
import me.noramibu.lumentooltips.config.OptionControl;
import me.noramibu.lumentooltips.config.SaveMode;
import me.noramibu.lumentooltips.tooltip.preview.LumenTooltipPreview;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

public final class LumenConfigScreen extends Screen {
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final int MUTED_TEXT_COLOR = 0xFFA0A0A0;
  private static final int WARNING_TEXT_COLOR = 0xFFFFAA00;
  private static final int BUTTON_WIDTH = 200;
  private static final int BUTTON_HEIGHT = 20;
  private static final int COLUMN_GAP = 10;
  private static final int ROW_PITCH = 24;
  private static final int MAX_ROWS = 6;
  private static final int NAVIGATION_WIDTH = 75;
  private static final int ACTION_WIDTH = 96;
  private static final int FOOTER_GAP = 4;
  private static final int PREVIEW_GAP = 8;
  private static final int PREVIEW_MARGIN = 6;
  private static final List<Category> CATEGORIES =
      List.of(
          new Category("controls.", "screen.lumen_tooltips.config.controls"),
          new Category("modules.durability.", "screen.lumen_tooltips.config.durability"),
          new Category("modules.food.", "screen.lumen_tooltips.config.food"),
          new Category("modules.enchantments.", "screen.lumen_tooltips.config.enchantments"),
          new Category("modules.comparison.", "screen.lumen_tooltips.config.comparison"),
          new Category("modules.navigation.", "screen.lumen_tooltips.config.navigation"),
          new Category("modules.safety.", "screen.lumen_tooltips.config.safety"),
          new Category("modules.tooltip.", "screen.lumen_tooltips.config.tooltip"),
          new Category(
              "modules.tooltipFlags.", "screen.lumen_tooltips.config.tooltip_flags"),
          new Category("modules.preview.", "screen.lumen_tooltips.config.previews"));

  private final Screen parent;
  private final List<AbstractWidget> pageWidgets = new ArrayList<>();
  private final List<OptionWidget> optionWidgets = new ArrayList<>();
  private String searchText = "";
  private int page;
  private int pageCount = 1;
  private int rowsPerPage = MAX_ROWS;
  private int visibleOptionCount;
  private boolean searching;
  private boolean showingCategories = true;
  private EditBox searchBox;
  private OptionWidget capturingOption;
  private Category selectedCategory;

  public LumenConfigScreen(Screen parent) {
    super(Component.translatable("screen.lumen_tooltips.config.title"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    int searchWidth = Math.min(BUTTON_WIDTH, this.width - 24);
    int searchY = this.height / 7 + 5;
    this.searchBox =
        this.addRenderableWidget(
            new EditBox(
                this.font,
                this.width / 2 - searchWidth / 2,
                searchY,
                searchWidth,
                BUTTON_HEIGHT,
                Component.translatable("screen.lumen_tooltips.config.search")));
    this.searchBox.setMaxLength(80);
    this.searchBox.setHint(
        Component.translatable("screen.lumen_tooltips.config.search_hint"));
    this.searchBox.setValue(this.searchText);
    this.searchBox.setResponder(
        value -> {
          this.searchText = value;
          this.page = 0;
          rebuildPage();
        });

    int gridY = searchY + ROW_PITCH;
    this.rowsPerPage =
        Math.clamp((this.height - gridY - BUTTON_HEIGHT - 8) / ROW_PITCH, 1, MAX_ROWS);
    rebuildPage();
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    if (this.capturingOption != null) {
      if (event.key() == InputConstants.KEY_ESCAPE) {
        stopKeyCapture();
        return true;
      }
      if (event.key() == InputConstants.KEY_BACKSPACE || event.key() == InputConstants.KEY_DELETE) {
        this.capturingOption.setConfigValue(LumenInputBinding.UNBOUND);
      } else if (!LumenInputBinding.isModifier(event)) {
        this.capturingOption.setConfigValue(LumenInputBinding.fromEvent(event));
      } else {
        return true;
      }
      stopKeyCapture();
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean keyReleased(KeyEvent event) {
    if (this.capturingOption != null && LumenInputBinding.isModifier(event)) {
      this.capturingOption.setConfigValue(LumenInputBinding.fromEvent(event));
      stopKeyCapture();
      return true;
    }
    return super.keyReleased(event);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (this.capturingOption != null) {
      this.capturingOption.setConfigValue(LumenInputBinding.fromEvent(event));
      stopKeyCapture();
      return true;
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
    super.extractRenderState(graphics, mouseX, mouseY, tickDelta);
    Component sectionTitle =
        this.showingCategories
            ? Component.translatable("screen.lumen_tooltips.config.categories")
            : this.searching
                ? Component.translatable("screen.lumen_tooltips.config.search")
                : this.selectedCategory.title();
    Component pageTitle =
        this.title
            .copy()
            .append(" - ")
            .append(sectionTitle)
            .append(" (" + (this.page + 1) + "/" + this.pageCount + ")");
    graphics.centeredText(this.font, pageTitle, this.width / 2, 12, TEXT_COLOR);
    boolean missingItemEditor =
        !this.searching
            && this.selectedCategory != null
            && "controls.".equals(this.selectedCategory.prefix())
            && !LumenItemEditor.isAvailable();
    if (missingItemEditor) {
      graphics.centeredText(
          this.font,
          Component.translatable("screen.lumen_tooltips.config.item_editor_missing"),
          this.width / 2,
          24,
          WARNING_TEXT_COLOR);
    }
    if (!this.showingCategories && this.visibleOptionCount == 0) {
      graphics.centeredText(
          this.font,
          Component.translatable("screen.lumen_tooltips.config.no_results"),
          this.width / 2,
          this.searchBox.getY() + ROW_PITCH + 8,
          MUTED_TEXT_COLOR);
    }
    for (OptionWidget option : this.optionWidgets) {
      if (option.control.isHovered()) {
        renderPreviewTooltips(graphics, option, mouseX, mouseY);
        break;
      }
    }
  }

  @Override
  public void onClose() {
    if (!this.searchText.isBlank()) {
      this.searchBox.setValue("");
      return;
    }
    if (this.selectedCategory != null) {
      this.selectedCategory = null;
      this.page = 0;
      rebuildPage();
      return;
    }
    closeToParent();
  }

  private void closeToParent() {
    this.minecraft.setScreen(this.parent);
  }

  private void rebuildPage() {
    this.capturingOption = null;
    this.pageWidgets.forEach(this::removeWidget);
    this.pageWidgets.clear();
    this.optionWidgets.clear();
    this.visibleOptionCount = 0;

    String query = this.searchText.toLowerCase(Locale.ROOT).trim();
    this.searching = !query.isEmpty();
    this.showingCategories = !this.searching && this.selectedCategory == null;
    int pageSize = this.rowsPerPage * 2;
    int optionWidth = Math.clamp((this.width - 24 - COLUMN_GAP) / 2, 90, BUTTON_WIDTH);
    int gridWidth = optionWidth * 2 + COLUMN_GAP;
    int gridX = this.width / 2 - gridWidth / 2;
    int gridY = this.searchBox.getY() + ROW_PITCH;

    if (this.showingCategories) {
      this.pageCount = Math.max(1, (CATEGORIES.size() + pageSize - 1) / pageSize);
      this.page = Math.min(this.page, this.pageCount - 1);
      int firstCategory = this.page * pageSize;
      int categoryCount = Math.min(pageSize, CATEGORIES.size() - firstCategory);
      for (int index = 0; index < categoryCount; index++) {
        Category category = CATEGORIES.get(firstCategory + index);
        int x = gridX + (index % 2) * (optionWidth + COLUMN_GAP);
        int y = gridY + (index / 2) * ROW_PITCH;
        addPageWidget(
            Button.builder(category.title(), button -> openCategory(category))
                .bounds(x, y, optionWidth, BUTTON_HEIGHT)
                .build());
      }
      addFooter(gridWidth, gridY + this.rowsPerPage * ROW_PITCH);
      return;
    }

    List<ConfigOption> matchingOptions = currentMenuOptions(true);
    this.pageCount = Math.max(1, (matchingOptions.size() + pageSize - 1) / pageSize);
    this.page = Math.min(this.page, this.pageCount - 1);

    int firstOption = this.page * pageSize;
    int optionCount = Math.min(pageSize, matchingOptions.size() - firstOption);
    for (int index = 0; index < optionCount; index++) {
      int x = gridX + (index % 2) * (optionWidth + COLUMN_GAP);
      int y = gridY + (index / 2) * ROW_PITCH;
      OptionWidget optionWidget =
          new OptionWidget(matchingOptions.get(firstOption + index), x, y, optionWidth);
      this.optionWidgets.add(optionWidget);
      this.visibleOptionCount++;
      addPageWidget(optionWidget.control);
    }

    addFooter(gridWidth, gridY + this.rowsPerPage * ROW_PITCH);
  }

  private void addFooter(int gridWidth, int y) {
    int navigationWidth =
        Math.clamp(
            (gridWidth - 2 * ACTION_WIDTH - 3 * FOOTER_GAP) / 2, 30, NAVIGATION_WIDTH);
    int actionWidth =
        Math.min(
            ACTION_WIDTH,
            (gridWidth - 2 * navigationWidth - 3 * FOOTER_GAP) / 2);
    int footerWidth = 2 * navigationWidth + 2 * actionWidth + 3 * FOOTER_GAP;
    int x = this.width / 2 - footerWidth / 2;

    boolean canGoBack = this.page > 0 || (!this.searching && this.selectedCategory != null);
    Button previous =
        Button.builder(Component.literal("<"), button -> navigateBack())
            .bounds(x, y, navigationWidth, BUTTON_HEIGHT)
            .build();
    previous.active = canGoBack;
    previous.setTooltip(
        Tooltip.create(
            this.page > 0
                ? Component.translatable("screen.lumen_tooltips.config.previous")
                : !this.searching && this.selectedCategory != null
                    ? Component.translatable("screen.lumen_tooltips.config.back_categories")
                    : Component.translatable("screen.lumen_tooltips.config.previous")));
    addPageWidget(previous);
    x += navigationWidth + FOOTER_GAP;

    Button reset =
        Button.builder(
                Component.translatable("screen.lumen_tooltips.config.reset_current"),
                button -> resetCurrentMenu())
            .bounds(x, y, actionWidth, BUTTON_HEIGHT)
            .build();
    reset.active = !this.showingCategories && this.visibleOptionCount > 0;
    addPageWidget(reset);
    x += actionWidth + FOOTER_GAP;

    addPageWidget(
        Button.builder(CommonComponents.GUI_DONE, button -> closeToParent())
            .bounds(x, y, actionWidth, BUTTON_HEIGHT)
            .build());
    x += actionWidth + FOOTER_GAP;

    Button next =
        Button.builder(Component.literal(">"), button -> changePage(1))
            .bounds(x, y, navigationWidth, BUTTON_HEIGHT)
            .build();
    next.active = this.page + 1 < this.pageCount;
    next.setTooltip(
        Tooltip.create(Component.translatable("screen.lumen_tooltips.config.next")));
    addPageWidget(next);
  }

  private void addPageWidget(AbstractWidget widget) {
    this.pageWidgets.add(this.addRenderableWidget(widget));
  }

  private void changePage(int amount) {
    this.page += amount;
    rebuildPage();
  }

  private void navigateBack() {
    if (this.page > 0) {
      changePage(-1);
      return;
    }
    if (!this.searching && this.selectedCategory != null) {
      this.selectedCategory = null;
      rebuildPage();
    }
  }

  private void openCategory(Category category) {
    this.selectedCategory = category;
    this.page = 0;
    rebuildPage();
  }

  private void resetCurrentMenu() {
    LumenConfig config = LumenConfigManager.editingCopy();
    currentMenuOptions(this.searching)
        .forEach(option -> option.setFromString(config, option.defaultValue()));
    LumenConfigManager.apply(config, SaveMode.DISK);
    rebuildPage();
  }

  private List<ConfigOption> currentMenuOptions(boolean visibleOnly) {
    String query = this.searchText.toLowerCase(Locale.ROOT).trim();
    LumenConfig current = LumenConfigManager.current();
    return LumenOptionRegistry.options().stream()
        .filter(option -> !visibleOnly || LumenOptionRegistry.isVisible(option, current))
        .filter(
            option ->
                this.searching
                    ? option.matchesSearch(query)
                    : this.selectedCategory != null
                        && this.selectedCategory.contains(option.path()))
        .toList();
  }

  private void startKeyCapture(OptionWidget option) {
    if (this.capturingOption != null) {
      this.capturingOption.refreshMessage();
    }
    this.capturingOption = option;
    option.showCaptureMessage();
  }

  private void stopKeyCapture() {
    OptionWidget option = this.capturingOption;
    this.capturingOption = null;
    if (option != null) {
      option.refreshMessage();
    }
  }

  private void renderPreviewTooltips(
      GuiGraphicsExtractor graphics, OptionWidget option, int mouseX, int mouseY) {
    int lineWidth = Math.clamp((this.width - PREVIEW_MARGIN * 3 - PREVIEW_GAP) / 2, 80, 200);
    LumenConfig currentConfig = LumenConfigManager.current();
    LumenConfig modifiedConfig = currentConfig.copy();
    option.option.cycle(modifiedConfig);
    Component currentPreview = option.option.preview(currentConfig);
    Component modifiedPreview = option.option.preview(modifiedConfig);
    TooltipComponent sample = option.sample;
    if (currentPreview == null && modifiedPreview == null && sample == null) {
      renderDescriptionTooltip(graphics, option, mouseX, mouseY, lineWidth);
      return;
    }
    boolean currentVisible =
        sample != null && Boolean.parseBoolean(option.option.getAsString(currentConfig));
    boolean modifiedVisible =
        sample != null && Boolean.parseBoolean(option.option.getAsString(modifiedConfig));
    List<ClientTooltipComponent> current =
        previewLines(
            Component.translatable("screen.lumen_tooltips.config.preview.current")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            previewText(currentPreview, currentVisible),
            currentVisible ? ClientTooltipComponent.create(sample) : null,
            lineWidth,
            false,
            option.option.description());
    List<ClientTooltipComponent> modified =
        previewLines(
            Component.translatable("screen.lumen_tooltips.config.preview.modify")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
            previewText(modifiedPreview, modifiedVisible),
            modifiedVisible ? ClientTooltipComponent.create(sample) : null,
            lineWidth,
            true,
            option.option.description());
    int currentWidth = tooltipWidth(current);
    int modifiedWidth = tooltipWidth(modified);
    int currentHeight = tooltipHeight(current);
    int modifiedHeight = tooltipHeight(modified);
    int combinedWidth = currentWidth + PREVIEW_GAP + modifiedWidth;
    int maxHeight = Math.max(currentHeight, modifiedHeight);
    int x =
        previewX(
            this.width, option.control.getX(), option.control.getWidth(), combinedWidth);
    boolean corner =
        x != option.control.getX() + option.control.getWidth() + PREVIEW_GAP
            && x != option.control.getX() - PREVIEW_GAP - combinedWidth;
    int y = previewY(this.height, mouseY, maxHeight, corner);
    int currentX = x;
    int modifiedX = x + currentWidth + PREVIEW_GAP;
    int tooltipY = y;

    graphics.nextStratum();
    renderTooltip(graphics, current, mouseX, mouseY, currentX, tooltipY);
    renderTooltip(graphics, modified, mouseX, mouseY, modifiedX, tooltipY);
  }

  private void renderDescriptionTooltip(
      GuiGraphicsExtractor graphics,
      OptionWidget option,
      int mouseX,
      int mouseY,
      int lineWidth) {
    List<ClientTooltipComponent> lines =
        previewLines(
            option.option.title().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            null,
            null,
            lineWidth,
            true,
            option.option.description());
    int width = tooltipWidth(lines);
    int height = tooltipHeight(lines);
    int x = previewX(this.width, option.control.getX(), option.control.getWidth(), width);
    boolean corner =
        x != option.control.getX() + option.control.getWidth() + PREVIEW_GAP
            && x != option.control.getX() - PREVIEW_GAP - width;
    int y = previewY(this.height, mouseY, height, corner);
    graphics.nextStratum();
    renderTooltip(graphics, lines, mouseX, mouseY, x, y);
  }

  private void renderTooltip(
      GuiGraphicsExtractor graphics,
      List<ClientTooltipComponent> lines,
      int mouseX,
      int mouseY,
      int x,
      int y) {
    graphics.tooltip(
        this.font,
        lines,
        mouseX,
        mouseY,
        (screenWidth, screenHeight, anchorX, anchorY, width, height) -> new Vector2i(x, y),
        null);
  }

  private List<ClientTooltipComponent> previewLines(
      Component heading,
      @Nullable Component preview,
      @Nullable ClientTooltipComponent image,
      int width,
      boolean includeDescription,
      Component description) {
    List<ClientTooltipComponent> components = new ArrayList<>();
    components.add(ClientTooltipComponent.create(heading.getVisualOrderText()));
    if (preview != null) {
      this.font.split(preview, width).stream()
          .map(ClientTooltipComponent::create)
          .forEach(components::add);
    }
    if (image != null) {
      components.add(image);
    }
    if (includeDescription) {
      components.add(ClientTooltipComponent.create(Component.empty().getVisualOrderText()));
      this.font
          .split(description.copy().withStyle(ChatFormatting.GRAY), width)
          .stream()
          .map(ClientTooltipComponent::create)
          .forEach(components::add);
    }
    return components;
  }

  private static @Nullable Component previewText(
      @Nullable Component preview, boolean imageVisible) {
    return preview != null || imageVisible
        ? preview
        : Component.translatable("screen.lumen_tooltips.config.preview.hidden")
            .withStyle(ChatFormatting.RED);
  }

  private int tooltipWidth(List<ClientTooltipComponent> lines) {
    return lines.stream().mapToInt(line -> line.getWidth(this.font)).max().orElse(0);
  }

  private int tooltipHeight(List<ClientTooltipComponent> lines) {
    return (lines.size() == 1 ? -2 : 0)
        + lines.stream().mapToInt(line -> line.getHeight(this.font)).sum();
  }

  private static int previewX(int screenWidth, int controlX, int controlWidth, int combinedWidth) {
    int right = controlX + controlWidth + PREVIEW_GAP;
    if (right + combinedWidth <= screenWidth - PREVIEW_MARGIN) {
      return right;
    }
    int left = controlX - PREVIEW_GAP - combinedWidth;
    return left >= PREVIEW_MARGIN
        ? left
        : Math.max(PREVIEW_MARGIN, screenWidth - PREVIEW_MARGIN - combinedWidth);
  }

  private static int previewY(int screenHeight, int mouseY, int tooltipHeight, boolean corner) {
    return corner
        ? PREVIEW_MARGIN
        : Math.clamp(
            mouseY - 12,
            PREVIEW_MARGIN,
            Math.max(PREVIEW_MARGIN, screenHeight - tooltipHeight - PREVIEW_MARGIN));
  }

  private final class OptionWidget {
    private final ConfigOption option;
    private final AbstractWidget control;
    private final TooltipComponent sample;

    OptionWidget(ConfigOption option, int x, int y, int width) {
      this.option = option;
      this.sample = LumenTooltipPreview.configSample(option.path()).orElse(null);
      this.control = createControl(x, y, width);
    }

    void refreshMessage() {
      if (this.control instanceof NumericSlider slider) {
        slider.refreshFromConfig();
        return;
      }
      if (this.control instanceof EditBox text) {
        String value = this.option.getAsString(LumenConfigManager.current());
        if (!text.getValue().equals(value)) {
          text.setValue(value);
        }
        return;
      }
      this.control.setMessage(this.option.valueComponent(LumenConfigManager.current()));
    }

    void showCaptureMessage() {
      this.control.setMessage(
          Component.translatable("screen.lumen_tooltips.config.press_key"));
    }

    void setConfigValue(String value) {
      setConfigValue(value, SaveMode.DISK);
    }

    private void setConfigValue(String value, SaveMode saveMode) {
      LumenConfig config = LumenConfigManager.editingCopy();
      this.option.setFromString(config, value);
      LumenConfigManager.apply(config, saveMode);
      refreshMessage();
    }

    private AbstractWidget createControl(int x, int y, int width) {
      if (this.option.control() == OptionControl.TEXT
          || this.option.control() == OptionControl.NUMBER) {
        boolean number = this.option.control() == OptionControl.NUMBER;
        EditBox text =
            number
                ? new NumericEditBox(x, y, width, this.option.title())
                : new EditBox(
                    LumenConfigScreen.this.font,
                    x,
                    y,
                    width,
                    BUTTON_HEIGHT,
                    this.option.title());
        text.setMaxLength(
            number
                ? Integer.toString(this.option.maxValue()).length()
                : this.option.maxValue());
        text.setHint(this.option.title().copy().withStyle(ChatFormatting.DARK_GRAY));
        text.setValue(this.option.getAsString(LumenConfigManager.current()));
        text.setResponder(
            value -> {
              if (!number || !value.isEmpty()) {
                setConfigValue(value);
              }
            });
        return text;
      }
      if (this.option.control() == OptionControl.PERCENT
          || this.option.control() == OptionControl.INTEGER) {
        return new NumericSlider(x, y, width);
      }
      return Button.builder(
              this.option.valueComponent(LumenConfigManager.current()), button -> click())
          .bounds(x, y, width, BUTTON_HEIGHT)
          .build();
    }

    private void click() {
      if (this.option.control() == OptionControl.KEY_BIND) {
        LumenConfigScreen.this.startKeyCapture(this);
        return;
      }
      LumenConfig config = LumenConfigManager.editingCopy();
      this.option.cycle(config);
      LumenConfigManager.apply(config, SaveMode.DISK);
      LumenConfigScreen.this.rebuildPage();
    }

    private final class NumericSlider extends AbstractSliderButton {
      private boolean dragging;

      NumericSlider(int x, int y, int width) {
        super(
            x,
            y,
            width,
            BUTTON_HEIGHT,
            OptionWidget.this.option.valueComponent(LumenConfigManager.current()),
            sliderValue(OptionWidget.this.option));
      }

      void refreshFromConfig() {
        this.value = sliderValue(OptionWidget.this.option);
        updateMessage();
      }

      @Override
      public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.dragging = true;
        super.onClick(event, doubleClick);
      }

      @Override
      public void onRelease(MouseButtonEvent event) {
        this.dragging = false;
        LumenConfigManager.save();
        super.onRelease(event);
      }

      @Override
      protected void updateMessage() {
        setMessage(OptionWidget.this.option.valueComponent(LumenConfigManager.current()));
      }

      @Override
      protected void applyValue() {
        OptionWidget.this.setConfigValue(
            Integer.toString(sliderConfigValue()),
            this.dragging ? SaveMode.MEMORY : SaveMode.DISK);
      }

      private int sliderConfigValue() {
        int minValue = OptionWidget.this.option.minValue();
        int maxValue = LumenConfigScreen.this.sliderMaxValue(OptionWidget.this.option);
        int step = Math.max(1, OptionWidget.this.option.step());
        int rawValue = minValue + (int) Math.round(this.value * (maxValue - minValue));
        int steppedValue = minValue + Math.round((rawValue - minValue) / (float) step) * step;
        return Math.clamp(steppedValue, minValue, maxValue);
      }
    }
  }

  private final class NumericEditBox extends EditBox {
    NumericEditBox(int x, int y, int width, Component narration) {
      super(LumenConfigScreen.this.font, x, y, width, BUTTON_HEIGHT, narration);
    }

    @Override
    public void insertText(String input) {
      super.insertText(input.replaceAll("[^0-9]", ""));
    }
  }

  private double sliderValue(ConfigOption option) {
    int minValue = option.minValue();
    int maxValue = sliderMaxValue(option);
    if (maxValue <= minValue) {
      return 0.0;
    }
    int value = Integer.parseInt(option.getAsString(LumenConfigManager.current()));
    int clampedValue = Math.clamp(value, minValue, maxValue);
    return (clampedValue - minValue) / (double) (maxValue - minValue);
  }

  private int sliderMaxValue(ConfigOption option) {
    return "modules.tooltip.maxWidth".equals(option.path())
        ? Math.clamp(
            Integer.parseInt(option.getAsString(LumenConfigManager.current())),
            Math.min(this.width * 2, option.maxValue()),
            option.maxValue())
        : option.maxValue();
  }

  private record Category(String prefix, String titleKey) {
    boolean contains(String path) {
      return path.startsWith(this.prefix)
          || "controls.".equals(this.prefix) && path.startsWith("modules.itemEditor.");
    }

    Component title() {
      return Component.translatable(this.titleKey);
    }
  }
}
