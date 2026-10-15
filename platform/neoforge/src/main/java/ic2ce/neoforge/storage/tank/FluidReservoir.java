package ic2ce.neoforge.storage.tank;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.util.FluidContainerOutputMode;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.FluidPorts;
import ic2ce.neoforge.fluid.LiquidUtil;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Portable fluid storage commits handheld container transfers on the server and retains exact
 * components.
 */
public abstract class FluidReservoir extends InventoryBlockEntity implements IHasGui {
  protected final FluidPorts fluidsComponent;
  @GuiSynced protected final FluidPorts.InternalFluidTank contents;

  public FluidReservoir(
      BlockEntityType<? extends FluidReservoir> type,
      BlockPos position,
      BlockState state,
      int buckets) {
    super(type, position, state);
    if (buckets <= 0 || buckets > Integer.MAX_VALUE / 1000)
      throw new IllegalArgumentException("Invalid portable tank capacity");
    fluidsComponent = addComponent(new FluidPorts(this));
    contents = fluidsComponent.addTank("contents", buckets * 1000);
  }

  @Override
  protected List<ItemStack> getAuxDrops(int fortune) {
    return List.of();
  }

  @Override
  public void onPlaced(ItemStack blockItem, LivingEntity placer, Direction facing) {
    super.onPlaced(blockItem, placer, facing);
    if (getLevel() == null || getLevel().isClientSide()) return;
    var data = StackTag.get(blockItem);
    var stored = data == null || data.contains("Empty") ? null : Ic2FluidStack.read(data);
    contents.setFluidStack(
        stored == null || stored.isEmpty()
            ? null
            : stored.copyWithAmountMb(Math.min(stored.getAmountMb(), contents.getCapacity())));
    setChanged();
  }

  @Override
  public ItemStack adjustDrop(ItemStack original, boolean wrench) {
    if (original == null || original.isEmpty()) return ItemStack.EMPTY;
    var owned = original.copy();
    var data = StackTag.getOrCreate(owned);
    contents.toNbt(data);
    StackTag.set(owned, data);
    return owned;
  }

  @Override
  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction face, Vec3 hit) {
    if (!LiquidUtil.isFluidContainer(player.getItemInHand(hand)))
      return super.onActivated(player, hand, face, hit);
    if (getLevel().isClientSide()) return InteractionResult.SUCCESS;
    var stored = contents.getFluidStack();
    if (stored != null && !stored.isEmpty()) {
      int accepted =
          LiquidUtil.fillContainer(
              player, hand, stored.copy(), FluidContainerOutputMode.InPlacePreferred, false);
      if (accepted > 0) {
        contents.drainMbUnchecked(accepted, false);
        setChanged();
        return InteractionResult.SUCCESS;
      }
    }
    int free = Math.max(0, contents.getCapacity() - contents.getFluidAmount());
    if (free <= 0) return InteractionResult.PASS;
    var requested = stored == null || stored.isEmpty() ? null : stored.getFluid();
    var prediction =
        LiquidUtil.drainContainer(
            player, hand, requested, free, FluidContainerOutputMode.InPlacePreferred, true);
    if (prediction == null
        || prediction.isEmpty()
        || contents.fillMbUnchecked(prediction, true) != prediction.getAmountMb())
      return InteractionResult.PASS;
    var committed =
        LiquidUtil.drainContainer(
            player, hand, requested, free, FluidContainerOutputMode.InPlacePreferred, false);
    if (committed == null || contents.fillMbUnchecked(committed, false) != committed.getAmountMb())
      throw new IllegalStateException("Portable tank container reservation changed during commit");
    setChanged();
    return InteractionResult.SUCCESS;
  }

  @Override
  public void addInformation(ItemStack blockItem, List<String> tooltip, TooltipFlag flags) {
    tooltip.add(LocalizedText.translate("ic2.fluid.capacity", contents.getCapacity()));
    var data = StackTag.get(blockItem);
    var stored = data == null || data.contains("Empty") ? null : Ic2FluidStack.read(data);
    if (stored == null || stored.isEmpty()) {
      tooltip.add(LocalizedText.translate("ic2.generic.text.empty"));
      return;
    }
    tooltip.add(LocalizedText.translate(stored.getFluid().getFluidType().getDescriptionId()));
    tooltip.add(LocalizedText.translate("ic2.fluid.amount", stored.getAmountMb()));
    tooltip.add(
        LocalizedText.translate(
            "ic2.fluid.type",
            LocalizedText.translate(
                FluidHandler.isGaseous(stored.getFluid()) ? "ic2.fluid.gas" : "ic2.fluid.liquid")));
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return DocumentMenu.create(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory playerInventory, GrowingBuffer data) {
    return DocumentMenu.create(syncId, playerInventory, this);
  }
}
