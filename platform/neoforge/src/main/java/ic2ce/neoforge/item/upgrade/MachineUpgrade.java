package ic2ce.neoforge.item.upgrade;

import ic2ce.api.item.IItemHudInfo;
import ic2ce.api.upgrade.*;
import ic2ce.neoforge.handheld.IHandHeldSubInventory;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.inventory.StackUtil;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.document.HandheldDocumentMenu;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.text.DecimalFormat;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Distinct rate, signal, range, item-transfer and fluid-transfer upgrade policies. */
public class MachineUpgrade extends Item
    implements IFullUpgrade, IHandHeldSubInventory, IItemHudInfo {
  private static final List<MachineUpgrade> PENDING = new ArrayList<>();
  private static final ThreadLocal<DecimalFormat> NUMBERS =
      ThreadLocal.withInitial(() -> new DecimalFormat("0.##"));
  private static final Direction[] DIRECTIONS = Direction.values();
  public final UpgradeType type;

  public MachineUpgrade(Properties properties, UpgradeType type) {
    super(properties);
    this.type = Objects.requireNonNull(type);
    synchronized (PENDING) {
      PENDING.add(this);
    }
  }

  public static void registerPending() {
    synchronized (PENDING) {
      for (var item : PENDING) UpgradeRegistry.register(new ItemStack(item));
      PENDING.clear();
    }
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(LocalizedText.translate("ic2.tooltip.machine_upgrade"));
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      Item.TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> output,
      TooltipFlag flags) {
    int count = stack.getCount();
    switch (type) {
      case overclocker -> {
        tip(
            output,
            "ic2.tooltip.upgrade.overclocker.time",
            NUMBERS.get().format(100 * Math.pow(.7, count)));
        tip(
            output,
            "ic2.tooltip.upgrade.overclocker.power",
            NUMBERS.get().format(100 * Math.pow(1.6, count)));
      }
      case transformer -> tip(output, "ic2.tooltip.upgrade.transformer", count);
      case energy_storage -> tip(output, "ic2.tooltip.upgrade.storage", (long) count * 10_000);
      case ejector, advanced_ejector, fluid_ejector ->
          tip(output, "ic2.tooltip.upgrade.ejector", LocalizedText.translate(side(stack)));
      case pulling, advanced_pulling, fluid_pulling ->
          tip(output, "ic2.tooltip.upgrade.pulling", LocalizedText.translate(side(stack)));
      case redstone_inverter -> tip(output, "ic2.tooltip.upgrade.redstone");
      case remote_interface -> tip(output, "ic2.tooltip.upgrade.remote_interface", count);
    }
  }

  private static void tip(
      java.util.function.Consumer<Component> output, String key, Object... values) {
    output.accept(Component.translatable(key, values).withStyle(ChatFormatting.GRAY));
  }

  private static String side(ItemStack stack) {
    var side = getDirection(stack);
    return side == null
        ? "ic2.tooltip.upgrade.ejector.anyside"
        : switch (side) {
          case DOWN -> "ic2.dir.bottom";
          case UP -> "ic2.dir.top";
          case NORTH -> "ic2.dir.north";
          case SOUTH -> "ic2.dir.south";
          case WEST -> "ic2.dir.west";
          case EAST -> "ic2.dir.east";
        };
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var owner = context.getPlayer();
    var stack = context.getItemInHand();
    if (!type.directional
        || owner == null
        || !stack.is(this)
        || !owner.mayBuild()
        || !owner.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack))
      return InteractionResult.PASS;
    if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
    int direction = context.getClickedFace().ordinal() + 1;
    StackTag.update(
        stack,
        data -> {
          if (data.getIntOr("dir", 0) == direction) data.remove("dir");
          else data.putByte("dir", (byte) direction);
          return data;
        });
    ProjectRuntime.sideProxy.messagePlayer(
        owner,
        LocalizedText.translate(
            type == UpgradeType.ejector
                    || type == UpgradeType.advanced_ejector
                    || type == UpgradeType.fluid_ejector
                ? "ic2.tooltip.upgrade.ejector"
                : "ic2.tooltip.upgrade.pulling",
            LocalizedText.translate(side(stack))));
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult use(Level level, Player owner, InteractionHand hand) {
    var stack = owner.getItemInHand(hand);
    if (!advanced() || !stack.is(this)) return InteractionResult.PASS;
    if (!level.isClientSide()) getInventory(owner, hand, stack).openManagedItem(owner, hand, null);
    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
  }

  public boolean onDroppedByPlayer(ItemStack stack, Player owner) {
    if (advanced()
        && !owner.level().isClientSide()
        && !stack.isEmpty()
        && owner.containerMenu instanceof HandheldDocumentMenu menu
        && menu.base instanceof AdvancedFilterInventory inventory
        && inventory.isThisContainer(stack)) {
      inventory.saveAsThrown(stack);
      if (owner instanceof ServerPlayer server) server.closeContainer();
    }
    return true;
  }

  private boolean advanced() {
    return type == UpgradeType.advanced_ejector || type == UpgradeType.advanced_pulling;
  }

  @Override
  public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> properties) {
    if (properties == null) return false;
    return switch (type) {
      case overclocker ->
          properties.contains(UpgradableProperty.Processing)
              || properties.contains(UpgradableProperty.Augmentable);
      case transformer -> properties.contains(UpgradableProperty.Transformer);
      case energy_storage -> properties.contains(UpgradableProperty.EnergyStorage);
      case ejector, advanced_ejector -> properties.contains(UpgradableProperty.ItemProducing);
      case pulling, advanced_pulling -> properties.contains(UpgradableProperty.ItemConsuming);
      case fluid_ejector -> properties.contains(UpgradableProperty.FluidProducing);
      case fluid_pulling -> properties.contains(UpgradableProperty.FluidConsuming);
      case redstone_inverter -> properties.contains(UpgradableProperty.RedstoneSensitive);
      case remote_interface -> properties.contains(UpgradableProperty.RemotelyAccessible);
    };
  }

  @Override
  public int getAugmentation(ItemStack stack, IUpgradableBlock owner) {
    return type == UpgradeType.overclocker ? 1 : 0;
  }

  @Override
  public int getExtraProcessTime(ItemStack stack, IUpgradableBlock owner) {
    return 0;
  }

  @Override
  public double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock owner) {
    return type == UpgradeType.overclocker ? .7 : 1;
  }

  @Override
  public int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock owner) {
    return 0;
  }

  @Override
  public double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock owner) {
    return type == UpgradeType.overclocker ? 1.6 : 1;
  }

  @Override
  public int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock owner) {
    return type == UpgradeType.energy_storage ? 10_000 : 0;
  }

  @Override
  public double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock owner) {
    return 1;
  }

  @Override
  public int getExtraTier(ItemStack stack, IUpgradableBlock owner) {
    return type == UpgradeType.transformer ? 1 : 0;
  }

  @Override
  public boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock owner) {
    return type == UpgradeType.redstone_inverter;
  }

  @Override
  public int getRedstoneInput(ItemStack stack, IUpgradableBlock owner, int external) {
    return type == UpgradeType.redstone_inverter ? 15 - Math.clamp(external, 0, 15) : external;
  }

  @Override
  public int getRangeAmplification(ItemStack stack, IUpgradableBlock owner, int range) {
    return type == UpgradeType.remote_interface
        ? (int)
            Math.min(
                Integer.MAX_VALUE,
                Math.scalb((double) Math.max(0, range), Math.clamp(stack.getCount(), 0, 31)))
        : range;
  }

  @Override
  public boolean onTick(ItemStack stack, IUpgradableBlock owner) {
    if (!(owner instanceof BlockEntity tile)
        || tile.getLevel() == null
        || tile.getLevel().isClientSide()
        || stack.isEmpty()
        || !isSuitableFor(stack, owner.getUpgradableProperties())) return false;
    int rate = 1 << (2 * Math.clamp(stack.getCount() - 1, 0, 4));
    int changed = 0;
    var direction = getDirection(stack);
    switch (type) {
      case ejector, advanced_ejector, pulling, advanced_pulling -> {
        boolean pull = type == UpgradeType.pulling || type == UpgradeType.advanced_pulling;
        var targets =
            direction == null
                ? StackUtil.ENV.getAdjacentInventories(tile)
                : singleInventory(tile, direction);
        var filter = advanced() ? CompiledUpgradeFilter.of(stack) : null;
        for (var adjacent : targets) {
          var inside = StackUtil.ENV.wrapInventory(tile, adjacent.getSide());
          if (inside == null) continue;
          changed +=
              filter == null
                  ? StackUtil.ENV.transfer(pull ? adjacent : inside, pull ? inside : adjacent, rate)
                  : StackUtil.ENV.transfer(
                      pull ? adjacent : inside, pull ? inside : adjacent, rate, filter);
        }
      }
      case fluid_ejector, fluid_pulling -> {
        if (!LiquidUtil.isFluidTile(tile, null)) return false;
        var targets =
            direction == null ? LiquidUtil.getAdjacentHandlers(tile) : singleFluid(tile, direction);
        for (var adjacent : targets) {
          var moved =
              type == UpgradeType.fluid_pulling
                  ? LiquidUtil.transfer(
                      adjacent.handler, adjacent.dir.getOpposite(), tile, rate * 50)
                  : LiquidUtil.transfer(tile, adjacent.dir, adjacent.handler, rate * 50);
          if (moved != null) changed += moved.getAmountMb();
        }
      }
      default -> {
        return false;
      }
    }
    return changed > 0;
  }

  private static List<ic2ce.neoforge.inventory.transfer.EnvItemHandler.AdjacentInventory> singleInventory(
      BlockEntity tile, Direction direction) {
    var target = StackUtil.ENV.getAdjacentInventory(tile, direction);
    return target == null ? List.of() : List.of(target);
  }

  private static List<LiquidUtil.AdjacentFluidHandler> singleFluid(
      BlockEntity tile, Direction direction) {
    var target = LiquidUtil.getAdjacentHandler(tile, direction);
    return target == null ? List.of() : List.of(target);
  }

  @Override
  public Collection<ItemStack> onProcessEnd(
      ItemStack stack, IUpgradableBlock owner, Collection<ItemStack> output) {
    return output;
  }

  @Override
  public IHasGui getInventory(Player owner, InteractionHand hand, ItemStack stack) {
    return advanced() ? new AdvancedFilterInventory(owner, hand, stack) : null;
  }

  @Override
  public IHasGui getSubInventory(Player owner, InteractionHand hand, ItemStack stack, int id) {
    return advanced() ? AdvancedFilterInventory.delegate(owner, hand, stack, id) : null;
  }

  private record DirectionSelection(
      net.minecraft.world.item.component.CustomData data, Direction selected) {}

  private static final ThreadLocal<DirectionSelection> LAST_DIRECTION = new ThreadLocal<>();

  public static Direction getDirection(ItemStack stack) {
    var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
    if (data == null || !data.contains("dir")) return null;
    var cached = LAST_DIRECTION.get();
    if (cached != null && cached.data() == data) return cached.selected();
    int index = data.copyTag().getIntOr("dir", 0);
    var selected = index >= 1 && index <= DIRECTIONS.length ? DIRECTIONS[index - 1] : null;
    LAST_DIRECTION.set(new DirectionSelection(data, selected));
    return selected;
  }

  public enum UpgradeType {
    overclocker(false),
    transformer(false),
    energy_storage(false),
    redstone_inverter(false),
    ejector(true),
    advanced_ejector(true),
    pulling(true),
    advanced_pulling(true),
    fluid_ejector(true),
    fluid_pulling(true),
    remote_interface(false);
    public final boolean directional;

    UpgradeType(boolean directional) {
      this.directional = directional;
    }
  }
}
