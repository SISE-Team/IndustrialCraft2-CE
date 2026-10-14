package ic2ce.core.block.storage;

import ic2ce.core.block.storage.box.TileEntityStorageBox;
import ic2ce.neoforge.block.MachineBlockType;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Actual native block destruction, portable snapshots and server-hand bucket transactions. */
public final class PortableStorageContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native portable storage: " + label);
  }

  public static Result run(ServerLevel world) {
    return new PortableStorageContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8320, 100, 8320);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.IRON_STORAGE_BOX.defaultBlockState());
    var box = (TileEntityStorageBox) world.getBlockEntity(position);
    box.getInventorySlot("inventory").put(new ItemStack(Items.IRON_INGOT, 13));
    var original = new ItemStack(BlockDefinitions.IRON_STORAGE_BOX);
    var saved = box.adjustDrop(original, true);
    check(
        !original.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)
            && saved.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA),
        "retained drop owns its snapshot without editing the input item");
    world.destroyBlock(position, true);
    var drops = world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3));
    int loose =
        drops.stream()
            .filter(item -> item.getItem().is(Items.IRON_INGOT))
            .mapToInt(item -> item.getItem().getCount())
            .sum();
    var nativeDrop =
        drops.stream()
            .map(ItemEntity::getItem)
            .filter(stack -> stack.is(BlockDefinitions.IRON_STORAGE_BOX.asItem()))
            .findFirst()
            .orElse(ItemStack.EMPTY)
            .copy();
    check(
        loose == 0 && !nativeDrop.isEmpty(),
        "real block destruction yields one retained box without duplicate loose contents");
    for (var drop : drops) drop.discard();
    world.setBlockAndUpdate(position, BlockDefinitions.IRON_STORAGE_BOX.defaultBlockState());
    box = (TileEntityStorageBox) world.getBlockEntity(position);
    box.onPlaced(nativeDrop, null, Direction.NORTH);
    check(
        box.getInventorySlot("inventory").get().is(Items.IRON_INGOT)
            && box.getInventorySlot("inventory").get().getCount() == 13,
        "actual native loot item restores thirteen owned ingots");
    box.getInventorySlot("inventory").clear();
    var empty = box.adjustDrop(saved, true);
    box.onPlaced(empty, null, Direction.NORTH);
    check(
        box.getInventorySlot("inventory").isEmpty(),
        "empty retained snapshots clear stale stored items");
    world.removeBlock(position, false);
    var machinePosition = position.east(8);
    world.setBlockAndUpdate(machinePosition, BlockDefinitions.MACERATOR.defaultBlockState());
    var machine = (ic2ce.neoforge.block.InventoryBlockEntity) world.getBlockEntity(machinePosition);
    machine.getInventorySlot("input").put(new ItemStack(Items.IRON_ORE, 11));
    world.destroyBlock(machinePosition, false);
    machine.preRemoveSideEffects(machinePosition, machine.getBlockState());
    var machineDrops =
        world.getEntitiesOfClass(ItemEntity.class, new AABB(machinePosition).inflate(3));
    int inputs =
        machineDrops.stream()
            .filter(item -> item.getItem().is(Items.IRON_ORE))
            .mapToInt(item -> item.getItem().getCount())
            .sum();
    check(
        inputs == 11,
        "ordinary machine removal spills each input once even after a repeated side-effect"
            + " callback");
    for (var drop : machineDrops) drop.discard();
    var tankPosition = position.south(8);
    world.setBlockAndUpdate(tankPosition, BlockDefinitions.BRONZE_TANK.defaultBlockState());
    var tank = (NativeBlockEntity) world.getBlockEntity(tankPosition);
    var contents = tank.getComponent(FluidPorts.class).getFluidTank("contents");
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("aef894be-e3f6-4ab2-9f7c-0b38b097e520"), "IC2TankProbe"));
    player.getInventory().clearContent();
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
    var hit = new BlockHitResult(Vec3.atCenterOf(tankPosition), Direction.UP, tankPosition, false);
    ((MachineBlockType) tank.getBlockState().getBlock())
        .useWithoutItem(tank.getBlockState(), world, tankPosition, player, hit);
    check(
        contents.getFluidAmount() == 1000 && player.getMainHandItem().is(Items.BUCKET),
        "server-hand water bucket commits exactly one bucket and its empty remainder");
    ((MachineBlockType) tank.getBlockState().getBlock())
        .useWithoutItem(tank.getBlockState(), world, tankPosition, player, hit);
    check(
        contents.isEmpty() && player.getMainHandItem().is(Items.WATER_BUCKET),
        "server-hand empty bucket extracts exactly the paid water");
    world.removeBlock(tankPosition, false);
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
