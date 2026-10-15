package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.agriculture.AgricultureItems;
import ic2ce.neoforge.cable.CableItems;
import ic2ce.neoforge.crafting.CraftingItems;
import ic2ce.neoforge.energy.EnergyItems;
import ic2ce.neoforge.explosion.ExplosionItems;
import ic2ce.neoforge.item.armor.ArmorItems;
import ic2ce.neoforge.item.consumable.ConsumableItems;
import ic2ce.neoforge.item.container.ContainerItems;
import ic2ce.neoforge.item.energy.BatteryItems;
import ic2ce.neoforge.item.machining.MachiningItems;
import ic2ce.neoforge.item.tool.ToolItems;
import ic2ce.neoforge.item.upgrade.UpgradeItems;
import ic2ce.neoforge.kinetic.KineticItems;
import ic2ce.neoforge.material.MaterialItems;
import ic2ce.neoforge.mining.MiningItems;
import ic2ce.neoforge.personal.PersonalItems;
import ic2ce.neoforge.power.PowerItems;
import ic2ce.neoforge.processing.ProcessingItems;
import ic2ce.neoforge.reactor.ReactorItems;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.storage.StorageItems;
import ic2ce.neoforge.terrain.TerrainItems;
import ic2ce.neoforge.thermal.ThermalItems;
import ic2ce.neoforge.transport.TransportItems;
import ic2ce.neoforge.utility.UtilityItems;
import ic2ce.neoforge.vehicle.VehicleItems;
import ic2ce.neoforge.world.WorldItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ItemDefinitions {
   /** Loads every domain item holder after all blocks; fuel values follow once all items exist. */
   public static void init() {
      WorldItems.init();
      MiningItems.init();
      UtilityItems.init();
      ExplosionItems.init();
      CableItems.init();
      EnergyItems.init();
      StorageItems.init();
      ProcessingItems.init();
      TransportItems.init();
      ReactorItems.init();
      CraftingItems.init();
      PowerItems.init();
      ThermalItems.init();
      KineticItems.init();
      TerrainItems.init();
      AgricultureItems.init();
      PersonalItems.init();
      MaterialItems.init();
      ConsumableItems.init();
      MachiningItems.init();
      ToolItems.init();
      ContainerItems.init();
      VehicleItems.init();
      BatteryItems.init();
      UpgradeItems.init();
      ArmorItems.init();
      EnvProxy.get().registerBurnTime(Items.SUGAR_CANE, 50);
      EnvProxy.get().registerBurnTime(Items.CACTUS, 50);
      EnvProxy.get().registerBurnTime(WorldItems.RUBBER_SAPLING, 80);
      EnvProxy.get().registerBurnTime(WorldItems.WOODEN_SCAFFOLD, 300);
      EnvProxy.get().registerBurnTime(StorageItems.WOODEN_STORAGE_BOX, 1200);
      EnvProxy.get().registerBurnTime(MaterialItems.WOODEN_ROTOR_BLADE, 300);
      EnvProxy.get().registerBurnTime(KineticItems.WOODEN_ROTOR, 300);
      EnvProxy.get().registerBurnTime(MaterialItems.SCRAP, 350);
      EnvProxy.get().registerBurnTime(MaterialItems.SCRAP_BOX, 3150);
   }

   public static <T extends Item> T register(String name, T item) {
      EnvProxy.get().registerItem(ProjectIds.id(name), item);
      return item;
   }
}
