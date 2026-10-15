package ic2ce.core.util;

import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.projectile.Vector3;
import ic2ce.neoforge.runtime.AutomationActors;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.lang.reflect.Modifier;
import java.math.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Native identity, checked numeric presentation and cached interface inspection use standard
 * Java/world APIs.
 */
public final class Util {
  private Util() {}

  public static final Direction[] ALL_DIRS = Direction.values();
  public static final Direction[] HORIZONTAL_DIRS = {
    Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
  };
  public static final Set<Direction> noFacings = Set.of(),
      onlyNorth = Set.of(Direction.NORTH),
      horizontalFacings = Set.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
      verticalFacings = Set.of(Direction.DOWN, Direction.UP),
      downSideFacings =
          Set.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
      allFacings = Set.of(Direction.values());
  public static final InteractionHand[] HANDS = InteractionHand.values();
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private static final StackWalker CALLERS =
      StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
  private static final boolean DEVELOPMENT = System.getProperty("INDEV") != null,
      WORLD_HASH = System.getProperty("ic2.debug.includeworldhash") != null;
  private static final String[] PREFIXES = {
    "y", "z", "a", "f", "p", "n", "µ", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y"
  };
  private static final ClassValue<Boolean> INTERFACES =
      new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
          var pending = new ArrayDeque<Class<?>>();
          for (Class<?> parent = type; parent != null; parent = parent.getSuperclass())
            Collections.addAll(pending, parent.getInterfaces());
          var visited = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>());
          while (!pending.isEmpty()) {
            var contract = pending.removeFirst();
            if (!visited.add(contract)) continue;
            Collections.addAll(pending, contract.getInterfaces());
            for (var signature : contract.getMethods()) {
              if (Modifier.isStatic(signature.getModifiers()) || signature.isDefault()) continue;
              try {
                var implementation =
                    type.getMethod(signature.getName(), signature.getParameterTypes());
                if (Modifier.isAbstract(implementation.getModifiers())
                    || !signature.getReturnType().isAssignableFrom(implementation.getReturnType()))
                  return false;
              } catch (NoSuchMethodException error) {
                return false;
              }
            }
          }
          return true;
        }
      };

  public static Iterable<ItemStack> armorItems(Player player) {
    return () ->
        new Iterator<>() {
          private int index;

          public boolean hasNext() {
            return index < ARMOR.length;
          }

          public ItemStack next() {
            if (!hasNext()) throw new NoSuchElementException();
            return player.getItemBySlot(ARMOR[index++]);
          }
        };
  }

  public static NonNullList<ItemStack> playerMainItems(Player player) {
    return player.getInventory().getNonEquipmentItems();
  }

  public static int roundToNegInf(float value) {
    return (int) Math.floor(value);
  }

  public static int roundToNegInf(double value) {
    return (int) Math.floor(value);
  }

  public static int saturatedCast(double value) {
    return (int) value;
  }

  public static int limit(int value, int minimum, int maximum) {
    return Math.clamp(value, minimum, maximum);
  }

  public static float limit(float value, float minimum, float maximum) {
    return Math.clamp(value, minimum, maximum);
  }

  public static double limit(double value, double minimum, double maximum) {
    return Math.clamp(value, minimum, maximum);
  }

  public static double map(double value, double sourceMaximum, double targetMaximum) {
    if (!Double.isFinite(sourceMaximum) || sourceMaximum <= 0 || !Double.isFinite(targetMaximum))
      return 0;
    return (Double.isNaN(value) ? 0 : Math.clamp(value, 0, sourceMaximum))
        / sourceMaximum
        * targetMaximum;
  }

  public static double lerp(double start, double end, double fraction) {
    return start + (end - start) * fraction;
  }

  public static float lerp(float start, float end, float fraction) {
    return start + (end - start) * fraction;
  }

  public static int square(int value) {
    return value * value;
  }

  public static float square(float value) {
    return value * value;
  }

  public static double square(double value) {
    return value * value;
  }

  public static boolean isSimilar(float first, float second) {
    return Math.abs(first - second) < 1e-5F;
  }

  public static boolean isSimilar(double first, double second) {
    return Math.abs(first - second) < 1e-5;
  }

  public static int countInArray(Object[] values, Class<?>... types) {
    int count = 0;
    for (var value : values)
      if (value != null) for (var type : types) if (type.isInstance(value)) count++;
    return count;
  }

  public static int countInArray(Object[] values, Class<?> type) {
    int count = 0;
    for (var value : values) if (type.isInstance(value)) count++;
    return count;
  }

  public static boolean checkInterfaces(Class<?> type) {
    return INTERFACES.get(Objects.requireNonNull(type));
  }

  public static boolean inDev() {
    return DEVELOPMENT;
  }

  public static boolean hasAssertions() {
    return Util.class.desiredAssertionStatus();
  }

  public static boolean isCallingFromIc2() {
    return isCallingFromIc2(1);
  }

  public static boolean isCallingFromIc2(int extraFrames) {
    if (extraFrames < 0) throw new IllegalArgumentException("Negative stack offset");
    return CALLERS.walk(
        frames ->
            frames
                .skip(1L + extraFrames)
                .findFirst()
                .map(frame -> frame.getClassName().startsWith("ic2ce."))
                .orElse(true));
  }

  public static Block getBlock(String name) {
    var id = Identifier.tryParse(Objects.requireNonNull(name));
    return id == null ? null : getBlock(id);
  }

  public static Block getBlock(Identifier id) {
    return BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
  }

  public static Item getItem(String name) {
    var id = Identifier.tryParse(Objects.requireNonNull(name));
    return id == null ? null : getItem(id);
  }

  public static Item getItem(Identifier id) {
    return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
  }

  public static Fluid getFluid(Identifier id) {
    return BuiltInRegistries.FLUID.getOptional(id).orElse(null);
  }

  public static Identifier getName(Block block) {
    return BuiltInRegistries.BLOCK.getKey(block);
  }

  public static Identifier getName(Item item) {
    return BuiltInRegistries.ITEM.getKey(item);
  }

  public static Identifier getName(Fluid fluid) {
    return BuiltInRegistries.FLUID.getKey(fluid);
  }

  public static Identifier getDimId(Level world) {
    return world.dimension().identifier();
  }

  private static final Set<Block> SHEARABLE =
      Set.of(
          Blocks.COBWEB,
          Blocks.SHORT_GRASS,
          Blocks.FERN,
          Blocks.DEAD_BUSH,
          Blocks.HANGING_ROOTS,
          Blocks.VINE,
          Blocks.TRIPWIRE);

  public static boolean canShear(BlockState state) {
    return state.is(BlockTags.LEAVES)
        || state.is(BlockTags.WOOL)
        || SHEARABLE.contains(state.getBlock());
  }

  public static Vector3 getEyePosition(Entity entity) {
    return new Vector3(entity.getEyePosition());
  }

  public static Vector3 getLook(Entity entity) {
    return new Vector3(entity.getLookAngle());
  }

  public static String toString(BlockEntity tile) {
    return tile == null ? "(null)" : toString(tile, tile.getLevel(), tile.getBlockPos());
  }

  public static String toString(Object value, BlockGetter world, BlockPos position) {
    return toString(value, world, position.getX(), position.getY(), position.getZ());
  }

  public static String toString(Object value, BlockGetter world, int x, int y, int z) {
    return (value == null
            ? "null"
            : value.getClass().getName()
                + "@"
                + Integer.toHexString(System.identityHashCode(value)))
        + " ("
        + formatPosition(world, x, y, z)
        + ")";
  }

  public static String toCamel(String text) {
    var result = new StringBuilder(text.length());
    boolean upper = false;
    for (int index = 0; index < text.length(); index++) {
      char value = text.charAt(index);
      if (value == '_') {
        upper = !result.isEmpty();
        continue;
      }
      result.append(upper ? Character.toUpperCase(value) : value);
      upper = false;
    }
    return result.toString();
  }

  public static String formatPosition(BlockEntity tile) {
    return tile == null ? "(null)" : formatPosition(tile.getLevel(), tile.getBlockPos());
  }

  public static String formatPosition(BlockGetter world, BlockPos position) {
    return formatPosition(world, position.getX(), position.getY(), position.getZ());
  }

  public static String formatPosition(BlockGetter world, int x, int y, int z) {
    String position =
        formatPosition(world instanceof Level level ? getDimId(level) : null, x, y, z);
    return WORLD_HASH
        ? position + " (@" + Integer.toHexString(System.identityHashCode(world)) + ")"
        : position;
  }

  public static String formatPosition(Identifier dimension, int x, int y, int z) {
    return "dim " + dimension + ": " + formatPosition(x, y, z);
  }

  public static String formatPosition(BlockPos position) {
    return formatPosition(position.getX(), position.getY(), position.getZ());
  }

  public static String formatPosition(int x, int y, int z) {
    return x + "/" + y + "/" + z;
  }

  public static String toSiString(double value, int digits) {
    if (digits < 1 || digits > 18)
      throw new IllegalArgumentException("Significant digits outside [1,18]");
    if (value == 0) return "0 ";
    if (Double.isNaN(value)) return "NaN ";
    if (Double.isInfinite(value)) return value < 0 ? "-∞ " : "∞ ";
    var exact = BigDecimal.valueOf(value);
    int group = Math.floorDiv(exact.precision() - exact.scale() - 1, 3);
    var scaled =
        exact.scaleByPowerOfTen(-3 * group).round(new MathContext(digits, RoundingMode.HALF_UP));
    if (scaled.abs().compareTo(BigDecimal.valueOf(1000)) >= 0) {
      group++;
      scaled = scaled.scaleByPowerOfTen(-3);
    }
    String prefix = group >= -8 && group <= 8 ? PREFIXES[group + 8] : "E" + (3 * group);
    return scaled.stripTrailingZeros().toPlainString() + " " + prefix;
  }

  public static void exit(int status) {
    System.exit(status);
  }

  public static boolean isFakePlayer(Player actor, boolean fuzzy) {
    return actor != null && ProjectRuntime.envProxy.isFakePlayer(actor);
  }

  public static boolean isAreaLoaded(LevelReader world, BlockPos center, int radius) {
    if (radius < 0) return false;
    long left = (long) center.getX() - radius,
        right = (long) center.getX() + radius,
        top = (long) center.getZ() - radius,
        bottom = (long) center.getZ() + radius;
    return left >= Integer.MIN_VALUE
        && right <= Integer.MAX_VALUE
        && top >= Integer.MIN_VALUE
        && bottom <= Integer.MAX_VALUE
        && world.hasChunksAt((int) left, (int) top, (int) right, (int) bottom);
  }

  public static boolean harvestBlock(Level world, BlockPos position) {
    if (!(world instanceof ServerLevel level) || !level.isLoaded(position)) return false;
    var actor = AutomationActors.get(level);
    if (!(actor instanceof ServerPlayer player)) return false;
    var held = player.getMainHandItem();
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
    try {
      return player.gameMode.destroyBlock(position);
    } finally {
      player.setItemInHand(InteractionHand.MAIN_HAND, held);
    }
  }

  public static boolean matchesOD(ItemStack stack, Object material) {
    if (material instanceof ItemStack expected)
      return !StackUtil.isEmpty(stack) && ItemStack.isSameItem(stack, expected);
    if (material instanceof TagKey<?> tag && tag.isFor(BuiltInRegistries.ITEM.key()))
      return !StackUtil.isEmpty(stack)
          && tag.cast(BuiltInRegistries.ITEM.key()).map(stack::is).orElse(false);
    return stack == material;
  }
}
