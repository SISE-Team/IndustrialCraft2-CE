package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.reactor.*;
import ic2ce.neoforge.reactor.part.ChemicalCondensator;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

final class AllReactorCases {
  private final AllMachinesVerification s;

  AllReactorCases(AllMachinesVerification suite) {
    s = suite;
  }

  private void fuel(NuclearCore reactor) {
    reactor.reactorSlot.put(0, stack("uranium_fuel_rod", 1));
    reactor.reactorSlot.put(1, stack("heat_vent", 1));
    reactor.reactorSlot.put(9, stack("reactor_heat_vent", 1));
    reactor.setChanged();
  }

  void setup() {
    s.guard(
        "nuclear_reactor",
        "safe EU reactor setup",
        () -> {
          var reactor = (NuclearCore) s.tile("nuclear_reactor");
          fuel(reactor);
          var recipient = s.helper("mfsu", reactor.getBlockPos().east());
          s.level.setBlockAndUpdate(
              reactor.getBlockPos().south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
          s.at(
              700,
              () -> {
                s.behave(
                    "nuclear_reactor",
                    "single fuel rod and cooling components produce real EU without overheating",
                    recipient.getComponent(ElectricalBuffer.class).getEnergy() > 0
                        && reactor.getHeat() < 1000
                        && !reactor.isFluidCooled());
                s.observe(
                    "nuclear_reactor",
                    "receiverEU",
                    recipient.getComponent(ElectricalBuffer.class).getEnergy());
                s.level.setBlockAndUpdate(
                    reactor.getBlockPos().south(), Blocks.AIR.defaultBlockState());
              });
          s.at(
              800,
              () ->
                  s.behave(
                      "nuclear_reactor",
                      "redstone removal stops nuclear production",
                      reactor.output == 0));
        });
    s.guard(
        "reactor_chamber",
        "chamber expansion setup",
        () -> {
          var chamber = (ReactorChamber) s.tile("reactor_chamber");
          s.at(
              100,
              () -> {
                var reactor = chamber.getReactorInstance();
                s.behave(
                    "reactor_chamber",
                    "attached chamber resolves core and expands grid width",
                    reactor != null && reactor.getReactorSize() == 4);
                s.behave(
                    "reactor_chamber",
                    "expanded grid retains shared core inventory",
                    reactor.reactorSlot.size() == 24 && reactor.reactorSlot.rawSize() == 54);
              });
        });
    for (String id : List.of("reactor_access_hatch", "reactor_fluid_port", "reactor_redstone_port"))
      s.guard(
          id,
          "fluid reactor fixture",
          () -> {
            var reactor = (NuclearCore) s.level.getBlockEntity(s.tile(id).getBlockPos().west(2));
            s.at(
                100,
                () ->
                    s.guard(
                        id,
                        "fluid reactor lookup",
                        () -> {
                          var resolved =
                              id.equals("reactor_fluid_port")
                                  ? ((CoolantPort) s.tile(id)).getReactorInstance()
                                  : ((VesselEndpoint) s.tile(id)).getReactorInstance();
                          s.behave(
                              id,
                              "completed vessel and six chambers resolve fluid reactor",
                              resolved == reactor && reactor.isFluidCooled());
                          if (id.equals("reactor_access_hatch")) {
                            var hatch = (VesselHatch) s.tile(id);
                            hatch.setItem(0, stack("heat_vent", 1));
                            s.behave(
                                id,
                                "hatch insertion and extraction use actual core inventory",
                                reactor.getItem(0).is(ReactorItems.HEAT_VENT)
                                    && hatch.removeItem(0, 1).is(ReactorItems.HEAT_VENT)
                                    && reactor.getItem(0).isEmpty());
                          } else if (id.equals("reactor_fluid_port")) {
                            int accepted =
                                LiquidUtil.fillTile(
                                    s.tile(id),
                                    Direction.EAST,
                                    Ic2FluidStack.create(fluid("coolant"), 1000),
                                    false);
                            s.behave(
                                id,
                                "port insertion forwards coolant to core tank",
                                accepted == 1000 && reactor.inputTank.getFluidAmount() == 1000);
                            fuel(reactor);
                            s.level.setBlockAndUpdate(
                                reactor.getBlockPos().above(3),
                                Blocks.REDSTONE_BLOCK.defaultBlockState());
                            // A shell redstone port carries the signal into the core.
                            s.helper("reactor_redstone_port", reactor.getBlockPos().above(2));
                          } else {
                            fuel(reactor);
                            reactor.inputTank.fillMbUnchecked(
                                Ic2FluidStack.create(fluid("coolant"), 1000), false);
                            s.level.setBlockAndUpdate(
                                s.tile(id).getBlockPos().east(),
                                Blocks.REDSTONE_BLOCK.defaultBlockState());
                          }
                        }));
            if (id.equals("reactor_fluid_port"))
              s.at(
                  800,
                  () -> {
                    var output =
                        LiquidUtil.drainTile(
                            s.tile(id).getBlockState(), s.tile(id), Direction.EAST, 1000, false);
                    s.behave(
                        id,
                        "working fluid reactor outputs extractable hot coolant through port",
                        output != null
                            && output.getAmountMb() > 0
                            && output.getFluid() == fluid("hot_coolant"));
                    s.level.setBlockAndUpdate(
                        reactor.getBlockPos().above(3), Blocks.AIR.defaultBlockState());
                  });
            if (id.equals("reactor_redstone_port")) {
              s.at(
                  800,
                  () -> {
                    s.behave(
                        id,
                        "external port signal starts linked core fuel processing",
                        reactor.redstone.hasRedstoneInput()
                            && reactor.output > 0
                            && reactor.outputTank.getFluidAmount() > 0);
                    s.level.setBlockAndUpdate(
                        s.tile(id).getBlockPos().east(), Blocks.AIR.defaultBlockState());
                  });
              s.at(
                  900,
                  () ->
                      s.behave(
                          id,
                          "removing port signal stops linked reactor",
                          !reactor.redstone.hasRedstoneInput() && reactor.output == 0));
            }
          });
    for (String id : List.of("rci_rsh", "rci_lzh"))
      s.guard(
          id,
          "coolant injector setup",
          () -> {
            var injector = (CondensatorService) s.tile(id);
            var reactor = (NuclearCore) s.helper("nuclear_reactor", injector.getBlockPos().south());
            var cell = stack(id.equals("rci_rsh") ? "rsh_condensator" : "lzh_condensator", 1);
            var item = (ChemicalCondensator) cell.getItem();
            item.setUse(cell, (int) (((Number) get(item, "maxUse")).intValue() * 0.9));
            reactor.reactorSlot.put(0, cell);
            reactor.setChanged();
            injector.inputSlot.put(
                new ItemStack(id.equals("rci_rsh") ? Items.REDSTONE_BLOCK : Items.LAPIS_BLOCK, 2));
            injector.setChanged();
            s.at(
                300,
                () -> {
                  s.behave(
                      id,
                      "powered injector consumes one coolant block and resets worn matching cell",
                      item.getUseFraction(reactor.reactorSlot.get()) == 0
                          && injector.inputSlot.get().getCount() == 1);
                  s.observe(
                      id, "restoredUseFraction", item.getUseFraction(reactor.reactorSlot.get()));
                });
          });
  }
}
