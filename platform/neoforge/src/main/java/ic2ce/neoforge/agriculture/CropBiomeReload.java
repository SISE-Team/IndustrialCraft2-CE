package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.Crops;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/**
 * Biome tags can change while native holders stay alive; invalidate both logical sides after tag
 * publication.
 */
@EventBusSubscriber(modid = "ic2")
public final class CropBiomeReload {
  private CropBiomeReload() {}

  @SubscribeEvent
  public static void tagsUpdated(TagsUpdatedEvent event) {
    if (Crops.instance instanceof CropCatalog catalog) catalog.invalidateBiomeCache();
  }
}
