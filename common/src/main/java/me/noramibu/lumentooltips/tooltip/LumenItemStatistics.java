package me.noramibu.lumentooltips.tooltip;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public final class LumenItemStatistics {
    private LumenItemStatistics() {}

    public static int fuelTime(ItemStack stack) {
        var fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null) return 0;
        return fuel.burnTime() instanceof ResolvableInt.Constant constant ? constant.value() : -1;
    }

    public static float compostChance(ItemStack stack) {
        var compost = stack.get(DataComponents.COMPOSTABLE);
        if (compost == null) return 0;
        return compost.layers() instanceof ResolvableInt.Constant constant ? (constant.value() > 0 ? 1.0F : 0.0F) : -1;
    }

    static int vanillaDefault(ResolvableInt value, String category) {
        if (!(value instanceof ResolvableInt.Reference reference)
                || !reference.key().identifier().getNamespace().equals("minecraft")
                || !reference.key().identifier().getPath().startsWith(category)) return -1;
        return switch (reference.key().identifier().getPath()) {
            case "compostable/low" -> 30;
            case "compostable/low_medium" -> 50;
            case "compostable/medium" -> 65;
            case "compostable/medium_high" -> 85;
            case "compostable/always_add_one" -> 100;
            case "cooking/time_bamboo", "cooking/time_wool_slabs" -> 50;
            case "cooking/time_wool_carpets" -> 67;
            case "cooking/time_dry_plants", "cooking/time_wood_items_extra_small", "cooking/time_wool" -> 100;
            case "cooking/time_wood_slabs" -> 150;
            case "cooking/time_wood_items_large" -> 200;
            case "cooking/time_roots", "cooking/time_wood_blocks", "cooking/time_wood_items_small" -> 300;
            case "cooking/time_hanging_signs" -> 800;
            case "cooking/time_boats" -> 1200;
            case "cooking/time_coal" -> 1600;
            case "cooking/time_blaze_rod" -> 2400;
            case "cooking/time_dried_kelp_block" -> 4001;
            case "cooking/time_coal_block" -> 16000;
            case "cooking/time_lava_bucket" -> 20000;
            default -> -1;
        };
    }
}
