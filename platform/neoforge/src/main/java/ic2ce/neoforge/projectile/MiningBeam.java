package ic2ce.neoforge.projectile;

import ic2ce.neoforge.explosion.ExplosionBlocks;
import ic2ce.neoforge.explosion.NativeBlast;
import ic2ce.neoforge.registry.EntityDefinitions;
import ic2ce.neoforge.runtime.AutomationActors;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Finite native beam budgets, actual protected block removal, smelting and one-shot explosive
 * completion.
 */
public class MiningBeam extends ThrowableProjectile {
  public static final double laserSpeed = 3;
  public LivingEntity owner;
  public boolean isSmeltMode, removeBlock, isExplosiveMode;
  public float range, power;
  public int blockBreaks;
  private boolean exploded;

  public MiningBeam(Level world) {
    super(EntityDefinitions.LASER_BULLET, world);
  }

  public MiningBeam(EntityType<? extends MiningBeam> type, Level world) {
    super(type, world);
  }

  public MiningBeam(Level world, LivingEntity owner) {
    this(world);
    this.owner = owner;
    setOwner(owner);
  }

  public MiningBeam(
      Level world,
      Vector3 start,
      Vector3 direction,
      LivingEntity owner,
      float range,
      float power,
      int blocks,
      boolean explosive) {
    this(world, owner);
    setPos(start.x, start.y, start.z);
    init(owner, range, power, blocks, explosive, false, true);
  }

  @Override
  protected double getDefaultGravity() {
    return 0;
  }

  @Override
  protected void defineSynchedData(
      net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

  public void init(
      LivingEntity owner,
      float range,
      float power,
      int blocks,
      boolean explosive,
      boolean smelt,
      boolean remove) {
    this.owner = owner;
    setOwner(owner);
    this.range = Float.isNaN(range) ? 0 : Math.max(0, range);
    this.power = Float.isFinite(power) ? Math.max(0, power) : 0;
    blockBreaks = Math.max(0, blocks);
    isExplosiveMode = explosive;
    isSmeltMode = smelt;
    removeBlock = remove;
  }

  @Override
  public void tick() {
    if (!level().isClientSide()
        && (range <= 0 || power <= 0 || blockBreaks <= 0 || tickCount >= 1200)) {
      finish();
      return;
    }
    var before = position();
    super.tick();
    if (isRemoved() || level().isClientSide()) return;
    double moved = position().distanceTo(before);
    if (Float.isFinite(range)) range = Math.max(0, range - (float) moved);
    power -= .5F;
    if (range <= 0 || power <= 0 || blockBreaks <= 0) finish();
  }

  private void finish() {
    if (isExplosiveMode) explode();
    discard();
  }

  private void explode() {
    if (exploded || level().isClientSide()) return;
    exploded = true;
    new NativeBlast(level(), this, getX(), getY(), getZ(), 5, .85F).doExplosion();
  }

  @Override
  protected void onHitEntity(EntityHitResult result) {
    if (!(level() instanceof ServerLevel world)) return;
    if (isExplosiveMode) {
      finish();
      return;
    }
    var target = result.getEntity();
    int damage = (int) power;
    if (damage <= 0) {
      discard();
      return;
    }
    if (target.hurtServer(world, world.damageSources().mobProjectile(this, owner), damage))
      target.igniteForSeconds(damage * (isSmeltMode ? 2 : 1));
    power -= .5F;
    discard();
  }

  @Override
  protected void onHitBlock(BlockHitResult result) {
    super.onHitBlock(result);
    if (!(level() instanceof ServerLevel world)) return;
    if (isExplosiveMode) {
      finish();
      return;
    }
    var pos = result.getBlockPos();
    var state = world.getBlockState(pos);
    var block = state.getBlock();
    if (state.isAir()
        || block == Blocks.GLASS
        || block == Blocks.GLASS_PANE
        || block instanceof StainedGlassBlock
        || block instanceof StainedGlassPaneBlock) {
      setPos(position().add(getDeltaMovement().normalize().scale(.05)));
      return;
    }
    var player = owner instanceof Player actor ? actor : AutomationActors.get(world);
    if (player == null
        || !player.mayBuild()
        || player.blockActionRestricted(world, pos, world.getServer().getDefaultGameType())
        || !player.mayUseItemAt(pos, result.getDirection(), player.getMainHandItem())) {
      discard();
      return;
    }
    float hardness = state.getDestroySpeed(world, pos);
    if (!Float.isFinite(hardness) || hardness < 0) {
      discard();
      return;
    }
    float cost = hardness / 1.5F;
    if (cost > power) {
      discard();
      return;
    }
    var canceled = NeoForge.EVENT_BUS.post(new BreakBlockEvent(world, pos, state, player));
    if (canceled.isCanceled()) {
      discard();
      return;
    }
    power -= cost;
    blockBreaks--;
    if (!removeBlock) return;
    if (block == ExplosionBlocks.ITNT
        || block == ExplosionBlocks.NUKE
        || block == ExplosionBlocks.CLASSIC_NUKE) {
      state.onProjectileHit(world, state, result, this);
      discard();
      return;
    }
    if (!isSmeltMode) {
      if (!world.destroyBlock(pos, true, player)) discard();
      return;
    }
    var output = new ArrayList<ItemStack>();
    if (!state.is(net.minecraft.tags.BlockTags.LOGS_THAT_BURN)
        && !state.is(net.minecraft.tags.BlockTags.PLANKS)) {
      var drops =
          Block.getDrops(state, world, pos, world.getBlockEntity(pos), player, ItemStack.EMPTY);
      for (var drop : drops) {
        var input = new SingleRecipeInput(drop);
        var smelt =
            world.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, world).orElse(null);
        var replacement = smelt == null ? drop.copy() : smelt.value().assemble(input).copy();
        if (smelt != null) replacement.setCount(replacement.getCount() * drop.getCount());
        output.add(replacement);
      }
    }
    if (!world.removeBlock(pos, false)) {
      discard();
      return;
    }
    for (var drop : output) {
      if (drop.getItem() instanceof BlockItem material
          && drop.getCount() == 1
          && world.isEmptyBlock(pos)
          && material.getBlock().defaultBlockState().canSurvive(world, pos))
        world.setBlock(pos, material.getBlock().defaultBlockState(), 3);
      else Block.popResource(world, pos, drop);
    }
    power = 0;
    discard();
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.putFloat("Range", range);
    output.putFloat("Power", power);
    output.putInt("BlockBreaks", blockBreaks);
    output.putBoolean("Explosive", isExplosiveMode);
    output.putBoolean("Smelt", isSmeltMode);
    output.putBoolean("RemoveBlock", removeBlock);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    float distance = input.getFloatOr("Range", 0), savedPower = input.getFloatOr("Power", 0);
    range = Float.isNaN(distance) ? 0 : Math.max(0, distance);
    power = Float.isFinite(savedPower) ? Math.max(0, savedPower) : 0;
    blockBreaks = Math.max(0, input.getIntOr("BlockBreaks", 0));
    isExplosiveMode = input.getBooleanOr("Explosive", false);
    isSmeltMode = input.getBooleanOr("Smelt", false);
    removeBlock = input.getBooleanOr("RemoveBlock", false);
    if (getOwner() instanceof LivingEntity living) owner = living;
  }
}
