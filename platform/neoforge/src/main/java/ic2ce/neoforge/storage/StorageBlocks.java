package ic2ce.neoforge.storage;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import ic2ce.neoforge.storage.box.TileEntityBronzeStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIridiumStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIronStorageBox;
import ic2ce.neoforge.storage.box.TileEntitySteelStorageBox;
import ic2ce.neoforge.storage.box.TileEntityWoodenStorageBox;
import ic2ce.neoforge.storage.tank.TileEntityBronzeTank;
import ic2ce.neoforge.storage.tank.TileEntityIridiumTank;
import ic2ce.neoforge.storage.tank.TileEntityIronTank;
import ic2ce.neoforge.storage.tank.TileEntitySteelTank;
import ic2ce.neoforge.world.DirectionSets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/** Storage blocks, staged in declaration order. */
public final class StorageBlocks {
  public static final Block TANK =
      BlockDefinitions.register(
          "tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("tank")))
                  .strength(2.0F, 10.0F)
                  .requiresCorrectToolForDrops()
                  .sound(SoundType.METAL),
              StationaryFluidStore.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.horizontalFacings,
              true));
  public static final Block WOODEN_STORAGE_BOX =
      BlockDefinitions.register(
          "wooden_storage_box",
          MachineBlockType.create(
              BlockProfiles.wood()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "wooden_storage_box")))
                  .strength(1.0F, 10.0F)
                  .sound(SoundType.WOOD),
              TileEntityWoodenStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block IRON_STORAGE_BOX =
      BlockDefinitions.register(
          "iron_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("iron_storage_box")))
                  .strength(1.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityIronStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block BRONZE_STORAGE_BOX =
      BlockDefinitions.register(
          "bronze_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "bronze_storage_box")))
                  .strength(1.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityBronzeStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block STEEL_STORAGE_BOX =
      BlockDefinitions.register(
          "steel_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("steel_storage_box")))
                  .strength(2.0F, 20.0F)
                  .sound(SoundType.METAL),
              TileEntitySteelStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block IRIDIUM_STORAGE_BOX =
      BlockDefinitions.register(
          "iridium_storage_box",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "iridium_storage_box")))
                  .strength(3.0F, 100.0F)
                  .sound(SoundType.METAL),
              TileEntityIridiumStorageBox.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block BRONZE_TANK =
      BlockDefinitions.register(
          "bronze_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("bronze_tank")))
                  .strength(3.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityBronzeTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block IRON_TANK =
      BlockDefinitions.register(
          "iron_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("iron_tank")))
                  .strength(3.0F, 15.0F)
                  .sound(SoundType.METAL),
              TileEntityIronTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block STEEL_TANK =
      BlockDefinitions.register(
          "steel_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("steel_tank")))
                  .strength(4.0F, 20.0F)
                  .sound(SoundType.METAL),
              TileEntitySteelTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));
  public static final Block IRIDIUM_TANK =
      BlockDefinitions.register(
          "iridium_tank",
          MachineBlockType.create(
              BlockProfiles.machine()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("iridium_tank")))
                  .strength(5.0F, 100.0F)
                  .sound(SoundType.METAL),
              TileEntityIridiumTank.class,
              false,
              MachineBlockType.DefaultDrop.Self,
              DirectionSets.noFacings,
              false));

  private StorageBlocks() {}

  public static void init() {}
}
