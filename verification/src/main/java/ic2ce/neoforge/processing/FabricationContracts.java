package ic2ce.neoforge.processing;

import ic2ce.neoforge.processing.fabrication.AmplifiedMatterFabricator;
import ic2ce.neoforge.processing.fabrication.ClassicMatterFabricator;
import ic2ce.neoforge.processing.fabrication.LiquidMatterFabricator;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Native item/fluid UU payment, fractional amplification and blocked-output persistence. */
public final class FabricationContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native fabrication: " + label);
  }

  public static Result run(ServerLevel world) {
    return new FabricationContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8300, 100, 8300);
    var classic =
        new ClassicMatterFabricator(
            position, BlockDefinitions.CLASSIC_MASS_FABRICATOR.defaultBlockState());
    classic.setLevel(world);
    check(
        !classic.attemptGeneration() && classic.outputSlot.isEmpty(),
        "public classic generation cannot create unpaid matter");
    classic.scrap = 1;
    for (int packet = 0; packet < 20; packet++) {
      classic.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(0.1);
      MachineFixtureAccess.tick(classic);
    }
    check(
        Math.abs(
                    classic
                            .getComponent(ic2ce.neoforge.component.ElectricalBuffer.class)
                            .getEnergy()
                        - 7)
                < 1e-10
            && classic.scrap == 0,
        "twenty 0.1-EU packets spend exactly one amplifier credit and provide only five bonus EU");
    var saved = new CompoundTag();
    classic.saveAdditional(saved);
    check(
        saved.getDoubleOr("scrapFraction", -1) == 0,
        "fully spent fractional credit persists as zero");
    classic
        .getComponent(ic2ce.neoforge.component.ElectricalBuffer.class)
        .forceAddEnergy(
            classic.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getCapacity());
    classic.outputSlot.put(new ItemStack(ItemDefinitions.UU_MATTER, 64));
    double before =
        classic.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy();
    check(
        !classic.attemptGeneration()
            && classic.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy()
                == before
            && classic.outputSlot.get().getCount() == 64,
        "blocked solid output preserves every paid energy unit");
    classic.outputSlot.clear();
    check(
        classic.attemptGeneration()
            && classic.outputSlot.get().getCount() == 1
            && Math.abs(
                    classic
                            .getComponent(ic2ce.neoforge.component.ElectricalBuffer.class)
                            .getEnergy()
                        - 7)
                < 1e-10,
        "one solid matter unit consumes exactly its full capacity cost and retains the prior"
            + " remainder");
    var fluid =
        new LiquidMatterFabricator(position, BlockDefinitions.MATTER_GENERATOR.defaultBlockState());
    fluid.setLevel(world);
    check(
        !fluid.attemptGeneration() && fluid.fluidTank.isEmpty(),
        "public fluid generation cannot create unpaid millibuckets");
    fluid
        .getComponent(ic2ce.neoforge.component.ElectricalBuffer.class)
        .forceAddEnergy(
            fluid.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getCapacity());
    check(
        fluid.attemptGeneration()
            && fluid.fluidTank.getFluidAmount() == 1
            && fluid.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy() == 0,
        "one fluid matter millibucket consumes its complete native EU budget");
    var quota =
        new AmplifiedMatterFabricator(
            position, BlockDefinitions.MASS_FABRICATOR.defaultBlockState());
    quota.setLevel(world);
    quota
        .getComponent(ic2ce.neoforge.component.ElectricalBuffer.class)
        .forceAddEnergy(
            quota.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getCapacity());
    MachineFixtureAccess.tick(quota);
    check(
        quota.outputSlot.isEmpty()
            && quota.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).getEnergy()
                == quota
                    .getComponent(ic2ce.neoforge.component.ElectricalBuffer.class)
                    .getCapacity(),
        "mandatory-amplifier fabrication cannot substitute EU for missing scrap quota");
    return new Result(checks, List.copyOf(failures));
  }
}
