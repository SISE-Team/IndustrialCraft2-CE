package ic2ce.neoforge.network;

import com.mojang.authlib.GameProfile;
import ic2ce.api.crops.CropCard;
import ic2ce.api.crops.Crops;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.network.IGrowingBuffer;
import ic2ce.api.network.INetworkCustomEncoder;
import ic2ce.api.recipe.IElectrolyzerRecipeManager;
import ic2ce.neoforge.fluid.Ic2FluidTank;
import ic2ce.engine.value.ValueTuples;
import ic2ce.neoforge.component.NativeComponent;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.runtime.SideServices;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

/**
 * Protocol 2: bounded typed values, complete components and independently framed addon payloads.
 */
public final class DataEncoder {
  private static final int MAX_ELEMENTS = 1 << 20, MAX_BYTES = 8 << 20, MAX_DEPTH = 64;

  private record Extension(Class<?> type, INetworkCustomEncoder codec) {}

  private record EnumBinding(Class<?> type, Object[] values) {}

  private static final Map<Class<?>, Extension> extensions = new ConcurrentHashMap<>();
  private static final Map<String, Extension> extensionNames = new ConcurrentHashMap<>();
  private static final Map<String, EnumBinding> enums = new ConcurrentHashMap<>();
  private static final ClassValue<EncodedType> kinds =
      new ClassValue<>() {
        @Override
        protected EncodedType computeValue(Class<?> type) {
          var extension = extensions.get(type);
          if (extension != null)
            return extension.codec().isThreadSafe() ? EncodedType.Addon : EncodedType.UnSafeAddon;
          for (var kind : EncodedType.types)
            if (kind.cls != null && kind.cls.isAssignableFrom(type)) return kind;
          throw new IllegalArgumentException("Unsupported network value: " + type.getName());
        }
      };
  private static final ClassValue<byte[]> typeNames =
      new ClassValue<>() {
        @Override
        protected byte[] computeValue(Class<?> type) {
          return type.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
      };
  private static final ThreadLocal<Budget> encodeWorkspace = ThreadLocal.withInitial(Budget::new);

  private static final class Budget {
    int depth, values, rootArrayLimit = MAX_ELEMENTS;

    void enter() throws IOException {
      if (++depth > MAX_DEPTH || ++values > MAX_ELEMENTS)
        throw new IOException("Network value nesting/quantity limit exceeded");
    }

    void array(int count) throws IOException {
      if (count < 0 || count > MAX_ELEMENTS - values || depth == 1 && count > rootArrayLimit)
        throw new IOException("Network array quantity limit exceeded");
    }
  }

  @FunctionalInterface
  private interface Deferred {
    Object resolve(MinecraftServer server);
  }

  private static final class DeferredExtension implements Deferred {
    private final Extension binding;
    private final byte[] bytes;
    private boolean completed;
    private Object value;
    private RuntimeException failure;

    DeferredExtension(Extension binding, byte[] bytes) {
      this.binding = binding;
      this.bytes = bytes;
    }

    @Override
    public synchronized Object resolve(MinecraftServer server) {
      if (!completed) {
        try {
          value = decodeExtension(binding, bytes);
        } catch (IOException error) {
          failure = new IllegalStateException("Failed deferred network extension", error);
        } catch (RuntimeException error) {
          failure = error;
        } finally {
          completed = true;
        }
      }
      if (failure != null) throw failure;
      return value;
    }
  }

  private DataEncoder() {}

  static {
    registerEnumType(Direction.class);
  }

  public static void registerEnumType(Class<? extends Enum<?>> type) {
    Objects.requireNonNull(type);
    String name = type.getName();
    if (!enums.containsKey(name))
      enums.putIfAbsent(name, new EnumBinding(type, type.getEnumConstants()));
  }

  public static void addNetworkEncoder(Class<?> type, INetworkCustomEncoder encoder) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(encoder);
    var binding = new Extension(type, encoder);
    if (extensions.putIfAbsent(type, binding) != null)
      throw new IllegalStateException("Duplicate network encoder: " + type.getName());
    extensionNames.put(type.getName(), binding);
    kinds.remove(type);
  }

  private static EncodedType kind(Class<?> type) {
    if (type == null) return EncodedType.Null;
    if (type.isArray()) return EncodedType.Array;
    if (type.isPrimitive()) type = boxed(type);
    return kinds.get(type);
  }

  private static EncodedType kind(Object value) {
    return value == null ? EncodedType.Null : kind(value.getClass());
  }

  private static EncodedType fromId(int id) throws IOException {
    if (id < 0 || id >= EncodedType.types.length)
      throw new IOException("Unknown network value identifier: " + id);
    return EncodedType.types[id];
  }

  private static HolderLookup.Provider lookup() throws IOException {
    var registry = EnvProxy.get().getRegistryLookup();
    if (registry == null) throw new IOException("Network components require a registry context");
    return registry;
  }

  public static void encode(GrowingBuffer output, Object value) throws IOException {
    encode(output, value, true);
  }

  public static void encode(IGrowingBuffer output, Object value, boolean tagged)
      throws IOException {
    var budget = encodeWorkspace.get();
    boolean root = budget.depth == 0;
    if (root) {
      budget.values = 0;
      budget.rootArrayLimit = MAX_ELEMENTS;
    }
    try {
      write(output, value, tagged, budget);
    } finally {
      if (root) {
        budget.depth = 0;
        budget.values = 0;
      }
    }
  }

  private static void write(IGrowingBuffer output, Object value, boolean tagged, Budget budget)
      throws IOException {
    budget.enter();
    try {
      var type = kind(value);
      if (tagged) output.writeByte(type.ordinal());
      switch (type) {
        case Null -> {
          if (!tagged) throw new IOException("A null network value requires a type tag");
        }
        case Byte -> output.writeByte((Byte) value);
        case Short -> output.writeShort((Short) value);
        case Integer -> output.writeInt((Integer) value);
        case Long -> output.writeLong((Long) value);
        case Float -> output.writeFloat((Float) value);
        case Double -> output.writeDouble((Double) value);
        case Boolean -> output.writeBoolean((Boolean) value);
        case Character -> output.writeChar((Character) value);
        case String -> output.writeString((String) value);
        case Enum -> output.writeVarInt(((Enum<?>) value).ordinal());
        case UUID -> {
          var id = (UUID) value;
          output.writeLong(id.getMostSignificantBits());
          output.writeLong(id.getLeastSignificantBits());
        }
        case Identifier -> {
          var id = (Identifier) value;
          output.writeString(id.getNamespace());
          output.writeString(id.getPath());
        }
        case Block -> write(output, BuiltInRegistries.BLOCK.getKey((Block) value), false, budget);
        case Item -> write(output, BuiltInRegistries.ITEM.getKey((Item) value), false, budget);
        case Fluid -> write(output, BuiltInRegistries.FLUID.getKey((Fluid) value), false, budget);
        case Potion ->
            write(output, BuiltInRegistries.MOB_EFFECT.getKey((MobEffect) value), false, budget);
        case BlockPos -> {
          var position = (BlockPos) value;
          output.writeInt(position.getX());
          output.writeInt(position.getY());
          output.writeInt(position.getZ());
        }
        case ChunkPos -> {
          var position = (ColumnPos) value;
          output.writeInt(position.x());
          output.writeInt(position.z());
        }
        case Vec3 -> {
          var vector = (Vec3) value;
          output.writeDouble(vector.x);
          output.writeDouble(vector.y);
          output.writeDouble(vector.z);
        }
        case World -> write(output, ((Level) value).dimension().identifier(), false, budget);
        case TileEntity -> {
          var tile = (BlockEntity) value;
          write(output, tile.getLevel(), false, budget);
          write(output, tile.getBlockPos(), false, budget);
        }
        case GameProfile -> {
          var profile = (GameProfile) value;
          write(output, profile.id(), true, budget);
          output.writeString(profile.name());
        }
        case NBTTagCompound -> NbtIo.write((CompoundTag) value, output);
        case ItemStack -> writeItem(output, (ItemStack) value, budget);
        case FluidStack -> {
          var data = new CompoundTag();
          ((Ic2FluidStack) value).toNbt(data);
          write(output, data, false, budget);
        }
        case FluidTank -> {
          var tank = (Ic2FluidTank) value;
          write(output, tank.getFluidStack(), true, budget);
          output.writeInt(tank.getCapacity());
        }
        case InventorySlot -> {
          var slot = (InventorySlot) value;
          var contents = new ItemStack[slot.size()];
          for (int index = 0; index < contents.length; index++) contents[index] = slot.get(index);
          write(output, contents, false, budget);
        }
        case Component -> {
          var data = ((NativeComponent) value).writeToNbt();
          write(output, data == null ? new CompoundTag() : data, false, budget);
        }
        case CropCard -> {
          var crop = (CropCard) value;
          output.writeString(crop.getOwner());
          output.writeString(crop.getId());
        }
        case ElectrolyzerRecipe -> {
          var recipe = (IElectrolyzerRecipeManager.ElectrolyzerRecipe) value;
          output.writeInt(recipe.inputAmount());
          output.writeInt(recipe.EUaTick());
          output.writeInt(recipe.ticksNeeded());
          if (recipe.outputs().size() > 255) throw new IOException("Too many electrolyzer outputs");
          output.writeByte(recipe.outputs().size());
          for (var target : recipe.outputs()) {
            write(output, target.fluid(), false, budget);
            output.writeInt(target.fluidAmount());
            output.writeByte(target.tankDirection().get3DDataValue());
          }
        }
        case Enchantment -> {
          var entry =
              lookup()
                  .lookupOrThrow(Registries.ENCHANTMENT)
                  .listElements()
                  .filter(holder -> holder.value() == value)
                  .findFirst()
                  .orElseThrow(() -> new IOException("Unregistered enchantment value"));
          write(output, entry.key().identifier(), false, budget);
        }
        case TupleT2 -> {
          var tuple = (ValueTuples.T2<?, ?>) value;
          write(output, tuple.a, true, budget);
          write(output, tuple.b, true, budget);
        }
        case TupleT3 -> {
          var tuple = (ValueTuples.T3<?, ?, ?>) value;
          write(output, tuple.a, true, budget);
          write(output, tuple.b, true, budget);
          write(output, tuple.c, true, budget);
        }
        case Array -> writeArray(output, value, budget);
        case Collection -> {
          if (value instanceof List<?> list && value instanceof java.util.RandomAccess)
            writeList(output, list, budget);
          else writeArray(output, ((Collection<?>) value).toArray(), budget);
        }
        case Addon, UnSafeAddon -> writeExtension(output, value);
        case Object ->
            throw new IOException("No registered encoder for " + value.getClass().getName());
      }
    } finally {
      budget.depth--;
    }
  }

  private static void writeItem(IGrowingBuffer output, ItemStack stack, Budget budget)
      throws IOException {
    if (stack.isEmpty()) {
      output.writeVarInt(0);
      return;
    }
    output.writeVarInt(stack.getCount());
    write(output, stack.getItem(), false, budget);
    var patch =
        DataComponentPatch.CODEC
            .encodeStart(
                lookup().createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch())
            .getOrThrow(message -> new IOException("Cannot encode item components: " + message));
    if (!(patch instanceof CompoundTag data))
      throw new IOException("Item components did not encode as a compound");
    write(output, data, false, budget);
  }

  private static void writeExtension(IGrowingBuffer output, Object value) throws IOException {
    var binding = extensions.get(value.getClass());
    if (binding == null) throw new IOException("Missing registered network encoder");
    var body = new GrowingBuffer(128);
    binding.codec().encode(body, value);
    body.flip();
    if (body.available() > MAX_BYTES) throw new IOException("Addon payload size limit exceeded");
    output.writeString(binding.type().getName());
    output.writeVarInt(body.available());
    output.write(body.copyBytes(MAX_BYTES));
  }

  private static Class<?> arrayComponent(Object array) throws IOException {
    var declared = array.getClass().getComponentType();
    if (declared != Object.class) return declared;
    Class<?> candidate = null;
    for (Object entry : (Object[]) array) {
      if (entry == null) continue;
      Class<?> current =
          entry instanceof Enum<?> member ? member.getDeclaringClass() : entry.getClass();
      if (candidate == null) candidate = current;
      else if (candidate != current) {
        if (candidate.isEnum() || current.isEnum())
          throw new IOException("An enum array requires one declaring enum type");
        return Object.class;
      }
    }
    return candidate == null ? Object.class : candidate;
  }

  @SuppressWarnings("unchecked")
  private static void writeComponent(IGrowingBuffer output, Class<?> component, int depth)
      throws IOException {
    if (depth > MAX_DEPTH) throw new IOException("Array type nesting limit exceeded");
    var type = kind(component);
    output.writeByte(type.ordinal());
    if (component.isArray()) {
      writeComponent(output, component.getComponentType(), depth + 1);
      return;
    }
    output.writeBoolean(component.isPrimitive());
    if (component.isEnum()) registerEnumType((Class<? extends Enum<?>>) component);
    if (type == EncodedType.Enum || type == EncodedType.Addon || type == EncodedType.UnSafeAddon) {
      var name = typeNames.get(component);
      output.writeVarInt(name.length);
      output.write(name);
    }
  }

  private static void writeList(IGrowingBuffer output, List<?> values, Budget budget)
      throws IOException {
    int count = values.size();
    budget.array(count);
    Class<?> component = null;
    for (int index = 0; index < count; index++) {
      Object entry = values.get(index);
      if (entry == null) continue;
      Class<?> current =
          entry instanceof Enum<?> member ? member.getDeclaringClass() : entry.getClass();
      if (component == null) component = current;
      else if (component != current) {
        if (component.isEnum() || current.isEnum())
          throw new IOException("An enum array requires one declaring enum type");
        component = Object.class;
        break;
      }
    }
    if (component == null) component = Object.class;
    writeComponent(output, component, 0);
    output.writeVarInt(count);
    for (int index = 0; index < count; index++) {
      Object entry = values.get(index);
      if (entry != null && !component.isInstance(entry))
        throw new IOException("Network list changed type while encoding");
      write(output, entry, true, budget);
    }
    if (values.size() != count) throw new IOException("Network list changed size while encoding");
  }

  private static void writeArray(IGrowingBuffer output, Object array, Budget budget)
      throws IOException {
    int count = Array.getLength(array);
    budget.array(count);
    if (array.getClass().getComponentType().isPrimitive()) budget.values += count;
    writeComponent(output, arrayComponent(array), 0);
    output.writeVarInt(count);
    switch (array) {
      case byte[] values -> output.write(values);
      case short[] values -> {
        for (short value : values) output.writeShort(value);
      }
      case int[] values -> {
        for (int value : values) output.writeInt(value);
      }
      case long[] values -> {
        for (long value : values) output.writeLong(value);
      }
      case float[] values -> {
        for (float value : values) output.writeFloat(value);
      }
      case double[] values -> {
        for (double value : values) output.writeDouble(value);
      }
      case boolean[] values -> {
        for (boolean value : values) output.writeBoolean(value);
      }
      case char[] values -> {
        for (char value : values) output.writeChar(value);
      }
      case Object[] values -> {
        for (Object value : values) write(output, value, true, budget);
      }
      default -> throw new IOException("Unsupported array storage");
    }
  }

  public static Object decode(IGrowingBuffer input) throws IOException {
    return read(input, fromId(input.readUnsignedByte()), new Budget());
  }

  public static Object decodeArguments(IGrowingBuffer input, int maximum) throws IOException {
    if (maximum < 0 || maximum > MAX_ELEMENTS)
      throw new IllegalArgumentException("Invalid network argument limit");
    if (fromId(input.readUnsignedByte()) != EncodedType.Array)
      throw new IOException("Expected a tagged argument array");
    var budget = new Budget();
    budget.rootArrayLimit = maximum;
    return read(input, EncodedType.Array, budget);
  }

  @SuppressWarnings("unchecked")
  public static <T> T decode(IGrowingBuffer input, Class<T> type) throws IOException {
    remember(type);
    var encoded = kind(type);
    if (!encoded.threadSafe)
      throw new IOException("World values must be resolved on the owning thread");
    Object value = read(input, encoded, new Budget());
    return (T) (type.isEnum() ? enumValue(type, (Integer) value) : value);
  }

  public static <T extends Enum<T>> T decodeEnum(IGrowingBuffer input, Class<T> type)
      throws IOException {
    return type.cast(enumValue(type, input.readVarInt()));
  }

  public static Object decodeDeferred(GrowingBuffer input, Class<?> type) throws IOException {
    remember(type);
    Object value = read(input, kind(type), new Budget());
    return type.isEnum() ? enumValue(type, (Integer) value) : value;
  }

  public static Object decode(IGrowingBuffer input, EncodedType type) throws IOException {
    return read(input, Objects.requireNonNull(type), new Budget());
  }

  @SuppressWarnings("unchecked")
  private static void remember(Class<?> type) {
    if (type.isArray()) remember(type.getComponentType());
    else if (type.isEnum()) registerEnumType((Class<? extends Enum<?>>) type);
  }

  private static Object tagged(IGrowingBuffer input, Budget budget) throws IOException {
    return read(input, fromId(input.readUnsignedByte()), budget);
  }

  private static Object read(IGrowingBuffer input, EncodedType type, Budget budget)
      throws IOException {
    budget.enter();
    try {
      return switch (type) {
        case Null -> null;
        case Byte -> input.readByte();
        case Short -> input.readShort();
        case Integer -> input.readInt();
        case Long -> input.readLong();
        case Float -> input.readFloat();
        case Double -> input.readDouble();
        case Boolean -> input.readBoolean();
        case Character -> input.readChar();
        case String -> input.readString();
        case Enum -> input.readVarInt();
        case UUID -> new UUID(input.readLong(), input.readLong());
        case Identifier -> Identifier.fromNamespaceAndPath(input.readString(), input.readString());
        case Block ->
            BuiltInRegistries.BLOCK.getValue(
                (Identifier) read(input, EncodedType.Identifier, budget));
        case Item ->
            BuiltInRegistries.ITEM.getValue(
                (Identifier) read(input, EncodedType.Identifier, budget));
        case Fluid ->
            BuiltInRegistries.FLUID.getValue(
                (Identifier) read(input, EncodedType.Identifier, budget));
        case Potion ->
            BuiltInRegistries.MOB_EFFECT.getValue(
                (Identifier) read(input, EncodedType.Identifier, budget));
        case BlockPos -> new BlockPos(input.readInt(), input.readInt(), input.readInt());
        case ChunkPos -> new ColumnPos(input.readInt(), input.readInt());
        case Vec3 -> new Vec3(input.readDouble(), input.readDouble(), input.readDouble());
        case World -> {
          var dimension = (Identifier) read(input, EncodedType.Identifier, budget);
          yield (Deferred) server -> SideServices.proxy().getWorld(server, dimension);
        }
        case TileEntity -> {
          var dimension = (Deferred) read(input, EncodedType.World, budget);
          var position = (BlockPos) read(input, EncodedType.BlockPos, budget);
          yield (Deferred)
              server -> {
                var world = (Level) dimension.resolve(server);
                return world == null ? null : world.getBlockEntity(position);
              };
        }
        case GameProfile -> new GameProfile((UUID) tagged(input, budget), input.readString());
        case NBTTagCompound, Component -> NbtIo.read(input, NbtAccounter.create(MAX_BYTES));
        case ItemStack -> readItem(input, budget);
        case FluidStack -> {
          var data = (CompoundTag) read(input, EncodedType.NBTTagCompound, budget);
          var fluid = Ic2FluidStack.read(data);
          if (fluid == null) throw new IOException("Invalid encoded fluid stack");
          yield fluid;
        }
        case FluidTank -> {
          var content = (Ic2FluidStack) tagged(input, budget);
          int capacity = input.readInt();
          if (capacity < 0) throw new IOException("Negative encoded fluid capacity");
          var tank = new Ic2FluidTank(capacity);
          tank.setFluidStack(content);
          yield tank;
        }
        case InventorySlot -> {
          var contents = (ItemStack[]) read(input, EncodedType.Array, budget);
          var slot = new InventorySlot(contents.length);
          for (int index = 0; index < contents.length; index++) slot.put(index, contents[index]);
          yield slot;
        }
        case CropCard -> Crops.instance.getCropCard(input.readString(), input.readString());
        case ElectrolyzerRecipe -> {
          int volume = input.readInt(),
              demand = input.readInt(),
              ticks = input.readInt(),
              count = input.readUnsignedByte();
          if (volume <= 0 || demand < 0 || ticks <= 0 || count < 1 || count > 5)
            throw new IOException("Invalid electrolyzer recipe budget");
          var outputs = new IElectrolyzerRecipeManager.ElectrolyzerOutput[count];
          for (int index = 0; index < count; index++) {
            var fluid = (Fluid) read(input, EncodedType.Fluid, budget);
            int amount = input.readInt(), direction = input.readUnsignedByte();
            if (amount <= 0 || direction >= 6) throw new IOException("Invalid electrolyzer output");
            outputs[index] =
                new IElectrolyzerRecipeManager.ElectrolyzerOutput(
                    fluid, amount, Direction.from3DDataValue(direction));
          }
          yield new IElectrolyzerRecipeManager.ElectrolyzerRecipe(volume, demand, ticks, outputs);
        }
        case Enchantment ->
            lookup()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(
                    ResourceKey.create(
                        Registries.ENCHANTMENT,
                        (Identifier) read(input, EncodedType.Identifier, budget)))
                .value();
        case TupleT2 -> {
          var a = tagged(input, budget);
          var b = tagged(input, budget);
          yield a instanceof Deferred || b instanceof Deferred
              ? (Deferred) server -> new ValueTuples.T2<>(getValue(a, server), getValue(b, server))
              : new ValueTuples.T2<>(a, b);
        }
        case TupleT3 -> {
          var a = tagged(input, budget);
          var b = tagged(input, budget);
          var c = tagged(input, budget);
          yield a instanceof Deferred || b instanceof Deferred || c instanceof Deferred
              ? (Deferred)
                  server ->
                      new ValueTuples.T3<>(
                          getValue(a, server), getValue(b, server), getValue(c, server))
              : new ValueTuples.T3<>(a, b, c);
        }
        case Array -> readArray(input, budget);
        case Collection -> {
          var values = readArray(input, budget);
          yield values instanceof Deferred delayed
              ? (Deferred) server -> arrayList(delayed.resolve(server))
              : arrayList(values);
        }
        case Addon, UnSafeAddon -> readExtension(input);
        case Object -> throw new IOException("Bare objects have no network encoding");
      };
    } catch (IllegalArgumentException | ClassCastException error) {
      throw new IOException("Invalid network value for " + type, error);
    } finally {
      budget.depth--;
    }
  }

  private static ItemStack readItem(IGrowingBuffer input, Budget budget) throws IOException {
    int count = input.readVarInt();
    if (count == 0) return ItemStack.EMPTY;
    var item = (Item) read(input, EncodedType.Item, budget);
    if (item == null || item == net.minecraft.world.item.Items.AIR)
      throw new IOException("Unknown encoded item");
    var data = (CompoundTag) read(input, EncodedType.NBTTagCompound, budget);
    var patch =
        DataComponentPatch.CODEC
            .parse(lookup().createSerializationContext(NbtOps.INSTANCE), data)
            .getOrThrow(message -> new IOException("Cannot decode item components: " + message));
    var stack = new ItemStack(item, count);
    stack.applyComponents(patch);
    return stack;
  }

  private static Object readExtension(IGrowingBuffer input) throws IOException {
    String name = input.readString();
    var binding = extensionNames.get(name);
    if (binding == null) throw new IOException("Unregistered network extension: " + name);
    int length = input.readVarInt();
    if (length > MAX_BYTES) throw new IOException("Addon payload size limit exceeded");
    if (input instanceof GrowingBuffer buffer && length > buffer.available())
      throw new IOException("Truncated addon payload");
    var bytes = new byte[length];
    input.readFully(bytes);
    if (binding.codec().isThreadSafe()) return decodeExtension(binding, bytes);
    return new DeferredExtension(binding, bytes);
  }

  private static Object decodeExtension(Extension binding, byte[] bytes) throws IOException {
    var input = GrowingBuffer.wrap(bytes);
    var value = binding.codec().decode(input);
    if (input.hasAvailable() || value != null && !binding.type().isInstance(value))
      throw new IOException("Network extension violated its declared value/payload boundary");
    return value;
  }

  private static Class<?> readComponent(IGrowingBuffer input, int depth) throws IOException {
    if (depth > MAX_DEPTH) throw new IOException("Array type nesting limit exceeded");
    var type = fromId(input.readUnsignedByte());
    if (type == EncodedType.Array)
      return Array.newInstance(readComponent(input, depth + 1), 0).getClass();
    boolean primitive = input.readBoolean();
    Class<?> component = type.cls;
    if (type == EncodedType.Enum) {
      var binding = enums.get(input.readString());
      if (binding == null) throw new IOException("Unregistered network enum array type");
      component = binding.type();
    } else if (type == EncodedType.Addon || type == EncodedType.UnSafeAddon) {
      var binding = extensionNames.get(input.readString());
      if (binding == null) throw new IOException("Unregistered network addon array type");
      component = binding.type();
    }
    if (component == null) throw new IOException("Invalid network array component type");
    if (primitive) {
      component = unboxed(component);
      if (!component.isPrimitive())
        throw new IOException("Invalid primitive network array component");
    }
    return component;
  }

  private static Object readArray(IGrowingBuffer input, Budget budget) throws IOException {
    Class<?> component = readComponent(input, 0);
    int count = input.readVarInt();
    budget.array(count);
    if (component.isPrimitive()) {
      Object result = Array.newInstance(component, count);
      budget.values += count;
      switch (result) {
        case byte[] values -> input.readFully(values);
        case short[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readShort();
        }
        case int[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readInt();
        }
        case long[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readLong();
        }
        case float[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readFloat();
        }
        case double[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readDouble();
        }
        case boolean[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readBoolean();
        }
        case char[] values -> {
          for (int i = 0; i < count; i++) values[i] = input.readChar();
        }
        default -> throw new IOException("Unsupported primitive array");
      }
      return result;
    }
    var values = new Object[count];
    boolean deferred = false;
    for (int index = 0; index < count; index++) {
      values[index] = tagged(input, budget);
      deferred |= values[index] instanceof Deferred;
    }
    return deferred
        ? (Deferred) server -> assembleArray(component, values, server)
        : assembleArray(component, values, null);
  }

  private static Object assembleArray(Class<?> component, Object[] values, MinecraftServer server) {
    var result = Array.newInstance(component, values.length);
    for (int index = 0; index < values.length; index++) {
      Object value = getValue(values[index], server);
      if (component.isEnum() && value instanceof Integer ordinal) {
        try {
          value = enumValue(component, ordinal);
        } catch (IOException error) {
          throw new IllegalArgumentException(error);
        }
      }
      Array.set(result, index, value);
    }
    return result;
  }

  private static List<Object> arrayList(Object array) {
    int length = Array.getLength(array);
    var result = new ArrayList<Object>(length);
    for (int i = 0; i < length; i++) result.add(Array.get(array, i));
    return result;
  }

  private static Object enumValue(Class<?> type, int ordinal) throws IOException {
    if (!type.isEnum()) throw new IOException("Expected a local enum class");
    var binding =
        enums.computeIfAbsent(type.getName(), _ -> new EnumBinding(type, type.getEnumConstants()));
    if (ordinal < 0 || ordinal >= binding.values().length)
      throw new IOException("Invalid enum ordinal");
    return binding.values()[ordinal];
  }

  @SuppressWarnings("unchecked")
  public static <T> T getValue(Object decoded, MinecraftServer server) {
    return (T) (decoded instanceof Deferred value ? value.resolve(server) : decoded);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> boolean copyValue(T source, T destination) {
    if (source == null || destination == null) return false;
    switch (destination) {
      case ItemStack target when source instanceof ItemStack from -> {
        if (!ItemStack.isSameItemSameComponents(from, target)) return false;
        target.setCount(from.getCount());
        return true;
      }
      case Ic2FluidTank target when source instanceof Ic2FluidTank from -> {
        target.setCapacity(from.getCapacity());
        var fluid = from.getFluidStack();
        target.setFluidStack(fluid == null ? null : fluid.copy());
        return true;
      }
      case InventorySlot target when source instanceof InventorySlot from -> {
        if (from.size() != target.size())
          throw new IllegalArgumentException("Inventory synchronization size mismatch");
        for (int index = 0; index < from.size(); index++)
          if (!ItemStack.matches(from.get(index), target.get(index)))
            target.put(index, from.get(index).copy());
        return true;
      }
      case NativeComponent target when source instanceof CompoundTag data -> {
        target.readFromNbt(data);
        return true;
      }
      case it.unimi.dsi.fastutil.longs.LongOpenHashSet target
          when source instanceof Collection<?> from -> {
        if (source == destination) return true;
        if (from.size() > MAX_ELEMENTS)
          throw new IllegalArgumentException("Primitive set synchronization is too large");
        var replacement = new it.unimi.dsi.fastutil.longs.LongOpenHashSet(from.size());
        for (var value : from) {
          if (!(value instanceof Long number))
            throw new IllegalArgumentException("Primitive long set contains a non-long value");
          replacement.add(number.longValue());
        }
        target.clear();
        target.addAll(replacement);
        return true;
      }
      case Collection target when source instanceof Collection from -> {
        if (source == destination) return true;
        Class<?> type = target.getClass();
        if (type != ArrayList.class
            && type != java.util.LinkedList.class
            && type != java.util.HashSet.class
            && type != java.util.LinkedHashSet.class) return false;
        // These exact standard implementations permit replacement; immutable/custom collections
        // need field publication.
        var replacement = new ArrayList<>(from);
        target.clear();
        target.addAll(replacement);
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  private static Class<?> boxed(Class<?> type) {
    if (type == byte.class) return Byte.class;
    if (type == short.class) return Short.class;
    if (type == int.class) return Integer.class;
    if (type == long.class) return Long.class;
    if (type == float.class) return Float.class;
    if (type == double.class) return Double.class;
    if (type == boolean.class) return Boolean.class;
    if (type == char.class) return Character.class;
    return type;
  }

  private static Class<?> unboxed(Class<?> type) {
    if (type == Byte.class) return byte.class;
    if (type == Short.class) return short.class;
    if (type == Integer.class) return int.class;
    if (type == Long.class) return long.class;
    if (type == Float.class) return float.class;
    if (type == Double.class) return double.class;
    if (type == Boolean.class) return boolean.class;
    if (type == Character.class) return char.class;
    return type;
  }

  public enum EncodedType {
    Null(null),
    Array(null),
    Byte(Byte.class),
    Short(Short.class),
    Integer(Integer.class),
    Long(Long.class),
    Float(Float.class),
    Double(Double.class),
    Boolean(Boolean.class),
    Character(Character.class),
    String(String.class),
    Enum(Enum.class),
    UUID(UUID.class),
    Block(Block.class),
    Item(Item.class),
    TileEntity(BlockEntity.class, false),
    ItemStack(ItemStack.class),
    World(Level.class, false),
    NBTTagCompound(CompoundTag.class),
    Identifier(Identifier.class),
    GameProfile(GameProfile.class),
    Potion(MobEffect.class),
    Enchantment(Enchantment.class),
    BlockPos(BlockPos.class),
    ChunkPos(ColumnPos.class),
    Vec3(Vec3.class),
    Fluid(Fluid.class),
    FluidStack(Ic2FluidStack.class),
    FluidTank(Ic2FluidTank.class),
    InventorySlot(InventorySlot.class),
    Component(NativeComponent.class, false),
    CropCard(CropCard.class),
    ElectrolyzerRecipe(IElectrolyzerRecipeManager.ElectrolyzerRecipe.class),
    TupleT2(ValueTuples.T2.class),
    TupleT3(ValueTuples.T3.class),
    Addon(null),
    UnSafeAddon(null, false),
    Collection(Collection.class),
    Object(Object.class);
    final Class<?> cls;
    final boolean threadSafe;
    static final EncodedType[] types = values();

    EncodedType(Class<?> type) {
      this(type, true);
    }

    EncodedType(Class<?> type, boolean safe) {
      cls = type;
      threadSafe = safe;
    }
  }
}
