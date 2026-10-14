package ic2ce.neoforge.kinetic;

import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Native rotor ownership, projected geometry, signed animation and shared extraction budgets. */
public final class RotorContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native rotors: " + label);
  }

  public static Result run(ServerLevel world) {
    return new RotorContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8330, 100, 8330);
    world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
    world.getChunkAt(position);
    var wind =
        new WindTurbine(position, BlockDefinitions.WIND_KINETIC_GENERATOR.defaultBlockState());
    wind.setLevel(world);
    wind.rotorSlot.put(new ItemStack(Items.COAL));
    check(
        !wind.hasRotor() && wind.getRotorDiameter() == 0 && wind.getKuOutput() == 0,
        "forced non-rotor item creates neither radius nor production");
    wind.rotorSlot.put(new ItemStack(ItemDefinitions.WOODEN_ROTOR));
    check(
        wind.hasRotor() && wind.getRotorDiameter() > 0,
        "native wooden rotor exposes its own capability");
    check(wind.rotorHasSpace(), "empty front rotor plane is usable");
    var obstruction = position.relative(wind.getFacing());
    world.setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
    check(!wind.rotorHasSpace(), "solid front-plane cell prevents rotation");
    world.setBlockAndUpdate(obstruction, Blocks.AIR.defaultBlockState());
    check(
        RotorVolume.scan(
                world,
                position,
                Direction.UP,
                5,
                10,
                false,
                state -> state.isAir(),
                entity -> false)
            == -1,
        "unsupported vertical rotor axes return a blocked geometry rather than throwing");
    wind.budget.reset(100);
    var output = wind.getFacing().getOpposite();
    check(
        wind.drawKineticEnergy(output, -1, false) == 0 && wind.budget.available() == 100,
        "negative rotor requests cannot add energy");
    check(
        wind.drawKineticEnergy(wind.getFacing(), 1, false) == 0 && wind.budget.available() == 100,
        "rotor shaft has one designated output side");
    check(
        wind.drawKineticEnergy(output, 10, true) == 10 && wind.budget.available() == 100,
        "rotor simulation preserves the tick budget");
    check(
        wind.drawKineticEnergy(output, 40, false) == 40
            && wind.drawKineticEnergy(output, 100, false) == 60
            && wind.drawKineticEnergy(output, 1, false) == 0,
        "multiple connected consumers share exactly one produced tick budget");
    var water =
        new WaterTurbine(
            position.above(20), BlockDefinitions.WATER_KINETIC_GENERATOR.defaultBlockState());
    water.setLevel(world);
    water.rotorSlot.put(new ItemStack(ItemDefinitions.WOODEN_ROTOR));
    check(!water.hasRotor(), "wind-only wooden rotor cannot be forced into a water gearbox");
    water.rotorSlot.put(new ItemStack(ItemDefinitions.IRON_ROTOR));
    check(water.hasRotor(), "water gearbox accepts the declared compatible iron rotor");
    wind.speed(-0.5F);
    float first = wind.getAngle(), second = wind.getAngle();
    check(
        first >= 0 && first < 360 && second >= 0 && second < 360,
        "signed monotonic rotor animation stays in a finite angle interval");
    return new Result(checks, List.copyOf(failures));
  }
}
