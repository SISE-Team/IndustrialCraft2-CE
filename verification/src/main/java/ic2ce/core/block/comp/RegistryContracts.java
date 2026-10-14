package ic2ce.core.block.comp;

import ic2ce.core.FluidHeatManager;
import ic2ce.core.SemiFluidFuelManager;
import ic2ce.neoforge.component.ComponentTypes;
import ic2ce.neoforge.component.NativeComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.material.Fluids;

/**
 * Failed component/fuel registrations must preserve the previous published bidirectional tables.
 */
public final class RegistryContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native registries: " + label);
  }

  private static final class First extends NativeComponent {
    First() {
      super(null);
    }
  }

  private static final class Second extends NativeComponent {
    Second() {
      super(null);
    }
  }

  public static Result run() {
    return new RegistryContracts().exercise();
  }

  private Result exercise() {
    ComponentTypes.register(First.class, "registry_contract_first");
    boolean rejected = false;
    try {
      ComponentTypes.register(Second.class, "registry_contract_first");
    } catch (IllegalStateException expected) {
      rejected = true;
    }
    check(
        rejected
            && ComponentTypes.<First>getClass("registry_contract_first") == First.class
            && ComponentTypes.getId(Second.class) == null,
        "duplicate identifier cannot corrupt either registry direction");
    rejected = false;
    try {
      ComponentTypes.register(First.class, "registry_contract_second");
    } catch (IllegalStateException expected) {
      rejected = true;
    }
    check(
        rejected
            && ComponentTypes.getClass("registry_contract_second") == null
            && ComponentTypes.getId(First.class).equals("registry_contract_first"),
        "duplicate class cannot leave an orphan alias");
    var semi = new SemiFluidFuelManager();
    rejected = false;
    try {
      semi.addFluid(Fluids.WATER, 10, Double.NaN);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(
        rejected && semi.getBurnProperties().isEmpty(),
        "nonfinite fuel power cannot publish a recipe");
    semi.addFluid(Fluids.LAVA, 10, 16);
    var snapshot = semi.getBurnProperties();
    rejected = false;
    try {
      semi.addFluid(Fluids.LAVA, 20, 32);
    } catch (IllegalStateException expected) {
      rejected = true;
    }
    check(
        rejected
            && semi.getBurnProperty(Fluids.LAVA).amount == 10
            && semi.getBurnProperty(Fluids.LAVA).power == 16,
        "duplicate fuel retains the old recipe");
    semi.addFluid(Fluids.WATER, 1, 1);
    check(
        snapshot.size() == 1 && semi.getBurnProperties().size() == 2,
        "previous published generation remains immutable after another registration");
    rejected = false;
    try {
      semi.getBurnProperties().clear();
    } catch (UnsupportedOperationException expected) {
      rejected = true;
    }
    check(rejected, "fuel recipe views cannot clear the live table");
    var thermal = new FluidHeatManager();
    rejected = false;
    try {
      thermal.addFluid(Fluids.LAVA, -1, 32);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(
        rejected && !thermal.acceptsFluid(Fluids.LAVA),
        "negative fuel batch size cannot create thermal energy");
    thermal.addFluid(Fluids.LAVA, 10, 32);
    check(
        thermal.getBurnProperty(Fluids.LAVA).heat == 32 && thermal.getBurnProperty(null) == null,
        "thermal properties remain distinct from electrical power");
    return new Result(checks, List.copyOf(failures));
  }
}
