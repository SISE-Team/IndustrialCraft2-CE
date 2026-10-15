package ic2ce.neoforge.power;

import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.neoforge.block.ActiveBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.inventory.ChargeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Buffered sources tick their own fuel policy; environmental sources may override production. */
public abstract class BufferedGenerator extends ActiveBlockEntity implements IHasGui {
  public final ChargeInventory chargeSlot;
  protected final ElectricalBuffer energy;
  @GuiSynced public int fuel;
  protected double production;
  private int activityTicks = ProjectRuntime.random.nextInt(256), activityBalance;

  protected BufferedGenerator(
      BlockEntityType<? extends BufferedGenerator> type,
      BlockPos position,
      BlockState block,
      double output,
      int tier,
      int capacity) {
    super(type, position, block);
    production = output;
    chargeSlot = new ChargeInventory(this, 1);
    energy =
        addComponent(
            ElectricalBuffer.asBasicSource(this, capacity, tier).addManagedSlot(chargeSlot));
  }

  protected final boolean validProduction() {
    return Double.isFinite(production) && production > 0;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean refuelled = needsFuel() && gainFuel();
    boolean producing = gainEnergy();
    if (refuelled) setChanged();
    if (!delayActiveUpdate()) setActiveState(producing, false);
    else {
      activityBalance += producing ? 1 : -1;
      if ((++activityTicks & 255) == 0) {
        setActiveState(activityBalance > 0, false);
        activityBalance = 0;
      }
    }
  }

  public boolean gainEnergy() {
    if (!validProduction() || !isConverting()) return false;
    energy.addEnergy(production);
    if (fuel > 0) fuel--;
    return true;
  }

  public boolean isConverting() {
    return !needsFuel() && validProduction() && energy.getFreeEnergy() >= production;
  }

  public boolean needsFuel() {
    return fuel <= 0 && validProduction() && energy.getFreeEnergy() >= production;
  }

  public abstract boolean gainFuel();

  protected boolean delayActiveUpdate() {
    return false;
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    fuel = Math.max(0, data.getIntOr("fuel", 0));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("fuel", fuel);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return DocumentMenu.create(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }
}
