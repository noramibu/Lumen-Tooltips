package me.noramibu.lumentooltips.neoforge;

import me.noramibu.lumentooltips.LumenTooltips;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LumenTooltips.MOD_ID)
public final class NeoForgeLumenTooltips {

  public NeoForgeLumenTooltips() {
    LumenTooltips.init(
        FMLPaths.CONFIGDIR.get(),
        new LumenTooltips.Platform(
            namespace ->
                ModList.get()
                    .getModContainerById(namespace)
                    .map(mod -> mod.getModInfo().getDisplayName())
                    .orElse(namespace),
            stack -> {
              var level = Minecraft.getInstance().level;
              return level == null ? 0 : stack.getBurnTime(null, level.fuelValues());
            },
            ComposterBlock::getValue,
            NeoForgeLumenTooltips::baseBlastResistance,
            () -> modVersion(LumenTooltips.MOD_ID),
            () -> modVersion("minecraft")));
    NeoForge.EVENT_BUS.register(this);
  }

  private static String modVersion(String modId) {
    return ModList.get()
        .getModContainerById(modId)
        .map(mod -> mod.getModInfo().getVersion().toString())
        .orElse("");
  }

  @SuppressWarnings("deprecation")
  private static float baseBlastResistance(Block block) {
    return block.getExplosionResistance();
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void removeAppleSkinFoodTooltip(RenderTooltipEvent.GatherComponents event) {
    var food = LumenConfigManager.current().modules.food;
    if (!food.enabled
        || !(food.showHunger || food.showSaturation)
        || event.getItemStack().get(DataComponents.FOOD) == null) {
      return;
    }
    event
        .getTooltipElements()
        .removeIf(
            element ->
                element
                    .map(
                        text -> false,
                        component ->
                            component
                                .getClass()
                                .getName()
                                .equals(
                                    "squeek.appleskin.client.TooltipOverlayHandler$FoodTooltip")));
  }
}
