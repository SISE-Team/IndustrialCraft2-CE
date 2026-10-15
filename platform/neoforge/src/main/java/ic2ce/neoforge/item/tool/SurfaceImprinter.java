package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.tile.RetexturableBlock;
import ic2ce.neoforge.item.PriorityUsableItem;
import ic2ce.neoforge.network.IPlayerItemDataListener;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.item.client.FaceSampler;
import ic2ce.neoforge.item.energy.ElectricItemBase;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.world.surface.StateVariants;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Paid native face imprinting uses server-owned source selection and a separate physical client
 * sampler.
 */
public class SurfaceImprinter extends ElectricItemBase
    implements PriorityUsableItem, IPlayerItemDataListener {
  private record Scan(
      net.minecraft.core.BlockPos pos, BlockState state, Direction side, long tick) {}

  private static final Map<Player, Scan> PENDING = Collections.synchronizedMap(new WeakHashMap<>());

  public SurfaceImprinter(Properties properties) {
    super(properties, 100_000, 250, 2);
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var owner = context.getPlayer();
    var world = context.getLevel();
    var pos = context.getClickedPos();
    var side = context.getClickedFace();
    if (owner == null
        || !stack.is(this)
        || !owner.mayBuild()
        || !owner.mayUseItemAt(pos, side, stack)) return InteractionResult.PASS;
    if (owner.isShiftKeyDown()) {
      if (!ElectricItem.manager.canUse(stack, 20_000)) return InteractionResult.PASS;
      if (world.isClientSide())
        return FaceSampler.sample(stack, owner, world, pos, side)
            ? InteractionResult.SUCCESS
            : InteractionResult.PASS;
      var state = world.getBlockState(pos);
      if (!state.isAir())
        PENDING.put(owner, new Scan(pos.immutable(), state, side, world.getGameTime()));
      return InteractionResult.SUCCESS;
    }
    if (world.isClientSide()) return InteractionResult.PASS;
    var data = StackTag.get(stack);
    var reference = getState(data);
    var refSide = getSide(data);
    var colors = getColorMultipliers(data);
    if (reference == null || refSide == null || colors == null) {
      if (data != null) {
        clear(data);
        StackTag.set(stack, data.isEmpty() ? null : data);
      }
      return InteractionResult.PASS;
    }
    if (!ElectricItem.manager.use(stack, 5000, null)) return InteractionResult.PASS;
    var target = world.getBlockState(pos);
    boolean applied;
    try {
      applied =
          target.getBlock() instanceof RetexturableBlock textured
              ? textured.retexture(
                  target,
                  world,
                  pos,
                  side,
                  owner,
                  reference,
                  getVariant(data),
                  refSide,
                  colors.clone())
              : EnvProxy.get().announceRetexture(
                  world,
                  pos,
                  target,
                  side,
                  owner,
                  reference,
                  getVariant(data),
                  refSide,
                  colors.clone());
    } catch (RuntimeException error) {
      ElectricItem.manager.charge(stack, 5000, Integer.MAX_VALUE, true, false);
      throw error;
    }
    if (!applied) {
      ElectricItem.manager.charge(stack, 5000, Integer.MAX_VALUE, true, false);
      return InteractionResult.PASS;
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public void onPlayerItemNetworkData(Player owner, int slot, Object... values) {
    if (owner == null
        || owner.level().isClientSide()
        || slot < 0
        || slot >= owner.getInventory().getContainerSize()
        || (values.length != 4 && values.length != 5)
        || !(values[0] instanceof Block block)
        || !(values[1] instanceof String variant)
        || !(values[2] instanceof Integer side)
        || !(values[3] instanceof int[] colors)
        || side < 0
        || side >= 6
        || colors.length < 1
        || colors.length > 64
        || variant.length() > 4096) return;
    var stack = owner.getInventory().getItem(slot);
    if (!stack.is(this) || !(slot == owner.getInventory().getSelectedSlot() || slot == 40)) return;
    var pending = PENDING.remove(owner);
    if (values.length == 5
        && values[4] instanceof net.minecraft.core.BlockPos source
        && owner.isShiftKeyDown()
        && owner.level().hasChunkAt(source)
        && owner.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(source))
            <= Math.pow(owner.blockInteractionRange() + 1, 2)
        && owner.mayUseItemAt(source, Direction.values()[side], stack))
      pending =
          new Scan(
              source.immutable(),
              owner.level().getBlockState(source),
              Direction.values()[side],
              owner.level().getGameTime());
    if (pending == null && owner.isShiftKeyDown()) {
      var hit = owner.pick(owner.blockInteractionRange(), 0, false);
      if (hit instanceof net.minecraft.world.phys.BlockHitResult result
          && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
        var source = owner.level().getBlockState(result.getBlockPos());
        if (owner.mayUseItemAt(result.getBlockPos(), result.getDirection(), stack))
          pending =
              new Scan(
                  result.getBlockPos(), source, result.getDirection(), owner.level().getGameTime());
      }
    }
    if (pending == null
        || owner.level().getGameTime() - pending.tick > 40
        || pending.side.ordinal() != side
        || pending.state.getBlock() != block
        || !owner.level().getBlockState(pending.pos).equals(pending.state)) return;
    var state = StateVariants.getState(block, variant);
    if (state == null
        || !state.equals(pending.state)
        || !ElectricItem.manager.use(stack, 20_000, null)) return;
    StackTag.update(
        stack,
        data -> {
          data.putString("refBlock", BuiltInRegistries.BLOCK.getKey(block).toString());
          data.putString("refVariant", variant);
          data.putByte("refSide", side.byteValue());
          data.putIntArray("refColorMuls", colors.clone());
          return data;
        });
  }

  public static BlockState getState(CompoundTag data) {
    if (data == null) return null;
    var id = Identifier.tryParse(data.getStringOr("refBlock", ""));
    if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) return null;
    var state = StateVariants.getState(BuiltInRegistries.BLOCK.getValue(id), getVariant(data));
    return state == null || state.isAir() ? null : state;
  }

  public static String getVariant(CompoundTag data) {
    return data == null ? "" : data.getStringOr("refVariant", "");
  }

  public static Direction getSide(CompoundTag data) {
    if (data == null) return null;
    int side = data.getIntOr("refSide", -1);
    return side >= 0 && side < 6 ? Direction.values()[side] : null;
  }

  public static int[] getColorMultipliers(CompoundTag data) {
    if (data == null) return null;
    var colors = data.getIntArray("refColorMuls").orElse(null);
    return colors == null || colors.length == 0 || colors.length > 64 ? null : colors.clone();
  }

  public static void setColorMultipliers(CompoundTag data, int[] colors) {
    if (colors == null || colors.length == 0 || colors.length > 64)
      throw new IllegalArgumentException("Invalid face colors");
    data.putIntArray("refColorMuls", colors.clone());
  }

  private static void clear(CompoundTag data) {
    for (var key : List.of("refBlock", "refVariant", "refSide", "refColorMul", "refColorMuls"))
      data.remove(key);
  }

  public static int[] internTints(int[] values) {
    return values.clone();
  }

  public static int[] internColorMultipliers(int[] values) {
    return values.clone();
  }

  public static ObscuredRenderInfo getRenderInfo(BlockState state, Direction side) {
    return FaceSampler.renderInfo(state, side);
  }

  public static final class ObscuredRenderInfo {
    public final float[] uvs;
    public final int[] tints;
    public final net.minecraft.client.renderer.texture.TextureAtlasSprite[] sprites;

    public ObscuredRenderInfo(
        float[] uvs,
        int[] tints,
        net.minecraft.client.renderer.texture.TextureAtlasSprite[] sprites) {
      this.uvs = uvs.clone();
      this.tints = tints.clone();
      this.sprites = sprites.clone();
    }
  }
}
