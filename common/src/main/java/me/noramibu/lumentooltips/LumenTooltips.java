package me.noramibu.lumentooltips;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class LumenTooltips {
  public static final String MOD_ID = "lumen_tooltips";
  private static Platform platform =
      new Platform(Function.identity(), stack -> 0, stack -> -1.0, block -> 0.0);

  private LumenTooltips() {}

  public static void init(Path configDirectory, Platform platform) {
    LumenTooltips.platform = platform;
    LumenConfigManager.load(configDirectory);
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

  public record Platform(
      Function<String, String> modName,
      ToIntFunction<ItemStack> fuelTime,
      ToDoubleFunction<ItemStack> compostChance,
      ToDoubleFunction<Block> blastResistance) {}
}
