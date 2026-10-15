package ic2ce.neoforge.inventory;

import com.mojang.authlib.GameProfile;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.inventory.transfer.EnvItemHandler;
import ic2ce.engine.value.ValueTuples;
import ic2ce.neoforge.registry.ItemGroups;
import ic2ce.neoforge.runtime.AutomationActors;
import ic2ce.neoforge.runtime.ProjectRuntime;
import it.unimi.dsi.fastutil.ints.*;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Owned item values, two-phase player bills and planned balancing preserve native component
 * identity and quantities.
 */
public final class StackUtil {
  private StackUtil() {}

  private static final List<TagKey<Item>> ORES =
      List.of(
          ItemGroups.ORES,
          ItemTags.COAL_ORES,
          ItemTags.COPPER_ORES,
          ItemTags.DIAMOND_ORES,
          ItemTags.GOLD_ORES,
          ItemTags.IRON_ORES,
          ItemTags.EMERALD_ORES,
          ItemTags.LAPIS_ORES,
          ItemTags.REDSTONE_ORES);
  static final Set<String> ignoredNbtKeys = Set.of("damage", "charge", "energy", "advDmg");
  public static final Predicate<ItemStack> anyStack = stack -> true;
  public static final ItemStack emptyStack = ItemStack.EMPTY;
  public static final EnvItemHandler ENV = ProjectRuntime.envProxy.createItemHandler();

  public static boolean isEmpty(ItemStack stack) {
    return stack == null || stack.isEmpty();
  }

  public static boolean isEmpty(Player player, InteractionHand hand) {
    return isEmpty(get(player, hand));
  }

  public static int getSize(ItemStack stack) {
    return isEmpty(stack) ? 0 : stack.getCount();
  }

  public static ItemStack wrapEmpty(ItemStack stack) {
    return isEmpty(stack) ? ItemStack.EMPTY : stack;
  }

  public static boolean isOreStack(ItemStack stack) {
    if (isEmpty(stack)) return false;
    for (var tag : ORES) if (stack.is(tag)) return true;
    return false;
  }

  public static ItemStack setSize(ItemStack stack, int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative item quantity");
    if (isEmpty(stack)) return ItemStack.EMPTY;
    stack.setCount(amount);
    return wrapEmpty(stack);
  }

  public static ItemStack incSize(ItemStack stack) {
    return incSize(stack, 1);
  }

  public static ItemStack incSize(ItemStack stack, int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative increase");
    return setSize(stack, Math.addExact(getSize(stack), amount));
  }

  public static ItemStack decSize(ItemStack stack) {
    return decSize(stack, 1);
  }

  public static ItemStack decSize(ItemStack stack, int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative decrease");
    return setSize(stack, Math.max(0, getSize(stack) - amount));
  }

  public static ItemStack copy(ItemStack stack) {
    return isEmpty(stack) ? ItemStack.EMPTY : stack.copy();
  }

  public static ItemStack copyWithSize(ItemStack stack, int amount) {
    if (amount < 0) throw new IllegalArgumentException("Negative copied quantity");
    return isEmpty(stack) || amount == 0 ? ItemStack.EMPTY : stack.copyWithCount(amount);
  }

  public static ItemStack copyShrunk(ItemStack stack, int amount) {
    return copyWithSize(stack, Math.max(0, getSize(stack) - amount));
  }

  public static Collection<ItemStack> copy(Collection<ItemStack> stacks) {
    var result = new ArrayList<ItemStack>(stacks.size());
    for (var stack : stacks) result.add(copy(stack));
    return result;
  }

  public static boolean check(ItemStack stack) {
    return stack != null && stack.getItem() != null;
  }

  public static boolean check(ItemStack[] stacks) {
    return stacks != null && check(Arrays.asList(stacks));
  }

  public static boolean check(Iterable<ItemStack> stacks) {
    if (stacks == null) return false;
    for (var stack : stacks) if (!check(stack)) return false;
    return true;
  }

  public static boolean check2(Iterable<List<ItemStack>> stacks) {
    if (stacks == null) return false;
    for (var group : stacks) if (!check(group)) return false;
    return true;
  }

  public static String toStringSafe(ItemStack stack) {
    return stack == null ? "(null)" : stack.toString();
  }

  public static String toStringSafe(ItemStack[] stacks) {
    return stacks == null ? "(null)" : toStringSafe(Arrays.asList(stacks));
  }

  public static String toStringSafe(Iterable<ItemStack> stacks) {
    var result = new StringJoiner(", ", "[", "]");
    for (var stack : stacks) result.add(toStringSafe(stack));
    return result.toString();
  }

  public static String toStringSafe2(Iterable<List<ItemStack>> stacks) {
    var result = new StringJoiner(", ", "[", "]");
    for (var group : stacks) result.add(toStringSafe(group));
    return result.toString();
  }

  public static CompoundTag getOrCreateNbtData(ItemStack stack) {
    return StackTag.getOrCreate(stack);
  }

  public static boolean checkNbtEquality(CompoundTag first, CompoundTag second) {
    if (first == second) return true;
    if (first != null)
      for (var key : first.keySet())
        if (!ignoredNbtKeys.contains(key)
            && (second == null || !Objects.equals(first.get(key), second.get(key)))) return false;
    if (second != null)
      for (var key : second.keySet())
        if (!ignoredNbtKeys.contains(key)
            && (first == null || !Objects.equals(second.get(key), first.get(key)))) return false;
    return true;
  }

  public static boolean checkNbtEqualityStrict(ItemStack first, ItemStack second) {
    return Objects.equals(StackTag.get(first), StackTag.get(second));
  }

  public static boolean checkItemEqualityStrict(ItemStack first, ItemStack second) {
    return isEmpty(first) && isEmpty(second)
        || !isEmpty(first) && !isEmpty(second) && ItemStack.isSameItemSameComponents(first, second);
  }

  public static boolean checkItemEquality(ItemStack first, Item item) {
    return isEmpty(first) ? item == null : first.is(item);
  }

  public static boolean checkItemEquality(ItemStack first, ItemStack second) {
    if (isEmpty(first) || isEmpty(second)) return isEmpty(first) && isEmpty(second);
    if (!ItemStack.isSameItem(first, second)
        || !checkNbtEquality(StackTag.get(first), StackTag.get(second))) return false;
    for (var type : first.getComponents().keySet())
      if (type != DataComponents.CUSTOM_DATA
          && type != DataComponents.DAMAGE
          && !Objects.equals(first.get(type), second.get(type))) return false;
    for (var type : second.getComponents().keySet())
      if (type != DataComponents.CUSTOM_DATA && type != DataComponents.DAMAGE && !first.has(type))
        return false;
    return true;
  }

  public static Predicate<ItemStack> sameStack(ItemStack prototype) {
    if (isEmpty(prototype)) throw new IllegalArgumentException("Empty comparison prototype");
    var owned = prototype.copyWithCount(1);
    return offered -> checkItemEquality(owned, offered);
  }

  public static Predicate<ItemStack> sameItem(Item item) {
    Objects.requireNonNull(item);
    return offered -> !isEmpty(offered) && offered.is(item);
  }

  public static Predicate<ItemStack> sameItem(ItemLike item) {
    return sameItem(item.asItem());
  }

  public static Predicate<ItemStack> recipeInput(IRecipeInput input) {
    return Objects.requireNonNull(input)::matches;
  }

  private static void quantity(int amount) {
    if (amount <= 0) throw new IllegalArgumentException("Positive item bill required");
  }

  public static ItemStack get(Player player, InteractionHand hand) {
    return player.getItemInHand(hand);
  }

  public static void set(Player player, InteractionHand hand, ItemStack stack) {
    player.setItemInHand(hand, wrapEmpty(stack));
  }

  public static void clear(Player player, InteractionHand hand) {
    set(player, hand, ItemStack.EMPTY);
  }

  public static void clearEmpty(Player player, InteractionHand hand) {
    if (isEmpty(get(player, hand))) clear(player, hand);
  }

  private static ItemStack consumeHand(
      Player player, InteractionHand hand, Predicate<ItemStack> accepted, int amount) {
    quantity(amount);
    var held = get(player, hand);
    if (isEmpty(held)
        || !accepted.test(held)
        || !player.getAbilities().instabuild && held.getCount() < amount) return ItemStack.EMPTY;
    var result = held.copyWithCount(amount);
    if (!player.getAbilities().instabuild) {
      held.shrink(amount);
      set(player, hand, held);
    }
    return result;
  }

  public static boolean consume(
      Player player, InteractionHand hand, Predicate<ItemStack> request, int amount) {
    return !consumeHand(player, hand, request, amount).isEmpty();
  }

  public static ItemStack consumeAndGet(Player player, Predicate<ItemStack> request, int amount) {
    return consumeAndGet(player, InteractionHand.MAIN_HAND, request, amount);
  }

  public static ItemStack consumeAndGet(
      Player player, InteractionHand hand, Predicate<ItemStack> request, int amount) {
    return consumeHand(player, hand, request, amount);
  }

  public static void consumeOrError(Player player, InteractionHand hand, int amount) {
    consumeOrError(player, hand, anyStack, amount);
  }

  public static void consumeOrError(
      Player player, InteractionHand hand, Predicate<ItemStack> request, int amount) {
    if (!consume(player, hand, request, amount))
      throw new IllegalStateException("Unpaid item bill");
  }

  private record Debit(int slot, int amount, ItemStack before) {}

  private static List<Debit> inventoryBill(
      Player player,
      Predicate<ItemStack> accepted,
      int amount,
      boolean excludeSelected,
      boolean uniform) {
    quantity(amount);
    var inventory = player.getInventory();
    var plan = new ArrayList<Debit>();
    ItemStack prototype = ItemStack.EMPTY;
    int remaining = amount;
    for (int slot = 0; slot < inventory.getNonEquipmentItems().size(); slot++) {
      if (excludeSelected && slot == inventory.getSelectedSlot()) continue;
      var stack = inventory.getItem(slot);
      if (isEmpty(stack)
          || !accepted.test(stack)
          || uniform && !prototype.isEmpty() && !checkItemEqualityStrict(prototype, stack))
        continue;
      if (prototype.isEmpty()) prototype = stack;
      int paid =
          player.getAbilities().instabuild ? remaining : Math.min(remaining, stack.getCount());
      plan.add(new Debit(slot, paid, stack.copy()));
      remaining -= paid;
      if (remaining == 0) return plan;
    }
    return List.of();
  }

  private static boolean commitBill(Player player, List<Debit> bill) {
    if (bill.isEmpty()) return false;
    var inventory = player.getInventory();
    for (var debit : bill) {
      var now = inventory.getItem(debit.slot());
      if (now.getCount() != debit.before().getCount()
          || !checkItemEqualityStrict(now, debit.before())) return false;
    }
    if (!player.getAbilities().instabuild)
      for (var debit : bill)
        inventory.setItem(
            debit.slot(), copyWithSize(debit.before(), debit.before().getCount() - debit.amount()));
    inventory.setChanged();
    return true;
  }

  public static ItemStack consumeFromPlayerInventoryAndGet(
      Player player, Predicate<ItemStack> request, int amount) {
    return consumeFromPlayerInventoryAndGet(player, request, amount, false);
  }

  public static ItemStack consumeFromPlayerInventoryAndGet(
      Player player, Predicate<ItemStack> request, int amount, boolean excludeSelected) {
    var plan = inventoryBill(player, request, amount, excludeSelected, true);
    if (plan.isEmpty() || !commitBill(player, plan)) return ItemStack.EMPTY;
    return plan.getFirst().before().copyWithCount(amount);
  }

  public static boolean consumeFromPlayerInventory(
      Player player, Predicate<ItemStack> request, int amount, boolean simulate) {
    var plan = inventoryBill(player, request, amount, false, false);
    return !plan.isEmpty() && (simulate || commitBill(player, plan));
  }

  public static boolean addToPlayerInventory(Player player, ItemStack stack) {
    return player.getInventory().add(stack);
  }

  public static boolean damage(
      Player player, InteractionHand hand, Predicate<ItemStack> accepted, int amount) {
    quantity(amount);
    var held = get(player, hand);
    if (isEmpty(held) || !held.isDamageableItem() || !accepted.test(held)) return false;
    if (!player.getAbilities().instabuild) {
      held.hurtAndBreak(amount, player, hand);
      set(player, hand, held);
    }
    return true;
  }

  public static void damageOrError(Player player, InteractionHand hand, int amount) {
    damageOrError(player, hand, anyStack, amount);
  }

  public static void damageOrError(
      Player player, InteractionHand hand, Predicate<ItemStack> request, int amount) {
    if (!damage(player, hand, request, amount))
      throw new IllegalStateException("Unpaid durability bill");
  }

  public static boolean storeInventoryItem(ItemStack stack, Player player, boolean simulate) {
    if (isEmpty(stack)) return true;
    if (!simulate) return player.getInventory().add(stack);
    long remaining = stack.getCount();
    var inventory = player.getInventory();
    for (int slot = 0; slot < inventory.getNonEquipmentItems().size(); slot++) {
      var present = inventory.getItem(slot);
      int maximum = Math.min(inventory.getMaxStackSize(stack), stack.getMaxStackSize());
      if (present.isEmpty()) remaining -= maximum;
      else if (checkItemEqualityStrict(stack, present))
        remaining -= Math.max(0, maximum - present.getCount());
      if (remaining <= 0) return true;
    }
    return false;
  }

  public static void dropAsEntity(Level world, BlockPos position, ItemStack stack) {
    if (world.isClientSide() || isEmpty(stack)) return;
    var random = world.getRandom();
    var drop =
        new ItemEntity(
            world,
            position.getX() + .15 + random.nextFloat() * .7,
            position.getY() + .15 + random.nextFloat() * .7,
            position.getZ() + .15 + random.nextFloat() * .7,
            stack.copy());
    drop.setDefaultPickUpDelay();
    world.addFreshEntity(drop);
  }

  public static ItemStack setImmutableSize(ItemStack stack, int amount) {
    return getSize(stack) == amount ? wrapEmpty(stack) : copyWithSize(stack, amount);
  }

  public static int fetch(BlockEntity source, ItemStack request, boolean simulate) {
    return ENV.fetch(source, request, simulate);
  }

  public static int distribute(BlockEntity source, ItemStack request, boolean simulate) {
    return ENV.distribute(source, request, simulate);
  }

  public static void distributeDrops(BlockEntity source, List<ItemStack> stacks) {
    for (var stack : stacks) {
      if (isEmpty(stack)) continue;
      int moved = distribute(source, stack, false);
      dropAsEntity(
          source.getLevel(),
          source.getBlockPos(),
          copyWithSize(stack, Math.max(0, stack.getCount() - moved)));
    }
    stacks.clear();
  }

  public static boolean placeBlock(ItemStack offered, Level world, BlockPos position) {
    if (world.isClientSide() || isEmpty(offered) || !(offered.getItem() instanceof BlockItem))
      return false;
    var actor = AutomationActors.get(world);
    if (actor == null) return false;
    var previous = actor.getMainHandItem();
    var working = offered.copyWithCount(1);
    actor.setItemInHand(InteractionHand.MAIN_HAND, working);
    try {
      return working
          .getItem()
          .useOn(
              new UseOnContext(
                  actor,
                  InteractionHand.MAIN_HAND,
                  new BlockHitResult(
                      Vec3.atLowerCornerOf(position).add(.5, 1, .5),
                      Direction.DOWN,
                      position,
                      false)))
          .consumesAction();
    } finally {
      actor.setItemInHand(InteractionHand.MAIN_HAND, previous);
    }
  }

  public static int putInInventory(
      BlockEntity tile, Direction side, ItemStack offered, boolean simulate) {
    return putInInventory(tile, side, offered, null, simulate);
  }

  public static int putInInventory(
      BlockEntity tile, Direction side, ItemStack offered, GameProfile accessor, boolean simulate) {
    return ENV.deposit(tile, side, offered, accessor, simulate);
  }

  public static ItemStack getPickStack(
      Level world, BlockPos position, BlockState state, Player player) {
    return wrapEmpty(state.getBlock().getCloneItemStack(world, position, state, true, null));
  }

  public static List<ItemStack> getDrops(
      BlockGetter world, BlockPos position, BlockState state, int fortune) {
    return getDrops(world, position, state, null, fortune, false);
  }

  public static List<ItemStack> getDrops(
      BlockGetter world, BlockPos position, BlockState state, Block block, int fortune) {
    return getDrops(world, position, state, null, fortune, false);
  }

  public static List<ItemStack> getDrops(
      BlockGetter world,
      BlockPos position,
      BlockState state,
      Player player,
      int fortune,
      boolean silkTouch) {
    if (!(world instanceof ServerLevel level) || state.isAir()) return List.of();
    var tool = new ItemStack(Items.DIAMOND_PICKAXE);
    if (silkTouch || fortune > 0) {
      var enchantments =
          level
              .registryAccess()
              .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
      tool.enchant(
          enchantments.getOrThrow(silkTouch ? Enchantments.SILK_TOUCH : Enchantments.FORTUNE),
          silkTouch ? 1 : fortune);
    }
    return Block.getDrops(state, level, position, level.getBlockEntity(position), player, tool);
  }

  public static IntSet getSlotsFromInv(Container inventory) {
    var slots = new IntOpenHashSet(inventory.getContainerSize());
    for (int index = 0; index < inventory.getContainerSize(); index++) slots.add(index);
    return slots;
  }

  public static ValueTuples.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(
      Container inventory) {
    return balanceStacks(inventory, List.of());
  }

  public static ValueTuples.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(
      Container inventory, ItemStack additional) {
    return balanceStacks(inventory, List.of(additional));
  }

  public static ValueTuples.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(
      Container inventory, Collection<ItemStack> additional) {
    return balanceStacks(
        inventory,
        pair -> !isEmpty(inventory.getItem(pair.b)),
        getSlotsFromInv(inventory),
        additional);
  }

  public static ValueTuples.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(
      Container inventory, Predicate<ValueTuples.T2<ItemStack, Integer>> accepted) {
    return balanceStacks(inventory, accepted, getSlotsFromInv(inventory), List.of());
  }

  private static final class BalanceGroup {
    final ItemStack prototype;
    long total;

    BalanceGroup(ItemStack prototype) {
      this.prototype = prototype.copyWithCount(1);
    }
  }

  public static ValueTuples.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(
      Container inventory,
      Predicate<ValueTuples.T2<ItemStack, Integer>> accepted,
      IntSet available,
      Collection<ItemStack> additional) {
    var before = new ItemStack[inventory.getContainerSize()];
    for (int slot = 0; slot < before.length; slot++) before[slot] = copy(inventory.getItem(slot));
    int[] slots = available.toIntArray();
    Arrays.sort(slots);
    var groups = new LinkedHashMap<ItemResource, BalanceGroup>();
    for (int slot : slots) {
      Objects.checkIndex(slot, before.length);
      var stack = before[slot];
      if (!stack.isEmpty())
        groups.computeIfAbsent(ItemResource.of(stack), key -> new BalanceGroup(stack)).total +=
            stack.getCount();
    }
    for (var stack : additional)
      if (!isEmpty(stack))
        groups.computeIfAbsent(ItemResource.of(stack), key -> new BalanceGroup(stack)).total +=
            stack.getCount();
    var used = new IntOpenHashSet();
    var planned = new ItemStack[before.length];
    var leftovers = new ArrayList<ItemStack>();
    for (var group : groups.values()) {
      var targets = new IntArrayList();
      for (int slot : slots)
        if (!used.contains(slot)
            && (before[slot].isEmpty() || checkItemEqualityStrict(before[slot], group.prototype))
            && accepted.test(new ValueTuples.T2<>(group.prototype.copy(), slot))) targets.add(slot);
      long remaining = group.total;
      for (int slot : slots)
        if (!targets.contains(slot)
            && !before[slot].isEmpty()
            && checkItemEqualityStrict(before[slot], group.prototype))
          remaining -= before[slot].getCount();
      for (int index = 0; index < targets.size(); index++) {
        int slot = targets.getInt(index),
            maximum =
                Math.min(
                    inventory.getMaxStackSize(group.prototype), group.prototype.getMaxStackSize());
        if (inventory instanceof ic2ce.neoforge.block.InventoryBlockEntity machine)
          maximum = Math.min(maximum, machine.getSlotLimit(slot));
        int count =
            (int)
                Math.min(
                    Math.max(0, maximum),
                    (remaining + targets.size() - index - 1) / (targets.size() - index));
        planned[slot] = copyWithSize(group.prototype, count);
        remaining -= count;
        used.add(slot);
      }
      while (remaining > 0) {
        int count = (int) Math.min(group.prototype.getMaxStackSize(), remaining);
        leftovers.add(group.prototype.copyWithCount(count));
        remaining -= count;
      }
    }
    for (int slot : slots) if (planned[slot] != null) inventory.setItem(slot, planned[slot]);
    inventory.setChanged();
    available.clear();
    available.addAll(used);
    return new ValueTuples.T2<>(leftovers, available);
  }
}
