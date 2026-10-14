package ic2ce.neoforge.inventory;

import ic2ce.core.block.IInventorySlotHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.level.material.Fluid;

/** A stable preference order for filling containers with an explicit fluid whitelist. */
public class FluidWhitelistInput extends FluidContainerInventory {
  private final List<Fluid> accepted;

  public FluidWhitelistInput(
      IInventorySlotHolder<?> holder, String name, int count, Fluid... fluids) {
    this(holder, name, Access.I, count, InvSide.TOP, OpType.Drain, fluids);
  }

  public FluidWhitelistInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      OpType operation,
      Fluid... fluids) {
    super(holder, name, access, count, side, operation);
    Objects.requireNonNull(fluids);
    accepted =
        switch (fluids.length) {
          case 0 -> List.of();
          case 1 -> List.of(Objects.requireNonNull(fluids[0]));
          case 2 -> {
            var first = Objects.requireNonNull(fluids[0]);
            var second = Objects.requireNonNull(fluids[1]);
            yield first == second ? List.of(first) : List.of(first, second);
          }
          default -> {
            var unique = new ArrayList<Fluid>(fluids.length);
            for (var fluid : fluids)
              if (!unique.contains(Objects.requireNonNull(fluid))) unique.add(fluid);
            yield List.copyOf(unique);
          }
        };
  }

  @Override
  protected boolean acceptsLiquid(Fluid fluid) {
    return accepted.contains(fluid);
  }

  @Override
  protected Iterable<Fluid> getPossibleFluids() {
    return accepted;
  }
}
