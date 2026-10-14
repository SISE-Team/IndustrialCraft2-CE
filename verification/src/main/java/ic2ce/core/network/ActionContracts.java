package ic2ce.core.network;

import com.mojang.authlib.GameProfile;
import ic2ce.api.network.ClientModifiable;
import ic2ce.neoforge.processing.fluid.MultiPolicyCanner;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Authoritative inventory/range policy and rejected hostile command frames in a native server. */
public final class ActionContracts {
  public record Result(int checks, List<String> failures) {}

  private static class Parent {
    @ClientModifiable int parent;
  }

  private static final class Fields extends Parent {
    @ClientModifiable int allowed;
    int denied;
    @ClientModifiable static int forbiddenStatic;
  }

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean good, String description) {
    checks++;
    if (!good) failures.add("native actions: " + description);
  }

  public static Result run(ServerLevel world) throws Exception {
    return new ActionContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) throws Exception {
    var player =
        FakePlayerFactory.get(
            world,
            new GameProfile(
                UUID.fromString("cf52b7a6-0881-4142-90db-491ed51080bd"), "ActionProbe"));
    player.setPos(8250, 100, 8250);
    var owned = new ItemStack(Items.IRON_INGOT, 7);
    player.getInventory().setItem(0, owned);
    check(
        IncomingActions.ownedItem(player, Items.IRON_INGOT) == owned,
        "item commands resolve the actual owned stack rather than a supplied copy");
    check(
        IncomingActions.ownedItem(player, Items.DIAMOND).isEmpty(),
        "a client cannot synthesize an unowned item event target");
    var near =
        new MultiPolicyCanner(
            new BlockPos(8251, 100, 8251), BlockDefinitions.CANNER.defaultBlockState());
    near.setLevel(world);
    var far =
        new MultiPolicyCanner(
            new BlockPos(8300, 100, 8300), BlockDefinitions.CANNER.defaultBlockState());
    far.setLevel(world);
    check(
        IncomingActions.reachable(player, near) && !IncomingActions.reachable(player, far),
        "tile commands cannot target distant world positions");
    check(
        IncomingActions.modifiable(Fields.class, "allowed") != null
            && IncomingActions.modifiable(Fields.class, "parent") != null,
        "writable-field metadata includes annotated instance fields and their parents");
    check(
        IncomingActions.modifiable(Fields.class, "denied") == null
            && IncomingActions.modifiable(Fields.class, "forbiddenStatic") == null,
        "unannotated and static fields cannot be written by a client");
    var packet = new GrowingBuffer(32);
    SubPacketType.KeyUpdate.writeTo(packet);
    packet.writeInt(256);
    packet.flip();
    boolean rejected = false;
    try {
      IncomingActions.receive(packet, player);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "unknown keyboard bits are rejected before scheduling an action");
    packet.clear();
    SubPacketType.PlayerItemData.writeTo(packet);
    packet.writeByte(0);
    DataEncoder.encode(packet, Items.IRON_INGOT, false);
    packet.writeVarInt(1000000);
    packet.flip();
    rejected = false;
    try {
      IncomingActions.receive(packet, player);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "excess player item arguments are rejected before allocating their array");
    packet.clear();
    SubPacketType.ContainerData.writeTo(packet);
    packet.writeInt(1);
    packet.writeString("x".repeat(65));
    packet.flip();
    rejected = false;
    try {
      IncomingActions.receive(packet, player);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "invalid field names are rejected before decoding a supplied value");
    packet.clear();
    SubPacketType.KeyUpdate.writeTo(packet);
    packet.writeInt(0);
    packet.writeByte(1);
    packet.flip();
    rejected = false;
    try {
      IncomingActions.receive(packet, player);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "extra command bytes cannot smuggle another action into one frame");
    packet.clear();
    packet.writeByte(DataEncoder.EncodedType.Array.ordinal());
    packet.writeByte(DataEncoder.EncodedType.Object.ordinal());
    packet.writeBoolean(false);
    packet.writeVarInt(33);
    packet.flip();
    rejected = false;
    try {
      DataEncoder.decodeArguments(packet, 32);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(
        rejected, "RPC root-array limit is enforced before reading or allocating argument values");
    var fields = new Fields();
    var cached = ic2ce.core.util.ReflectionUtil.getFieldRecursive(Fields.class, "allowed");
    check(
        cached == ic2ce.core.util.ReflectionUtil.getFieldRecursive(Fields.class, "allowed"),
        "field lookup reuses class-lifetime metadata");
    ic2ce.core.util.ReflectionUtil.setValue(fields, cached, 19);
    check(
        fields.allowed == 19
            && ic2ce.core.util.ReflectionUtil.getFieldRecursive(Fields.class, "parent") != null,
        "cached field updates preserve inherited instance semantics");
    var storage =
        new ic2ce.neoforge.processing.fabrication.PatternLibrary(
            new BlockPos(8252, 100, 8252), BlockDefinitions.PATTERN_STORAGE.defaultBlockState());
    storage.setLevel(world);
    var supplied = new ItemStack(Items.IRON_INGOT, 64);
    check(
        storage.addPattern(supplied) && storage.getPatterns().getFirst().getCount() == 1,
        "pattern storage records one unit rather than duplicating supplied stack quantity");
    supplied.setCount(0);
    var exposed = storage.getPatterns().getFirst();
    exposed.setCount(20);
    check(
        storage.getPatterns().getFirst().getCount() == 1
            && storage.containsPattern(new ItemStack(Items.IRON_INGOT)),
        "caller input and exported pattern objects cannot mutate stored patterns");
    check(
        !storage.addPattern(new ItemStack(Items.IRON_INGOT))
            && !storage.addPattern(ItemStack.EMPTY),
        "duplicate and empty patterns are rejected without an exception");
    storage.diskSlot.put(new ItemStack(ic2ce.neoforge.registry.ItemDefinitions.CRYSTAL_MEMORY));
    storage.onNetworkEvent(player, 3);
    check(storage.getPatterns().size() == 1, "blank crystal import preserves stored patterns");
    var saved = new net.minecraft.nbt.CompoundTag();
    storage.saveAdditional(saved);
    storage.addPattern(new ItemStack(Items.GOLD_INGOT));
    storage.readContents(saved);
    check(
        storage.getPatterns().size() == 1 && storage.getPatterns().getFirst().is(Items.IRON_INGOT),
        "pattern save reload replaces previous contents instead of appending");
    var delta = new TeUpdateDataServer();
    delta.addPlayerField("active", player);
    delta.addGlobalField("active");
    check(
        delta.getGlobalFields().size() == 1 && delta.getPlayerFields(player).isEmpty(),
        "a global tile field supersedes the same player's private field");
    delta.addGlobalField("teBlk=ic2:first");
    delta.addGlobalField("teBlk=ic2:second");
    check(
        delta.getGlobalFields().size() == 2
            && delta.getGlobalFields().contains("teBlk=ic2:second")
            && !delta.getGlobalFields().contains("teBlk=ic2:first"),
        "literal tile field values coalesce by logical field name");
    delta.addPlayerField("mode", player);
    delta.addPlayerField("mode", player);
    check(
        delta.getPlayerFields(player).size() == 1, "private tile fields serialize once per update");
    var worldData = ic2ce.core.event.WorldData.get(world);
    worldData.tesToUpdate.remove(storage);
    var network = ic2ce.neoforge.runtime.ProjectRuntime.network.get(true);
    network.updateTileEntityFields(storage, new String[] {"active", "mode", "inventory"}, 5);
    var masked = worldData.tesToUpdate.get(storage);
    check(
        masked != null
            && new ArrayList<>(masked.getGlobalFields()).equals(List.of("active", "inventory")),
        "masked publication queues only selected fields in declaration order");
    masked.addPlayerField("mode", player);
    network.updateTileEntityFields(storage, new String[] {"active", "mode", "inventory"}, 6);
    check(
        masked.getGlobalFields().size() == 3 && masked.getPlayerFields(player).isEmpty(),
        "repeated masked fields coalesce and supersede matching private values");
    rejected = false;
    try {
      network.updateTileEntityFields(storage, new String[] {"active"}, 2);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(rejected, "masked publication rejects fields outside the supplied declaration");
    worldData.tesToUpdate.remove(storage);
    network.updateTileEntityFields(storage, new String[] {"active"}, 0);
    check(
        !worldData.tesToUpdate.containsKey(storage),
        "an empty field mask does not create a world delta");
    packet.clear();
    DataEncoder.encode(
        packet, net.minecraft.resources.Identifier.parse("minecraft:overworld"), false);
    DataEncoder.encode(packet, new BlockPos(0, 100, 0), false);
    packet.writeShort(1025);
    packet.flip();
    rejected = false;
    try {
      TeUpdate.receive(packet);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "hostile tile field count is rejected before allocating its fields");
    player.getInventory().clearContent();
    return new Result(checks, List.copyOf(failures));
  }
}
