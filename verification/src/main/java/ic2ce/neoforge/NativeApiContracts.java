package ic2ce.neoforge;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.RecipeOutputWeighted;
import ic2ce.neoforge.explosion.PrimedNuclearCharge;
import ic2ce.neoforge.registry.EntityDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

/** Real loader API services, fluid components and explosive save/load contracts. */
public final class NativeApiContracts {
  public record Result(int checks, List<String> failures) {}

  private static PrimedNuclearCharge pendingIgniter;
  private static net.minecraft.world.entity.LivingEntity expectedIgniter;
  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean success, String label) {
    checks++;
    if (!success) failures.add("native API: " + label);
  }

  public static Result run(ServerLevel world) {
    return new NativeApiContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    world.setChunkForced(8196 >> 4, 8196 >> 4, true);
    world.getChunk(8196 >> 4, 8196 >> 4);
    check(
        ic2ce.api.info.Info.getItemInfo().getFuelValue(new ItemStack(Items.COAL), false) > 0,
        "public fuel API resolves migrated package services");
    var nativeValue = new FluidStack(Fluids.WATER, 1200);
    nativeValue.set(DataComponents.CUSTOM_NAME, Component.literal("Component probe"));
    Ic2FluidStack value = new Ic2FluidStackImpl(nativeValue), copy = value.copy();
    check(
        value.hasExactFluid(copy) && copy.getAmountMb() == 1200,
        "copy retains non-custom-data components");
    check(!value.hasExactFluid(Fluids.WATER), "component fluid is distinct from bare fluid");
    copy.setAmountMb(500);
    check(value.getAmountMb() == 1200, "copied quantity is independent");
    var data = new CompoundTag();
    value.toNbt(data);
    var restored = Ic2FluidStack.read(data);
    check(
        restored != null && restored.equals(value),
        "complete native components round-trip through fluid save format");
    var legacy = new CompoundTag();
    legacy.putString("FluidName", "minecraft:water");
    legacy.putInt("Amount", 900);
    check(Ic2FluidStack.read(legacy).getAmountMb() == 900, "legacy fluid quantity format retained");
    var malformed = new CompoundTag();
    malformed.putString("FluidName", "invalid space");
    malformed.putInt("Amount", 20);
    check(
        Ic2FluidStack.read(malformed) == null,
        "malformed fluid identifier rejected without crashing");
    boolean immutable = false;
    try {
      Ic2FluidStack.EMPTY.setAmountMb(1);
    } catch (IllegalArgumentException error) {
      immutable = true;
    }
    check(
        immutable && Ic2FluidStack.EMPTY.getAmountMb() == 0,
        "global empty fluid cannot acquire quantity");
    var weighted =
        new RecipeOutputWeighted()
            .addOutput(new ItemStack(Items.IRON_INGOT), Integer.MAX_VALUE)
            .addOutput(new ItemStack(Items.GOLD_INGOT), Integer.MAX_VALUE);
    check(weighted.drawOutput() != null, "large weighted outputs avoid signed integer overflow");
    boolean readOnly = false;
    try {
      weighted.getWeights().set(0, 0);
    } catch (UnsupportedOperationException error) {
      readOnly = true;
    }
    check(readOnly, "weight view cannot invalidate the selection index");
    var counter = new int[1];
    weighted.forEach((_, _) -> counter[0]++);
    check(counter[0] == 2, "weighted iteration visits each output exactly once");
    var source = new PrimedNuclearCharge(world, 8196, 100, 8196, 12.5F, 24);
    source.setFuse(40000);
    source.dropRate = 0.41F;
    source.damageVsEntities = 2.75F;
    var cow = EntityType.COW.create(world, EntitySpawnReason.TRIGGERED);
    cow.setPos(8196, 100, 8196);
    world.addFreshEntity(cow);
    source.setCausingEntity(cow);
    var output =
        TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
    source.saveWithoutId(output);
    var destination = new PrimedNuclearCharge(EntityDefinitions.NUKE, world);
    destination.load(
        TagValueInput.create(
            ProblemReporter.DISCARDING, world.registryAccess(), output.buildResult()));
    check(destination.getFuse() == 40000, "long fuse saves without narrowing to short");
    check(
        destination.explosivePower == 12.5F
            && destination.radiationRange == 24
            && destination.dropRate == 0.41F
            && destination.damageVsEntities == 2.75F,
        "explosive parameters survive entity reload");
    check(
        destination.renderBlockState.equals(source.renderBlockState),
        "explosive renderer block survives entity reload");
    var secondOutput =
        TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
    destination.saveWithoutId(secondOutput);
    check(
        java.util.Objects.equals(
            output.buildResult().get("Igniter"), secondOutput.buildResult().get("Igniter")),
        "saved igniter identity survives reload before entity tracking starts");
    var mutablePos = new net.minecraft.core.BlockPos.MutableBlockPos(8196, 100, 8196);
    int[] tints = {0xff00ff};
    var imprint =
        new ic2ce.api.event.RetextureEvent(
            world,
            mutablePos,
            net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
            net.minecraft.core.Direction.NORTH,
            null,
            net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState(),
            "normal",
            net.minecraft.core.Direction.SOUTH,
            tints);
    mutablePos.move(1, 0, 0);
    tints[0] = 0;
    check(
        imprint.pos.getX() == 8196 && imprint.refColorMultipliers[0] == 0xff00ff,
        "public surface proposal owns caller coordinate and tint arrays");
    var names = new java.util.LinkedHashSet<String>(List.of("classic", "modern"));
    var profile = new ic2ce.api.energy.ProfileEvent.Load(names, "modern");
    names.clear();
    boolean fixedProfile = false;
    try {
      profile.loaded.clear();
    } catch (UnsupportedOperationException expected) {
      fixedProfile = true;
    }
    check(
        fixedProfile && profile.loaded.size() == 2,
        "public profile event owns immutable ordered membership");
    var node = new ic2ce.api.energy.NodeStats(1, 2, 3);
    check(
        node.equals(new ic2ce.api.energy.NodeStats(1, 2, 3))
            && node.getEnergyIn() == 1
            && node.getEnergyOut() == 2
            && node.getVoltage() == 3,
        "record energy observation retains public getters and value equality");
    int[] drawn = {0};
    var legacyKinetic =
        new ic2ce.api.energy.tile.IKineticSource() {
          public int maxrequestkineticenergyTick(net.minecraft.core.Direction side) {
            return 100;
          }

          public int requestkineticenergy(net.minecraft.core.Direction side, int amount) {
            drawn[0] += amount;
            return amount;
          }
        };
    check(
        legacyKinetic.drawKineticEnergy(net.minecraft.core.Direction.NORTH, 7, true) == 7
            && drawn[0] == 0,
        "legacy kinetic simulation honors the actual requested quantity without drawing");
    check(
        legacyKinetic.drawKineticEnergy(net.minecraft.core.Direction.NORTH, -1, false) == 0
            && drawn[0] == 0,
        "negative legacy kinetic draw cannot pay a reversed bill");
    check(
        legacyKinetic.drawKineticEnergy(net.minecraft.core.Direction.NORTH, 150, false) == 100
            && drawn[0] == 100,
        "legacy kinetic actual draw stays within advertised native bandwidth");
    ic2ce.api.tile.ExplosionWhitelist.addWhitelistedBlock(
        net.minecraft.world.level.block.Blocks.STONE);
    check(
        ic2ce.api.tile.ExplosionWhitelist.isBlockWhitelisted(
                net.minecraft.world.level.block.Blocks.STONE)
            && !ic2ce.api.tile.ExplosionWhitelist.isBlockWhitelisted(null),
        "public blast whitelist owns a valid identity generation");
    ic2ce.api.tile.ExplosionWhitelist.removeWhitelistedBlock(
        net.minecraft.world.level.block.Blocks.STONE);
    check(
        !ic2ce.api.tile.ExplosionWhitelist.isBlockWhitelisted(
            net.minecraft.world.level.block.Blocks.STONE),
        "public blast whitelist detach publishes the new generation");
    var tank = new ItemStack(ic2ce.neoforge.registry.ItemDefinitions.CF_PACK);
    var reservoir = (ic2ce.neoforge.item.armor.WearableFluidReservoir) tank.getItem();
    var foam =
        Ic2FluidStack.create(
            ic2ce.neoforge.registry.FluidDefinitions.CONSTRUCTION_FOAM.still, 2000);
    check(
        reservoir.fillMb(tank, foam, true, null) == 2000
            && ic2ce.core.util.StackTag.get(tank) == null,
        "reservoir fill simulation writes no item data");
    reservoir.fillMb(tank, foam, false, null);
    var before = tank.copy();
    var simulated = reservoir.drainMb(tank, 900, true, null);
    simulated.setAmountMb(1);
    check(
        ItemStack.matches(before, tank) && reservoir.getFluidStack(tank).getAmountMb() == 2000,
        "reservoir drain simulation owns its returned quantity");
    var drained = reservoir.drainMb(tank, 750, false, null);
    check(
        drained.getAmountMb() == 750 && reservoir.getFluidStack(tank).getAmountMb() == 1250,
        "reservoir actual drain conserves exactly seven hundred fifty millibuckets");
    ic2ce.core.fluid.StandardFluidItem.setFs(tank, foam.copyWithAmountMb(90000));
    before = tank.copy();
    check(
        reservoir.fillMb(tank, foam, false, null) == 0 && ItemStack.matches(before, tank),
        "corrupt overfull native reservoir never returns a negative fill or removes saved fluid");
    var container = new ItemStack(Items.BUCKET, 2);
    var water = Ic2FluidStack.create(Fluids.WATER, 1500);
    var proposal = new ic2ce.api.recipe.IFillFluidContainerRecipeManager.Input(container, water);
    container.setCount(9);
    water.setAmountMb(1);
    check(
        proposal.container.getCount() == 2 && proposal.fluid.getAmountMb() == 1500,
        "public fill proposal owns both mutable physical bills");
    var fill =
        new ic2ce.neoforge.recipe.processing.ContainerFillingRecipes().apply(proposal, false);
    check(
        fill != null
            && fill.getOutput().stream().anyMatch(stack -> stack.is(Items.WATER_BUCKET))
            && fill.getAdjustedInput().container.getCount() == 1
            && fill.getAdjustedInput().fluid.getAmountMb() == 500
            && proposal.container.getCount() == 2
            && proposal.fluid.getAmountMb() == 1500,
        "native dynamic fill manager conserves the candidate two-bucket and"
            + " fifteen-hundred-millibucket bills without mutating its caller");
    var drain =
        new ic2ce.neoforge.recipe.processing.ContainerDrainingRecipes()
            .apply(new ItemStack(Items.WATER_BUCKET), false);
    check(
        drain != null
            && drain.getOutput().fluid.getAmountMb() == 1000
            && drain.getOutput().container.stream().anyMatch(stack -> stack.is(Items.BUCKET)),
        "native dynamic draining returns the actual bucket and whole water bill");
    var savedItem = new CompoundTag();
    savedItem.putInt("Slot", 4);
    var titled = new ItemStack(Items.IRON_INGOT, 2);
    titled.set(DataComponents.CUSTOM_NAME, Component.literal("previous saved title"));
    ic2ce.core.util.StackTag.save(world.registryAccess(), titled, savedItem);
    ic2ce.core.util.StackTag.save(
        world.registryAccess(), new ItemStack(Items.IRON_INGOT, 3), savedItem);
    var parsed = ic2ce.core.util.StackTag.parse(world.registryAccess(), savedItem);
    check(
        parsed.getCount() == 3
            && !parsed.has(DataComponents.CUSTOM_NAME)
            && savedItem.getIntOr("Slot", 0) == 4,
        "reused item snapshot removes stale components and preserves surrounding slot data");
    var tag = new CompoundTag();
    tag.putInt("owned", 3);
    ic2ce.core.util.StackTag.set(parsed, tag);
    tag.putInt("owned", 9);
    check(
        ic2ce.core.util.StackTag.get(parsed).getIntOr("owned", 0) == 3,
        "native item custom-data writes never retain caller tag mutations");
    var fluidTag = new CompoundTag();
    fluidTag.putInt("owned", 4);
    ic2ce.core.util.FluidTag.set(nativeValue, fluidTag);
    fluidTag.putInt("owned", 8);
    check(
        ic2ce.core.util.FluidTag.get(nativeValue).getIntOr("owned", 0) == 4,
        "native fluid custom-data writes never retain caller tag mutations");
    ic2ce.core.util.FluidTag.set(nativeValue, new CompoundTag());
    check(
        !ic2ce.core.util.FluidTag.has(nativeValue),
        "empty native fluid metadata update prunes the custom-data component");
    var keyState = new ic2ce.core.util.Keyboard();
    var inputOwner =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("314d1d29-2fe8-443d-a115-ed7fd8e72b24"), "KeyProbe"));
    keyState.processKeyUpdate(inputOwner, 1 << 4);
    check(
        keyState.isJumpKeyDown(inputOwner) && !keyState.isAltKeyDown(inputOwner),
        "primitive native keyboard state preserves the established packet bit order");
    keyState.processKeyUpdate(inputOwner, 1 << 30);
    check(
        !keyState.isJumpKeyDown(inputOwner) && !keyState.isBoostKeyDown(inputOwner),
        "unknown keyboard packet bits cannot retain prior active controls");
    keyState.processKeyUpdate(inputOwner, 3);
    keyState.removePlayerReferences(inputOwner);
    check(
        !keyState.isAltKeyDown(inputOwner)
            && !keyState.isBoostKeyDown(inputOwner)
            && !keyState.isSneakKeyDown(null),
        "keyboard player removal clears weak state and detached reads are inert");
    var zeroVector = new ic2ce.core.util.Vector3();
    zeroVector.normalize();
    check(
        zeroVector.x == 0 && zeroVector.y == 0 && zeroVector.z == 0,
        "native zero projectile vector does not normalize to NaN");
    var largeVector = new ic2ce.core.util.Vector3(1e300, 0, 0).normalize();
    check(
        largeVector.x == 1 && largeVector.y == 0 && largeVector.z == 0,
        "large finite native direction normalizes without squared overflow");
    var largePos = new net.minecraft.core.BlockPos(29999999, 100, -29999999);
    var hit =
        new net.minecraft.world.phys.BlockHitResult(
            new net.minecraft.world.phys.Vec3(largePos.getX() + .1, 100.5, largePos.getZ() + .5),
            net.minecraft.core.Direction.NORTH,
            largePos,
            false);
    check(
        ic2ce.core.util.RotationUtil.rotateByRay(hit) == net.minecraft.core.Direction.WEST,
        "actual large-world face hit subtracts the coordinate before float projection");
    for (var face : net.minecraft.core.Direction.values())
      check(
          ic2ce.core.util.RotationUtil.rotateByHit(face, .5F, .5F, .5F) == face,
          "native center hit retains each facing " + face);
    check(
        ic2ce.neoforge.runtime.ProjectRuntime.getWorldMaxHeight(world) == world.getMaxY() + 1
            && ic2ce.neoforge.runtime.ProjectRuntime.getWorldMaxHeight(world)
                    - ic2ce.neoforge.runtime.ProjectRuntime.getWorldMinHeight(world)
                == world.getHeight(),
        "native exclusive upper world bound retains the actual minimum Y offset");
    pendingIgniter = destination;
    expectedIgniter = cow;
    return new Result(checks, List.copyOf(failures));
  }

  public static Result afterWorldTicks() {
    var probe = new NativeApiContracts();
    probe.check(
        pendingIgniter != null && pendingIgniter.getCausingEntity() == expectedIgniter,
        "igniter resolves after the chunk starts tracking entities");
    if (expectedIgniter != null) expectedIgniter.discard();
    pendingIgniter = null;
    expectedIgniter = null;
    return new Result(probe.checks, List.copyOf(probe.failures));
  }
}
