package me.noramibu.lumentooltips.tooltip;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import me.noramibu.lumentooltips.config.LumenConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class LumenNavigationTooltip {
  private static final int MAP_SIZE = 128;
  private static final List<String> MAP_DIRECTIONS =
      List.of(
          "north", "northeast", "east", "southeast",
          "south", "southwest", "west", "northwest");
  private static final List<String> BEARINGS =
      List.of(
          "n", "nne", "ne", "ene", "e", "ese", "se", "sse",
          "s", "ssw", "sw", "wsw", "w", "wnw", "nw", "nnw");
  private static final Map<MapItemSavedData, MapStats> mapStats = new WeakHashMap<>();

  private LumenNavigationTooltip() {}

  static void append(
      ItemStack stack,
      Player player,
      List<Component> tooltip,
      LumenConfig.NavigationConfig config) {
    if (config.maps) {
      appendMap(stack, tooltip);
    }
    if (!config.compasses || player == null) {
      return;
    }
    GlobalPos target = target(stack, player);
    if (target != null) {
      appendTarget(target, player, tooltip);
    }
  }

  public static void invalidateMap(MapId id) {
    ClientLevel level = Minecraft.getInstance().level;
    if (level != null) {
      mapStats.remove(level.getMapData(id));
    }
  }

  private static void appendMap(ItemStack stack, List<Component> tooltip) {
    MapId id = stack.get(DataComponents.MAP_ID);
    ClientLevel level = Minecraft.getInstance().level;
    if (id == null || level == null) {
      return;
    }
    MapItemSavedData data = level.getMapData(id);
    if (data == null) {
      return;
    }
    MapStats stats = mapStats.computeIfAbsent(data, LumenNavigationTooltip::analyzeMap);
    tooltip.add(line("map_scale", 1 << stats.scale()));
    tooltip.add(line("map_explored", stats.exploredPercent()));
    if (stats.outsideDirection() != null) {
      tooltip.add(
          line("map_outside", directionComponent(stats.outsideDirection())));
    }
    if (stats.nearestUnexploredDirection() != null) {
      tooltip.add(
          line(
              "map_nearest_unexplored",
              directionComponent(stats.nearestUnexploredDirection())));
    }
  }

  private static MapStats analyzeMap(MapItemSavedData data) {
    MapDecoration playerMarker = uniquePlayerMarker(data);
    int sourceX = playerMarker == null ? MAP_SIZE / 2 : mapCoordinate(playerMarker.x());
    int sourceZ = playerMarker == null ? MAP_SIZE / 2 : mapCoordinate(playerMarker.y());
    String outside =
        playerMarker != null && isOutsideMarker(playerMarker)
            ? mapDirection(playerMarker.x(), playerMarker.y())
            : null;
    return analyzePixels(data.scale, data.colors, sourceX, sourceZ, outside);
  }

  private static MapStats analyzePixels(
      byte scale, byte[] colors, int sourceX, int sourceZ, String outside) {
    int explored = 0;
    int nearestDistance = Integer.MAX_VALUE;
    int nearestX = 0;
    int nearestZ = 0;
    for (int index = 0; index < colors.length; index++) {
      if (colors[index] != 0) {
        explored++;
        continue;
      }
      int dx = index % MAP_SIZE - sourceX;
      int dz = index / MAP_SIZE - sourceZ;
      int distance = dx * dx + dz * dz;
      if (distance > 0 && distance < nearestDistance) {
        nearestDistance = distance;
        nearestX = dx;
        nearestZ = dz;
      }
    }
    String unexplored =
        nearestDistance == Integer.MAX_VALUE ? null : mapDirection(nearestX, nearestZ);
    return new MapStats(
        scale,
        explored * 100 / colors.length,
        unexplored,
        outside);
  }

  private static MapDecoration uniquePlayerMarker(MapItemSavedData data) {
    MapDecoration result = null;
    for (MapDecoration decoration : data.getDecorations()) {
      if (!isPlayerMarker(decoration)) {
        continue;
      }
      if (result != null) {
        return null;
      }
      result = decoration;
    }
    return result;
  }

  private static boolean isPlayerMarker(MapDecoration decoration) {
    return decoration.type().equals(MapDecorationTypes.PLAYER)
        || isOutsideMarker(decoration);
  }

  private static boolean isOutsideMarker(MapDecoration decoration) {
    return decoration.type().equals(MapDecorationTypes.PLAYER_OFF_MAP)
        || decoration.type().equals(MapDecorationTypes.PLAYER_OFF_LIMITS);
  }

  private static int mapCoordinate(byte coordinate) {
    return Math.clamp(Math.round(coordinate / 2.0F) + MAP_SIZE / 2, 0, MAP_SIZE - 1);
  }

  private static GlobalPos target(ItemStack stack, Player player) {
    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
    if (tracker != null) {
      return tracker.target().orElse(null);
    }
    return stack.is(Items.RECOVERY_COMPASS)
        ? player.getLastDeathLocation().orElse(null)
        : null;
  }

  private static void appendTarget(GlobalPos target, Player player, List<Component> tooltip) {
    Component dimension =
        Component.translatable(target.dimension().identifier().toLanguageKey("dimension"));
    tooltip.add(line("target_dimension", dimension));
    tooltip.add(
        line(
            "target_coordinates",
            target.pos().getX(),
            target.pos().getY(),
            target.pos().getZ()));
    if (!target.dimension().equals(player.level().dimension())) {
      tooltip.add(line("inactive_dimension"));
      return;
    }
    double dx = target.pos().getX() + 0.5 - player.getX();
    double dz = target.pos().getZ() + 0.5 - player.getZ();
    long distance = Math.round(Math.hypot(dx, dz));
    tooltip.add(line("distance", distance));
    if (distance > 0) {
      tooltip.add(
          line(
              "bearing",
              Component.translatable(
                  "tooltip.lumen_tooltips.navigation.bearing." + bearing(dx, dz))));
    }
  }

  private static String mapDirection(double dx, double dz) {
    return direction(dx, dz, MAP_DIRECTIONS);
  }

  private static String bearing(double dx, double dz) {
    return direction(dx, dz, BEARINGS);
  }

  private static String direction(double dx, double dz, List<String> directions) {
    int index =
        Math.floorMod(
            (int)
                Math.round(
                    Math.atan2(dx, -dz) * directions.size() / (Math.PI * 2.0)),
            directions.size());
    return directions.get(index);
  }

  private static Component directionComponent(String direction) {
    return Component.translatable(
        "tooltip.lumen_tooltips.navigation.direction." + direction);
  }

  private static Component line(String key, Object... arguments) {
    return Component.translatable(
            "tooltip.lumen_tooltips.navigation." + key, arguments)
        .withStyle(ChatFormatting.GRAY);
  }

  private record MapStats(
      byte scale,
      int exploredPercent,
      String nearestUnexploredDirection,
      String outsideDirection) {}
}
