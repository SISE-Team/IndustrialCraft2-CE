package ic2ce.neoforge.personal;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Personal blocks, staged in declaration order. */
public final class PersonalBlocks {
  public static final Block ENERGY_O_MAT =
      BlockDefinitions.register(
          "energy_o_mat",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("energy_o_mat")))
                  .strength(-1.0F, 3600000.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              EnergyTradeTerminal.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.allFacings,
              false));
  public static final Block PERSONAL_CHEST =
      BlockDefinitions.register(
          "personal_chest",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("personal_chest")))
                  .strength(-1.0F, 3600000.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              PrivateChest.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              false));
  public static final Block TRADE_O_MAT =
      BlockDefinitions.register(
          "trade_o_mat",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("trade_o_mat")))
                  .strength(-1.0F, 3600000.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              ItemTradeTerminal.class,
              true,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              false));

  private PersonalBlocks() {}

  public static void init() {}
}
