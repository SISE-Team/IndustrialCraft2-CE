package ic2ce.neoforge.personal;

import com.mojang.authlib.GameProfile;
import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.tile.*;
import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.upgrade.IUpgradableBlock;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.core.util.ProfileUtil;
import ic2ce.core.util.StackTag;
import ic2ce.core.util.StackUtil;
import ic2ce.neoforge.block.InventoryBlockEntity;
import ic2ce.neoforge.inventory.ChargeInventory;
import ic2ce.neoforge.inventory.InventorySlot;
import ic2ce.neoforge.inventory.LinkedInput;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.personal.menu.OwnerEnergyTradeMenu;
import ic2ce.neoforge.personal.menu.PublicEnergyTradeMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Paid energy is a finite ledger: completed payment buys credit, incoming EU consumes that credit.
 */
public class EnergyTradeTerminal extends InventoryBlockEntity
    implements OwnedDevice,
        IHasGui,
        IEnergySink,
        IEnergySource,
        INetworkClientTileEntityEventListener,
        IUpgradableBlock {
  public int euOffer = 1000, paidFor;
  public double euBuffer;
  private GameProfile owner;
  private boolean attached;
  private int bufferLimit = 10000, tier = 1;
  private double creditFraction;
  private ItemStack payment = ItemStack.EMPTY;
  private int pendingCredit;
  public final InventorySlot demandSlot =
      new InventorySlot(this, "demand", InventorySlot.Access.NONE, 1);
  public final LinkedInput inputSlot = new LinkedInput(this, "input", 1, demandSlot);
  public final ChargeInventory chargeSlot = new ChargeInventory(this, 1);
  public final UpgradeInventory upgradeSlot = new UpgradeInventory(this, "upgrade", 1);

  public EnergyTradeTerminal(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.ENERGY_O_MAT, position, state);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    owner =
        data.contains("ownerGameProfile")
            ? ProfileUtil.read(data.getCompoundOrEmpty("ownerGameProfile"))
            : null;
    euOffer = Math.max(100, data.getIntOr("euOffer", 1000));
    paidFor = Math.max(0, data.getIntOr("paidFor", 0));
    creditFraction = bounded(data.getDoubleOr("paidFraction", 0), Math.nextDown(1.0));
    euBuffer = bounded(data.getDoubleOr("euBuffer", 0), Integer.MAX_VALUE);
    payment =
        data.contains("pendingPayment")
            ? StackTag.parseHolder(data.getCompoundOrEmpty("pendingPayment"))
            : ItemStack.EMPTY;
    pendingCredit = payment.isEmpty() ? 0 : Math.max(100, data.getIntOr("pendingCredit", 1000));
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.remove("ownerGameProfile");
    if (owner != null) {
      var profile = new CompoundTag();
      ProfileUtil.write(profile, owner);
      data.put("ownerGameProfile", profile);
    }
    data.putInt("euOffer", euOffer);
    data.putInt("paidFor", paidFor);
    data.putDouble("paidFraction", creditFraction);
    data.putDouble("euBuffer", euBuffer);
    data.remove("pendingPayment");
    data.remove("pendingCredit");
    if (!payment.isEmpty()) {
      var stored = new CompoundTag();
      StackTag.save(getLevel().registryAccess(), payment, stored);
      data.put("pendingPayment", stored);
      data.putInt("pendingCredit", pendingCredit);
    }
  }

  private static double bounded(double value, double maximum) {
    return Double.isFinite(value) ? Math.clamp(value, 0, maximum) : 0;
  }

  private void refreshUpgrades() {
    bufferLimit = upgradeSlot.getEnergyStorage(10000, 0, 0);
    tier = upgradeSlot.getTier(1);
    chargeSlot.setTier(tier);
  }

  @Override
  protected void onLoaded() {
    super.onLoaded();
    var world = getLevel();
    if (world != null && !world.isClientSide()) {
      refreshUpgrades();
      if (!attached) {
        EnergyNet.instance.addBlockEntityTile(this);
        attached = true;
      }
    }
  }

  @Override
  protected void onUnloaded() {
    if (attached) {
      attached = false;
      EnergyNet.instance.removeTile(this);
    }
    super.onUnloaded();
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    refreshUpgrades();
    if (payment.isEmpty() && euOffer >= 100 && (long) paidFor + euOffer <= Integer.MAX_VALUE) {
      var preview = inputSlot.consumeLinked(true);
      if (preview != null
          && !preview.isEmpty()
          && StackUtil.distribute(this, preview.copy(), true) == preview.getCount()) {
        var taken = inputSlot.consumeLinked(false);
        if (taken != null && !taken.isEmpty()) {
          payment = taken.copy();
          pendingCredit = euOffer;
          setChanged();
        }
      }
    }
    if (!payment.isEmpty()) {
      int delivered = StackUtil.distribute(this, payment.copy(), false);
      if (delivered < 0 || delivered > payment.getCount())
        throw new IllegalStateException("Payment recipient exceeded reserved item count");
      if (delivered > 0) {
        payment = payment.copyWithCount(payment.getCount() - delivered);
        if (payment.isEmpty()) {
          paidFor = Math.addExact(paidFor, pendingCredit);
          pendingCredit = 0;
        }
        setChanged();
      }
    }
    if (euBuffer > 0) {
      double sent = chargeSlot.charge(euBuffer);
      if (!Double.isFinite(sent) || sent < 0 || sent > euBuffer)
        throw new IllegalStateException("Charging target exceeded paid buffer");
      if (sent > 0) {
        euBuffer -= sent;
        setChanged();
      }
    }
  }

  @Override
  protected List<ItemStack> getAuxDrops(int fortune) {
    var drops = super.getAuxDrops(fortune);
    if (!payment.isEmpty()) drops.add(payment.copy());
    return drops;
  }

  @Override
  public boolean acceptsEnergyFrom(IEnergyEmitter sender, Direction side) {
    return side != getFacing();
  }

  public boolean facingMatchesDirection(Direction side) {
    return side == getFacing();
  }

  @Override
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side) {
    return side == getFacing();
  }

  @Override
  public double getOfferedEnergy() {
    return bounded(euBuffer, Integer.MAX_VALUE);
  }

  @Override
  public void drawEnergy(double amount) {
    useEnergy(Math.min(amount, euBuffer));
  }

  @Override
  public double getDemandedEnergy() {
    return Math.min(Math.max(0, paidFor) + creditFraction, Math.max(0, bufferLimit - euBuffer));
  }

  @Override
  public double injectEnergy(Direction side, double amount, double voltage) {
    if (!Double.isFinite(amount)
        || amount <= 0
        || !Double.isFinite(voltage)
        || voltage < 0
        || side == getFacing()) return amount;
    double accepted = Math.min(amount, getDemandedEnergy());
    if (accepted <= 0) return amount;
    double remaining = paidFor + creditFraction - accepted;
    paidFor = (int) Math.floor(Math.max(0, remaining));
    creditFraction = Math.max(0, remaining - paidFor);
    euBuffer += accepted;
    setChanged();
    return amount - accepted;
  }

  @Override
  public int getSourceTier() {
    return tier;
  }

  @Override
  public int getSinkTier() {
    return Integer.MAX_VALUE;
  }

  @Override
  public double getEnergy() {
    return euBuffer;
  }

  @Override
  public boolean useEnergy(double amount) {
    if (!Double.isFinite(amount) || amount < 0 || amount > euBuffer) return false;
    if (amount > 0) {
      euBuffer -= amount;
      setChanged();
    }
    return true;
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return Set.of(UpgradableProperty.EnergyStorage, UpgradableProperty.Transformer);
  }

  @Override
  public boolean permitsAccess(GameProfile profile) {
    return PrivateChest.checkAccess(this, profile);
  }

  @Override
  public Container getPrivilegedInventory(GameProfile profile) {
    return this;
  }

  @Override
  public GameProfile getOwner() {
    return owner;
  }

  @Override
  public void setOwner(GameProfile profile) {
    owner = profile;
    setChanged();
  }

  @Override
  public boolean wrenchCanRemove(Player player) {
    return permitsAccess(player.getGameProfile());
  }

  @Override
  protected boolean canEntityDestroy(Entity entity) {
    return false;
  }

  @Override
  protected boolean canSetFacingWrench(Direction side, Player player) {
    return player != null
        && permitsAccess(player.getGameProfile())
        && super.canSetFacingWrench(side, player);
  }

  @Override
  public List<String> getNetworkedFields() {
    var fields = super.getNetworkedFields();
    fields.add("owner");
    return fields;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return permitsAccess(player.getGameProfile())
        ? new OwnerEnergyTradeMenu(id, player.getInventory(), this)
        : new PublicEnergyTradeMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return data.readBoolean()
        ? new OwnerEnergyTradeMenu(id, inventory, this)
        : new PublicEnergyTradeMenu(id, inventory, this);
  }

  @Override
  public void writeScreenOpenData(Player player, InteractionHand hand, GrowingBuffer data)
      throws IOException {
    data.writeBoolean(permitsAccess(player.getGameProfile()));
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (player == null || event < 0 || event >= 8 || !permitsAccess(player.getGameProfile()))
      return;
    int delta =
        switch (event & 3) {
          case 0 -> 100000;
          case 1 -> 10000;
          case 2 -> 1000;
          default -> 100;
        };
    euOffer =
        (int) Math.clamp((long) euOffer + (event < 4 ? -delta : delta), 100, Integer.MAX_VALUE);
    setChanged();
  }
}
