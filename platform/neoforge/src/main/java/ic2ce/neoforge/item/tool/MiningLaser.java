package ic2ce.neoforge.item.tool;

import ic2ce.api.network.INetworkItemEventListener;
import ic2ce.neoforge.item.PriorityUsableItem;
import ic2ce.core.util.*;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.projectile.MiningBeam;
import ic2ce.neoforge.projectile.Vector3;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Eight native beam policies own separate energy, reach, power, geometry and smelting/explosion
 * semantics.
 */
public class MiningLaser extends PoweredTool
    implements INetworkItemEventListener, PriorityUsableItem {
  private enum Beam {
    MINING(1250, Float.POSITIVE_INFINITY, 5, Integer.MAX_VALUE, "mining"),
    FOCUS(100, 4, 5, 1, "lowFocus"),
    LONG(5000, Float.POSITIVE_INFINITY, 20, Integer.MAX_VALUE, "longRange"),
    HORIZONTAL(3000, Float.POSITIVE_INFINITY, 5, Integer.MAX_VALUE, "horizontal"),
    HEAT(2500, Float.POSITIVE_INFINITY, 8, Integer.MAX_VALUE, "superHeat"),
    SCATTER(10000, Float.POSITIVE_INFINITY, 12, Integer.MAX_VALUE, "scatter"),
    EXPLOSIVE(5000, Float.POSITIVE_INFINITY, 12, Integer.MAX_VALUE, "explosive"),
    GRID(7500, Float.POSITIVE_INFINITY, 5, Integer.MAX_VALUE, "3x3");
    final int cost, blocks;
    final float range, power;
    final String key;

    Beam(int cost, float range, float power, int blocks, String key) {
      this.cost = cost;
      this.range = range;
      this.power = power;
      this.blocks = blocks;
      this.key = "ic2.tooltip.mode." + key;
    }
  }

  private static final Beam[] MODES = Beam.values();

  public MiningLaser(Properties properties) {
    super(properties.rarity(Rarity.UNCOMMON), 100);
    maxCharge = 300_000;
    transferLimit = 512;
    tier = 3;
  }

  private static Beam mode(ItemStack stack) {
    var data = StackTag.get(stack);
    return MODES[Math.floorMod(data == null ? 0 : data.getIntOr("laserSetting", 0), MODES.length)];
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    super.appendHoverText(stack, context, display, output, flags);
    output.accept(
        Component.translatable("ic2.tooltip.mode", Component.translatable(mode(stack).key)));
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    var result = new ArrayList<>(super.getHudInfo(stack, advanced));
    result.add(
        LocalizedText.translate("ic2.tooltip.mode", LocalizedText.translate(mode(stack).key)));
    return List.copyOf(result);
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    var policy = mode(stack);
    if (ProjectRuntime.keyboard.isModeSwitchKeyDown(owner)) {
      int next = (policy.ordinal() + 1) % MODES.length;
      StackTag.update(
          stack,
          data -> {
            data.putInt("laserSetting", next);
            return data;
          });
      ProjectRuntime.sideProxy.messagePlayer(owner, "ic2.tooltip.mode", MODES[next].key);
      return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }
    if (policy == Beam.HORIZONTAL || policy == Beam.GRID) return InteractionResult.PASS;
    if (!consumeEnergy(stack, policy.cost, owner)) return InteractionResult.FAIL;
    int emitted = 0;
    var direction = owner.getLookAngle();
    var start = owner.getEyePosition().add(direction.scale(.2));
    if (policy == Beam.SCATTER) {
      var right = right(direction, owner);
      var up = right.cross(direction).normalize();
      for (int x = -2; x <= 2; x++)
        for (int y = -2; y <= 2; y++)
          if (fire(
              stack,
              world,
              start,
              direction.scale(8).add(right.scale(x)).add(up.scale(y)).normalize(),
              owner,
              policy)) emitted++;
    } else if (fire(stack, world, start, direction, owner, policy)) emitted++;
    if (emitted == 0) {
      refund(stack, policy.cost, owner);
      return InteractionResult.FAIL;
    }
    ProjectRuntime.network.get(true).initiateItemEvent(owner, stack, policy.ordinal(), true);
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var owner = context.getPlayer();
    var world = context.getLevel();
    if (owner == null || !stack.is(this) || ProjectRuntime.keyboard.isModeSwitchKeyDown(owner))
      return InteractionResult.PASS;
    var policy = mode(stack);
    if (policy != Beam.HORIZONTAL && policy != Beam.GRID) return InteractionResult.PASS;
    if (!owner.mayBuild()
        || !owner.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack))
      return InteractionResult.PASS;
    var look = owner.getLookAngle();
    boolean horizontal = Math.abs(look.y) < 1 / Math.sqrt(2);
    if (policy == Beam.HORIZONTAL && !horizontal) {
      if (!world.isClientSide())
        ProjectRuntime.sideProxy.messagePlayer(owner, "ic2.laser.angle_steep");
      return InteractionResult.FAIL;
    }
    if (world.isClientSide()) return InteractionResult.SUCCESS;
    if (!consumeEnergy(stack, policy.cost, owner)) return InteractionResult.FAIL;
    Vec3 direction, start, right, up;
    if (horizontal) {
      direction = new Vec3(look.x, 0, look.z).normalize();
      start =
          new Vec3(
                  owner.getEyePosition().x,
                  context.getClickedPos().getY() + .5,
                  owner.getEyePosition().z)
              .add(direction.scale(.2));
      right = new Vec3(-direction.z, 0, direction.x);
      up = new Vec3(0, 1, 0);
    } else {
      direction = new Vec3(0, Math.signum(look.y), 0);
      start =
          new Vec3(
                  context.getClickedPos().getX() + .5,
                  owner.getEyePosition().y,
                  context.getClickedPos().getZ() + .5)
              .add(direction.scale(.2));
      right = new Vec3(1, 0, 0);
      up = new Vec3(0, 0, 1);
    }
    int emitted = 0;
    int radius = policy == Beam.GRID ? 1 : 0;
    for (int x = -radius; x <= radius; x++)
      for (int y = -radius; y <= radius; y++)
        if (fire(
            stack, world, start.add(right.scale(x)).add(up.scale(y)), direction, owner, policy))
          emitted++;
    if (emitted == 0) {
      refund(stack, policy.cost, owner);
      return InteractionResult.FAIL;
    }
    ProjectRuntime.network.get(true).initiateItemEvent(owner, stack, policy.ordinal(), true);
    return InteractionResult.SUCCESS;
  }

  private static Vec3 right(Vec3 direction, LivingEntity owner) {
    var right = direction.cross(new Vec3(0, 1, 0));
    if (right.lengthSqr() < 1e-8) {
      double angle = Math.toRadians(owner.getYRot());
      return new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }
    return right.normalize();
  }

  private boolean fire(
      ItemStack stack, Level world, Vec3 start, Vec3 direction, LivingEntity owner, Beam policy) {
    return shootLaser(
        stack,
        world,
        new Vector3(start),
        new Vector3(direction),
        owner,
        policy.range,
        policy.power,
        policy.blocks,
        policy == Beam.EXPLOSIVE,
        policy == Beam.HEAT);
  }

  private static void refund(ItemStack stack, int bill, Player owner) {
    if (!owner.getAbilities().instabuild)
      ic2ce.api.item.ElectricItem.manager.charge(stack, bill, Integer.MAX_VALUE, true, false);
  }

  public boolean shootLaser(
      ItemStack stack,
      Level world,
      LivingEntity owner,
      float range,
      float power,
      int blocks,
      boolean explosive,
      boolean smelt) {
    return shootLaser(
        stack,
        world,
        new Vector3(owner.getLookAngle()),
        owner,
        range,
        power,
        blocks,
        explosive,
        smelt);
  }

  public boolean shootLaser(
      ItemStack stack,
      Level world,
      Vector3 direction,
      LivingEntity owner,
      float range,
      float power,
      int blocks,
      boolean explosive,
      boolean smelt) {
    var start = owner.getEyePosition().add(direction.x * .2, direction.y * .2, direction.z * .2);
    return shootLaser(
        stack, world, new Vector3(start), direction, owner, range, power, blocks, explosive, smelt);
  }

  public boolean shootLaser(
      ItemStack stack,
      Level world,
      Vector3 start,
      Vector3 direction,
      LivingEntity owner,
      float range,
      float power,
      int blocks,
      boolean explosive,
      boolean smelt) {
    if (world.isClientSide()
        || owner == null
        || !Double.isFinite(direction.x)
        || !Double.isFinite(direction.y)
        || !Double.isFinite(direction.z)) return false;
    var motion = new Vec3(direction.x, direction.y, direction.z);
    if (motion.lengthSqr() < 1e-12) return false;
    var bullet = new MiningBeam(world, start, direction, owner, range, power, blocks, explosive);
    bullet.init(owner, range, power, blocks, explosive, smelt, true);
    bullet.shoot(motion.x, motion.y, motion.z, 3, 0);
    bullet.setDeltaMovement(
        bullet
            .getDeltaMovement()
            .add(
                owner.getDeltaMovement().x,
                owner.onGround() ? 0 : owner.getDeltaMovement().y,
                owner.getDeltaMovement().z));
    return world.addFreshEntity(bullet);
  }

  public Rarity getRarity(ItemStack stack) {
    return Rarity.UNCOMMON;
  }

  @Override
  public void onNetworkEvent(ItemStack stack, Player owner, int event) {
    if (!owner.level().isClientSide() || event < 0 || event >= MODES.length) return;
    SoundEvent sound =
        switch (MODES[event]) {
          case FOCUS -> SoundDefinitions.ITEM_LASER_LOW_FOCUS;
          case LONG -> SoundDefinitions.ITEM_LASER_LONG_RANGE;
          case SCATTER, GRID -> SoundDefinitions.ITEM_LASER_SCATTER;
          case EXPLOSIVE -> SoundDefinitions.ITEM_LASER_EXPLOSIVE;
          default -> SoundDefinitions.ITEM_LASER_SHOOT;
        };
    owner.level().playSound(owner, owner.blockPosition(), sound, SoundSource.PLAYERS, 1, 1);
  }
}
