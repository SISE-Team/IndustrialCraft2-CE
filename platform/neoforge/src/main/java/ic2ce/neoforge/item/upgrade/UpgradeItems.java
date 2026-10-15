package ic2ce.neoforge.item.upgrade;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/** Upgrade items, staged in declaration order. */
public final class UpgradeItems {
   public static final Item OVERCLOCKER_UPGRADE = ItemDefinitions.register(
      "overclocker_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("overclocker_upgrade"))), MachineUpgrade.UpgradeType.overclocker)
   );
   public static final Item TRANSFORMER_UPGRADE = ItemDefinitions.register(
      "transformer_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("transformer_upgrade"))), MachineUpgrade.UpgradeType.transformer)
   );
   public static final Item ENERGY_STORAGE_UPGRADE = ItemDefinitions.register(
      "energy_storage_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("energy_storage_upgrade"))), MachineUpgrade.UpgradeType.energy_storage)
   );
   public static final Item REDSTONE_INVERTER_UPGRADE = ItemDefinitions.register(
      "redstone_inverter_upgrade",
      new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("redstone_inverter_upgrade"))), MachineUpgrade.UpgradeType.redstone_inverter)
   );
   public static final Item EJECTOR_UPGRADE = ItemDefinitions.register(
      "ejector_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("ejector_upgrade"))), MachineUpgrade.UpgradeType.ejector)
   );
   public static final Item ADVANCED_EJECTOR_UPGRADE = ItemDefinitions.register(
      "advanced_ejector_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_ejector_upgrade"))), MachineUpgrade.UpgradeType.advanced_ejector)
   );
   public static final Item PULLING_UPGRADE = ItemDefinitions.register(
      "pulling_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("pulling_upgrade"))), MachineUpgrade.UpgradeType.pulling)
   );
   public static final Item ADVANCED_PULLING_UPGRADE = ItemDefinitions.register(
      "advanced_pulling_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("advanced_pulling_upgrade"))), MachineUpgrade.UpgradeType.advanced_pulling)
   );
   public static final Item FLUID_EJECTOR_UPGRADE = ItemDefinitions.register(
      "fluid_ejector_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fluid_ejector_upgrade"))), MachineUpgrade.UpgradeType.fluid_ejector)
   );
   public static final Item FLUID_PULLING_UPGRADE = ItemDefinitions.register(
      "fluid_pulling_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("fluid_pulling_upgrade"))), MachineUpgrade.UpgradeType.fluid_pulling)
   );
   public static final Item REMOTE_INTERFACE_UPGRADE = ItemDefinitions.register(
      "remote_interface_upgrade", new MachineUpgrade(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("remote_interface_upgrade"))), MachineUpgrade.UpgradeType.remote_interface)
   );

   private UpgradeItems() {}

   public static void init() {}
}
