package ic2ce.neoforge.energy;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.ItemDefinitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/** Energy items, staged in declaration order. */
public final class EnergyItems {
   public static final Item BATBOX_CHARGEPAD = ItemDefinitions.register(
      "batbox_chargepad", new BlockItem(EnergyBlocks.BATBOX_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("batbox_chargepad"))))
   );
   public static final Item CESU_CHARGEPAD = ItemDefinitions.register(
      "cesu_chargepad", new BlockItem(EnergyBlocks.CESU_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cesu_chargepad"))))
   );
   public static final Item MFE_CHARGEPAD = ItemDefinitions.register(
      "mfe_chargepad", new BlockItem(EnergyBlocks.MFE_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mfe_chargepad"))))
   );
   public static final Item MFSU_CHARGEPAD = ItemDefinitions.register(
      "mfsu_chargepad", new BlockItem(EnergyBlocks.MFSU_CHARGEPAD, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mfsu_chargepad"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item BATBOX = ItemDefinitions.register("batbox", new BlockItem(EnergyBlocks.BATBOX, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("batbox")))));
   public static final Item CESU = ItemDefinitions.register("cesu", new BlockItem(EnergyBlocks.CESU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("cesu")))));
   public static final Item MFE = ItemDefinitions.register("mfe", new BlockItem(EnergyBlocks.MFE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mfe")))));
   public static final Item CLASSIC_MFE = ItemDefinitions.register(
      "classic_mfe", new BlockItem(EnergyBlocks.CLASSIC_MFE, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_mfe"))))
   );
   public static final Item MFSU = ItemDefinitions.register(
      "mfsu", new BlockItem(EnergyBlocks.MFSU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mfsu"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item CLASSIC_MFSU = ItemDefinitions.register(
      "classic_mfsu", new BlockItem(EnergyBlocks.CLASSIC_MFSU, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("classic_mfsu"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item LV_TRANSFORMER = ItemDefinitions.register(
      "lv_transformer", new BlockItem(EnergyBlocks.LV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("lv_transformer"))))
   );
   public static final Item MV_TRANSFORMER = ItemDefinitions.register(
      "mv_transformer", new BlockItem(EnergyBlocks.MV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("mv_transformer"))))
   );
   public static final Item HV_TRANSFORMER = ItemDefinitions.register(
      "hv_transformer", new BlockItem(EnergyBlocks.HV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("hv_transformer"))).rarity(Rarity.UNCOMMON))
   );
   public static final Item EV_TRANSFORMER = ItemDefinitions.register(
      "ev_transformer", new BlockItem(EnergyBlocks.EV_TRANSFORMER, new Properties().useBlockDescriptionPrefix().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ProjectIds.id("ev_transformer"))).rarity(Rarity.UNCOMMON))
   );

   private EnergyItems() {}

   public static void init() {}
}
