package ic2ce.neoforge.power;

import ic2ce.api.tile.IRotorProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rotor animation uses a monotonic clock and never integrates time before its first rendered frame.
 */
public abstract class RotorGenerator extends BufferedGenerator implements IRotorProvider {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath("ic2", "textures/item/rotor/iron_rotor_model.png");
  private final int diameter;
  private float angle;
  private long previousNanos;

  protected RotorGenerator(
      BlockEntityType<? extends RotorGenerator> type,
      BlockPos position,
      BlockState block,
      double output,
      int tier,
      int capacity,
      int diameter) {
    super(type, position, block, output, tier, capacity);
    this.diameter = diameter;
  }

  @Override
  public int getRotorDiameter() {
    return diameter;
  }

  protected abstract boolean shouldRotorRotate();

  protected float rotorSpeedFactor() {
    return 1;
  }

  @Override
  public float getAngle() {
    long now = System.nanoTime();
    long elapsed = previousNanos == 0 ? 0 : Math.clamp(now - previousNanos, 0, 250000000);
    previousNanos = now;
    if (shouldRotorRotate()) {
      float speed = rotorSpeedFactor();
      if (Float.isFinite(speed))
        angle = (float) ((angle + elapsed / 1000000.0 * 0.4 * Math.max(0, speed)) % 360);
    }
    return angle;
  }

  @Override
  public Identifier getRotorRenderTexture() {
    return TEXTURE;
  }
}
