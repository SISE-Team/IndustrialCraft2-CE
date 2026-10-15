package ic2ce.neoforge.cable;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockProfiles;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

/** Cable blocks, staged in declaration order. */
public final class CableBlocks {
  private static final java.util.function.Supplier<Properties> cableSettings =
      () -> Properties.of().mapColor(MapColor.METAL).strength(0.5F, 5.0F).sound(SoundType.METAL);
  private static final java.util.function.Supplier<Properties> insulatedCableSettings =
      () -> BlockProfiles.wool().strength(0.5F, 5.0F).sound(SoundType.WOOL);
  public static final FoamConductorBlock COPPER_FOAM_CABLE =
      BlockDefinitions.register(
          "copper_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(cableSettings, "copper_foam_cable"), ConductorMaterial.copper, 0));
  public static final Block COPPER_CABLE =
      BlockDefinitions.register(
          "copper_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(cableSettings, "copper_cable"),
              ConductorMaterial.copper,
              0,
              COPPER_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_COPPER_FOAM_CABLE =
      BlockDefinitions.register(
          "insulated_copper_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_copper_foam_cable"),
              ConductorMaterial.copper,
              1));
  public static final Block INSULATED_COPPER_CABLE =
      BlockDefinitions.register(
          "insulated_copper_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_copper_cable"),
              ConductorMaterial.copper,
              1,
              INSULATED_COPPER_FOAM_CABLE));
  public static final FoamConductorBlock GLASS_FIBRE_FOAM_CABLE =
      BlockDefinitions.register(
          "glass_fibre_foam_cable",
          FoamConductorBlock.create(
              BlockProfiles.glass()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id(
                              "glass_fibre_foam_cable")))
                  .strength(0.5F, 5.0F)
                  .sound(SoundType.GLASS),
              ConductorMaterial.glass,
              0));
  public static final Block GLASS_FIBRE_CABLE =
      BlockDefinitions.register(
          "glass_fibre_cable",
          BareCableBlock.create(
              BlockProfiles.glass()
                  .setId(
                      net.minecraft.resources.ResourceKey.create(
                          net.minecraft.core.registries.Registries.BLOCK,
                          ProjectIds.id("glass_fibre_cable")))
                  .strength(0.5F, 5.0F)
                  .sound(SoundType.GLASS),
              ConductorMaterial.glass,
              0,
              GLASS_FIBRE_FOAM_CABLE));
  public static final FoamConductorBlock GOLD_FOAM_CABLE =
      BlockDefinitions.register(
          "gold_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(cableSettings, "gold_foam_cable"), ConductorMaterial.gold, 0));
  public static final Block GOLD_CABLE =
      BlockDefinitions.register(
          "gold_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(cableSettings, "gold_cable"), ConductorMaterial.gold, 0, GOLD_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_GOLD_FOAM_CABLE =
      BlockDefinitions.register(
          "insulated_gold_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_gold_foam_cable"),
              ConductorMaterial.gold,
              1));
  public static final Block INSULATED_GOLD_CABLE =
      BlockDefinitions.register(
          "insulated_gold_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_gold_cable"),
              ConductorMaterial.gold,
              1,
              INSULATED_GOLD_FOAM_CABLE));
  public static final FoamConductorBlock DOUBLE_INSULATED_GOLD_FOAM_CABLE =
      BlockDefinitions.register(
          "double_insulated_gold_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "double_insulated_gold_foam_cable"),
              ConductorMaterial.gold,
              2));
  public static final Block DOUBLE_INSULATED_GOLD_CABLE =
      BlockDefinitions.register(
          "double_insulated_gold_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "double_insulated_gold_cable"),
              ConductorMaterial.gold,
              2,
              DOUBLE_INSULATED_GOLD_FOAM_CABLE));
  public static final FoamConductorBlock IRON_FOAM_CABLE =
      BlockDefinitions.register(
          "iron_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(cableSettings, "iron_foam_cable"), ConductorMaterial.iron, 0));
  public static final Block IRON_CABLE =
      BlockDefinitions.register(
          "iron_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(cableSettings, "iron_cable"), ConductorMaterial.iron, 0, IRON_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_IRON_FOAM_CABLE =
      BlockDefinitions.register(
          "insulated_iron_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_iron_foam_cable"),
              ConductorMaterial.iron,
              1));
  public static final Block INSULATED_IRON_CABLE =
      BlockDefinitions.register(
          "insulated_iron_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_iron_cable"),
              ConductorMaterial.iron,
              1,
              INSULATED_IRON_FOAM_CABLE));
  public static final FoamConductorBlock DOUBLE_INSULATED_IRON_FOAM_CABLE =
      BlockDefinitions.register(
          "double_insulated_iron_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "double_insulated_iron_foam_cable"),
              ConductorMaterial.iron,
              2));
  public static final Block DOUBLE_INSULATED_IRON_CABLE =
      BlockDefinitions.register(
          "double_insulated_iron_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "double_insulated_iron_cable"),
              ConductorMaterial.iron,
              2,
              DOUBLE_INSULATED_IRON_FOAM_CABLE));
  public static final FoamConductorBlock TRIPLE_INSULATED_IRON_FOAM_CABLE =
      BlockDefinitions.register(
          "triple_insulated_iron_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "triple_insulated_iron_foam_cable"),
              ConductorMaterial.iron,
              3));
  public static final Block TRIPLE_INSULATED_IRON_CABLE =
      BlockDefinitions.register(
          "triple_insulated_iron_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "triple_insulated_iron_cable"),
              ConductorMaterial.iron,
              3,
              TRIPLE_INSULATED_IRON_FOAM_CABLE));
  public static final FoamConductorBlock TIN_FOAM_CABLE =
      BlockDefinitions.register(
          "tin_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(cableSettings, "tin_foam_cable"), ConductorMaterial.tin, 0));
  public static final Block TIN_CABLE =
      BlockDefinitions.register(
          "tin_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(cableSettings, "tin_cable"), ConductorMaterial.tin, 0, TIN_FOAM_CABLE));
  public static final FoamConductorBlock INSULATED_TIN_FOAM_CABLE =
      BlockDefinitions.register(
          "insulated_tin_foam_cable",
          FoamConductorBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_tin_foam_cable"),
              ConductorMaterial.tin,
              1));
  public static final Block INSULATED_TIN_CABLE =
      BlockDefinitions.register(
          "insulated_tin_cable",
          BareCableBlock.create(
              BlockDefinitions.withId(insulatedCableSettings, "insulated_tin_cable"),
              ConductorMaterial.tin,
              1,
              INSULATED_TIN_FOAM_CABLE));
  public static final FoamedTrafficSensorBlock DETECTOR_FOAM_CABLE =
      BlockDefinitions.register(
          "detector_foam_cable",
          FoamedTrafficSensorBlock.create(BlockDefinitions.withId(cableSettings, "detector_foam_cable")));
  public static final Block DETECTOR_CABLE =
      BlockDefinitions.register(
          "detector_cable",
          BareTrafficSensorBlock.create(
              BlockDefinitions.withId(cableSettings, "detector_cable"), DETECTOR_FOAM_CABLE));
  public static final FoamedRedstoneGateBlock SPLITTER_FOAM_CABLE =
      BlockDefinitions.register(
          "splitter_foam_cable",
          FoamedRedstoneGateBlock.create(BlockDefinitions.withId(cableSettings, "splitter_foam_cable")));
  public static final Block SPLITTER_CABLE =
      BlockDefinitions.register(
          "splitter_cable",
          BareRedstoneGateBlock.create(
              BlockDefinitions.withId(cableSettings, "splitter_cable"), SPLITTER_FOAM_CABLE));

  private CableBlocks() {}

  public static void init() {}
}
