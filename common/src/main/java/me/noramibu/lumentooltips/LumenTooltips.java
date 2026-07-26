package me.noramibu.lumentooltips;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import me.noramibu.lumentooltips.service.LumenUsageReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class LumenTooltips {
  public static final String MOD_ID = "lumen_tooltips";
  private static Platform platform =
      new Platform(
          Function.identity(), stack -> 0, stack -> -1.0, block -> 0.0, () -> "", () -> "");

  private LumenTooltips() {}

  public static void init(Path configDirectory, Platform platform) {
    LumenTooltips.platform = platform;
    LumenConfigManager.load(configDirectory);
    LumenUsageReporter.initialize();
  }

  public static String modName(String namespace) {
    return platform.modName().apply(namespace);
  }

  public static int fuelTime(ItemStack stack) {
    return platform.fuelTime().applyAsInt(stack);
  }

  public static float compostChance(ItemStack stack) {
    return (float) platform.compostChance().applyAsDouble(stack);
  }

  public static float blastResistance(Block block) {
    return (float) platform.blastResistance().applyAsDouble(block);
  }

  public static String modVersion() {
    return platform.modVersion().get();
  }

  public static String minecraftVersion() {
    return platform.minecraftVersion().get();
  }

  public static int buildNumber(String version) {
    int end = version.length();
    int start = end;
    while (start > 0 && Character.isDigit(version.charAt(start - 1))) {
      start--;
    }
    try {
      return start == end ? 0 : Integer.parseInt(version.substring(start, end));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  public record Platform(
      Function<String, String> modName,
      ToIntFunction<ItemStack> fuelTime,
      ToDoubleFunction<ItemStack> compostChance,
      ToDoubleFunction<Block> blastResistance,
      Supplier<String> modVersion,
      Supplier<String> minecraftVersion) {}
}
