package ic2ce.verification;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.processing.uu.ItemCrystalMemory;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/**
 * Remaining real item operations and the actual placed upgrade/reactor matrices share one artifact
 * ledger.
 */
public final class CompleteItemUse {
  private final AllItemsVerification ledger;
  private final ServerPlayer player;
  private final ExtendedValidation observations = new ExtendedValidation();
  private UpgradeFunctionality upgrades;
  private AdvancedFilterFunctionality filters;
  private ReactorMatrix reactorPairs;
  private ReactorFunctionality reactors;
  private int stage;
  private boolean recorded;

  public CompleteItemUse(AllItemsVerification ledger, ServerPlayer player) {
    this.ledger = ledger;
    this.player = player;
    observations.s = AllMachinesVerification.ACTIVE;
  }

  public void prepare() {
    directItems();
    upgrades = new UpgradeFunctionality(observations);
    upgrades.prepare();
  }

  private void mark(Item item, String operation, boolean pass, String evidence) {
    ledger.check(pass, operation + " " + BuiltInRegistries.ITEM.getKey(item) + " " + evidence);
    if (pass) ledger.mark(item, operation, evidence);
  }

  private void directItems() {
    var world = player.level();
    var pos = new BlockPos(11008, 100, 11008);
    world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    world.getChunkAt(pos);
    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
    player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 2.5);
    player.getInventory().clearContent();
    for (var color : DyeColor.values()) {
      world.setBlock(
          pos,
          (color == DyeColor.WHITE ? Blocks.BLACK_WOOL : Blocks.WHITE_WOOL).defaultBlockState(),
          3);
      var item =
          BuiltInRegistries.ITEM.getValue(
              ProjectIds.id(color.getName() + "_painter"));
      var stack = new ItemStack(item);
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      item.useOn(
          new UseOnContext(
              player,
              InteractionHand.MAIN_HAND,
              new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false)));
      mark(
          item,
          "native_physical_wool_paint",
          world
              .getBlockState(pos)
              .is(
                  BuiltInRegistries.BLOCK.getValue(
                      net.minecraft.resources.Identifier.withDefaultNamespace(
                          color.getName() + "_wool"))),
          world.getBlockState(pos).toString());
    }
    for (Item item : List.of(ItemDefinitions.COFFEE_MUG, ItemDefinitions.COLD_COFFEE_MUG)) {
      var stack = new ItemStack(item);
      var remainder = item.finishUsingItem(stack, world, player);
      mark(
          item,
          "native_finished_drink",
          remainder.is(ItemDefinitions.EMPTY_MUG)
              && player.hasEffect(net.minecraft.world.effect.MobEffects.SPEED),
          remainder.toString());
    }
    var crystal = new ItemStack(ItemDefinitions.CRYSTAL_MEMORY);
    var memory = (ItemCrystalMemory) crystal.getItem();
    memory.writecontentsTag(world, crystal, new ItemStack(Items.IRON_INGOT));
    var restored = memory.readItemStack(crystal);
    mark(
        crystal.getItem(),
        "native_pattern_write_read",
        restored.is(Items.IRON_INGOT) && restored.getCount() == 1,
        restored.toString());
    var tap = new ItemStack(ItemDefinitions.ELECTRIC_TREETAP);
    ic2ce.api.item.ElectricItem.manager.charge(tap, 50, 1, true, false);
    player.setItemInHand(InteractionHand.MAIN_HAND, tap);
    world.setBlock(
        pos,
        BlockDefinitions.RUBBER_LOG
            .defaultBlockState()
            .setValue(
                ic2ce.neoforge.world.block.ResinLog.stateProperty,
                ic2ce.neoforge.world.block.ResinLog.RubberWoodState.wet_north),
        3);
    tap.getItem()
        .useOn(
            new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false)));
    mark(
        tap.getItem(),
        "native_paid_resin_tap",
        ic2ce.api.item.ElectricItem.manager.getCharge(tap) == 0,
        world.getBlockState(pos).toString());
    player.removeAllEffects();
    player.getInventory().clearContent();
    ledger.flush();
  }

  public boolean tick() {
    if (stage == 0) {
      if (!upgrades.tick()) return false;
      filters = new AdvancedFilterFunctionality(observations);
      filters.prepare();
      stage = 1;
    } else if (stage == 1) {
      if (!filters.tick()) return false;
      reactorPairs = new ReactorMatrix(observations);
      reactorPairs.prepare();
      stage = 2;
    } else if (stage == 2) {
      if (!reactorPairs.tick()) return false;
      reactors = new ReactorFunctionality(observations);
      reactors.prepare();
      stage = 3;
    } else if (stage == 3) {
      if (!reactors.tick()) return false;
      stage = 4;
    }
    if (stage < 4) return false;
    if (!recorded) {
      recorded = true;
      var failures = observations.failureList();
      for (var failure : failures) ledger.check(false, "combined actual matrices: " + failure);
      ledger.phases.put("completePhysicalItemMatrices", observations.details);
      if (failures.isEmpty()) {
        for (var id : BuiltInRegistries.ITEM.keySet())
          if (id.getNamespace().equals("ic2")) {
            var item = BuiltInRegistries.ITEM.getValue(id);
            if (item instanceof ic2ce.api.reactor.IReactorComponent)
              ledger.mark(
                  item,
                  "placed_reactor_pair_and_function_matrix",
                  "actual component pairs, heat, wear, orientation, breeding and lifecycle"
                      + " reports");
          }
        for (String id :
            List.of(
                "overclocker_upgrade",
                "transformer_upgrade",
                "energy_storage_upgrade",
                "redstone_inverter_upgrade",
                "advanced_ejector_upgrade",
                "advanced_pulling_upgrade"))
          ledger.mark(
              BuiltInRegistries.ITEM.getValue(ProjectIds.id(id)),
              "placed_upgrade_transfer_function",
              "actual rates, storage, tier, signals and filtered item conservation");
      }
      ledger.flush();
    }
    return true;
  }
}
