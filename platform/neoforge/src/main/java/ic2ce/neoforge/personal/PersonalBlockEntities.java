package ic2ce.neoforge.personal;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Personal block entity types, staged in declaration order. */
public final class PersonalBlockEntities {
  public static final BlockEntityType<EnergyTradeTerminal> ENERGY_O_MAT =
      BlockEntityDefinitions.register("energy_o_mat", EnergyTradeTerminal::new, PersonalBlocks.ENERGY_O_MAT);
  public static final BlockEntityType<PrivateChest> PERSONAL_CHEST =
      BlockEntityDefinitions.register("personal_chest", PrivateChest::new, PersonalBlocks.PERSONAL_CHEST);
  public static final BlockEntityType<ItemTradeTerminal> TRADE_O_MAT =
      BlockEntityDefinitions.register("trade_o_mat", ItemTradeTerminal::new, PersonalBlocks.TRADE_O_MAT);

  private PersonalBlockEntities() {}

  public static void init() {}
}
