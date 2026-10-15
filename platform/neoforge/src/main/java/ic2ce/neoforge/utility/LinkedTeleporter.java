package ic2ce.neoforge.utility;

import ic2ce.api.network.INetworkTileEntityEventListener;
import ic2ce.api.tile.IEnergyStorage;
import ic2ce.engine.fluid.FairAllocation;
import ic2ce.neoforge.block.ActiveBlockEntity;
import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProgressionService;
import ic2ce.neoforge.runtime.SharedRandom;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.world.DirectionSets;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Linked endpoints with a complete, observed reservation across at most six native banks. */
public class LinkedTeleporter extends ActiveBlockEntity implements INetworkTileEntityEventListener {
  private BlockPos target;
  private int targetCheckTicker = SharedRandom.INSTANCE.nextInt(1024), cooldown;

  public LinkedTeleporter(BlockPos position, BlockState state) {
    super(UtilityBlockEntities.TELEPORTER, position, state);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    target =
        data.contains("targetX") && data.contains("targetY") && data.contains("targetZ")
            ? new BlockPos(
                data.getIntOr("targetX", 0),
                data.getIntOr("targetY", 0),
                data.getIntOr("targetZ", 0))
            : null;
    cooldown = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.remove("targetX");
    data.remove("targetY");
    data.remove("targetZ");
    if (target != null) {
      data.putInt("targetX", target.getX());
      data.putInt("targetY", target.getY());
      data.putInt("targetZ", target.getZ());
    }
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    updateComparatorLevel();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var world = getLevel();
    if (world == null) return;
    boolean waiting = cooldown > 0;
    if (waiting) cooldown--;
    if (target == null || !world.hasNeighborSignal(worldPosition)) {
      shutdown(false);
      return;
    }
    activate(false);
    if (waiting) return;
    Entity closest = null;
    double nearest = Double.POSITIVE_INFINITY;
    for (var candidate :
        world.getEntitiesOfClass(
            Entity.class,
            new AABB(worldPosition).inflate(1, 0, 1).expandTowards(0, 2, 0),
            entity -> entity.isAlive() && !entity.isSpectator() && !entity.isPassenger())) {
      double distance =
          worldPosition.distToLowCornerSqr(candidate.getX(), candidate.getY(), candidate.getZ());
      if (distance < nearest) {
        closest = candidate;
        nearest = distance;
      }
    }
    if (closest != null) teleport(closest, Math.sqrt(worldPosition.distSqr(target)));
    else if (++targetCheckTicker >= 1024) {
      targetCheckTicker = 0;
      destination();
    }
  }

  private LinkedTeleporter destination() {
    var world = getLevel();
    if (world == null || target == null) return null;
    if (!target.equals(worldPosition)
        && !world.isOutsideBuildHeight(target)
        && world.getBlockEntity(target) instanceof LinkedTeleporter endpoint
        && !endpoint.isRemoved()) return endpoint;
    setTarget(null);
    shutdown(false);
    return null;
  }

  public void teleport(Entity entity, double ignoredDistance) {
    var world = getLevel();
    if (world == null
        || world.isClientSide()
        || entity == null
        || !entity.isAlive()
        || entity.level() != world
        || entity.isPassenger()
        || entity.isSpectator()
        || !Double.isFinite(ignoredDistance)
        || ignoredDistance < 0
        || cooldown > 0) return;
    var endpoint = destination();
    if (endpoint == null) return;
    int weight = getWeightOf(entity);
    if (weight <= 0) return;
    double distance = Math.sqrt(worldPosition.distSqr(endpoint.worldPosition));
    double bill = weight * Math.pow(distance + 10, 0.7) * 5;
    if (!Double.isFinite(bill) || bill > Integer.MAX_VALUE) return;
    var reservation = reserve((int) bill);
    if (reservation == null) return;
    double x = endpoint.worldPosition.getX() + 0.5,
        y = endpoint.worldPosition.getY() + 1.5,
        z = endpoint.worldPosition.getZ() + 0.5;
    try {
      entity.teleportTo(x, y, z);
    } catch (RuntimeException | Error error) {
      reservation.refund();
      throw error;
    }
    if (entity.distanceToSqr(x, y, z) > 0.01) {
      reservation.refund();
      return;
    }
    cooldown = endpoint.cooldown = 20;
    SideServices.network(true).updateTileEntityField(this, "cooldown");
    SideServices.network(true).updateTileEntityField(endpoint, "cooldown");
    SideServices.network(true).initiateTileEntityEvent(this, 0, true);
    if (entity instanceof Player player && distance >= 1000)
      ProgressionService.issueAchievement(player, "teleportFarAway");
  }

  private record Reservation(IEnergyStorage[] banks, int[] paid) {
    void refund() {
      for (int index = 0; index < paid.length; index++)
        if (paid[index] > 0) banks[index].addEnergy(paid[index]);
    }
  }

  private IEnergyStorage[] banks() {
    var result = new IEnergyStorage[6];
    var world = getLevel();
    if (world == null) return result;
    int count = 0;
    for (var direction : DirectionSets.ALL_DIRS) {
      var position = worldPosition.relative(direction);
      if (!world.isLoaded(position)) continue;
      if (!(world.getBlockEntity(position) instanceof IEnergyStorage bank)
          || !bank.isTeleporterCompatible(direction.getOpposite())) continue;
      boolean seen = false;
      for (int index = 0; index < count; index++)
        if (result[index] == bank) {
          seen = true;
          break;
        }
      if (!seen) result[count++] = bank;
    }
    return result;
  }

  private Reservation reserve(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative teleport energy");
    var world = getLevel();
    if (world == null || world.isClientSide()) return null;
    var sources = banks();
    var available = new int[6];
    long sum = 0;
    for (int index = 0; index < 6; index++)
      if (sources[index] != null) {
        available[index] = Math.max(0, sources[index].getStored());
        sum += available[index];
      }
    if (sum < amount) return null;
    var planned = new int[6];
    FairAllocation.plan(amount, available, planned, targetCheckTicker++);
    var paid = new int[6];
    var reservation = new Reservation(sources, paid);
    try {
      for (int index = 0; index < 6; index++)
        if (planned[index] > 0) {
          var bank = sources[index];
          int before = Math.max(0, bank.getStored());
          bank.addEnergy(-planned[index]);
          paid[index] = (int) Math.clamp((long) before - bank.getStored(), 0, Integer.MAX_VALUE);
          if (paid[index] != planned[index]) {
            reservation.refund();
            return null;
          }
        }
    } catch (RuntimeException | Error error) {
      reservation.refund();
      throw error;
    }
    return reservation;
  }

  /** An insufficient request leaves every adjacent bank intact. */
  public void consumeEnergy(int amount) {
    reserve(amount);
  }

  public boolean tryConsumeEnergy(int amount) {
    return reserve(amount) != null;
  }

  public int getAvailableEnergy() {
    long total = 0;
    for (var bank : banks()) if (bank != null) total += Math.max(0, bank.getStored());
    return (int) Math.min(Integer.MAX_VALUE, total);
  }

  public int getWeightOf(Entity root) {
    if (root == null) return 0;
    boolean inventories =
        NativeConfigValues.getBool(
            ProjectConfiguration.get(), "balance/teleporterUseInventoryWeight");
    var queue = new ArrayDeque<Entity>();
    var seen = new IdentityHashMap<Entity, Boolean>();
    queue.add(root);
    long total = 0;
    while (!queue.isEmpty()) {
      var entity = queue.removeFirst();
      if (seen.put(entity, Boolean.TRUE) != null) continue;
      total +=
          switch (entity) {
            case ItemEntity item -> stackCost(item.getItem());
            case Player player -> {
              long result = 1000;
              if (inventories)
                for (var stack : player.getInventory().getNonEquipmentItems()) result += stackCost(stack);
              yield result;
            }
            case WitherBoss _ -> 5000;
            case EnderDragon _ -> 10000;
            case Ghast _ -> 2500;
            case Animal _, AbstractMinecart _, Boat _ -> 100;
            case Mob _ -> 500;
            default -> 0;
          };
      if (inventories && entity instanceof LivingEntity living) {
        if (!(entity instanceof Player)) total += stackCost(living.getMainHandItem());
        total += stackCost(living.getOffhandItem());
      }
      if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
      queue.addAll(entity.getPassengers());
    }
    return (int) total;
  }

  private static int stackCost(ItemStack stack) {
    return stack.isEmpty()
        ? 0
        : (int)
            Math.min(
                Integer.MAX_VALUE, 100L * stack.getCount() / Math.max(1, stack.getMaxStackSize()));
  }

  private void updateComparatorLevel() {
    comparator.setLevel(target == null ? 0 : 15);
  }

  public boolean hasTarget() {
    return target != null;
  }

  public BlockPos getTarget() {
    return target;
  }

  public void setTarget(BlockPos position) {
    var next = position == null ? null : position.immutable();
    if (Objects.equals(target, next)) return;
    target = next;
    setChanged();
    updateComparatorLevel();
    var world = getLevel();
    if (world != null && !world.isClientSide())
      SideServices.network(true).updateTileEntityField(this, "target");
  }

  @Override
  protected boolean canEntityDestroy(Entity entity) {
    return !(entity instanceof EnderDragon) && !(entity instanceof WitherBoss);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_TELEPORTER_CHARGE;
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("target");
    fields.add("cooldown");
    return fields;
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive()) {
      if (cooldown > 0) {
        cooldown--;
        spawnGreenParticles(2, worldPosition);
      } else spawnBlueParticles(2, worldPosition);
    }
  }

  public void spawnBlueParticles(int count, BlockPos position) {
    particles(count, position, 0x2055ff);
  }

  public void spawnGreenParticles(int count, BlockPos position) {
    particles(count, position, 0x20ff55);
  }

  private void particles(int count, BlockPos position, int color) {
    var world = getLevel();
    if (world == null || !world.isClientSide() || position == null) return;
    var random = world.getRandom();
    var effect = new DustParticleOptions(color, 1);
    for (int index = 0; index < Math.clamp(count, 0, 64); index++)
      for (int height = 1; height <= 2; height++)
        world.addParticle(
            effect,
            position.getX() + random.nextFloat(),
            position.getY() + height + random.nextFloat(),
            position.getZ() + random.nextFloat(),
            0,
            0,
            0);
  }

  @Override
  public void onNetworkEvent(int event) {
    var world = getLevel();
    if (event != 0 || world == null || !world.isClientSide()) return;
    world.playLocalSound(
        worldPosition.getX(),
        worldPosition.getY(),
        worldPosition.getZ(),
        SoundDefinitions.MACHINE_TELEPORTER_USE,
        SoundSource.BLOCKS,
        1,
        1,
        true);
    spawnBlueParticles(20, worldPosition);
    spawnBlueParticles(20, target);
  }
}
