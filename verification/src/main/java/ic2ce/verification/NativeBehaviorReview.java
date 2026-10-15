package ic2ce.verification;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.cable.RedstoneGateBlock;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

/**
 * Routes through current vanilla BlockState/ItemStack APIs, rather than legacy helper overloads.
 */
public final class NativeBehaviorReview {
  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add(label);
  }

  public static Map<String, Object> run(ServerPlayer player) {
    return new NativeBehaviorReview().execute(player);
  }

  private Map<String, Object> execute(ServerPlayer player) {
    var level = player.level();
    var pos = new BlockPos(94, 101, 94);
    level.getChunkAt(pos);
    for (int x = -3; x <= 3; x++)
      for (int z = -3; z <= 3; z++)
        for (int y = -1; y <= 2; y++)
          level.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
    level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
    level.setBlock(pos, BlockDefinitions.IRON_FENCE.defaultBlockState(), 3);
    level.setBlock(pos.east(), BlockDefinitions.IRON_FENCE.defaultBlockState(), 3);
    check(
        level.getBlockState(pos).getValue(CrossCollisionBlock.EAST),
        "iron fence connects after neighbor placement");
    level.removeBlock(pos.east(), false);
    check(
        !level.getBlockState(pos).getValue(CrossCollisionBlock.EAST),
        "iron fence disconnects after neighbor removal");
    level.removeBlock(pos, false);
    var splitter = BuiltInRegistries.BLOCK.getValue(ProjectIds.id("splitter_cable"));
    level.setBlock(pos, splitter.defaultBlockState(), 3);
    level.setBlock(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
    check(
        level.getBlockState(pos).getValue(RedstoneGateBlock.active),
        "splitter joins its energy network after redstone is added");
    level.removeBlock(pos.east(), false);
    check(
        !level.getBlockState(pos).getValue(RedstoneGateBlock.active),
        "splitter disconnects when redstone is removed");
    level.removeBlock(pos, false);
    for (var sheet : List.of(BlockDefinitions.RESIN_SHEET, BlockDefinitions.RUBBER_SHEET)) {
      level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
      level.setBlock(pos, sheet.defaultBlockState(), 3);
      level.removeBlock(pos.below(), false);
      check(
          level.isEmptyBlock(pos),
          BuiltInRegistries.BLOCK.getKey(sheet) + " drops when support disappears");
    }
    for (var scaffold :
        List.of(
            BlockDefinitions.WOODEN_SCAFFOLD,
            BlockDefinitions.IRON_SCAFFOLD,
            BlockDefinitions.REINFORCED_WOODEN_SCAFFOLD,
            BlockDefinitions.REINFORCED_IRON_SCAFFOLD)) {
      level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
      level.setBlock(pos, scaffold.defaultBlockState(), 3);
      level.removeBlock(pos.below(), false);
      check(
          level.isEmptyBlock(pos),
          BuiltInRegistries.BLOCK.getKey(scaffold) + " collapses when support disappears");
    }
    player.setDeltaMovement(new Vec3(1, -1, 1));
    player.fallDistance = 10;
    BlockDefinitions.RESIN_SHEET
        .defaultBlockState()
        .entityInside(level, pos, player, InsideBlockEffectApplier.NOOP, true);
    check(
        player.getDeltaMovement().x() == 0.6
            && player.getDeltaMovement().y() == -0.85
            && player.fallDistance == 7.5,
        "resin sheet native entity callback slows motion and falling");
    player.fallDistance = 10;
    BlockDefinitions.WOOL_SHEET
        .defaultBlockState()
        .entityInside(level, pos, player, InsideBlockEffectApplier.NOOP, true);
    check(
        Math.abs(player.fallDistance - 9.5) < 0.01,
        "wool sheet native callback reduces fall distance");
    player.setShiftKeyDown(false);
    player.setDeltaMovement(new Vec3(1, -1, 1));
    player.fallDistance = 10;
    BlockDefinitions.WOODEN_SCAFFOLD
        .defaultBlockState()
        .entityInside(level, pos, player, InsideBlockEffectApplier.NOOP, true);
    check(
        player.getDeltaMovement().y() == -0.07
            && player.getDeltaMovement().x() == 0.15
            && player.fallDistance == 0,
        "scaffold native callback controls climbing and falling");
    for (String id : List.of("drill", "diamond_drill", "iridium_drill", "chainsaw")) {
      var item = BuiltInRegistries.ITEM.getValue(ProjectIds.id(id));
      var stack = new ItemStack(item);
      ElectricItem.manager.charge(stack, 10000, Integer.MAX_VALUE, true, false);
      check(stack.getMaxDamage() == 0, id + " uses charge without physical tool durability");
      check(
          !stack.has(net.minecraft.core.component.DataComponents.ENCHANTABLE),
          id + " retains original non-enchantability");
      var state =
          id.equals("chainsaw")
              ? Blocks.COBWEB.defaultBlockState()
              : Blocks.DIRT.defaultBlockState();
      check(stack.isCorrectToolForDrops(state), id + " native multi-tag or shearing drop rules");
    }
    player.setDeltaMovement(Vec3.ZERO);
    player.fallDistance = 0;
    level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
    return Map.of("checks", checks, "failures", failures, "mode", "server_native_dispatch");
  }
}
