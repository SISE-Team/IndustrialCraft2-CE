package ic2ce.neoforge.world.surface;

import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.NativeComponent;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Six independently owned surface descriptors publish atomically on complete saved-state
 * replacement.
 */
public final class SurfaceMap extends NativeComponent {
  private static final Direction[] FACES = Direction.values();
  private final Runnable changed;
  private ObscurationData[] faces;

  public SurfaceMap(NativeBlockEntity owner, Runnable changed) {
    super(owner);
    this.changed = Objects.requireNonNull(changed);
  }

  @Override
  public void readFromNbt(CompoundTag input) {
    var next = new ObscurationData[FACES.length];
    boolean any = false;
    for (var face : FACES) {
      var data = input.getCompound(face.getSerializedName()).orElse(null);
      if (data == null) continue;
      var id = Identifier.tryParse(data.getStringOr("block", ""));
      int side = data.getIntOr("side", -1);
      var tints = data.getIntArray("colorMuls").orElse(new int[0]);
      String variant = data.getStringOr("variant", "");
      if (id == null
          || side < 0
          || side >= FACES.length
          || tints.length < 1
          || tints.length > 64
          || variant.length() > 4096) continue;
      var block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
      if (block == null) continue;
      try {
        var state = StateVariants.getState(block, variant);
        if (state != null) {
          next[face.ordinal()] = new ObscurationData(state, variant, FACES[side], tints);
          any = true;
        }
      } catch (IllegalArgumentException ignored) {
      }
    }
    faces = any ? next : null;
  }

  @Override
  public CompoundTag writeToNbt() {
    if (faces == null) return null;
    var output = new CompoundTag();
    for (var face : FACES) {
      var value = faces[face.ordinal()];
      if (value == null) continue;
      var row = new CompoundTag();
      row.putString("block", BuiltInRegistries.BLOCK.getKey(value.state.getBlock()).toString());
      row.putString("variant", value.variant);
      row.putByte("side", (byte) value.side.ordinal());
      row.putIntArray("colorMuls", value.colorMultipliers.clone());
      output.put(face.getSerializedName(), row);
    }
    return output;
  }

  public boolean applyObscuration(Direction face, ObscurationData value) {
    Objects.requireNonNull(face);
    Objects.requireNonNull(value);
    if (faces != null && value.equals(faces[face.ordinal()])) return false;
    var next = faces == null ? new ObscurationData[FACES.length] : faces.clone();
    next[face.ordinal()] = value.owned();
    faces = next;
    changed.run();
    return true;
  }

  public void clear() {
    if (faces == null) return;
    faces = null;
    changed.run();
  }

  public boolean hasObscuration() {
    return faces != null;
  }

  public ObscurationData[] getRenderState() {
    if (faces == null) return null;
    var copy = faces.clone();
    for (int index = 0; index < copy.length; index++)
      if (copy[index] != null) copy[index] = copy[index].owned();
    return copy;
  }

  public static final class ObscurationData {
    public final BlockState state;
    public final String variant;
    public final Direction side;
    public final int[] colorMultipliers;

    public ObscurationData(BlockState state, String variant, Direction side, int[] tints) {
      this.state = Objects.requireNonNull(state);
      this.variant = Objects.requireNonNull(variant);
      this.side = Objects.requireNonNull(side);
      Objects.requireNonNull(tints);
      if (variant.length() > 4096 || tints.length < 1 || tints.length > 64)
        throw new IllegalArgumentException("Invalid native surface descriptor");
      colorMultipliers = tints.clone();
    }

    private ObscurationData owned() {
      return new ObscurationData(state, variant, side, colorMultipliers);
    }

    public ObscurationData intern() {
      return owned();
    }

    @Override
    public boolean equals(Object value) {
      return value instanceof ObscurationData other
          && state == other.state
          && variant.equals(other.variant)
          && side == other.side
          && Arrays.equals(colorMultipliers, other.colorMultipliers);
    }

    @Override
    public int hashCode() {
      return 31 * Objects.hash(state, variant, side) + Arrays.hashCode(colorMultipliers);
    }
  }
}
