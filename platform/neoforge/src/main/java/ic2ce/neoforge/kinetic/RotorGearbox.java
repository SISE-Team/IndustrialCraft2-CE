package ic2ce.neoforge.kinetic;

import ic2ce.api.energy.tile.IKineticSource;
import ic2ce.api.item.IKineticRotor;
import ic2ce.api.tile.IRotorProvider;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.engine.energy.TickBudget;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.RotorInput;
import ic2ce.neoforge.inventory.TypeFilteredInput;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared physical shaft, rotor ownership and animation; environmental production remains
 * device-specific.
 */
public abstract class RotorGearbox extends InventoryBlockEntity
    implements IKineticSource, IRotorProvider, IHasGui {
  public final TypeFilteredInput rotorSlot;
  protected float rotationSpeed;
  protected final TickBudget budget = new TickBudget();
  private final IKineticRotor.GearboxType gearbox;
  private float angle;
  private long lastNanos;
  private static final Identifier FALLBACK =
      Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/wood_rotor_model.png");

  protected RotorGearbox(
      BlockEntityType<? extends RotorGearbox> type,
      BlockPos position,
      BlockState block,
      IKineticRotor.GearboxType gearbox) {
    super(type, position, block);
    this.gearbox = gearbox;
    rotorSlot =
        new RotorInput(
            this,
            "rotorslot",
            InventorySlot.Access.IO,
            1,
            InventorySlot.InvSide.ANY,
            gearbox,
            "rotorSlot");
  }

  protected IKineticRotor rotor() {
    var stack = rotorSlot.get();
    return stack.getItem() instanceof IKineticRotor rotor && rotor.isAcceptedType(stack, gearbox)
        ? rotor
        : null;
  }

  public boolean hasRotor() {
    return rotor() != null;
  }

  public float getEfficiency() {
    var rotor = rotor();
    float value = rotor == null ? 0 : rotor.getEfficiency(rotorSlot.get());
    return Float.isFinite(value) ? Math.max(0, value) : 0;
  }

  @Override
  public int getRotorDiameter() {
    var rotor = rotor();
    return rotor == null ? 0 : Math.clamp(rotor.getDiameter(rotorSlot.get()), 0, 64);
  }

  @Override
  public Identifier getRotorRenderTexture() {
    var rotor = rotor();
    return rotor == null ? FALLBACK : rotor.getRotorRenderTexture(rotorSlot.get());
  }

  protected String health(String key) {
    var stack = rotorSlot.get();
    return hasRotor()
        ? LocalizedText.translate(
            key,
            Math.clamp(
                100
                    - (int)
                        ((long) stack.getDamageValue() * 100 / Math.max(1, stack.getMaxDamage())),
                0,
                100))
        : "";
  }

  protected void speed(float value) {
    float next = Float.isFinite(value) ? value : 0;
    if (rotationSpeed != next) {
      rotationSpeed = next;
      if (getLevel() != null && !getLevel().isClientSide())
        ic2ce.neoforge.runtime.ProjectRuntime.network
            .get(true)
            .updateTileEntityField(this, "rotationSpeed");
    }
  }

  protected float animationScale() {
    return 1;
  }

  @Override
  public float getAngle() {
    long now = System.nanoTime();
    long elapsed = lastNanos == 0 ? 0 : Math.clamp(now - lastNanos, 0, 250000000);
    lastNanos = now;
    double next = (angle + elapsed / 1000000.0 * rotationSpeed * animationScale()) % 360;
    angle = (float) (next < 0 ? next + 360 : next);
    return angle;
  }

  protected abstract int production();

  @Override
  public int getConnectionBandwidth(Direction side) {
    return side != null && side.getOpposite() == getFacing() ? production() : 0;
  }

  @Override
  public int maxrequestkineticenergyTick(Direction side) {
    return drawKineticEnergy(side, Integer.MAX_VALUE, true);
  }

  @Override
  public int requestkineticenergy(Direction side, int request) {
    return drawKineticEnergy(side, request, false);
  }

  @Override
  public int drawKineticEnergy(Direction side, int request, boolean simulate) {
    return side != null && side.getOpposite() == getFacing() ? budget.draw(request, simulate) : 0;
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("rotationSpeed");
    fields.add("rotorSlot");
    return fields;
  }
}
