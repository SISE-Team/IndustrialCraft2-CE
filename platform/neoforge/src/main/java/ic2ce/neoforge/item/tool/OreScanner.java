package ic2ce.neoforge.item.tool;

import ic2ce.api.item.*;
import ic2ce.core.IHasGui;
import ic2ce.core.item.IHandHeldInventory;
import ic2ce.core.util.*;
import ic2ce.engine.value.ValueTuples;
import ic2ce.neoforge.handheld.ScannerSession;
import ic2ce.neoforge.handheld.menu.ScannerMenu;
import ic2ce.neoforge.item.energy.ElectricItemBase;
import ic2ce.neoforge.material.CommonMaterialTags;
import ic2ce.neoforge.registry.SoundDefinitions;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Native ore probe counts only already loaded bounded columns with primitive item aggregation. */
public class OreScanner extends ElectricItemBase implements IBoxable, IHandHeldInventory {
  private static final TagKey<Block> ORES =
      TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));
  private static final TagKey<Item> ORE_ITEMS =
      TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores"));

  public OreScanner(Properties properties, double maximum, double rate, int tier) {
    super(properties, maximum, rate, tier);
  }

  protected int scanCost() {
    return tier == 1 ? 50 : 250;
  }

  public int getScanRange() {
    return 6;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    super.appendHoverText(stack, context, display, output, flags);
    output.accept(
        Component.translatable("ic2.scanner.range", Integer.toString(getScanRange()))
            .withStyle(ChatFormatting.GRAY));
  }

  @Override
  public InteractionResult use(Level world, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!stack.is(this)) return InteractionResult.PASS;
    if (world.isClientSide()) {
      if (!ElectricItem.manager.canUse(stack, scanCost())) return InteractionResult.FAIL;
      owner.playSound(SoundDefinitions.ITEM_SCANNER_USE, 1, 1);
      return InteractionResult.SUCCESS;
    }
    if (!ElectricItem.manager.use(stack, scanCost(), owner)) return InteractionResult.FAIL;
    if (!getInventory(owner, hand, stack).openManagedItem(owner, hand, null)
        || !(owner.containerMenu instanceof ScannerMenu menu)) {
      if (!owner.getAbilities().instabuild)
        ElectricItem.manager.charge(stack, scanCost(), Integer.MAX_VALUE, true, false);
      return InteractionResult.FAIL;
    }
    menu.setResults(scan(world, owner.blockPosition(), getScanRange()));
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }

  public boolean onDroppedByPlayer(ItemStack stack, Player owner) {
    if (!owner.level().isClientSide()
        && owner.containerMenu instanceof ScannerMenu menu
        && menu.base.isThisContainer(stack)) {
      menu.base.saveAsThrown(stack);
      if (owner instanceof ServerPlayer server) server.closeContainer();
    }
    return true;
  }

  public int startLayerScan(ItemStack stack) {
    return stack.is(this) && ElectricItem.manager.use(stack, scanCost(), null)
        ? getScanRange() / 2
        : 0;
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  @Override
  public IHasGui getInventory(Player owner, InteractionHand hand, ItemStack stack) {
    return new ScannerSession(owner, hand, stack);
  }

  public List<ValueTuples.T2<ItemStack, Integer>> scan(Level world, BlockPos center, int radius) {
    int range = Math.clamp(radius, 0, getScanRange());
    var counts = new IdentityHashMap<Item, int[]>();
    var prototypes = new IdentityHashMap<Item, ItemStack>();
    var position = new BlockPos.MutableBlockPos();
    int minY = Math.max(world.getMinY(), center.getY() - range),
        maxY = Math.min(world.getMaxY() - 1, center.getY() + range);
    for (int chunkX = (center.getX() - range) >> 4;
        chunkX <= (center.getX() + range) >> 4;
        chunkX++)
      for (int chunkZ = (center.getZ() - range) >> 4;
          chunkZ <= (center.getZ() + range) >> 4;
          chunkZ++) {
        var chunk = world.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) continue;
        int minX = Math.max(center.getX() - range, chunkX << 4),
            maxX = Math.min(center.getX() + range, (chunkX << 4) + 15),
            minZ = Math.max(center.getZ() - range, chunkZ << 4),
            maxZ = Math.min(center.getZ() + range, (chunkZ << 4) + 15);
        for (int y = minY; y <= maxY; y++)
          for (int z = minZ; z <= maxZ; z++)
            for (int x = minX; x <= maxX; x++) {
              position.set(x, y, z);
              var state = chunk.getBlockState(position);
              if (state.isAir()) continue;
              var item = state.getBlock().asItem();
              if (!CommonMaterialTags.matches(state, ORES) && !oreItem(item)) continue;
              var picked = state.getBlock().getCloneItemStack(world, position, state, true, null);
              if (picked.isEmpty()) continue;
              counts.computeIfAbsent(picked.getItem(), ignored -> new int[1])[0] +=
                  picked.getCount();
              prototypes.putIfAbsent(picked.getItem(), picked.copyWithCount(1));
            }
      }
    var result = new ArrayList<ValueTuples.T2<ItemStack, Integer>>(counts.size());
    for (var entry : counts.entrySet())
      result.add(new ValueTuples.T2<>(prototypes.get(entry.getKey()), entry.getValue()[0]));
    result.sort(
        Comparator.<ValueTuples.T2<ItemStack, Integer>>comparingInt(row -> row.b)
            .reversed()
            .thenComparing(
                row ->
                    net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(row.a.getItem())
                        .toString()));
    return List.copyOf(result);
  }

  private static boolean oreItem(Item item) {
    var holder = net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(item);
    for (var tag : CommonMaterialTags.itemAliases(ORE_ITEMS)) if (holder.is(tag)) return true;
    return false;
  }
}
