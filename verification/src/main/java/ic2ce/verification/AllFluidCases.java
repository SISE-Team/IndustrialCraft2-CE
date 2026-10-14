package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import ic2ce.api.energy.tile.IHeatSource;
import ic2ce.api.fluid.*;
import ic2ce.api.recipe.*;
import ic2ce.core.fluid.*;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.processing.fluid.IntegrationSteamConverter;
import ic2ce.neoforge.processing.fluid.LiquidPackager;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import ic2ce.neoforge.processing.fluid.PressureBoiler;
import ic2ce.neoforge.processing.thermal.AlloyFurnace;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

final class AllFluidCases {
  private final AllMachinesVerification s;

  AllFluidCases(AllMachinesVerification suite) {
    s = suite;
  }

  void setup() {
    for (String id :
        List.of(
            "blast_furnace",
            "fermenter",
            "condenser",
            "solar_distiller",
            "steam_generator",
            "steam_repressurizer",
            "liquid_heat_exchanger",
            "electrolyzer",
            "classic_electrolyzer",
            "fluid_bottler",
            "canner",
            "solid_canner",
            "classic_canner"))
      s.guard(
          id,
          "fluid processing setup",
          () -> {
            var tile = s.tile(id);
            switch (id) {
              case "blast_furnace" -> {
                var furnace = (AlloyFurnace) tile;
                furnace.inputSlot.put(new ItemStack(Items.IRON_INGOT));
                s.fill(id, "fluid", "air", 8000);
                s.heatSupply(id);
                s.at(
                    9000,
                    () -> {
                      s.behave(
                          id,
                          "heat and compressed air smelt iron into steel",
                          furnace.outputSlot.get().is(ItemDefinitions.STEEL_INGOT)
                              && furnace.inputSlot.isEmpty());
                      s.observe(id, "heat", furnace.heat);
                      s.observe(id, "airRemaining", furnace.fluidTank.getFluidAmount());
                    });
              }
              case "fermenter" -> {
                s.fill(id, "input", "biomass", 10000);
                s.heatSupply(id);
                s.at(
                    1000,
                    () -> {
                      int output = s.tank(id, "output").getFluidAmount();
                      var recipe = Recipes.fermenter.getFermentationInformation(fluid("biomass"));
                      s.behave(
                          id,
                          "biomass ferments into matching amount of biogas using heat",
                          output > 0
                              && s.tank(id, "output").hasExactFluid(fluid("biogas"))
                              && (10000 - s.tank(id, "input").getFluidAmount())
                                      * recipe.outputAmount
                                  == output * recipe.inputAmount);
                      s.observe(id, "biogasMB", output);
                    });
              }
              case "condenser" -> {
                s.fill(id, "inputTank", "steam", 20000);
                s.at(
                    350,
                    () ->
                        s.behave(
                            id,
                            "20000mB steam condenses into 200mB distilled water",
                            s.tank(id, "inputTank").isEmpty()
                                && s.tank(id, "outputTank").getFluidAmount() == 200
                                && s.tank(id, "outputTank")
                                    .hasExactFluid(fluid("distilled_water"))));
                s.at(
                    400,
                    () -> {
                      s.fill(id, "inputTank", "superheated_steam", 20000);
                      ((InventorySlot) get(tile, "ventSlots")).put(stack("heat_vent", 1));
                      tile.setChanged();
                    });
                s.at(
                    650,
                    () ->
                        s.behave(
                            id,
                            "powered heat vent accelerates superheated steam condensation",
                            s.tank(id, "inputTank").isEmpty()
                                && s.tank(id, "outputTank").getFluidAmount() == 400));
              }
              case "solar_distiller" -> {
                s.fill(id, "inputTank", "minecraft:water", 1000);
                s.at(
                    1000,
                    () ->
                        s.behave(
                            id,
                            "sunlight distills water with 1:1 volume conservation",
                            s.tank(id, "outputTank").getFluidAmount() > 0
                                && s.tank(id, "inputTank").getFluidAmount()
                                        + s.tank(id, "outputTank").getFluidAmount()
                                    == 1000));
                s.at(
                    1100,
                    () -> {
                      s.level.setBlockAndUpdate(
                          tile.getBlockPos().above(), Blocks.STONE.defaultBlockState());
                      s.observe(id, "roofOutput", s.tank(id, "outputTank").getFluidAmount());
                    });
                s.at(
                    1400,
                    () -> {
                      s.observe(id, "stableRoofOutput", s.tank(id, "outputTank").getFluidAmount());
                    });
                s.at(
                    1700,
                    () -> {
                      s.behave(
                          id,
                          "opaque roof stops solar distillation",
                          ((Number) s.rows.get(id).get("stableRoofOutput")).intValue()
                              == s.tank(id, "outputTank").getFluidAmount());
                      s.level.setBlockAndUpdate(
                          tile.getBlockPos().above(), Blocks.AIR.defaultBlockState());
                    });
              }
              case "steam_generator" -> {
                s.fill(id, "waterTank", "distilled_water", 10000);
                s.heatSupply(id);
                var condenser = s.helper("condenser", tile.getBlockPos().east());
                s.at(20, () -> s.event(id, 1));
                s.at(
                    3000,
                    () -> {
                      var output =
                          condenser.getComponent(FluidPorts.class).getFluidTank("outputTank");
                      var generator = (PressureBoiler) tile;
                      s.behave(
                          id,
                          "heat boils distilled water and transfers usable steam to condenser",
                          output.getFluidAmount() > 0
                              && s.tank(id, "waterTank").getFluidAmount() < 10000
                              && generator.getPressure() == 0
                              && generator.getInputMB() == 1);
                      s.observe(id, "condensedWaterMB", output.getFluidAmount());
                      s.event(id, -1);
                      s.level.setBlockAndUpdate(
                          tile.getBlockPos().north(), Blocks.AIR.defaultBlockState());
                    });
                s.at(
                    3100,
                    () ->
                        s.behave(
                            id,
                            "zero input control stops steam production",
                            ((PressureBoiler) tile).getInputMB() == 0));
              }
              case "steam_repressurizer" -> {
                s.fill(id, "input", "steam", 1000);
                s.heatSupply(id);
                s.observe(
                    id,
                    "integrationFixture",
                    "verification-only forge:steam tag supplies target fluid ic2:steam");
                s.at(
                    500,
                    () -> {
                      s.behave(
                          id,
                          "tagged integration steam converts using heat and configured ratio",
                          IntegrationSteamConverter.hasSteam()
                              && s.tank(id, "input").isEmpty()
                              && s.tank(id, "output").getFluidAmount() > 0);
                      s.observe(id, "regularOutputMB", s.tank(id, "output").getFluidAmount());
                      s.tank(id, "output").drainMbUnchecked(Integer.MAX_VALUE, false);
                      s.fill(id, "input", "superheated_steam", 1000);
                    });
                s.at(
                    1000,
                    () ->
                        s.behave(
                            id,
                            "superheated steam conversion produces configured output",
                            s.tank(id, "input").isEmpty()
                                && s.tank(id, "output").getFluidAmount()
                                    > ((Number) s.rows.get(id).get("regularOutputMB")).intValue()));
              }
              case "liquid_heat_exchanger" -> {
                s.fill(id, "inputTank", "hot_coolant", 1000);
                InventorySlot conductors = (InventorySlot) get(tile, "heatexchangerslots");
                for (int i = 0; i < conductors.size(); i++)
                  conductors.put(i, stack("heat_conductor", 1));
                for (int tick = 100; tick < 250; tick++)
                  s.at(tick, () -> ((IHeatSource) tile).drawHeat(tile.getFacing(), 100, false));
                s.at(
                    300,
                    () ->
                        s.behave(
                            id,
                            "hot coolant supplies HU and returns equal volume of coolant",
                            s.tank(id, "outputTank").getFluidAmount() > 0
                                && s.tank(id, "outputTank").hasExactFluid(fluid("coolant"))
                                && s.tank(id, "inputTank").getFluidAmount()
                                        + s.tank(id, "outputTank").getFluidAmount()
                                    == 1000));
              }
              case "electrolyzer" -> {
                var hydrogen = s.helper("tank", tile.getBlockPos().below());
                var oxygen = s.helper("tank", tile.getBlockPos().above());
                s.fill(id, "input", "minecraft:water", 40);
                s.at(
                    500,
                    () -> {
                      var h =
                          hydrogen.getComponent(FluidPorts.class).getAllTanks().iterator().next();
                      var o = oxygen.getComponent(FluidPorts.class).getAllTanks().iterator().next();
                      s.behave(
                          id,
                          "40mB water electrolyzes into 26mB hydrogen below and 13mB oxygen above",
                          s.tank(id, "input").isEmpty()
                              && h.getFluidAmount() == 26
                              && h.hasExactFluid(fluid("hydrogen"))
                              && o.getFluidAmount() == 13
                              && o.hasExactFluid(fluid("oxygen")));
                    });
              }
              case "classic_electrolyzer" -> {
                var bank = s.helper("mfsu", tile.getBlockPos().east());
                var energy = bank.getComponent(ElectricalBuffer.class);
                // A pre-charged battery bank is the scenario's initial resource.
                energy.addEnergy(energy.getCapacity());
                s.slot(id, "water").put(stack("water_cell", 1));
                s.at(
                    400,
                    () -> {
                      s.behave(
                          id,
                          "full adjacent bank stores energy in electrolyzed water cell",
                          s.slot(id, "water").isEmpty()
                              && s.slot(id, "hydrogen")
                                  .get()
                                  .is(ItemDefinitions.ELECTROLYZED_WATER_CELL));
                      energy.useEnergy(energy.getEnergy());
                    });
                s.at(
                    750,
                    () ->
                        s.behave(
                            id,
                            "empty bank recovers cell energy and returns water cell",
                            s.slot(id, "water").get().is(ItemDefinitions.WATER_CELL)
                                && s.slot(id, "hydrogen").isEmpty()
                                && energy.getEnergy() > 0));
              }
              case "fluid_bottler" -> {
                s.at(
                    240,
                    () ->
                        s.observe(
                            id,
                            "drainSnapshot",
                            List.of(
                                s.slot(id, "drainInput").get().toString(),
                                s.slot(id, "output").get().toString(),
                                s.tank(id, "fluidTank").getFluidAmount(),
                                ((LiquidPackager) tile).getProgress())));
                s.at(
                    490,
                    () ->
                        s.observe(
                            id,
                            "fillSnapshot",
                            List.of(
                                s.slot(id, "fillInput").get().toString(),
                                s.slot(id, "output").get().toString(),
                                s.tank(id, "fluidTank").getFluidAmount(),
                                ((LiquidPackager) tile).getProgress())));
                s.slot(id, "drainInput").put(new ItemStack(Items.WATER_BUCKET));
                tile.setChanged();
                s.at(
                    250,
                    () -> {
                      s.behave(
                          id,
                          "empties water bucket preserving empty bucket and 1000mB",
                          s.slot(id, "output").get().is(Items.BUCKET)
                              && s.tank(id, "fluidTank").getFluidAmount() == 1000
                              && s.slot(id, "drainInput").isEmpty());
                      s.slot(id, "output").clear();
                      s.slot(id, "fillInput").put(new ItemStack(Items.BUCKET));
                      tile.setChanged();
                    });
                s.at(
                    500,
                    () ->
                        s.behave(
                            id,
                            "fills bucket and consumes exactly 1000mB",
                            s.slot(id, "output").get().is(Items.WATER_BUCKET)
                                && s.tank(id, "fluidTank").isEmpty()
                                && s.slot(id, "fillInput").isEmpty()));
              }
              case "canner", "solid_canner", "classic_canner" -> {
                var recipe = Recipes.cannerBottle.get(s.level).getRecipes().iterator().next();
                var in = recipe.getInput();
                var container = in.container.getInputs().getFirst().copy();
                container.setCount(in.container.getAmount());
                var food = in.fill.getInputs().getFirst().copy();
                food.setCount(in.fill.getAmount());
                s.slot(id, "canInput").put(container);
                s.slot(id, "input").put(food);
                tile.setChanged();
                s.observe(id, "bottleExpected", recipe.getOutput().toString());
                s.at(
                    1000,
                    () ->
                        s.behave(
                            id,
                            "solid canning consumes valid container and ingredient to exact recipe"
                                + " output",
                            ItemStack.isSameItemSameComponents(
                                    s.slot(id, "output").get(), recipe.getOutput())
                                && s.slot(id, "output").get().getCount()
                                    == recipe.getOutput().getCount()
                                && s.slot(id, "input").isEmpty()
                                && s.slot(id, "canInput").isEmpty()));
                if (id.equals("canner")) {
                  s.at(
                      1490,
                      () ->
                          s.observe(
                              id,
                              "emptySnapshot",
                              List.of(
                                  s.slot(id, "canInput").get().toString(),
                                  s.slot(id, "output").get().toString(),
                                  s.tank(id, "outputTank").getFluidAmount(),
                                  ((MultiPolicyCanner) tile).getMode().name())));
                  s.at(
                      1940,
                      () ->
                          s.observe(
                              id,
                              "bottleSnapshot",
                              List.of(
                                  s.slot(id, "canInput").get().toString(),
                                  s.slot(id, "output").get().toString(),
                                  s.tank(id, "inputTank").getFluidAmount(),
                                  ((MultiPolicyCanner) tile).getMode().name())));
                  s.at(
                      1100,
                      () -> {
                        s.slot(id, "output").clear();
                        s.event(id, 1);
                        s.slot(id, "canInput").put(new ItemStack(Items.WATER_BUCKET));
                        tile.setChanged();
                      });
                  s.at(
                      1500,
                      () -> {
                        s.behave(
                            id,
                            "empty-liquid mode extracts bucket to output tank",
                            s.tank(id, "outputTank").getFluidAmount() == 1000
                                && s.slot(id, "output").get().is(Items.BUCKET));
                        s.slot(id, "output").clear();
                        s.event(id, 5);
                      });
                  s.at(
                      1520,
                      () -> {
                        s.behave(
                            id,
                            "swap-tanks button moves liquid without volume loss",
                            s.tank(id, "inputTank").getFluidAmount() == 1000
                                && s.tank(id, "outputTank").isEmpty());
                        s.event(id, 2);
                        s.slot(id, "canInput").put(new ItemStack(Items.BUCKET));
                        tile.setChanged();
                      });
                  s.at(
                      1950,
                      () -> {
                        s.behave(
                            id,
                            "bottle-liquid mode fills bucket from input tank",
                            s.slot(id, "output").get().is(Items.WATER_BUCKET)
                                && s.tank(id, "inputTank").isEmpty());
                        s.slot(id, "output").clear();
                        s.event(id, 3);
                      });
                  s.at(
                      1970,
                      () -> {
                        var enrich =
                            Recipes.cannerEnrich.get(s.level).getRecipes().iterator().next();
                        var ingredient = enrich.getInput().additive.getInputs().getFirst().copy();
                        ingredient.setCount(enrich.getInput().additive.getAmount());
                        s.slot(id, "input").put(ingredient);
                        s.tank(id, "inputTank")
                            .fillMbUnchecked(enrich.getInput().fluid.copy(), false);
                        tile.setChanged();
                        s.observe(id, "enrichExpected", enrich.getOutput().getAmountMb());
                        s.observe(id, "enrichFluid", enrich.getOutput().getFluid().toString());
                        s.at(
                            2450,
                            () ->
                                s.behave(
                                    id,
                                    "enrich-liquid mode consumes additive to matching recipe"
                                        + " liquid",
                                    s.tank(id, "outputTank").getFluidAmount()
                                            == enrich.getOutput().getAmountMb()
                                        && s.tank(id, "outputTank")
                                            .hasExactFluid(enrich.getOutput().getFluid())
                                        && s.slot(id, "input").isEmpty()));
                      });
                }
              }
            }
            tile.setChanged();
          });
  }
}
