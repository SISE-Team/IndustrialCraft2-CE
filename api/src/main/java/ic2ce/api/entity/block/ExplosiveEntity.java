package ic2ce.api.entity.block;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Primed explosive motion/fuse API, independent of the platform blast implementation. */
public abstract class ExplosiveEntity extends Entity {
  private static final EntityDataAccessor<Integer> FUSE =
      SynchedEntityData.defineId(ExplosiveEntity.class, EntityDataSerializers.INT);
  public LivingEntity causingEntity;
  public float explosivePower = 4;
  public float dropRate = 0.3F;
  public float damageVsEntities = 1;
  public int radiationRange;
  public BlockState renderBlockState = Blocks.TNT.defaultBlockState();
  private UUID igniter;

  public ExplosiveEntity(EntityType<? extends Entity> type, Level world) {
    super(type, world);
    blocksBuilding = true;
  }

  public ExplosiveEntity(
      EntityType<? extends Entity> type,
      Level world,
      double x,
      double y,
      double z,
      int fuse,
      float power,
      float drops,
      float damage,
      BlockState render,
      int radiation) {
    this(type, world);
    setPos(x, y, z);
    xo = x;
    yo = y;
    zo = z;
    double angle = world.getRandom().nextDouble() * Math.TAU;
    setDeltaMovement(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
    setFuse(fuse);
    explosivePower = power;
    dropRate = drops;
    damageVsEntities = damage;
    renderBlockState = render;
    radiationRange = radiation;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(FUSE, 80);
  }

  public final int getFuse() {
    return entityData.get(FUSE);
  }

  public final void setFuse(int ticks) {
    entityData.set(FUSE, Math.max(0, ticks));
  }

  @Override
  public void tick() {
    Vec3 velocity = getDeltaMovement();
    if (!isNoGravity()) velocity = velocity.add(0, -0.04, 0);
    setDeltaMovement(velocity);
    move(MoverType.SELF, velocity);
    velocity = getDeltaMovement().scale(0.98);
    setDeltaMovement(onGround() ? velocity.multiply(0.7, -0.5, 0.7) : velocity);
    int remaining = Math.max(0, getFuse() - 1);
    setFuse(remaining);
    if (remaining == 0) {
      discard();
      if (!level().isClientSide()) ExplosionAccess.detonate(this);
    } else if (level().isClientSide()) {
      level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 0, 0, 0);
    }
  }

  public ExplosiveEntity setCausingEntity(LivingEntity cause) {
    causingEntity = cause;
    igniter = cause == null ? null : cause.getUUID();
    return this;
  }

  @Nullable
  public LivingEntity getCausingEntity() {
    if (causingEntity == null
        && igniter != null
        && level() instanceof ServerLevel world
        && world.getEntity(igniter) instanceof LivingEntity found) causingEntity = found;
    return causingEntity;
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    output.putShort("Fuse", (short) getFuse());
    output.putInt("FuseTicks", getFuse());
    output.putFloat("Power", explosivePower);
    output.putFloat("DropRate", dropRate);
    output.putFloat("EntityDamage", damageVsEntities);
    output.putInt("RadiationRange", radiationRange);
    output.store("RenderBlock", BlockState.CODEC, renderBlockState);
    LivingEntity cause = getCausingEntity();
    UUID uuid = cause == null ? igniter : cause.getUUID();
    if (uuid != null) output.store("Igniter", UUIDUtil.CODEC, uuid);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    setFuse(input.getIntOr("FuseTicks", input.getShortOr("Fuse", (short) 0)));
    float power = input.getFloatOr("Power", explosivePower);
    explosivePower = Float.isFinite(power) && power >= 0 ? power : 0;
    float drops = input.getFloatOr("DropRate", dropRate);
    dropRate = Float.isFinite(drops) ? Math.clamp(drops, 0, 1) : 0;
    float damage = input.getFloatOr("EntityDamage", damageVsEntities);
    damageVsEntities = Float.isFinite(damage) && damage >= 0 ? damage : 0;
    radiationRange = Math.max(0, input.getIntOr("RadiationRange", radiationRange));
    renderBlockState = input.read("RenderBlock", BlockState.CODEC).orElse(renderBlockState);
    igniter = input.read("Igniter", UUIDUtil.CODEC).orElse(null);
    causingEntity = null;
  }

  @Override
  public boolean canBeCollidedWith(@Nullable Entity other) {
    return !isRemoved();
  }

  @Override
  public boolean isPickable() {
    return !isRemoved();
  }

  @Override
  protected MovementEmission getMovementEmission() {
    return MovementEmission.NONE;
  }

  @Override
  public boolean hurtServer(
      ServerLevel world, net.minecraft.world.damagesource.DamageSource source, float amount) {
    return false;
  }

  @Override
  public EntityDimensions getDimensions(Pose pose) {
    return super.getDimensions(pose).withEyeHeight(0.15F);
  }
}
