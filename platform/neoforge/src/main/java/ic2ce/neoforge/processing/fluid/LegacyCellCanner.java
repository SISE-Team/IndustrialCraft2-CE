package ic2ce.neoforge.processing.fluid;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.api.recipe.*;
import ic2ce.core.IHasGui;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.block.PoweredBlockEntity;
import ic2ce.neoforge.inventory.*;
import ic2ce.neoforge.item.armor.WearableFluidReservoir;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.ScreenValues;
import ic2ce.neoforge.processing.menu.LegacyCanningMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.registry.SoundDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Distinct food recipes, six paid fuel stages and actual fluid foam filling with blocked-output
 * preservation.
 */
public class LegacyCellCanner extends PoweredBlockEntity implements IHasGui, ScreenValues {
  public short progress;
  public int energyConsume = 1, operationLength = 600;
  private int fuelQuality;
  protected Mode mode = Mode.NONE;
  public final InventorySlot resInputSlot =
      new InventorySlot(this, "input", InventorySlot.Access.I, 1);
  public final ConsumableInventory inputSlot =
      new ConsumableInventory(
          this, "canInput", InventorySlot.Access.I, 1, InventorySlot.InvSide.TOP) {
        @Override
        public boolean accepts(ItemStack stack) {
          return stack != null
              && !stack.isEmpty()
              && (stack.is(ItemDefinitions.TIN_CAN)
                  || stack.is(ItemDefinitions.EMPTY_FUEL_CAN)
                  || stack.is(ItemDefinitions.JETPACK)
                  || stack.is(ItemDefinitions.CF_PACK));
        }

        @Override
        public void onChanged() {
          super.onChanged();
          if (LegacyCellCanner.this.inputSlot != null) {
            var next = getMode();
            if (next != mode) {
              mode = next;
              progress = 0;
              fuelQuality = 0;
            }
          }
        }
      };
  public final OutputInventory outputSlot = new OutputInventory(this, "output", 1);

  public LegacyCellCanner(BlockPos pos, BlockState state) {
    super(BlockEntityDefinitions.CLASSIC_CANNER, pos, state, 600, 1);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    mode = getMode();
    fuelQuality = Math.max(0, data.getIntOr("fuelQuality", 0));
    progress = (short) Math.clamp(data.getIntOr("progress", 0), 0, 599);
    if (mode != Mode.FUEL) fuelQuality = 0;
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("fuelQuality", fuelQuality);
    data.putInt("progress", progress);
  }

  @Override
  public double getGuiValue(String name) {
    if (!"progress".equals(name))
      throw new IllegalArgumentException("Unknown canning gauge: " + name);
    return Math.clamp(progress / (double) duration(), 0, 1);
  }

  private int duration() {
    return switch (mode) {
      case FOOD -> Math.max(1, Math.min(32767, getFoodValue(resInputSlot.get()) * 50));
      case CF -> 50;
      default -> 600;
    };
  }

  private MachineRecipeResult<
          ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
      food() {
    return getLevel() == null
        ? null
        : Recipes.cannerBottle
            .get(getLevel())
            .apply(
                new ICannerBottleRecipeManager.RawInput(inputSlot.get(), resInputSlot.get()),
                false);
  }

  private ItemStack fuelProduct(int quality) {
    if (inputSlot.get().is(ItemDefinitions.EMPTY_FUEL_CAN)) {
      var can = new ItemStack(ItemDefinitions.FILLED_FUEL_CAN);
      StackTag.update(
          can,
          data -> {
            data.putInt("value", quality);
            return data;
          });
      return can;
    }
    var product = inputSlot.get().copyWithCount(1);
    if (!(product.getItem() instanceof WearableFluidReservoir tank) || quality <= 0)
      return ItemStack.EMPTY;
    return tank.fillMb(
                product, Ic2FluidStack.create(FluidDefinitions.BIOGAS.still, quality), false, null)
            == quality
        ? product
        : ItemStack.EMPTY;
  }

  private ItemStack foamProduct() {
    var product = inputSlot.get().copyWithCount(1);
    if (!(product.getItem() instanceof WearableFluidReservoir tank)) return ItemStack.EMPTY;
    return tank.fillMb(
                product,
                Ic2FluidStack.create(FluidDefinitions.CONSTRUCTION_FOAM.still, 2000),
                false,
                null)
            == 2000
        ? product
        : ItemStack.EMPTY;
  }

  public boolean canOperate() {
    mode = getMode();
    if (inputSlot.isEmpty() || resInputSlot.isEmpty()) return false;
    return switch (mode) {
      case FOOD -> {
        var recipe = food();
        yield recipe != null
            && !recipe.getOutput().isEmpty()
            && outputSlot.canAdd(recipe.getOutput());
      }
      case FUEL -> {
        int bill = getFuelValue(resInputSlot.get());
        int quality = (int) Math.min(Integer.MAX_VALUE, (long) fuelQuality + bill);
        var product = fuelProduct(quality);
        yield bill > 0 && !product.isEmpty() && outputSlot.canAdd(product);
      }
      case CF -> {
        var product = foamProduct();
        yield getPelletValue(resInputSlot.get()) > 0
            && !product.isEmpty()
            && outputSlot.canAdd(product);
      }
      case NONE -> false;
    };
  }

  @Override
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean ready = canOperate();
    if (!ready || energyConsume < 0 || !energy.useEnergy(energyConsume)) {
      if (getActive()) shutdown(progress > 0);
      if (!ready && mode != Mode.FUEL) {
        progress = 0;
        fuelQuality = 0;
      }
      return;
    }
    activate(false);
    progress++;
    if (mode == Mode.FUEL) {
      if (progress % 100 == 0) {
        boolean last = progress >= 600;
        operate(!last);
        if (last) {
          progress = 0;
          fuelQuality = 0;
        }
      }
    } else if (progress >= duration()) {
      operate(false);
      progress = 0;
      fuelQuality = 0;
    }
    setChanged();
  }

  public void operate(boolean incremental) {
    if (getLevel() == null || getLevel().isClientSide() || !canOperate()) return;
    switch (mode) {
      case FOOD -> {
        var recipe = food();
        if (recipe == null || !outputSlot.canAdd(recipe.getOutput())) return;
        var adjusted = recipe.getAdjustedInput();
        outputSlot.add(recipe.getOutput().copy());
        resInputSlot.put(adjusted.fill);
        inputSlot.put(adjusted.container);
      }
      case FUEL -> {
        int quality =
            (int)
                Math.min(Integer.MAX_VALUE, (long) fuelQuality + getFuelValue(resInputSlot.get()));
        var product = fuelProduct(quality);
        if (product.isEmpty() || !outputSlot.canAdd(product)) return;
        resInputSlot.put(resInputSlot.get().copyWithCount(resInputSlot.get().getCount() - 1));
        fuelQuality = quality;
        if (!incremental) {
          outputSlot.add(product);
          inputSlot.consume(1);
          fuelQuality = 0;
        }
      }
      case CF -> {
        var product = foamProduct();
        if (product.isEmpty() || !outputSlot.canAdd(product)) return;
        resInputSlot.put(resInputSlot.get().copyWithCount(resInputSlot.get().getCount() - 1));
        boolean further =
            getPelletValue(resInputSlot.get()) > 0
                && product.getItem() instanceof WearableFluidReservoir tank
                && tank.fillMb(
                        product,
                        Ic2FluidStack.create(FluidDefinitions.CONSTRUCTION_FOAM.still, 2000),
                        true,
                        null)
                    == 2000;
        if (further) inputSlot.put(product);
        else {
          outputSlot.add(product);
          inputSlot.consume(1);
        }
      }
      case NONE -> {}
    }
    setChanged();
  }

  public Mode getMode() {
    var stack = inputSlot == null ? ItemStack.EMPTY : inputSlot.get();
    if (stack.is(ItemDefinitions.TIN_CAN)) return Mode.FOOD;
    if (stack.is(ItemDefinitions.EMPTY_FUEL_CAN) || stack.is(ItemDefinitions.JETPACK))
      return Mode.FUEL;
    return stack.is(ItemDefinitions.CF_PACK) ? Mode.CF : Mode.NONE;
  }

  public int getFoodValue(ItemStack stack) {
    if (getLevel() == null || stack == null || stack.isEmpty()) return 0;
    var recipe =
        Recipes.cannerBottle
            .get(getLevel())
            .apply(
                new ICannerBottleRecipeManager.RawInput(
                    new ItemStack(ItemDefinitions.TIN_CAN, 64), stack),
                false);
    return recipe == null ? 0 : recipe.getOutput().getCount();
  }

  public int getFuelValue(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return 0;
    if (stack.is(ItemDefinitions.COALFUEL_CELL)) return 2548;
    if (stack.is(ItemDefinitions.BIOFUEL_CELL)) return 868;
    double factor =
        stack.is(Items.REDSTONE)
            ? .2
            : stack.is(Items.GLOWSTONE_DUST) ? .3 : stack.is(Items.GUNPOWDER) ? .4 : 0;
    return (int) Math.min(Integer.MAX_VALUE, fuelQuality * factor);
  }

  public static int getPelletValue(ItemStack stack) {
    return stack != null && stack.is(ItemDefinitions.PELLET) ? stack.getCount() : 0;
  }

  @Override
  public OwnedMenu<LegacyCellCanner> createServerScreenHandler(int id, Player owner) {
    return new LegacyCanningMenu(id, owner.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new LegacyCanningMenu(id, inventory, this);
  }

  @Override
  public SoundEvent getLoopingSoundEvent() {
    return SoundDefinitions.MACHINE_CANNER_OPERATE;
  }

  @Override
  public SoundEvent getInterruptSoundEvent() {
    return SoundDefinitions.MACHINE_INTERRUPT1;
  }

  public enum Mode {
    NONE,
    FOOD,
    FUEL,
    CF
  }
}
