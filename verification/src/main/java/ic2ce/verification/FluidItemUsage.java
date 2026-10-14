package ic2ce.verification;

import ic2ce.api.fluid.Ic2FluidItem;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Transfers every IC2 bucket/cell and both wearable tanks through registered gameplay capabilities.
 */
public final class FluidItemUsage {
  public static List<Map<String, Object>> run(ServerPlayer player) {
    var rows = new ArrayList<Map<String, Object>>();
    var pos = new BlockPos(92, 101, 80);
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")) {
        var item = BuiltInRegistries.ITEM.getValue(id);
        if (!(item instanceof BucketItem || item instanceof Ic2FluidItem)) continue;
        for (var hand : InteractionHand.values())
          try {
            player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            player.level().setBlock(pos, BlockDefinitions.TANK.defaultBlockState(), 3);
            player.getInventory().clearContent();
            player.setItemInHand(hand, new ItemStack(item));
            var access = ItemAccess.forPlayerInteraction(player, hand);
            var handler = player.getItemInHand(hand).getCapability(Capabilities.Fluid.ITEM, access);
            if (handler == null) {
              rows.add(
                  Map.of(
                      "id",
                      id.toString(),
                      "hand",
                      hand.toString(),
                      "passed",
                      false,
                      "error",
                      "missing registered fluid capability"));
              continue;
            }
            var tile = player.level().getBlockEntity(pos);
            var tank = player.level().getCapability(Capabilities.Fluid.BLOCK, pos, Direction.UP);
            if (tank == null) throw new IllegalStateException("tank has no capability");
            var fluid = handler.getResource(0);
            int amount = handler.getAmountAsInt(0);
            if (fluid.isEmpty()
                && item instanceof ic2ce.neoforge.item.container.FixedCell
                && item != ItemDefinitions.EMPTY_CELL) continue;
            boolean filled = fluid.isEmpty();
            int moved;
            if (filled) {
              fluid =
                  FluidResource.of(
                      item == ItemDefinitions.CF_PACK
                          ? FluidDefinitions.CONSTRUCTION_FOAM.still
                          : item == ItemDefinitions.JETPACK
                              ? FluidDefinitions.BIOGAS.still
                              : Fluids.WATER);
              try (var root = Transaction.openRoot()) {
                int seed = tank.insert(fluid, 1000, root);
                if (seed != 1000) throw new IllegalStateException("tank rejects source fluid");
                root.commit();
              }
              try (var root = Transaction.openRoot()) {
                moved = handler.insert(0, fluid, 1000, root);
                int removed = tank.extract(fluid, moved, root);
                if (removed != moved) throw new IllegalStateException("fill source mismatch");
                if (moved > 0) root.commit();
              }
            } else {
              try (var root = Transaction.openRoot()) {
                int accepted = tank.insert(fluid, amount, root);
                moved = handler.extract(0, fluid, accepted, root);
                if (moved != accepted) throw new IllegalStateException("drain target mismatch");
                if (moved > 0) root.commit();
              }
            }
            var after = player.getItemInHand(hand);
            var current =
                after.getCapability(
                    Capabilities.Fluid.ITEM, ItemAccess.forPlayerInteraction(player, hand));
            int afterMB = current == null ? 0 : current.getAmountAsInt(0);
            int tankMB = 0;
            for (int i = 0; i < tank.size(); i++) tankMB += tank.getAmountAsInt(i);
            boolean pass =
                moved == 1000
                    && after.getCount() == 1
                    && (filled ? afterMB == 1000 && tankMB == 0 : afterMB == 0 && tankMB == 1000);
            rows.add(
                Map.of(
                    "id",
                    id.toString(),
                    "hand",
                    hand.toString(),
                    "operation",
                    filled ? "server_owned_fill" : "server_owned_drain",
                    "movedMB",
                    moved,
                    "tankMB",
                    tankMB,
                    "heldMB",
                    afterMB,
                    "held",
                    after.toString(),
                    "passed",
                    pass));
          } catch (Throwable e) {
            rows.add(
                Map.of(
                    "id",
                    id.toString(),
                    "hand",
                    hand.toString(),
                    "passed",
                    false,
                    "error",
                    e.toString()));
          }
      }
    player.level().removeBlock(pos, false);
    return rows;
  }
}
