package ic2ce.neoforge.storage;

import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.storage.box.TileEntityBronzeStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIridiumStorageBox;
import ic2ce.neoforge.storage.box.TileEntityIronStorageBox;
import ic2ce.neoforge.storage.box.TileEntitySteelStorageBox;
import ic2ce.neoforge.storage.box.TileEntityWoodenStorageBox;
import ic2ce.neoforge.storage.tank.TileEntityBronzeTank;
import ic2ce.neoforge.storage.tank.TileEntityIridiumTank;
import ic2ce.neoforge.storage.tank.TileEntityIronTank;
import ic2ce.neoforge.storage.tank.TileEntitySteelTank;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Storage block entity types, staged in declaration order. */
public final class StorageBlockEntities {
  public static final BlockEntityType<StationaryFluidStore> TANK =
      BlockEntityDefinitions.register("tank", StationaryFluidStore::new, StorageBlocks.TANK);
  public static final BlockEntityType<TileEntityWoodenStorageBox> WOODEN_STORAGE_BOX =
      BlockEntityDefinitions.register(
          "wooden_storage_box",
          TileEntityWoodenStorageBox::new,
          StorageBlocks.WOODEN_STORAGE_BOX);
  public static final BlockEntityType<TileEntityIronStorageBox> IRON_STORAGE_BOX =
      BlockEntityDefinitions.register(
          "iron_storage_box", TileEntityIronStorageBox::new, StorageBlocks.IRON_STORAGE_BOX);
  public static final BlockEntityType<TileEntityBronzeStorageBox> BRONZE_STORAGE_BOX =
      BlockEntityDefinitions.register(
          "bronze_storage_box",
          TileEntityBronzeStorageBox::new,
          StorageBlocks.BRONZE_STORAGE_BOX);
  public static final BlockEntityType<TileEntitySteelStorageBox> STEEL_STORAGE_BOX =
      BlockEntityDefinitions.register(
          "steel_storage_box", TileEntitySteelStorageBox::new, StorageBlocks.STEEL_STORAGE_BOX);
  public static final BlockEntityType<TileEntityIridiumStorageBox> IRIDIUM_STORAGE_BOX =
      BlockEntityDefinitions.register(
          "iridium_storage_box",
          TileEntityIridiumStorageBox::new,
          StorageBlocks.IRIDIUM_STORAGE_BOX);
  public static final BlockEntityType<TileEntityBronzeTank> BRONZE_TANK =
      BlockEntityDefinitions.register("bronze_tank", TileEntityBronzeTank::new, StorageBlocks.BRONZE_TANK);
  public static final BlockEntityType<TileEntityIronTank> IRON_TANK =
      BlockEntityDefinitions.register("iron_tank", TileEntityIronTank::new, StorageBlocks.IRON_TANK);
  public static final BlockEntityType<TileEntitySteelTank> STEEL_TANK =
      BlockEntityDefinitions.register("steel_tank", TileEntitySteelTank::new, StorageBlocks.STEEL_TANK);
  public static final BlockEntityType<TileEntityIridiumTank> IRIDIUM_TANK =
      BlockEntityDefinitions.register("iridium_tank", TileEntityIridiumTank::new, StorageBlocks.IRIDIUM_TANK);

  private StorageBlockEntities() {}

  public static void init() {}
}
