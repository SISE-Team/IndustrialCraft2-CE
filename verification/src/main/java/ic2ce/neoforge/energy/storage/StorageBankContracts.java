package ic2ce.neoforge.energy.storage;

import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.energy.EnergyBlocks;
import ic2ce.neoforge.item.energy.BatteryItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Accepted storage quantities and real player-owned wireless charging, including shared budgets.
 */
public final class StorageBankContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String label) {
    checks++;
    if (!good) failures.add("native energy storage: " + label);
  }

  private static final class Pad extends LowVoltagePad {
    Pad(BlockPos position) {
      super(position, EnergyBlocks.BATBOX_CHARGEPAD.defaultBlockState());
    }

    void contact(net.minecraft.world.entity.player.Player player) {
      onEntityCollision(player);
    }

    void step() {
      updateEntityServer();
    }
  }

  public static Result run(ServerLevel world) {
    return new StorageBankContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8340, 100, 8340);
    var bank = new LowVoltageBank(position, EnergyBlocks.BATBOX.defaultBlockState());
    bank.setLevel(world);
    bank.setStored(39990);
    check(
        bank.addEnergy(100) == 10 && bank.getStored() == 40000,
        "integer addition reports only the ten EU actually accepted");
    bank.setStored(-1);
    check(
        bank.getStored() == 0 && bank.addEnergy(-1) == 0,
        "negative stored state and extraction at empty storage remain zero");
    bank.setStored(Integer.MAX_VALUE);
    check(bank.getStored() == 40000, "setStored implements actual capacity-bounded restoration");
    bank.setStored(39999);
    bank.energy.forceAddEnergy(0.5);
    check(
        bank.addEnergy(1) == 0 && bank.energy.getEnergy() == 39999.5,
        "integer API never mutates fractional space it cannot report");
    bank.setStored(0);
    bank.energy.forceAddEnergy(10.75);
    check(
        bank.addEnergy(-100) == -10 && bank.energy.getEnergy() == 0.75,
        "integer extraction preserves the unreported fractional remainder");
    var data = new CompoundTag();
    data.putByte("redstoneMode", Byte.MIN_VALUE);
    bank.load(data);
    check(
        bank.redstoneMode == 0 && !bank.getRedstoneModeKey().isEmpty(),
        "corrupt bank modes resolve a real localized default");
    var pad = new Pad(position);
    pad.setLevel(world);
    data = new CompoundTag();
    data.putByte("redstoneMode", (byte) 6);
    pad.load(data);
    check(
        pad.redstoneMode == 0,
        "wireless pads enforce their own two-mode policy rather than seven bank modes");
    var first =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("3c224d16-c2df-499b-84c2-42f1e4641f46"), "IC2PadFirst"));
    var second =
        net.neoforged.neoforge.common.util.FakePlayerFactory.get(
            world,
            new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("a6a85898-f484-46d7-87d7-19353d28a95a"), "IC2PadSecond"));
    first.getInventory().clearContent();
    second.getInventory().clearContent();
    first.setPos(position.getX() + 0.3, position.getY() + 0.94, position.getZ() + 0.5);
    second.setPos(position.getX() + 0.7, position.getY() + 0.94, position.getZ() + 0.5);
    first.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BatteryItems.RE_BATTERY));
    second.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(BatteryItems.RE_BATTERY));
    for (int cycle = 0; cycle < 2; cycle++) {
      pad.energy.forceAddEnergy(64);
      pad.contact(first);
      pad.contact(second);
      pad.step();
      pad.step();
    }
    double firstCharge = ElectricItem.manager.getCharge(first.getMainHandItem()),
        secondCharge = ElectricItem.manager.getCharge(second.getOffhandItem());
    check(
        firstCharge == 64 && secondCharge == 64 && pad.energy.getEnergy() == 0,
        "rotating contacts deliver exactly 128 paid EU to main-hand and offhand batteries without"
            + " starvation");
    first.getInventory().clearContent();
    second.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
