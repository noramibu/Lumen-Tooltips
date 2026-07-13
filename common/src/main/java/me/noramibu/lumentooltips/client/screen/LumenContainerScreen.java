package me.noramibu.lumentooltips.client.screen;

import java.util.List;
import me.noramibu.lumentooltips.client.LumenItemEditor;
import me.noramibu.lumentooltips.tooltip.preview.LumenContainerContents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.jspecify.annotations.Nullable;

public final class LumenContainerScreen extends ContainerScreen {
  private static final int COLUMNS = 9;
  private static final int MAX_ROWS = 6;
  private static final Component SEPARATOR =
      Component.literal(" > ").withStyle(ChatFormatting.DARK_GRAY);
  private final Screen parent;
  private final Component breadcrumb;
  private final ItemStack containerItem;
  private @Nullable PageButton backButton;

  LumenContainerScreen(
      ItemStack containerItem,
      List<ItemStack> containerItems,
      Inventory playerInventory,
      Screen parent) {
    super(
        createMenu(containerItems, playerInventory),
        playerInventory,
        containerItem.getHoverName());
    this.parent = parent;
    this.containerItem = containerItem.copy();
    this.breadcrumb =
        parent instanceof LumenContainerScreen container
            ? container.breadcrumb.copy().append(SEPARATOR).append(containerItem.getHoverName())
            : containerItem.getHoverName();
  }

  @Override
  protected void init() {
    super.init();
    if (this.parent instanceof LumenContainerScreen) {
      this.backButton =
          this.addRenderableWidget(
              new PageButton(
                  this.leftPos + this.imageWidth - 29,
                  this.topPos + 5,
                  false,
                  button -> onClose(),
                  false));
      this.backButton.setTooltip(
          Tooltip.create(Component.translatable("screen.lumen_tooltips.container.back")));
    }
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    Component label =
        this.parent instanceof LumenContainerScreen
            ? breadcrumbLabel(this.imageWidth - this.titleLabelX - 35)
            : this.title;
    graphics.text(this.font, label, this.titleLabelX, this.titleLabelY, -12566464, false);
    graphics.text(
        this.font,
        this.playerInventoryTitle,
        this.inventoryLabelX,
        this.inventoryLabelY,
        -12566464,
        false);
  }

  private Component breadcrumbLabel(int maxWidth) {
    if (this.font.width(this.breadcrumb) <= maxWidth) {
      return this.breadcrumb;
    }
    String prefix = "...";
    return Component.literal(
        prefix
            + this.font.plainSubstrByWidth(
                this.breadcrumb.getString(), maxWidth - this.font.width(prefix), true));
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    return this.backButton != null && this.backButton.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    return this.backButton != null && this.backButton.mouseReleased(event);
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    if (this.hoveredSlot != null) {
      ItemStack stack = this.hoveredSlot.getItem();
      if (LumenItemEditor.trySaveToStorage(stack, event)
          || LumenContainerOpener.tryOpen(stack, event)) {
        return true;
      }
    }
    if (event.key() == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.matches(event)) {
      onClose();
      return true;
    }
    return false;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void onClose() {
    this.minecraft
        .getSoundManager()
        .play(SimpleSoundInstance.forUI(closeSound(), 1.0F, 0.5F));
    this.minecraft.setScreen(this.parent);
  }

  private SoundEvent closeSound() {
    if (this.containerItem.is(Items.ENDER_CHEST)) {
      return SoundEvents.ENDER_CHEST_CLOSE;
    }
    return LumenContainerContents.isShulker(this.containerItem)
        ? SoundEvents.SHULKER_BOX_CLOSE
        : SoundEvents.CHEST_CLOSE;
  }

  private static ChestMenu createMenu(List<ItemStack> items, Inventory playerInventory) {
    int rows = Math.clamp((items.size() + COLUMNS - 1) / COLUMNS, 1, MAX_ROWS);
    Container container = new SimpleContainer(rows * COLUMNS);
    for (int index = 0; index < Math.min(items.size(), container.getContainerSize()); index++) {
      container.setItem(index, items.get(index));
    }
    return new ChestMenu(menuType(rows), 0, playerInventory, container, rows);
  }

  private static MenuType<ChestMenu> menuType(int rows) {
    return switch (rows) {
      case 1 -> MenuType.GENERIC_9x1;
      case 2 -> MenuType.GENERIC_9x2;
      case 3 -> MenuType.GENERIC_9x3;
      case 4 -> MenuType.GENERIC_9x4;
      case 5 -> MenuType.GENERIC_9x5;
      case 6 -> MenuType.GENERIC_9x6;
      default -> throw new IllegalArgumentException("Unsupported row count: " + rows);
    };
  }
}
