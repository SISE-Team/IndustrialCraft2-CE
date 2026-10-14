package ic2ce.api.energy.prefab;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.info.ILocatable;
import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Native addon capacity, item conservation, corrupt persistence and real topology relocation. */
public final class EnergyBufferContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();
  private static BasicSink movedSink, circuitSink;
  private static BasicSource circuitSource;
  private static BlockPos former, current;
  private static ServerLevel testWorld;
  private static long readyTick;

  private static final class Owner implements ILocatable {
    Level world;
    BlockPos position;

    public Level getWorldObj() {
      return world;
    }

    public BlockPos getPosition() {
      return position;
    }
  }

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native addon buffer: " + label);
  }

  public static Result run(ServerLevel world) {
    return new EnergyBufferContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(11328, 100, 11328);
    world.setChunkForced(position.getX() >> 4, position.getZ() >> 4, true);
    world.getChunkAt(position);
    var sink = new BasicSink(world, position, 100, 1);
    sink.setEnergyStored(Double.NaN);
    check(sink.getEnergyStored() == 0, "NaN save/input cannot poison finite ledger");
    sink.setEnergyStored(-20);
    check(sink.getEnergyStored() == 0, "negative stored energy cannot become a debt");
    sink.setEnergyStored(90);
    check(
        sink.injectEnergy(null, 25, 32) == 15 && sink.getEnergyStored() == 100,
        "receiver returns exact unaccepted surplus");
    check(
        sink.addEnergy(-5) == 0 && sink.getEnergyStored() == 100 && !sink.useEnergy(-5),
        "negative admission/payment cannot manufacture energy");
    check(
        sink.addEnergy(Double.NaN) == 0 && !sink.useEnergy(Double.NaN),
        "NaN admission and bill rejected");
    sink.setCapacity(20);
    check(
        sink.getEnergyStored() == 20 && sink.getFreeCapacity() == 0,
        "capacity reduction clamps stored energy");
    sink.setEnergyStored(Double.POSITIVE_INFINITY);
    check(sink.getEnergyStored() == 0, "infinite corrupt stored energy normalized");
    boolean invalid = false;
    try {
      sink.setCapacity(Double.POSITIVE_INFINITY);
    } catch (IllegalArgumentException expected) {
      invalid = true;
    }
    check(
        invalid && sink.getCapacity() == 20,
        "infinite capacity rejected without replacing valid configuration");
    sink.setEnergyStored(10);
    var data = new CompoundTag();
    sink.writeToNBT(data);
    var restored = new BasicSink(world, position, 20, 1);
    restored.readFromNBT(data);
    check(
        restored.getEnergyStored() == 10,
        "legacy native buffer persistence retains exact quantity");
    data.getCompoundOrEmpty("IC2BasicSink").putDouble("energy", Double.NaN);
    restored.readFromNBT(data);
    check(restored.getEnergyStored() == 0, "corrupt persisted ledger becomes finite empty storage");
    sink.setCapacity(1000);
    sink.setEnergyStored(200);
    var battery = new ItemStack(ItemDefinitions.RE_BATTERY);
    double sum = sink.getEnergyStored() + ElectricItem.manager.getCharge(battery);
    check(
        sink.charge(battery)
            && sink.getEnergyStored() + ElectricItem.manager.getCharge(battery) == sum,
        "actual native battery charging conserves paid quantity");
    check(
        sink.discharge(battery, 50)
            && sink.getEnergyStored() + ElectricItem.manager.getCharge(battery) == sum,
        "actual native battery discharge conserves quantity and explicit limit");
    check(!sink.discharge(battery, -1), "negative addon item transfer limit rejected");
    var source = new BasicSource(world, position, 1, 2);
    check(source.getCapacity() >= 128, "source reserves its declared full native voltage packet");
    source.setEnergyStored(64);
    source.drawEnergy(1000);
    check(source.getEnergyStored() == 0, "excess source draw cannot create negative balance");
    source.drawEnergy(-20);
    check(source.getEnergyStored() == 0, "negative source draw cannot create free energy");
    var owner = new Owner();
    owner.world = world;
    former = position.offset(3, 0, 0);
    current = position.offset(5, 0, 0);
    owner.position = former;
    movedSink = new BasicSink(owner, 64, 1);
    movedSink.update();
    owner.position = current;
    movedSink.update();
    check(movedSink.getPosition().equals(current), "relocatable owner is read live and reconciled");
    testWorld = world;
    readyTick = world.getGameTime() + 20;
    circuitSink = new BasicSink(world, position.offset(8, 0, 0), 16, 1);
    circuitSource = new BasicSource(world, position.offset(7, 0, 0), 64, 1);
    circuitSource.setEnergyStored(64);
    circuitSource.onLoad();
    circuitSink.onLoad();
    return new Result(checks, List.copyOf(failures));
  }

  public static boolean ready() {
    return testWorld != null && testWorld.getGameTime() >= readyTick;
  }

  public static Result afterWorldTicks() {
    var probe = new EnergyBufferContracts();
    probe.check(
        EnergyNet.instance.getTile(testWorld, former) != movedSink
            && EnergyNet.instance.getTile(testWorld, current) == movedSink,
        "actual world ticks remove old coordinate and admit the moved addon node");
    probe.check(
        circuitSink.getEnergyStored() > 0
            && circuitSink.getEnergyStored() <= 16
            && circuitSource.getEnergyStored() + circuitSink.getEnergyStored() == 64,
        "actual source-to-sink world circuit conserves quantity without overflow: source="
            + circuitSource.getEnergyStored()
            + ", sink="
            + circuitSink.getEnergyStored()
            + ", connections="
            + EnergyNet.instance.getAdjacentConnections(circuitSource));
    movedSink.invalidate();
    circuitSink.invalidate();
    circuitSource.invalidate();
    movedSink = null;
    circuitSink = null;
    circuitSource = null;
    testWorld = null;
    return new Result(probe.checks, List.copyOf(probe.failures));
  }
}
