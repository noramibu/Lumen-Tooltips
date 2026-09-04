package me.noramibu.lumentooltips.tooltip.layout;

import java.util.ArrayList;
import java.util.List;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

public final class LumenTooltipLayout {
    private static final int EDGE_MARGIN = 4;
    private static final int SCREEN_PADDING = 16;
    private static final int MIN_TEXT_WIDTH = 40;

    private static int scrollOffset;
    private static int maxScroll;
    private static int lastScreenWidth = -1;
    private static int lastScreenHeight = -1;
    private static int lastMouseX = -1;
    private static int lastMouseY = -1;
    private static int lastTooltipWidth = -1;
    private static int lastTooltipHeight = -1;
    private static boolean tooltipSeenThisFrame;

    private LumenTooltipLayout() {}

    public static @Nullable List<FormattedCharSequence> wrapTextIfNeeded(
            Font font, List<Component> lines, int screenWidth) {
        int maxWidth = maxTextWidth(screenWidth);
        int firstWrapped = 0;
        while (firstWrapped < lines.size() && font.width(lines.get(firstWrapped)) <= maxWidth) {
            firstWrapped++;
        }
        if (firstWrapped == lines.size()) {
            return null;
        }
        List<FormattedCharSequence> wrapped = new ArrayList<>(lines.size());
        for (int index = 0; index < firstWrapped; index++) {
            wrapped.add(lines.get(index).getVisualOrderText());
        }
        wrapped.addAll(font.split(lines.get(firstWrapped), maxWidth));
        for (int index = firstWrapped + 1; index < lines.size(); index++) {
            Component line = lines.get(index);
            if (font.width(line) <= maxWidth) {
                wrapped.add(line.getVisualOrderText());
            } else {
                wrapped.addAll(font.split(line, maxWidth));
            }
        }
        return wrapped;
    }

    public static Vector2ic position(
            Vector2ic vanilla,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            int tooltipWidth,
            int tooltipHeight) {
        LumenConfig.TooltipConfig config = LumenConfigManager.current().modules.tooltip;
        if (!config.edgeFix && !config.scrollLongTooltips) {
            return vanilla;
        }
        if (config.scrollLongTooltips) {
            tooltipSeenThisFrame = true;
            updateScrollBounds(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
        }

        int x = config.edgeFix ? clampedX(vanilla.x(), screenWidth, tooltipWidth) : vanilla.x();
        int y = vanilla.y();
        if (config.scrollLongTooltips && maxScroll > 0) {
            y = EDGE_MARGIN - scrollOffset;
        } else if (config.edgeFix) {
            y = clampedY(y, screenHeight, tooltipHeight);
        }
        return x == vanilla.x() && y == vanilla.y() ? vanilla : new Vector2i(x, y);
    }

    public static boolean scroll(double verticalAmount) {
        LumenConfig.TooltipConfig config = LumenConfigManager.current().modules.tooltip;
        if (!config.scrollLongTooltips || maxScroll <= 0 || verticalAmount == 0.0) {
            return false;
        }
        int delta = verticalAmount < 0.0 ? config.scrollStep : -config.scrollStep;
        int nextOffset = Math.clamp(scrollOffset + delta, 0, maxScroll);
        if (nextOffset == scrollOffset) {
            return false;
        }
        scrollOffset = nextOffset;
        return true;
    }

    public static void finishFrame() {
        if (!tooltipSeenThisFrame && lastScreenWidth != -1) {
            resetScroll();
        }
        tooltipSeenThisFrame = false;
    }

    private static int maxTextWidth(int screenWidth) {
        int availableWidth = Math.max(MIN_TEXT_WIDTH, screenWidth - SCREEN_PADDING);
        int configuredWidth = LumenConfigManager.current().modules.tooltip.maxWidth;
        return configuredWidth == 0 ? availableWidth : configuredWidth;
    }

    private static void updateScrollBounds(
            int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        if (layoutChanged(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight)) {
            scrollOffset = 0;
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            lastTooltipWidth = tooltipWidth;
            lastTooltipHeight = tooltipHeight;
        }

        int visibleHeight = Math.max(1, screenHeight - EDGE_MARGIN * 2);
        maxScroll = tooltipHeight > visibleHeight ? tooltipHeight - visibleHeight : 0;
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
    }

    private static boolean layoutChanged(
            int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        return screenWidth != lastScreenWidth
                || screenHeight != lastScreenHeight
                || mouseX != lastMouseX
                || mouseY != lastMouseY
                || tooltipWidth != lastTooltipWidth
                || tooltipHeight != lastTooltipHeight;
    }

    private static int clampedX(int x, int screenWidth, int tooltipWidth) {
        int maxX = screenWidth - tooltipWidth - EDGE_MARGIN;
        return maxX < EDGE_MARGIN ? EDGE_MARGIN : Math.clamp(x, EDGE_MARGIN, maxX);
    }

    private static int clampedY(int y, int screenHeight, int tooltipHeight) {
        int maxY = screenHeight - tooltipHeight - EDGE_MARGIN;
        return maxY < EDGE_MARGIN ? EDGE_MARGIN : Math.clamp(y, EDGE_MARGIN, maxY);
    }

    private static void resetScroll() {
        scrollOffset = 0;
        maxScroll = 0;
        lastScreenWidth = -1;
        lastScreenHeight = -1;
        lastMouseX = -1;
        lastMouseY = -1;
        lastTooltipWidth = -1;
        lastTooltipHeight = -1;
    }
}
