package ic2ce.neoforge.item.armor;

import ic2ce.neoforge.registry.ArmorProfiles;
import ic2ce.neoforge.registry.FluidDefinitions;
import net.minecraft.world.item.ItemStack;

/**
 * Construction foam reservoir holds eighty buckets and does not implement a propulsion interface.
 */
public class FoamReservoir extends WearableFluidReservoir
    implements ic2ce.api.item.CreativeVariants {
  public FoamReservoir(Properties properties) {
    super(ArmorProfiles.CF_PACK, properties, FluidDefinitions.CONSTRUCTION_FOAM.still, 80_000);
  }

  public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
    var full = new ItemStack(this);
    filltank(full);
    output.accept(full);
    output.accept(new ItemStack(this));
  }
}
