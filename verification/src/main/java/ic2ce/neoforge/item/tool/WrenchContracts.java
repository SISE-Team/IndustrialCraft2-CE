package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.api.tile.IWrenchable;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Exact native wrench energy, cancelled removals, portable contents and ordinary inventory spill.
 */
public final class WrenchContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native wrench: " + label);
  }

  public static Result run(ServerLevel world) {
    return new WrenchContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(9328, 100, 9328);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position.below(), Blocks.STONE.defaultBlockState());
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fc4ad392-af63-479d-8a9f-40e83b987817"), "IC2WrenchProbe"));
    player.getInventory().clearContent();
    player.getAbilities().instabuild = false;
    world.setBlockAndUpdate(position, BlockDefinitions.GENERATOR.defaultBlockState());
    var before = world.getBlockState(position);
    var nativeBlock = (IWrenchable) before.getBlock();
    var facing = nativeBlock.getFacing(world, position);
    var target = facing == Direction.EAST ? Direction.WEST : Direction.EAST;
    var stack = new ItemStack(ItemDefinitions.ELECTRIC_WRENCH);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    var tool = (ElectricWrench) stack.getItem();
    ElectricItem.manager.charge(stack, 99, 1, true, false);
    var context =
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(position), target, position, false));
    tool.onItemUseFirst(stack, context);
    check(
        world.getBlockState(position) == before && ElectricItem.manager.getCharge(stack) == 99,
        "ninety-nine physical EU cannot rotate or debit the actual generator");
    ElectricItem.manager.charge(stack, 1, 1, true, false);
    tool.onItemUseFirst(stack, context);
    check(
        nativeBlock.getFacing(world, position) == target
            && ElectricItem.manager.getCharge(stack) == 0,
        "exact hundred-EU native rotation commits without an unpaid remainder");
    ElectricItem.manager.charge(stack, 1000, 1, true, false);
    var removal =
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(position), target, position, false));
    Consumer<BreakBlockEvent> deny =
        event -> {
          if (event.getPos().equals(position)) event.setCanceled(true);
        };
    NeoForge.EVENT_BUS.addListener(deny);
    try {
      tool.onItemUseFirst(stack, removal);
    } finally {
      NeoForge.EVENT_BUS.unregister(deny);
    }
    check(
        world.getBlockState(position).is(BlockDefinitions.GENERATOR)
            && ElectricItem.manager.getCharge(stack) == 1000,
        "actual cancelled block-break event preserves generator and complete reserved removal"
            + " energy");
    var inventory = (InventoryBlockEntity) world.getBlockEntity(position);
    inventory.setItem(0, new ItemStack(Items.COAL, 7));
    tool.onItemUseFirst(stack, removal);
    var drops = world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3));
    int coal = 0, generators = 0;
    for (var drop : drops) {
      if (drop.getItem().is(Items.COAL)) coal += drop.getItem().getCount();
      if (drop.getItem().is(ItemDefinitions.GENERATOR)) generators += drop.getItem().getCount();
    }
    check(
        world.getBlockState(position).isAir()
            && ElectricItem.manager.getCharge(stack) == 0
            && coal == 7
            && generators == 1,
        "exact thousand-EU removal conserves seven real inventory coal and one generator block;"
            + " coal="
            + coal
            + ", generators="
            + generators);
    drops.forEach(ItemEntity::discard);
    world.setBlockAndUpdate(position, BlockDefinitions.WOODEN_STORAGE_BOX.defaultBlockState());
    inventory = (InventoryBlockEntity) world.getBlockEntity(position);
    inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 13));
    int action = 0;
    var payment =
        new WrenchActions.Payment() {
          public boolean reserve(int units) {
            return true;
          }

          public void refund(int units) {}
        };
    var boxBlock = (IWrenchable) world.getBlockState(position).getBlock();
    var result =
        WrenchActions.execute(
            world, position, boxBlock.getFacing(world, position), player, true, payment);
    drops = world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3));
    ItemStack boxDrop = ItemStack.EMPTY;
    int looseIron = 0;
    for (var drop : drops) {
      if (drop.getItem().is(BlockDefinitions.WOODEN_STORAGE_BOX.asItem()))
        boxDrop = drop.getItem().copy();
      if (drop.getItem().is(Items.IRON_INGOT)) looseIron += drop.getItem().getCount();
    }
    check(
        result == WrenchActions.Action.REMOVE && !boxDrop.isEmpty() && looseIron == 0,
        "portable inventory travels in its sole real wrench drop without loose iron duplication");
    world.setBlockAndUpdate(position, BlockDefinitions.WOODEN_STORAGE_BOX.defaultBlockState());
    inventory = (InventoryBlockEntity) world.getBlockEntity(position);
    inventory.onPlaced(boxDrop, player, Direction.NORTH);
    check(
        inventory.getItem(0).is(Items.IRON_INGOT) && inventory.getItem(0).getCount() == 13,
        "actual placement restores all thirteen portable iron ingots after the new wrench removal");
    drops.forEach(ItemEntity::discard);
    inventory.clearContent();
    world.removeBlock(position, false);
    world.removeBlock(position.below(), false);
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
