package ic2ce.neoforge.network;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

/** Native heap/direct frame bounds and Unicode data passing through the actual IC2 codec. */
public final class WireContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private record DeferredNumber(int value) {}

  private static int initialized;

  private static final class InitializationTripwire {
    static {
      initialized++;
    }
  }

  private void check(boolean good, String description) {
    checks++;
    if (!good) failures.add("native wire: " + description);
  }

  public static Result run() throws Exception {
    return new WireContracts().exercise();
  }

  private Result exercise() throws Exception {
    var heap = Unpooled.wrappedBuffer(new byte[] {10, 20, 30, 40, 50, 60});
    try {
      var frame = GrowingBuffer.wrap(heap.slice(2, 3));
      check(
          frame.available() == 3 && frame.readByte() == 30,
          "sliced heap frame starts at its own array origin");
      frame.rewind();
      check(
          frame.readByte() == 30 && frame.skipBytes(-10) == -1 && frame.readByte() == 30,
          "rewind and backwards skips cannot read a sliced frame prefix");
      var view = frame.toByteBuf(false);
      try {
        check(
            view.readableBytes() == 2 && view.readByte() == 40 && frame.available() == 2,
            "Netty view preserves readable bytes and the source cursor");
      } finally {
        view.release();
      }
    } finally {
      heap.release();
    }
    var direct = Unpooled.directBuffer(8);
    try {
      direct.writeInt(0x10203040);
      direct.writeInt(0x50607080);
      direct.readInt();
      var copied = GrowingBuffer.wrap(direct);
      check(
          copied.available() == 4 && copied.readInt() == 0x50607080,
          "direct Netty frames use the readable range only");
    } finally {
      direct.release();
    }
    var immutable = List.of("old");
    var incoming = List.of("new");
    check(
        !DataEncoder.copyValue(incoming, immutable) && immutable.equals(List.of("old")),
        "immutable native menu list is replaced rather than cleared");
    check(
        DataEncoder.copyValue(immutable, immutable),
        "identical immutable menu generation is an observational no-op");
    var mutable = new java.util.ArrayList<>(immutable);
    check(
        DataEncoder.copyValue(incoming, mutable) && mutable.equals(incoming),
        "owned mutable menu list retains its native identity");
    var holder = new ImmutableField();
    ic2ce.neoforge.network.ReflectionUtil.setValueRecursive(holder, "values", incoming);
    check(
        holder.values.equals(incoming),
        "actual reflected native menu publication supports immutable previous fields");
    var unicode = new CompoundTag();
    unicode.putString("中文名称", "机器与升级 / IndustrialCraft2-CE / 漢字 / é / \u0000");
    unicode.putInt("数量", 123456);
    var encoded = new GrowingBuffer(0);
    DataEncoder.encode(encoded, unicode, true);
    encoded.flip();
    check(
        unicode.equals(DataEncoder.decode(encoded)),
        "native compound data preserves multibyte keys, values and embedded zero");
    for (String text : List.of("English", "中文测试", "漢字", "éÅñ", "\u0000")) {
      encoded.clear();
      encoded.writeUTF(text);
      encoded.flip();
      check(
          text.equals(encoded.readUTF()),
          "native modified UTF preserves " + text.length() + " characters");
    }
    encoded.clear();
    encoded.writeInt(123);
    encoded.writeString("传输");
    encoded.flip();
    var first = encoded.copy(4);
    check(
        first.readInt() == 123 && encoded.readString().equals("传输") && !encoded.hasAvailable(),
        "native copy advances exactly the requested readable bytes");
    var named =
        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 300);
    named.set(
        net.minecraft.core.component.DataComponents.CUSTOM_NAME,
        net.minecraft.network.chat.Component.literal("中文组件 / components"));
    var custom = new CompoundTag();
    custom.putInt("quantity", 123);
    ic2ce.core.util.StackTag.set(named, custom);
    encoded.clear();
    DataEncoder.encode(encoded, named, true);
    encoded.flip();
    var received = (net.minecraft.world.item.ItemStack) DataEncoder.decode(encoded);
    check(
        net.minecraft.world.item.ItemStack.matches(named, received),
        "item counts above signed-byte range and every component round-trip");
    var nativeFluid =
        new net.neoforged.neoforge.fluids.FluidStack(
            net.minecraft.world.level.material.Fluids.WATER, 900);
    nativeFluid.set(
        net.minecraft.core.component.DataComponents.CUSTOM_NAME,
        net.minecraft.network.chat.Component.literal("流体组件"));
    var savedFluid = new CompoundTag();
    savedFluid.putString("FluidName", "minecraft:water");
    savedFluid.putInt("Amount", 900);
    savedFluid.put(
        "Components",
        net.minecraft.core.component.DataComponentPatch.CODEC
            .encodeStart(
                ic2ce.neoforge.runtime.ProjectRuntime.envProxy
                    .getRegistryLookup()
                    .createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                nativeFluid.getComponentsPatch())
            .getOrThrow());
    var fluid = ic2ce.api.fluid.Ic2FluidStack.read(savedFluid);
    encoded.clear();
    DataEncoder.encode(encoded, fluid, true);
    encoded.flip();
    check(
        fluid.equals(DataEncoder.decode(encoded)),
        "fluid codec preserves components outside custom data");
    for (Object array :
        List.of(
            new byte[] {1, -1, 3},
            new int[] {Integer.MIN_VALUE, 123, Integer.MAX_VALUE},
            new long[] {Long.MIN_VALUE, 7, Long.MAX_VALUE},
            new char[] {'中', '文', 'é'},
            new int[][] {{1, 2}, {3}},
            new Object[] {null, null},
            new net.minecraft.core.Direction[] {
              net.minecraft.core.Direction.WEST, null, net.minecraft.core.Direction.UP
            })) {
      encoded.clear();
      DataEncoder.encode(encoded, array, true);
      encoded.flip();
      check(
          java.util.Objects.deepEquals(array, DataEncoder.decode(encoded)),
          "typed, nested, primitive and null array round-trip " + array.getClass().getSimpleName());
    }
    var numbers = new int[1];
    DataEncoder.addNetworkEncoder(
        DeferredNumber.class,
        new ic2ce.api.network.INetworkCustomEncoder() {
          public boolean isThreadSafe() {
            return false;
          }

          public void encode(ic2ce.api.network.IGrowingBuffer out, Object value)
              throws java.io.IOException {
            out.writeInt(((DeferredNumber) value).value());
          }

          public Object decode(ic2ce.api.network.IGrowingBuffer in) throws java.io.IOException {
            numbers[0]++;
            return new DeferredNumber(in.readInt());
          }
        });
    encoded.clear();
    DataEncoder.encode(encoded, new Object[] {new DeferredNumber(77), "tail", named}, true);
    encoded.writeInt(1234);
    encoded.flip();
    Object deferred = DataEncoder.decode(encoded);
    check(
        numbers[0] == 0 && encoded.readInt() == 1234,
        "deferred addon captures its own bounded frame before following values decode");
    Object[] resolved =
        DataEncoder.getValue(
            deferred, net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());
    check(
        ((DeferredNumber) resolved[0]).value() == 77
            && resolved[1].equals("tail")
            && net.minecraft.world.item.ItemStack.matches(
                named, (net.minecraft.world.item.ItemStack) resolved[2]),
        "world-thread resolution preserves the complete addon and following values");
    DataEncoder.getValue(
        deferred, net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());
    check(numbers[0] == 1, "an unsafe addon decoder runs once per captured frame");
    encoded.clear();
    encoded.writeByte(DataEncoder.EncodedType.Addon.ordinal());
    encoded.writeString(InitializationTripwire.class.getName());
    encoded.writeVarInt(0);
    encoded.flip();
    boolean rejected = false;
    try {
      DataEncoder.decode(encoded);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(
        rejected && initialized == 0,
        "unregistered wire class names never initialize arbitrary classes");
    encoded.clear();
    encoded.writeByte(DataEncoder.EncodedType.Array.ordinal());
    encoded.writeByte(DataEncoder.EncodedType.Integer.ordinal());
    encoded.writeBoolean(true);
    encoded.writeVarInt(Integer.MAX_VALUE);
    encoded.flip();
    rejected = false;
    try {
      DataEncoder.decode(encoded);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "oversized array length is rejected before allocating its contents");
    Object nested = "leaf";
    for (int i = 0; i < 80; i++) nested = new Object[] {nested};
    rejected = false;
    try {
      encoded.clear();
      DataEncoder.encode(encoded, nested, true);
    } catch (java.io.IOException expected) {
      rejected = true;
    }
    check(rejected, "deep values are rejected without an unbounded recursive traversal");
    var from = new ic2ce.neoforge.inventory.InventorySlot(1);
    var into = new ic2ce.neoforge.inventory.InventorySlot(1);
    from.put(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 7));
    into.put(named.copy());
    check(
        DataEncoder.copyValue(from, into)
            && into.get().getCount() == 7
            && !into.get().has(net.minecraft.core.component.DataComponents.CUSTOM_NAME),
        "inventory sync removes stale components when replacing an item");
    from.get().setCount(1);
    check(
        into.get().getCount() == 7,
        "synchronized inventory snapshots do not alias the incoming storage");

    var primitive = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
    primitive.add(7);
    var selection = List.of(11L, 12L, 12L);
    check(
        DataEncoder.copyValue(selection, primitive)
            && primitive.size() == 2
            && primitive.contains(11)
            && primitive.contains(12),
        "boxed wire selection replaces a native primitive set while retaining its final field"
            + " identity");
    boolean invalidSet = false;
    try {
      DataEncoder.copyValue(List.of(1L, "invalid"), primitive);
    } catch (IllegalArgumentException expected) {
      invalidSet = true;
    }
    check(
        invalidSet && primitive.size() == 2 && primitive.contains(11),
        "malformed primitive selection is rejected before mutating the working client selection");
    return new Result(checks, List.copyOf(failures));
  }

  public static final class ImmutableField {
    public List<String> values = List.of("old");
  }
}
