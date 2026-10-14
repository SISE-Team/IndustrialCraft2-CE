package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import ic2ce.api.energy.tile.IHeatSource;
import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.kinetic.ManualCrank;
import ic2ce.neoforge.power.BufferedGenerator;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AllGenerationCases {
  private final AllMachinesVerification s;
  private final Map<String, NativeBlockEntity> recipients = new LinkedHashMap<>();

  AllGenerationCases(AllMachinesVerification suite) {
    s = suite;
  }

  void setup() {
    for (String id : s.machines.keySet())
      s.guard(
          id,
          "generation setup",
          () -> {
            var tile = s.tile(id);
            if (tile instanceof BufferedGenerator
                || Set.of("stirling_generator", "kinetic_generator", "creative_generator")
                    .contains(id)) {
              var recipient = s.helper("mfsu", tile.getBlockPos().east());
              recipients.put(id, recipient);
              s.at(
                  500,
                  () -> {
                    double received = recipient.getComponent(ElectricalBuffer.class).getEnergy();
                    s.behave(id, "produces EU delivered through real energy network", received > 0);
                    s.observe(id, "receiverEU", received);
                  });
            }
            switch (id) {
              case "generator" -> s.slot(id, "fuel").put(new ItemStack(Items.COAL, 8));
              case "geo_generator" -> s.fill(id, "fluid", "minecraft:lava", 4000);
              case "semifluid_generator" -> s.fill(id, "fluid", "biogas", 4000);
              case "rt_generator" ->
                  ((InventorySlot) get(tile, "fuelSlot"))
                      .put(new ItemStack(ItemDefinitions.RTG_PELLET));
              case "water_generator" ->
                  ((InventorySlot) get(tile, "fuelSlot")).put(new ItemStack(Items.WATER_BUCKET));
              case "stirling_generator" -> s.heatSupply(id);
              case "kinetic_generator" -> {
                var source =
                    s.helper(
                        "electric_kinetic_generator",
                        tile.getBlockPos().relative(tile.getFacing()));
                s.facing(source, tile.getFacing().getOpposite());
                ((InventorySlot) get(source, "slotMotor"))
                    .put(new ItemStack(ItemDefinitions.ELECTRIC_MOTOR));
                s.helper("creative_generator", source.getBlockPos().above());
                source.setChanged();
              }
              case "electric_heat_generator" -> {
                InventorySlot slot = (InventorySlot) get(tile, "coilSlot");
                for (int i = 0; i < slot.size(); i++)
                  slot.put(i, new ItemStack(ItemDefinitions.COIL));
              }
              case "solid_heat_generator" ->
                  ((InventorySlot) get(tile, "fuelSlot")).put(new ItemStack(Items.COAL, 8));
              case "fluid_heat_generator" -> s.fill(id, "fluidTank", "biogas", 4000);
              case "rt_heat_generator" ->
                  ((InventorySlot) get(tile, "fuelSlot"))
                      .put(new ItemStack(ItemDefinitions.RTG_PELLET));
              case "electric_kinetic_generator" ->
                  ((InventorySlot) get(tile, "slotMotor"))
                      .put(new ItemStack(ItemDefinitions.ELECTRIC_MOTOR));
              case "steam_kinetic_generator" -> {
                ((InventorySlot) get(tile, "turbineSlot"))
                    .put(new ItemStack(ItemDefinitions.STEAM_TURBINE));
                s.helper("condenser", tile.getBlockPos().east());
                // Feed repeatedly, exactly as a connected steam producer would.
                for (int tick = 100; tick < 280; tick++)
                  s.at(
                      tick,
                      () ->
                          s.tank(id, "steamTank")
                              .fillMbUnchecked(Ic2FluidStack.create(fluid("steam"), 1000), false));
              }
              case "stirling_kinetic_generator" -> {
                var heat = s.helper("electric_heat_generator", tile.getBlockPos().west());
                s.facing(heat, Direction.EAST);
                ((InventorySlot) get(heat, "coilSlot")).put(new ItemStack(ItemDefinitions.COIL));
                s.helper("creative_generator", heat.getBlockPos().above());
                heat.setChanged();
                s.fill(id, "inputTank", "minecraft:water", 1000);
              }
              case "wind_kinetic_generator" -> {
                ((InventorySlot) get(tile, "rotorSlot"))
                    .put(new ItemStack(ItemDefinitions.WOODEN_ROTOR));
                for (int x = -4; x <= 4; x++)
                  for (int y = -3; y <= 4; y++)
                    for (int z = -5; z <= -1; z++)
                      s.level.setBlockAndUpdate(
                          tile.getBlockPos().offset(x, y, z), Blocks.AIR.defaultBlockState());
              }
              case "water_kinetic_generator" -> {
                ((InventorySlot) get(tile, "rotorSlot"))
                    .put(new ItemStack(ItemDefinitions.IRON_ROTOR));
                var p = tile.getBlockPos();
                var server = s.level.getServer();
                server
                    .getCommands()
                    .performPrefixedCommand(
                        server.createCommandSourceStack(),
                        "fillbiome "
                            + (p.getX() - 12)
                            + " "
                            + (p.getY() - 8)
                            + " "
                            + (p.getZ() - 12)
                            + " "
                            + (p.getX() + 12)
                            + " "
                            + (p.getY() + 8)
                            + " "
                            + (p.getZ() + 12)
                            + " minecraft:river");
                for (int x = -5; x <= 5; x++)
                  for (int y = -5; y <= 5; y++)
                    for (int z = -5; z <= 5; z++)
                      if (Math.abs(x) == 5 || Math.abs(y) == 5 || Math.abs(z) == 5)
                        s.level.setBlockAndUpdate(
                            p.offset(x, y, z), Blocks.GLASS.defaultBlockState());
                for (int x = -4; x <= 4; x++)
                  for (int y = -4; y <= 4; y++)
                    for (int z = -4; z <= 4; z++)
                      if (x != 0 || y != 0 || z != 0)
                        s.level.setBlockAndUpdate(
                            p.offset(x, y, z), Blocks.WATER.defaultBlockState());
              }
              case "manual_kinetic_generator" ->
                  s.at(
                      100,
                      () -> {
                        var p = tile.getBlockPos();
                        for (int click = 0; click < 12; click++)
                          tile.getBlockState()
                              .useWithoutItem(
                                  s.level,
                                  s.player,
                                  new BlockHitResult(
                                      Vec3.atCenterOf(p), Direction.NORTH, p, false));
                        var crank = (ManualCrank) tile;
                        s.behave(
                            id,
                            "player activation creates KU and enforces ten clicks per tick",
                            crank.currentKU > 0 && crank.clicks == 10 && crank.currentKU <= 1000);
                      });
            }
            tile.setChanged();
            if (tile instanceof IHeatSource source && id.endsWith("heat_generator"))
              s.at(
                  200,
                  () -> {
                    int before = source.drawHeat(tile.getFacing(), Integer.MAX_VALUE, true);
                    int again = source.drawHeat(tile.getFacing(), Integer.MAX_VALUE, true);
                    int drawn = source.drawHeat(tile.getFacing(), 5, false);
                    int after = source.drawHeat(tile.getFacing(), Integer.MAX_VALUE, true);
                    s.behave(id, "valid resources generate heat", before > 0);
                    s.behave(
                        id,
                        "simulation leaves heat and exact draw consumes buffer",
                        again == before && drawn == Math.min(5, before) && after == before - drawn);
                    s.behave(
                        id,
                        "opposite side cannot extract heat",
                        source.drawHeat(tile.getFacing().getOpposite(), 5, false) == 0);
                  });
            if (tile instanceof IKineticSource source)
              s.at(
                  id.equals("steam_kinetic_generator") ? 200 : 300,
                  () -> {
                    Direction output =
                        id.equals("wind_kinetic_generator") || id.equals("water_kinetic_generator")
                            ? tile.getFacing().getOpposite()
                            : tile.getFacing();
                    int available = source.drawKineticEnergy(output, 1, true);
                    int drawn = source.drawKineticEnergy(output, 1, false);
                    s.behave(id, "normal operation produces usable KU", available > 0 && drawn > 0);
                    if (!id.equals("manual_kinetic_generator"))
                      s.behave(
                          id,
                          "KU output is restricted to designated connector",
                          source.drawKineticEnergy(output.getOpposite(), 1, false) == 0);
                    s.observe(id, "kineticBandwidth", source.getConnectionBandwidth(output));
                    if (id.equals("stirling_kinetic_generator"))
                      s.behave(
                          id,
                          "stored KU remains within advertised buffer capacity",
                          source.drawKineticEnergy(output, Integer.MAX_VALUE, true)
                              <= source.getConnectionBandwidth(output));
                  });
          });
  }
}
