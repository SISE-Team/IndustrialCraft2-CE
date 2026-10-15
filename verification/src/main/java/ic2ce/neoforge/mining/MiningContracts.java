package ic2ce.neoforge.mining;

import ic2ce.neoforge.component.ElectricalBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Actual native mining payment, stale inputs and cancellable protection callbacks. */
public final class MiningContracts {
  public record Result(int checks, List<String> failures) {}

  public static final class Protection {
    private final BlockPos protectedPosition;

    Protection(BlockPos position) {
      protectedPosition = position;
    }

    @SubscribeEvent
    public void breaking(BreakBlockEvent event) {
      if (event.getPos().equals(protectedPosition)) event.setCanceled(true);
    }
  }

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native advanced miner: " + label);
  }

  public static Result run(ServerLevel world) {
    return new MiningContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8490, 100, 8490);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, MiningBlocks.ADVANCED_MINER.defaultBlockState());
    var miner = (AreaMiner) world.getBlockEntity(position);
    var energy = miner.getComponent(ElectricalBuffer.class);
    var target = position.west(2);
    world.setBlockAndUpdate(target, Blocks.IRON_ORE.defaultBlockState());
    var ore = world.getBlockState(target);
    miner.doMine(target, ore.getBlock(), ore);
    check(
        world.getBlockState(target) == ore && energy.getEnergy() == 0,
        "public direct mining cannot remove an unpaid ore block");
    energy.forceAddEnergy(512);
    var protection = new Protection(target);
    NeoForge.EVENT_BUS.register(protection);
    try {
      miner.doMine(target, ore.getBlock(), ore);
      check(
          world.getBlockState(target) == ore && energy.getEnergy() == 512,
          "cancelled native break event preserves the ore and full mining budget");
    } finally {
      NeoForge.EVENT_BUS.unregister(protection);
    }
    miner.doMine(target, ore.getBlock(), ore);
    check(
        world.getBlockState(target).isAir() && energy.getEnergy() == 0,
        "unprotected real ore removal pays exactly 512 EU");
    energy.forceAddEnergy(512);
    world.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
    miner.doMine(target, ore.getBlock(), ore);
    check(
        world.getBlockState(target).is(Blocks.STONE) && energy.getEnergy() == 512,
        "stale ore state cannot remove a replacement block");
    miner.load(new CompoundTag());
    check(
        miner.blacklist && miner.getMineTarget() == null,
        "empty native configuration restores the documented blacklist default and clears stale"
            + " cursor");
    var saved = new CompoundTag();
    saved.putInt("mineTargetX", 1);
    saved.putInt("mineTargetY", 1);
    saved.putInt("mineTargetZ", 1);
    miner.saveAdditional(saved);
    check(
        !saved.contains("mineTargetX")
            && !saved.contains("mineTargetY")
            && !saved.contains("mineTargetZ"),
        "reset cursor erases all stale saved coordinates");
    world.removeBlock(target, false);
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
