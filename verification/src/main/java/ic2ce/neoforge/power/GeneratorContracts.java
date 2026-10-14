package ic2ce.neoforge.power;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/** Native burn, environmental fuel, packet conversion and malformed-state conservation. */
public final class GeneratorContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native generators: " + label);
  }

  public static Result run(ServerLevel world) {
    return new GeneratorContracts().exercise(world);
  }

  private static final class Converter extends DemandDrivenConverter {
    int units = 3, drawn;

    Converter() {
      super(
          BlockEntityDefinitions.STIRLING_GENERATOR,
          new BlockPos(8240, 100, 8240),
          BlockDefinitions.STIRLING_GENERATOR.defaultBlockState());
    }

    protected int getEnergyAvailable() {
      return units;
    }

    protected void drawEnergyAvailable(int amount) {
      units -= amount;
      drawn += amount;
    }

    protected double getMultiplier() {
      return 0.5;
    }
  }

  private Result exercise(ServerLevel world) {
    var coal =
        new CombustionGenerator(
            new BlockPos(8241, 100, 8241), BlockDefinitions.GENERATOR.defaultBlockState());
    coal.setLevel(world);
    coal.fuelSlot.put(new ItemStack(Items.COAL, 2));
    coal.updateEntityServer();
    check(
        coal.fuel == 399
            && coal.totalFuel == 400
            && coal.fuelSlot.get().getCount() == 1
            && coal.energy.getEnergy() == 10,
        "coal ignition consumes one item and produces one exact burn tick");
    coal.energy.forceAddEnergy(3990);
    coal.updateEntityServer();
    check(
        coal.fuel == 398 && coal.energy.getEnergy() == 4000,
        "lit combustion keeps burning with a full output buffer");
    var data = new CompoundTag();
    data.putInt("fuel", 1);
    data.putInt("totalFuel", 0);
    coal.load(data);
    check(coal.getFuelRatio() == 1, "malformed total burn length cannot produce an infinite gauge");
    var geo =
        new GeothermalGenerator(
            new BlockPos(8242, 100, 8242), BlockDefinitions.GEO_GENERATOR.defaultBlockState());
    geo.setLevel(world);
    geo.fluidTank.fillMbUnchecked(Ic2FluidStack.create(Fluids.LAVA, 1000), false);
    geo.updateEntityServer();
    check(
        geo.fluidTank.getFluidAmount() == 998 && geo.fuel == 0 && geo.energy.getEnergy() == 20,
        "geothermal tick converts exactly two millibuckets");
    geo.energy.forceAddEnergy(2380);
    geo.updateEntityServer();
    check(
        geo.fluidTank.getFluidAmount() == 998 && geo.energy.getEnergy() == 2400,
        "full geothermal buffer reserves no additional lava");
    var biogas =
        new BiogasGenerator(
            new BlockPos(8243, 100, 8243),
            BlockDefinitions.SEMIFLUID_GENERATOR.defaultBlockState());
    biogas.setLevel(world);
    biogas.fluidTank.fillMbUnchecked(
        Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, 1000), false);
    biogas.updateEntityServer();
    check(
        biogas.fluidTank.getFluidAmount() == 990
            && biogas.fuel == 9
            && biogas.energy.getEnergy() == 16,
        "biogas batch reserves ten millibuckets for ten burn ticks");
    var water =
        new WaterMill(
            new BlockPos(8244, 100, 8244), BlockDefinitions.WATER_GENERATOR.defaultBlockState());
    water.setLevel(world);
    water.fuelSlot.put(new ItemStack(Items.WATER_BUCKET));
    check(
        water.gainFuel() && water.fuel == 500 && water.fuelSlot.get().is(Items.BUCKET),
        "water bucket supplies fuel and retains its actual empty container");
    int fuel = water.fuel;
    check(
        !water.gainFuel() && water.fuel == fuel,
        "empty bucket cannot be billed as another full water bucket");
    var radio =
        new RadioisotopeGenerator(
            new BlockPos(8245, 100, 8245), BlockDefinitions.RT_GENERATOR.defaultBlockState());
    radio.setLevel(world);
    radio.fuelSlot.put(new ItemStack(Items.COAL));
    check(!radio.gainEnergy(), "forced invalid radioisotope item cannot generate permanent EU");
    for (int slot = 0; slot < 6; slot++)
      radio.fuelSlot.put(slot, new ItemStack(ItemDefinitions.RTG_PELLET));
    radio.gainEnergy();
    check(
        radio.energy.getEnergy() == 32 && radio.fuelSlot.get().getCount() == 1,
        "six permanent pellets select thirty-two EU without consuming a pellet");
    var conversion = new Converter();
    conversion.setLevel(world);
    check(conversion.getOfferedEnergy() == 1.5, "converter offers its real input budget");
    for (int packet = 0; packet < 5; packet++) conversion.drawEnergy(0.1);
    check(
        conversion.drawn == 1
            && conversion.units == 2
            && Math.abs(conversion.getOfferedEnergy() - 1) < 1e-12,
        "five fractional packets conserve one indivisible heat unit");
    conversion.drawEnergy(Double.NaN);
    conversion.drawEnergy(-1);
    check(
        conversion.drawn == 1 && conversion.units == 2,
        "invalid extraction requests cannot consume or create energy");
    conversion.drawEnergy(0.1);
    data = new CompoundTag();
    conversion.saveAdditional(data);
    var restored = new Converter();
    restored.setLevel(world);
    restored.load(data);
    check(
        Math.abs(restored.getOfferedEnergy() - 1.9) < 1e-12,
        "prepaid converted output survives persistence without re-consuming input");
    return new Result(checks, List.copyOf(failures));
  }
}
