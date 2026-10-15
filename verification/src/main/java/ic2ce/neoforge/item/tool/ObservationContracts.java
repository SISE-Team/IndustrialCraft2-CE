package ic2ce.neoforge.item.tool;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.handheld.MeterSession;
import ic2ce.neoforge.handheld.menu.EnergyMeterMenu;
import ic2ce.neoforge.utility.UtilityBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Native loaded ore scans, complete observation bills and owned teleporter address behavior. */
public final class ObservationContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native observation: " + label);
  }

  public static Result run(ServerLevel world) {
    return new ObservationContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var player =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("8a5887a1-a4af-473b-85e6-27b07830393d"), "IC2ObservationProbe"));
    player.getInventory().clearContent();
    player.getAbilities().instabuild = false;
    var center = new BlockPos(10240, 100, 10240);
    for (int x = -1; x <= 1; x++)
      for (int z = -1; z <= 1; z++) {
        world.setChunkForced((center.getX() >> 4) + x, (center.getZ() >> 4) + z, true);
        world.getChunkAt(center.offset(x * 16, 0, z * 16));
      }
    for (int x = -12; x <= 12; x++)
      for (int z = -12; z <= 12; z++)
        for (int y = -12; y <= 12; y++)
          world.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
    world.setBlock(center, Blocks.IRON_ORE.defaultBlockState(), 3);
    world.setBlock(center.east(), Blocks.IRON_ORE.defaultBlockState(), 3);
    world.setBlock(center.east(10), Blocks.DIAMOND_ORE.defaultBlockState(), 3);
    var normal = (OreScanner) ToolItems.SCANNER;
    var advanced = (OreScanner) ToolItems.ADVANCED_SCANNER;
    var rows = normal.scan(world, center, Integer.MAX_VALUE);
    check(
        rows.size() == 1 && rows.getFirst().a.is(Items.IRON_ORE) && rows.getFirst().b == 2,
        "ordinary ore scanner caps a caller's radius at six and counts two actual native ore"
            + " blocks");
    rows = advanced.scan(world, center, 12);
    check(
        rows.size() == 2 && rows.getFirst().b == 2 && rows.getLast().a.is(Items.DIAMOND_ORE),
        "advanced scanner uses its distinct twelve-block radius and deterministic descending"
            + " counts");
    var scanner = new ItemStack(ToolItems.SCANNER);
    ElectricItem.manager.charge(scanner, 49, 1, true, false);
    check(
        normal.startLayerScan(scanner) == 0 && ElectricItem.manager.getCharge(scanner) == 49,
        "incomplete ordinary scan bill preserves charge");
    ElectricItem.manager.charge(scanner, 1, 1, true, false);
    check(
        normal.startLayerScan(scanner) == 3 && ElectricItem.manager.getCharge(scanner) == 0,
        "ordinary layer scan spends exactly fifty EU for its three-block miner radius");
    scanner = new ItemStack(ToolItems.ADVANCED_SCANNER);
    ElectricItem.manager.charge(scanner, 250, 2, true, false);
    check(
        advanced.startLayerScan(scanner) == 6 && ElectricItem.manager.getCharge(scanner) == 0,
        "advanced layer scan spends exactly two hundred fifty EU independently");
    var meter = new ItemStack(ToolItems.METER);
    player.setItemInHand(InteractionHand.MAIN_HAND, meter);
    var menu = new EnergyMeterMenu(124, new MeterSession(player, InteractionHand.MAIN_HAND, meter));
    menu.sample(.25);
    menu.sample(.75);
    check(
        menu.getResultAvg() == .5
            && menu.getResultMin() == .25
            && menu.getResultMax() == .75
            && menu.getResultCount() == 2,
        "actual meter retains fractional EU and its independent min/max/average values");
    menu.reset();
    for (int index = 0; index < 1000; index++) menu.sample(3_000_000_000.5);
    check(
        menu.getResultAvg() == 3_000_000_000.5 && menu.getResultCount() == 1000,
        "high-voltage meter observations cannot overflow an integer intermediate mean");
    int count = menu.getResultCount();
    menu.sample(Double.NaN);
    check(
        menu.getResultCount() == count,
        "malformed external statistics do not contaminate native meter history");
    var card = new ItemStack(ToolItems.FREQUENCY_TRANSMITTER);
    player.setItemInHand(InteractionHand.MAIN_HAND, card);
    var frequency = (TeleporterLinkCard) card.getItem();
    world.setBlock(center.south(4), UtilityBlocks.TELEPORTER.defaultBlockState(), 3);
    world.setBlock(center.south(7), UtilityBlocks.TELEPORTER.defaultBlockState(), 3);
    frequency.useOn(
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(
                Vec3.atCenterOf(center.south(4)), Direction.UP, center.south(4), false)));
    check(
        StackTag.get(card).getBooleanOr("targetSet", false)
            && StackTag.get(card)
                .getStringOr("targetDimension", "")
                .equals(world.dimension().identifier().toString()),
        "frequency card owns a recorded physical destination and its native dimension");
    frequency.useOn(
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(
                Vec3.atCenterOf(center.south(7)), Direction.UP, center.south(7), false)));
    var first = (ic2ce.neoforge.utility.LinkedTeleporter) world.getBlockEntity(center.south(4));
    var second = (ic2ce.neoforge.utility.LinkedTeleporter) world.getBlockEntity(center.south(7));
    check(
        first.getTarget().equals(second.getBlockPos())
            && second.getTarget().equals(first.getBlockPos()),
        "native frequency use creates actual bidirectional endpoints");
    StackTag.update(
        card,
        data -> {
          data.putString("targetDimension", "minecraft:the_nether");
          return data;
        });
    frequency.useOn(
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(
                Vec3.atCenterOf(center.south(7)), Direction.UP, center.south(7), false)));
    check(
        !StackTag.get(card).getBooleanOr("targetSet", true),
        "recorded addresses from another native dimension cannot link unrelated coordinates");
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
