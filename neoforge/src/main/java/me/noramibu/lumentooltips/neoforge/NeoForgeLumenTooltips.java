package me.noramibu.lumentooltips.neoforge;

import dev.faststats.Metrics;
import dev.faststats.neoforge.NeoForgeContext;
import me.noramibu.lumentooltips.LumenTooltips;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LumenTooltips.MOD_ID)
public final class NeoForgeLumenTooltips {
  private final NeoForgeContext fastStats =
      new NeoForgeContext.Factory(LumenTooltips.MOD_ID, "52824245513b7378c46a48dd8982008b")
          .metrics(Metrics.Factory::create)
          .create();

  public NeoForgeLumenTooltips() {
    LumenTooltips.init(FMLPaths.CONFIGDIR.get());
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void removeAppleSkinFoodTooltip(RenderTooltipEvent.GatherComponents event) {
    if (!LumenConfigManager.current().modules.food.enabled
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
