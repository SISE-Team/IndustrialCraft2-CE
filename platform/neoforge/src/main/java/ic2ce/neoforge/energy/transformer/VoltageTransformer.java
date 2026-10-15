package ic2ce.neoforge.energy.transformer;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.energy.menu.TransformerMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.world.DirectionSets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** One high-voltage packet or four low-voltage packets share the same conserved buffer. */
public abstract class VoltageTransformer extends InventoryBlockEntity
    implements IHasGui, INetworkClientTileEntityEventListener {
  private final int defaultTier;
  private final double lowPacket, highPacket;
  protected final ElectricalBuffer energy;
  private Mode configuredMode = Mode.redstone;
  private Mode appliedMode;
  private Direction appliedFacing;
  private double inputFlow, outputFlow;

  protected VoltageTransformer(
      BlockEntityType<? extends VoltageTransformer> type,
      BlockPos position,
      BlockState state,
      int lowTier) {
    super(type, position, state);
    if (lowTier < 0 || lowTier == Integer.MAX_VALUE)
      throw new IllegalArgumentException("Invalid low voltage tier");
    defaultTier = lowTier;
    lowPacket = EnergyNet.instance.getPowerFromTier(lowTier);
    highPacket = EnergyNet.instance.getPowerFromTier(lowTier + 1);
    energy =
        addComponent(
            new ElectricalBuffer(this, lowPacket * 8, Set.of(), Set.of(), lowTier, lowTier, true)
                .setMultiSource(true));
    inputFlow = highPacket;
    outputFlow = lowPacket;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    configuredMode = Mode.fromId(data.getIntOr("mode", 0));
    appliedMode = null;
    appliedFacing = null;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("mode", getMode().ordinal());
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    applyConfiguration();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    applyConfiguration();
  }

  @Override
  public void setFacing(Level world, Direction facing) {
    super.setFacing(world, facing);
    applyConfiguration();
  }

  public Mode getMode() {
    return configuredMode == null ? Mode.redstone : configuredMode;
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event < 0 || event >= Mode.VALUES.length || getLevel() == null || getLevel().isClientSide())
      return;
    Mode selected = Mode.VALUES[event];
    if (getMode() == selected) return;
    configuredMode = selected;
    setChanged();
    applyConfiguration();
  }

  private void applyConfiguration() {
    var world = getLevel();
    if (world == null || world.isClientSide()) return;
    Mode selected = getMode();
    boolean stepUp =
        selected == Mode.stepup
            || selected == Mode.redstone && world.hasNeighborSignal(worldPosition);
    Mode effective = stepUp ? Mode.stepup : Mode.stepdown;
    Direction facing = getFacing();
    if (appliedMode == effective && appliedFacing == facing) return;
    appliedMode = effective;
    appliedFacing = facing;
    energy.setSourceTier(defaultTier + (stepUp ? 1 : 0));
    energy.setSinkTier(defaultTier + (stepUp ? 0 : 1));
    energy.setPacketOutput(stepUp ? 1 : 4);
    var front = DirectionSets.ofMask(1 << facing.ordinal());
    var sides = DirectionSets.except(facing);
    energy.setDirections(stepUp ? sides : front, stepUp ? front : sides);
    energy.setEnabled(true);
    setActive(stepUp);
    inputFlow = stepUp ? lowPacket : highPacket;
    outputFlow = stepUp ? highPacket : lowPacket;
  }

  public double getinputflow() {
    return inputFlow;
  }

  public double getoutputflow() {
    return outputFlow;
  }

  @Override
  public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag flags) {
    super.addInformation(stack, tooltip, flags);
    tooltip.add(
        String.format(
            Locale.ROOT,
            "%s %.0f %s %s %.0f %s",
            LocalizedText.translate("ic2.item.tooltip.Low"),
            lowPacket,
            LocalizedText.translate("ic2.generic.text.EUt"),
            LocalizedText.translate("ic2.item.tooltip.High"),
            highPacket,
            LocalizedText.translate("ic2.generic.text.EUt")));
  }

  @Override
  public OwnedMenu<VoltageTransformer> createServerScreenHandler(int syncId, Player player) {
    return new TransformerMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new TransformerMenu(syncId, inventory, this);
  }

  public enum Mode {
    redstone,
    stepdown,
    stepup;
    static final Mode[] VALUES = values();

    static Mode fromId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : redstone;
    }
  }
}
