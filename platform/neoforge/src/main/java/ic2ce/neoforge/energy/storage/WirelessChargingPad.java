package ic2ce.neoforge.energy.storage;


import ic2ce.api.item.ElectricItem;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.energy.menu.ChargingPadMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.world.DirectionSets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Collision-driven wireless charging rotates contact priority and bills accepted item charge. */
public abstract class WirelessChargingPad extends EnergyBank {
  private static final List<AABB> SHAPE = List.of(new AABB(0, 0, 0, 1, 0.9375, 1));
  private static final DustParticleOptions PARTICLE = new DustParticleOptions(0x3333FF, 1);
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  public static byte redstoneModes = 2;
  private ArrayList<Player> contacts;
  private int updateTicker, rotation;

  public WirelessChargingPad(
      BlockEntityType<? extends WirelessChargingPad> type,
      BlockPos position,
      BlockState state,
      int tier,
      int packet,
      int capacity) {
    super(type, position, state, tier, packet, capacity);
    updatePadDirections(getFacing());
    updateTicker = Math.floorMod(position.hashCode(), getTickRate());
  }

  @Override
  protected int modeCount() {
    return 2;
  }

  private void updatePadDirections(Direction facing) {
    int outputMask = 1 << facing.ordinal();
    int inputs = 63 & ~(outputMask | 1 << Direction.UP.ordinal());
    energy.setDirections(DirectionSets.ofMask(inputs), DirectionSets.ofMask(outputMask));
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    updatePadDirections(getFacing());
  }

  @Override
  protected void setFacing(Level world, Direction facing) {
    superSetFacing(world, facing);
    updatePadDirections(facing);
  }

  @Override
  protected List<AABB> getAabbs(boolean collision) {
    return SHAPE;
  }

  @Override
  protected void onEntityCollision(Entity entity) {
    super.onEntityCollision(entity);
    if (getLevel().isClientSide()
        || !(entity instanceof Player player)
        || !player.isAlive()
        || player.isSpectator()) return;
    if (contacts == null) contacts = new ArrayList<>(2);
    if (contacts.size() < 64 && !contacts.contains(player)) contacts.add(player);
  }

  @Override
  protected void onUnloaded() {
    if (contacts != null) contacts.clear();
    super.onUnloaded();
  }

  protected int getTickRate() {
    return 2;
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (updateTicker++ % getTickRate() != 0) return;
    double previous = energy.getEnergy();
    if (contacts != null && !contacts.isEmpty()) {
      int size = contacts.size(), first = Math.floorMod(rotation++, size);
      var bounds = new AABB(worldPosition);
      for (int index = 0; index < size && energy.getEnergy() > 0; index++) {
        var player = contacts.get((first + index) % size);
        if (player.isAlive()
            && !player.isRemoved()
            && player.level() == getLevel()
            && player.getBoundingBox().intersects(bounds)) getItems(player);
      }
      contacts.clear();
    }
    boolean changed = energy.getEnergy() < previous;
    setActive(changed);
    rsEmitter.setLevel(shouldEmitRedstone() ? 15 : 0);
    if (changed) setChanged();
  }

  protected void getItems(Player player) {
    int rate = getOutput();
    for (var slot : ARMOR) chargeItem(player.getItemBySlot(slot), rate);
    var items = player.getInventory().getNonEquipmentItems();
    for (int index = 0; index < items.size() && energy.getEnergy() > 0; index++)
      chargeItem(items.get(index), rate);
    chargeItem(player.getOffhandItem(), rate);
  }

  protected void chargeItem(ItemStack stack, int rate) {
    if (stack == null || stack.isEmpty() || stack.is(ItemDefinitions.DEBUG_ITEM) || rate <= 0)
      return;
    double available = energy.getEnergy();
    if (available <= 0) return;
    double wanted =
        ElectricItem.manager.charge(
            stack,
            Math.min(available, (double) rate * getTickRate()),
            energy.getSourceTier(),
            true,
            true);
    if (!Double.isFinite(wanted) || wanted <= 0) return;
    double reserved = Math.min(wanted, Math.min(available, (double) rate * getTickRate()));
    if (!energy.useEnergy(reserved)) return;
    double accepted =
        ElectricItem.manager.charge(stack, reserved, energy.getSourceTier(), true, false);
    if (!Double.isFinite(accepted) || accepted < 0 || accepted > reserved)
      throw new IllegalStateException("Electric item violated its reserved charge budget");
    if (accepted < reserved) energy.forceAddEnergy(reserved - accepted);
  }

  @Override
  protected boolean shouldEmitRedstone() {
    return currentMode() == 0 ? getActive() : !getActive();
  }

  @Override
  public String getRedstoneMode() {
    return LocalizedText.translate(getRedstoneModeKey());
  }

  @Override
  public String getRedstoneModeKey() {
    return "ic2.blockChargepad.gui.mod.redstone" + currentMode();
  }

  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (!getActive() || getLevel().getRandom().nextInt(8) != 0) return;
    var random = getLevel().getRandom();
    for (int index = 0; index < 20; index++)
      getLevel()
          .addParticle(
              PARTICLE,
              worldPosition.getX() + random.nextFloat(),
              worldPosition.getY() + 0.9 + random.nextFloat(),
              worldPosition.getZ() + random.nextFloat(),
              0,
              0.1,
              0);
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int syncId, Player player) {
    return new ChargingPadMenu(syncId, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(
      int syncId, Inventory inventory, GrowingBuffer data) {
    return new ChargingPadMenu(syncId, inventory, this);
  }
}
