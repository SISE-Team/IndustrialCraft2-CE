package ic2ce.neoforge.inventory;

import ic2ce.api.item.IKineticRotor;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.world.item.ItemStack;

public class RotorInput extends TypeFilteredInput {
  private final IKineticRotor.GearboxType gearbox;
  private final String updateField;

  public RotorInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      IKineticRotor.GearboxType gearbox) {
    this(holder, name, access, count, side, gearbox, null);
  }

  public RotorInput(
      IInventorySlotHolder<?> holder,
      String name,
      Access access,
      int count,
      InvSide side,
      IKineticRotor.GearboxType gearbox,
      String field) {
    super(holder, name, access, count, side, IKineticRotor.class);
    this.gearbox = gearbox;
    updateField = field;
  }

  @Override
  public boolean accepts(ItemStack stack) {
    return stack != null
        && !stack.isEmpty()
        && stack.getItem() instanceof IKineticRotor rotor
        && rotor.isAcceptedType(stack, gearbox);
  }

  @Override
  public void onChanged() {
    if (updateField != null
        && base.getParent().hasLevel()
        && !base.getParent().getLevel().isClientSide())
      SideServices.network(true).updateTileEntityField(base.getParent(), updateField);
  }
}
