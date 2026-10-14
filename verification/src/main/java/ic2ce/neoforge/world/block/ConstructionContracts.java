package ic2ce.neoforge.world.block;

import com.mojang.authlib.GameProfile;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.world.surface.SurfaceMap;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Actual native both-hand wood stripping, foam bills, permissions and wall persistence/render
 * descriptors.
 */
public final class ConstructionContracts {
  public record Result(int checks, List<String> failures) {}

  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean pass, String text) {
    checks++;
    if (!pass) failures.add("native construction: " + text);
  }

  public static Result run(ServerLevel world) {
    return new ConstructionContracts().exercise(world);
  }

  public static final class Observer {
    BlockPos position;
    int resin;
    boolean canceled;

    @SubscribeEvent
    public void joined(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof ItemEntity item
          && item.getItem().is(ItemDefinitions.RESIN)
          && item.position().distanceToSqr(Vec3.atCenterOf(position)) < 9)
        resin += item.getItem().getCount();
    }

    @SubscribeEvent
    public void modified(BlockEvent.BlockToolModificationEvent event) {
      if (canceled && event.getPos().equals(position)) event.setCanceled(true);
    }
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(11408, 100, 11408);
    world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
    world.getChunkAt(position);
    var actor =
        FakePlayerFactory.get(
            world,
            new GameProfile(
                UUID.fromString("c3b6fa2d-45a9-4f04-9854-dfc4ab523528"), "ConstructionProbe"));
    actor.getInventory().clearContent();
    actor.getAbilities().mayBuild = true;
    actor.getAbilities().instabuild = false;
    actor.setPos(position.getX(), position.getY(), position.getZ() - 2);
    var observer = new Observer();
    observer.position = position;
    NeoForge.EVENT_BUS.register(observer);
    try {
      for (var axis : Direction.Axis.values())
        for (var hand : InteractionHand.values()) {
          observer.resin = 0;
          var state =
              BlockDefinitions.RUBBER_LOG
                  .defaultBlockState()
                  .setValue(RotatedPillarBlock.AXIS, axis)
                  .setValue(ResinLog.stateProperty, ResinLog.RubberWoodState.wet_north);
          world.setBlock(position, state, 3);
          var axe = new ItemStack(Items.IRON_AXE);
          actor.setItemInHand(hand, axe);
          actor.setItemInHand(
              hand == InteractionHand.MAIN_HAND
                  ? InteractionHand.OFF_HAND
                  : InteractionHand.MAIN_HAND,
              new ItemStack(Items.STICK));
          var hit = new BlockHitResult(Vec3.atCenterOf(position), Direction.NORTH, position, false);
          state.useItemOn(axe, world, actor, hand, hit);
          check(
              world.getBlockState(position).is(BlockDefinitions.STRIPPED_RUBBER_LOG)
                  && world.getBlockState(position).getValue(RotatedPillarBlock.AXIS) == axis
                  && axe.getDamageValue() == 1
                  && observer.resin >= 1
                  && observer.resin <= 2,
              "native axe strip preserves axis, one wear and one resin bill in "
                  + hand
                  + " "
                  + axis);
          check(
              actor
                  .getItemInHand(
                      hand == InteractionHand.MAIN_HAND
                          ? InteractionHand.OFF_HAND
                          : InteractionHand.MAIN_HAND)
                  .is(Items.STICK),
              "strip preserves unused native hand");
        }
      var wet =
          BlockDefinitions.RUBBER_LOG
              .defaultBlockState()
              .setValue(ResinLog.stateProperty, ResinLog.RubberWoodState.wet_east);
      world.setBlock(position, wet, 3);
      var axe = new ItemStack(Items.IRON_AXE);
      actor.setItemInHand(InteractionHand.MAIN_HAND, axe);
      var hit = new BlockHitResult(Vec3.atCenterOf(position), Direction.NORTH, position, false);
      observer.resin = 0;
      observer.canceled = true;
      wet.useItemOn(axe, world, actor, InteractionHand.MAIN_HAND, hit);
      check(
          world.getBlockState(position) == wet && axe.getDamageValue() == 0 && observer.resin == 0,
          "actual native modification cancellation cannot consume axe wear or emit resin");
      observer.canceled = false;
      var wood = BlockDefinitions.RUBBER_WOOD.defaultBlockState();
      world.setBlock(position, wood, 3);
      actor.setItemInHand(InteractionHand.OFF_HAND, axe);
      actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
      wood.useItemOn(axe, world, actor, InteractionHand.OFF_HAND, hit);
      check(
          world.getBlockState(position).is(BlockDefinitions.STRIPPED_RUBBER_WOOD)
              && axe.getDamageValue() == 1,
          "all-bark wood supports actual native offhand stripping");
      for (var type : CuringFoam.FoamType.values())
        for (var hand : InteractionHand.values()) {
          var foam =
              BlockDefinitions.FOAM.defaultBlockState().setValue(CuringFoam.typeProperty, type);
          world.setBlock(position, foam, 3);
          var sand = new ItemStack(Items.SAND, 3);
          actor.setItemInHand(hand, sand);
          actor.setItemInHand(
              hand == InteractionHand.MAIN_HAND
                  ? InteractionHand.OFF_HAND
                  : InteractionHand.MAIN_HAND,
              new ItemStack(Items.STICK));
          foam.useItemOn(sand, world, actor, hand, hit);
          check(
              world.getBlockState(position) == type.getResult()
                  && sand.getCount() == 2
                  && actor
                      .getItemInHand(
                          hand == InteractionHand.MAIN_HAND
                              ? InteractionHand.OFF_HAND
                              : InteractionHand.MAIN_HAND)
                      .is(Items.STICK),
              "actual " + type + " foam uses exactly one native sand in " + hand);
        }
      var foam = BlockDefinitions.FOAM.defaultBlockState();
      world.setBlock(position, foam, 3);
      actor.getAbilities().mayBuild = false;
      var sand = new ItemStack(Items.SAND, 2);
      actor.setItemInHand(InteractionHand.MAIN_HAND, sand);
      foam.useItemOn(sand, world, actor, InteractionHand.MAIN_HAND, hit);
      check(
          world.getBlockState(position) == foam && sand.getCount() == 2,
          "denied native construction preserves world and bill");
      actor.getAbilities().mayBuild = true;
      int ticks = world.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
      world.getGameRules().set(GameRules.RANDOM_TICK_SPEED, 0, world.getServer());
      try {
        ((CuringFoam) BlockDefinitions.FOAM)
            .randomTick(foam, world, position, RandomSource.create(1));
        check(
            world.getBlockState(position) == foam,
            "disabled random ticking does not throw or cure an explicitly invoked foam tick");
      } finally {
        world.getGameRules().set(GameRules.RANDOM_TICK_SPEED, ticks, world.getServer());
      }
      check(
          CuringFoam.getHardenChance(world, position, foam, CuringFoam.FoamType.normal)
              == 2
                  * CuringFoam.getHardenChance(
                      world, position, foam, CuringFoam.FoamType.reinforced),
          "reinforced foam retains twice the physical curing time");
      for (var color : DyeColor.values())
        check(
            ConstructionWall.get(color) != null, "native construction pigment registered " + color);
      world.setBlock(position, BlockDefinitions.OBSCURED_WALL.defaultBlockState(), 3);
      var wall = (ImprintedWall) world.getBlockEntity(position);
      wall.setColor(DyeColor.BLUE);
      var saved = new CompoundTag();
      wall.saveAdditional(saved);
      var restored =
          new ImprintedWall(position, BlockDefinitions.OBSCURED_WALL.defaultBlockState());
      restored.load(saved);
      var resaved = new CompoundTag();
      restored.saveAdditional(resaved);
      check(
          resaved.getIntOr("color", -1) == DyeColor.BLUE.getId(),
          "actual imprinted wall pigment survives native persistence");
      var changes = new int[1];
      var surfaces = new SurfaceMap(wall, () -> changes[0]++);
      int[] tint = {-1};
      var data =
          new SurfaceMap.ObscurationData(
              Blocks.STONE.defaultBlockState(), "", Direction.NORTH, tint);
      surfaces.applyObscuration(Direction.NORTH, data);
      tint[0] = 0;
      data.colorMultipliers[0] = 0;
      var render = surfaces.getRenderState();
      render[Direction.NORTH.ordinal()].colorMultipliers[0] = 123;
      check(
          surfaces.getRenderState()[Direction.NORTH.ordinal()].colorMultipliers[0] == -1
              && changes[0] == 1,
          "native surface publication owns constructor, action and render tints");
      var persisted = surfaces.writeToNbt();
      var surfacesRestored = new SurfaceMap(wall, () -> {});
      surfacesRestored.readFromNbt(persisted);
      check(surfacesRestored.hasObscuration(), "actual six-face data survives persistence");
      surfacesRestored.readFromNbt(new CompoundTag());
      check(
          !surfacesRestored.hasObscuration(),
          "empty native saved data replaces all previous surface faces atomically");
    } finally {
      NeoForge.EVENT_BUS.unregister(observer);
      actor.getInventory().clearContent();
      actor.getAbilities().mayBuild = true;
      world.removeBlock(position, false);
    }
    return new Result(checks, List.copyOf(failures));
  }
}
