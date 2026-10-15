package ic2ce.neoforge.item.tool;


import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.item.armor.ArmorItems;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Placed protected shearing, actor-local speed and partial armor drain exercise actual native
 * implementations.
 */
public final class PoweredToolContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native powered tools: " + label);
  }

  public static Result run(ServerLevel world) {
    return new PoweredToolContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(9408, 100, 9408);
    world.getChunkAt(position);
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fd5c37e0-8237-4199-9e86-96f37f240854"), "IC2PowerToolProbe"));
    player.getAbilities().instabuild = false;
    player.getInventory().clearContent();
    for (int index = 0; index < 3; index++) {
      var item =
          new Item[] {
                ToolItems.DRILL, ToolItems.DIAMOND_DRILL, ToolItems.IRIDIUM_DRILL
              }
              [index];
      var drill = (MiningDrill) item;
      var stack = new ItemStack(item);
      int[] costs = {50, 80, 800}, rates = {6, 20, 200}, duration = {200, 50, 20};
      ElectricItem.manager.charge(stack, costs[index], Integer.MAX_VALUE, true, false);
      check(
          drill.energyUse(stack, world, position, Blocks.STONE.defaultBlockState()) == rates[index]
              && drill.breakTime(stack, world, position, Blocks.STONE.defaultBlockState())
                  == duration[index],
          "each actual drill retains its distinct miner rate and progress duration");
      check(
          drill.breakBlock(stack, world, position, Blocks.STONE.defaultBlockState())
              && ElectricItem.manager.getCharge(stack) == 0
              && !drill.breakBlock(stack, world, position, Blocks.STONE.defaultBlockState()),
          "each actual drill pays precisely one complete block bill and rejects a later empty"
              + " operation");
    }
    var drillStack = new ItemStack(ToolItems.DRILL);
    ElectricItem.manager.charge(drillStack, 1000, 1, true, false);
    player.setItemInHand(InteractionHand.MAIN_HAND, drillStack);
    player.setOnGround(false);
    var airborne =
        new PlayerEvent.BreakSpeed(player, Blocks.STONE.defaultBlockState(), 8, position);
    NeoForge.EVENT_BUS.post(airborne);
    check(
        airborne.getNewSpeed() == 24,
        "actual native break-speed event applies charged-drill airborne compensation to its own"
            + " actor");
    player.setOnGround(true);
    var grounded =
        new PlayerEvent.BreakSpeed(player, Blocks.STONE.defaultBlockState(), 8, position);
    NeoForge.EVENT_BUS.post(grounded);
    check(
        grounded.getNewSpeed() == 8,
        "grounded charged drilling does not receive another player's flight multiplier");
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
    player.setOnGround(false);
    var other = new PlayerEvent.BreakSpeed(player, Blocks.STONE.defaultBlockState(), 8, position);
    NeoForge.EVENT_BUS.post(other);
    check(
        other.getNewSpeed() == 8,
        "a different held item does not inherit drill physics from another inventory or item"
            + " singleton");
    var saw = new ItemStack(ToolItems.CHAINSAW);
    player.setItemInHand(InteractionHand.MAIN_HAND, saw);
    ElectricItem.manager.charge(saw, 100, 1, true, false);
    var chainsaw = (PoweredSaw) saw.getItem();
    world.setBlockAndUpdate(position, Blocks.OAK_LEAVES.defaultBlockState());
    Consumer<BreakBlockEvent> denied =
        event -> {
          if (event.getPos().equals(position)) event.setCanceled(true);
        };
    NeoForge.EVENT_BUS.addListener(denied);
    try {
      chainsaw.onBlockStartBreak(player, world, InteractionHand.MAIN_HAND, position, Direction.UP);
    } finally {
      NeoForge.EVENT_BUS.unregister(denied);
    }
    check(
        world.getBlockState(position).is(Blocks.OAK_LEAVES)
            && ElectricItem.manager.getCharge(saw) == 100,
        "actual cancelled block-shearing event preserves leaves and the complete charged bill");
    chainsaw.onBlockStartBreak(player, world, InteractionHand.MAIN_HAND, position, Direction.UP);
    check(
        world.getBlockState(position).isAir() && ElectricItem.manager.getCharge(saw) == 0,
        "actual unblocked shearing spends exactly one hundred EU and removes one physical leaf"
            + " block");
    var blade = new ItemStack(ToolItems.NANO_SABER);
    player.setItemInHand(InteractionHand.MAIN_HAND, blade);
    ElectricItem.manager.charge(blade, 2400, 3, true, false);
    var saber = (NanoBladeBase) blade.getItem();
    saber.use(world, player, InteractionHand.MAIN_HAND);
    var victim = new net.minecraft.world.entity.animal.cow.Cow(EntityType.COW, world);
    var armor = new ItemStack(ArmorItems.NANO_CHESTPLATE);
    ElectricItem.manager.charge(armor, 1000, 3, true, false);
    victim.setItemSlot(EquipmentSlot.CHEST, armor);
    saber.hurtEnemy(blade, victim, player);
    check(
        victim.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
            && ElectricItem.manager.getCharge(blade) == 0,
        "partial thousand-EU nano armor is actually drained and removed after its exact"
            + " twenty-four-hundred-EU blade attack bill");
    var inactive = new ItemStack(ToolItems.NANO_SABER);
    check(
        !NanoBladeBase.isActive(inactive) && !NanoBladeBase.isActive(blade.copyWithCount(0)),
        "activation reads cannot add fake state to blank or empty physical blade stacks");
    player.getInventory().clearContent();
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
