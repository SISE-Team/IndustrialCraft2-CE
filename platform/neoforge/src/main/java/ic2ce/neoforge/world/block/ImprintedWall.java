package ic2ce.neoforge.world.block;

import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.world.WorldBlockEntities;
import ic2ce.neoforge.world.surface.SurfaceMap;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Imprinted wall independently persists base pigment and publishes complete changed render
 * generations.
 */
public final class ImprintedWall extends NativeBlockEntity {
  public static final net.neoforged.neoforge.model.data.ModelProperty<WallRenderState> MODEL_STATE =
      new net.neoforged.neoforge.model.data.ModelProperty<>();
  protected final SurfaceMap obscuration;
  private DyeColor color = ConstructionWall.DEFAULT_COLOR;
  private volatile WallRenderState renderState;

  public ImprintedWall(BlockPos position, BlockState state) {
    super(WorldBlockEntities.WALL, position, state);
    obscuration =
        addComponent(
            new SurfaceMap(
                this,
                () -> {
                  if (level != null && !level.isClientSide()) {
                    setChanged();
                    SideServices.network(true).updateTileEntityField(this, "obscuration");
                  }
                }));
  }

  public void setColor(DyeColor requested) {
    Objects.requireNonNull(requested);
    if (color == requested) return;
    color = requested;
    setChanged();
    if (level != null) {
      if (level.isClientSide()) {
        if (updateRenderState()) rerender();
      } else SideServices.network(true).updateTileEntityField(this, "color");
    }
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    int id = data.getIntOr("color", DEFAULT_COLOR_ID());
    color =
        id >= 0 && id < DyeColor.values().length
            ? DyeColor.byId(id)
            : ConstructionWall.DEFAULT_COLOR;
    if (level != null && level.isClientSide()) updateRenderState();
  }

  private static int DEFAULT_COLOR_ID() {
    return ConstructionWall.DEFAULT_COLOR.getId();
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putByte("color", (byte) color.getId());
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    if (level.isClientSide()) updateRenderState();
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = new ArrayList<>(super.getNetworkedFields());
    fields.add("color");
    fields.add("obscuration");
    return List.copyOf(fields);
  }

  @Override
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (updateRenderState()) rerender();
  }

  @Override
  protected boolean recolor(Direction side, DyeColor requested) {
    if (color == requested) return false;
    setColor(requested);
    return true;
  }

  @Override
  protected ItemStack getPickBlock(Player owner, BlockHitResult hit) {
    var block = ConstructionWall.get(color);
    return block == null ? ItemStack.EMPTY : new ItemStack(block);
  }

  @Override
  public net.neoforged.neoforge.model.data.ModelData getModelData() {
    var current = renderState;
    return current == null
        ? net.neoforged.neoforge.model.data.ModelData.EMPTY
        : net.neoforged.neoforge.model.data.ModelData.of(MODEL_STATE, current);
  }

  private boolean updateRenderState() {
    var next = new WallRenderState(color, obscuration.getRenderState());
    if (next.equals(renderState)) return false;
    renderState = next;
    requestModelDataUpdate();
    return true;
  }

  public static final class WallRenderState {
    public final DyeColor color;
    public final SurfaceMap.ObscurationData[] obscurations;

    public WallRenderState(DyeColor color, SurfaceMap.ObscurationData[] faces) {
      this.color = Objects.requireNonNull(color);
      obscurations = faces == null ? null : faces.clone();
      if (obscurations != null)
        for (int index = 0; index < obscurations.length; index++) {
          var face = obscurations[index];
          if (face != null)
            obscurations[index] =
                new SurfaceMap.ObscurationData(
                    face.state, face.variant, face.side, face.colorMultipliers);
        }
    }

    @Override
    public boolean equals(Object value) {
      return value instanceof WallRenderState other
          && color == other.color
          && Arrays.equals(obscurations, other.obscurations);
    }

    @Override
    public int hashCode() {
      return 31 * color.hashCode() + Arrays.hashCode(obscurations);
    }

    @Override
    public String toString() {
      return "WallRenderState[" + color + ", " + Arrays.toString(obscurations) + "]";
    }
  }
}
