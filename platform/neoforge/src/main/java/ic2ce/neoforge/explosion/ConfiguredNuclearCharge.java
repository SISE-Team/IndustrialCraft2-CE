package ic2ce.neoforge.explosion;

import ic2ce.neoforge.item.consumable.ConsumableItems;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.engine.explosion.NuclearCharge;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.world.WorldItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Real detonator and isotope inventories select distinct threshold, power and radiation profiles.
 */
public class ConfiguredNuclearCharge extends NuclearBlockEntity implements IHasGui {
  public int RadiationRange;
  public final ConsumableInventory outsideSlot, insideSlot;

  public ConfiguredNuclearCharge(BlockPos position, BlockState state) {
    super(ExplosionBlockEntities.NUKE, position, state);
    outsideSlot = new ItemWhitelistInput(this, "outsideSlot", 1, ExplosionItems.ITNT);
    insideSlot =
        new ItemWhitelistInput(
            this,
            "insideSlot",
            1,
            WorldItems.URANIUM_BLOCK,
            ConsumableItems.URANIUM_238,
            ConsumableItems.URANIUM_235,
            ConsumableItems.SMALL_URANIUM_235,
            ConsumableItems.PLUTONIUM,
            ConsumableItems.SMALL_PLUTONIUM);
  }

  @Override
  public int getRadiationRange() {
    return RadiationRange;
  }

  public void setRadiationRange(int range) {
    RadiationRange = Math.max(0, range);
  }

  @Override
  public float getNukeExplosivePower() {
    var isotope = insideSlot.get();
    var payload =
        isotope.isEmpty()
            ? NuclearCharge.Payload.NONE
            : isotope.is(ConsumableItems.URANIUM_238)
                ? NuclearCharge.Payload.URANIUM_238
                : isotope.is(WorldItems.URANIUM_BLOCK)
                    ? NuclearCharge.Payload.URANIUM_BLOCK
                    : isotope.is(ConsumableItems.SMALL_URANIUM_235)
                        ? NuclearCharge.Payload.SMALL_URANIUM_235
                        : isotope.is(ConsumableItems.URANIUM_235)
                            ? NuclearCharge.Payload.URANIUM_235
                            : isotope.is(ConsumableItems.SMALL_PLUTONIUM)
                                ? NuclearCharge.Payload.SMALL_PLUTONIUM
                                : isotope.is(ConsumableItems.PLUTONIUM)
                                    ? NuclearCharge.Payload.PLUTONIUM
                                    : NuclearCharge.Payload.NONE;
    var profile =
        NuclearCharge.evaluate(
            outsideSlot.isEmpty() ? 0 : outsideSlot.get().getCount(),
            payload,
            isotope.getCount(),
            NativeConfigValues.getFloat(
                ProjectConfiguration.get(), "protection/nukeExplosionPowerLimit"));
    RadiationRange = profile.radiationRange();
    return profile.power();
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return DocumentMenu.create(id, actor.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }

  @Override
  protected void onIgnite(LivingEntity igniter) {
    super.onIgnite(igniter);
    outsideSlot.clear();
    insideSlot.clear();
  }
}
