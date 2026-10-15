package ic2ce.neoforge.processing.thermal;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.core.IHasGui;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import ic2ce.core.util.ParticleUtil;
import ic2ce.engine.machine.ExperienceAccount;
import ic2ce.neoforge.block.ActiveBlockEntity;
import ic2ce.neoforge.inventory.FuelInput;
import ic2ce.neoforge.inventory.OutputInventory;
import ic2ce.neoforge.inventory.SmeltingInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * A fuel-burning furnace queries one recipe per tick and preserves fuel, output and fractional
 * experience.
 */
public class FuelSmelter extends ActiveBlockEntity
    implements IHasGui, ScreenValues, INetworkClientTileEntityEventListener {
  public final SmeltingInput inputSlot;
  public final OutputInventory outputSlot;
  public final FuelInput fuelSlot;
  @GuiSynced public int fuel, totalFuel;
  @GuiSynced public short progress;
  protected double xp;
  public static final short operationLength = 160;
  private ItemStack processingKey = ItemStack.EMPTY;

  public FuelSmelter(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.IRON_FURNACE, position, state);
    inputSlot = new SmeltingInput(this, "input", 1);
    outputSlot = new OutputInventory(this, "output", 1);
    fuelSlot = new FuelInput(this, "fuel", 1, true);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    fuel = Math.max(0, data.getIntOr("fuel", 0));
    totalFuel = Math.max(fuel, data.getIntOr("totalFuel", 0));
    progress = (short) Math.clamp(data.getShortOr("progress", (short) 0), 0, operationLength);
    xp = ExperienceAccount.normalize(data.getDoubleOr("xp", 0));
    processingKey = ItemStack.EMPTY;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("fuel", fuel);
    data.putInt("totalFuel", totalFuel);
    data.putShort("progress", progress);
    data.putDouble("xp", xp);
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    var recipe = inputSlot.process();
    boolean ready = recipe != null && outputSlot.canAdd(recipe.getOutput());
    boolean changed = false;
    if (ready) {
      var input = inputSlot.get();
      if (!processingKey.isEmpty() && !ItemStack.isSameItemSameComponents(processingKey, input))
        progress = 0;
      if (processingKey.isEmpty() || !ItemStack.isSameItemSameComponents(processingKey, input))
        processingKey = input.copyWithCount(1);
      if (fuel == 0) {
        fuel = totalFuel = Math.max(0, fuelSlot.consumeFuel());
        changed |= fuel > 0;
      }
    }
    if (ready && fuel > 0) {
      if (++progress >= operationLength) {
        inputSlot.consume(recipe);
        if (outputSlot.add(recipe.getOutput()) != 0)
          throw new IllegalStateException("Fuel furnace output changed before commit");
        var metadata = recipe.getRecipe().getMetaData();
        xp =
            ExperienceAccount.earn(xp, metadata == null ? 0 : metadata.getFloatOr("experience", 0));
        progress = 0;
        changed = true;
      }
    } else {
      progress = 0;
      processingKey = ItemStack.EMPTY;
    }
    if (fuel > 0) {
      fuel--;
      activate(false);
    } else shutdown(false);
    if (changed) setChanged();
  }

  public static double spawnXP(Player player, double balance) {
    double normalized = ExperienceAccount.normalize(balance);
    if (player.level().isClientSide()) return normalized;
    int paid = ExperienceAccount.claimable(normalized, 10000), remaining = paid;
    while (remaining > 0) {
      int value = ExperienceOrb.getExperienceValue(remaining);
      remaining -= value;
      player
          .level()
          .addFreshEntity(
              new ExperienceOrb(
                  player.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, value));
    }
    return ExperienceAccount.afterClaim(normalized, paid);
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event == 0
        && !getLevel().isClientSide()
        && player.level() == getLevel()
        && player.distanceToSqr(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5)
            <= 64) {
      xp = spawnXP(player, xp);
      setChanged();
    }
  }

  public double getProgress() {
    return Math.clamp(progress / (double) operationLength, 0, 1);
  }

  public double getFuelRatio() {
    return totalFuel <= 0 ? 0 : Math.clamp(fuel / (double) totalFuel, 0, 1);
  }

  @Override
  public double getGuiValue(String name) {
    return switch (name) {
      case "fuel" -> getFuelRatio();
      case "progress" -> getProgress();
      default -> throw new IllegalArgumentException("Unknown fuel furnace gauge: " + name);
    };
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player player) {
    return DocumentMenu.create(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return DocumentMenu.create(id, inventory, this);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_FURNACE_IRON_OPERATE;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (!getActive()) return;
    var world = getLevel();
    ParticleUtil.showFurnaceFlames(world, worldPosition, getFacing());
    if (world.getRandom().nextDouble() < 0.1)
      world.playLocalSound(
          worldPosition.getX() + 0.5,
          worldPosition.getY(),
          worldPosition.getZ() + 0.5,
          SoundEvents.FURNACE_FIRE_CRACKLE,
          SoundSource.BLOCKS,
          1,
          1,
          false);
  }
}
