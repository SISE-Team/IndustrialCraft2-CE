package ic2ce.neoforge.agriculture;

import ic2ce.api.crops.CropCard;
import ic2ce.api.crops.CropProperties;
import ic2ce.api.crops.ICropTile;
import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.FluidDefinitions;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.SharedRandom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.mutable.MutableObject;

/** Native prepaid crop work, actual exhaustion, precise care limits and fluid-cell simulation. */
public final class AgricultureContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native agriculture: " + label);
  }

  public static Result run(ServerLevel world) {
    return new AgricultureContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8992, 100, 8992);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, BlockDefinitions.CROP_HARVESTER.defaultBlockState());
    world.setBlockAndUpdate(position.east().below(), Blocks.FARMLAND.defaultBlockState());
    world.setBlockAndUpdate(position.east(), BlockDefinitions.WHEAT_CROP.defaultBlockState());
    var crop = (CropPlot) world.getBlockEntity(position.east());
    var harvester = (AutoHarvester) world.getBlockEntity(position);
    var nativeSpecies = crop.getCrop();
    var prototypes =
        new ItemStack[] {new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.GOLD_INGOT, 2)};
    var species =
        new CropCard(ic2ce.neoforge.agriculture.Cultivar.WHEAT) {
          @Override
          public Block getCropBlock() {
            return BlockDefinitions.WHEAT_CROP;
          }

          @Override
          public CropProperties getProperties() {
            return new CropProperties(1, 0, 0, 0, 0, 0);
          }

          @Override
          public double dropGainChance() {
            return 1;
          }

          @Override
          public ItemStack[] getGains(ICropTile ignored) {
            return prototypes;
          }

          @Override
          public int getAgeAfterHarvest(ICropTile ignored) {
            return 2;
          }

          @Override
          public List<net.minecraft.resources.Identifier> getTexturesLocation() {
            return List.of();
          }
        };
    try {
      var field = CropPlot.class.getDeclaredField("crop");
      field.setAccessible(true);
      field.set(crop, species);
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
    crop.setCurrentAge(species.getMaxAge());
    crop.setStatGain(0);
    harvester.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(21);
    harvester.scanX = 0;
    harvester.scanY = 0;
    harvester.scanZ = 0;
    SharedRandom.INSTANCE.setSeed(1);
    harvester.scan();
    check(
        crop.getCurrentAge() == species.getMaxAge()
            && harvester.contentSlot.isEmpty()
            && harvester.getEnergy() == 20,
        "insufficient complete stochastic drop bill leaves the real crop mature and every output"
            + " empty after the one-EU scan");
    harvester.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(61);
    harvester.scanX = 0;
    harvester.scanY = 0;
    harvester.scanZ = 0;
    SharedRandom.INSTANCE.setSeed(1);
    harvester.scan();
    int quantity = 0;
    for (var item : harvester.contentSlot) quantity += item.getCount();
    check(
        crop.getCurrentAge() == 2
            && quantity >= 8
            && quantity <= 12
            && harvester.getEnergy() == 0
            && prototypes[0].getCount() == 2
            && prototypes[1].getCount() == 2,
        "exact eighty-EU four-stack harvest bill conserves actual output and never mutates addon"
            + " gain prototypes; quantity="
            + quantity
            + ", EU="
            + harvester.getEnergy());
    world.removeBlock(position, false);
    world.setBlockAndUpdate(position, BlockDefinitions.CROPMATRON.defaultBlockState());
    var care = (FluidCropCaretaker) world.getBlockEntity(position);
    crop.setStorageNutrients(0);
    crop.setStorageWater(0);
    crop.setStorageWeedEX(0);
    care.fertilizerSlot.put(new ItemStack(ItemDefinitions.FERTILIZER));
    care.getWaterTank()
        .fillMbUnchecked(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 100), false);
    care.getExTank()
        .fillMbUnchecked(Ic2FluidStack.create(FluidDefinitions.WEED_EX.still, 50), false);
    care.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(11);
    care.scanX = 0;
    care.scanY = 0;
    care.scanZ = 0;
    care.scan();
    check(
        crop.getStorageNutrients() == 90
            && crop.getStorageWater() == 0
            && crop.getStorageWeedEX() == 0
            && care.getEnergy() == 0
            && care.getWaterTank().getFluidAmount() == 100
            && care.getExTank().getFluidAmount() == 50,
        "eleven actual EU buys one scan and fertilizer only, with no unpaid tank-fed water or"
            + " pesticide");
    care.getComponent(ic2ce.neoforge.component.ElectricalBuffer.class).forceAddEnergy(21);
    care.scanX = 0;
    care.scanY = 0;
    care.scanZ = 0;
    care.scan();
    check(
        crop.getStorageWater() == 100
            && crop.getStorageWeedEX() == 50
            && care.getEnergy() == 0
            && care.getWaterTank().isEmpty()
            && care.getExTank().isEmpty(),
        "two later prepaid tank operations commit their exact fluid amounts");
    crop.setStorageWater(0);
    crop.setStorageWeedEX(50);
    check(
        crop.applyHydration(-10, false) == 0
            && crop.getStorageWater() == 0
            && crop.applyWeedEx(-10, false, false, false) == 0
            && crop.getStorageWeedEX() == 50,
        "negative care requests cannot reverse stored water or herbicide");
    check(
        crop.applyWeedEx(50, true, true, false) == 50 && crop.getStorageWeedEX() == 100,
        "an exactly fitting fixed manual herbicide dose uses its complete legitimate capacity");
    var herbicide = new ItemStack(ItemDefinitions.WEED_EX_CELL);
    StackTag.update(
        herbicide,
        data -> {
          data.putInt("uses", 63);
          return data;
        });
    crop.setStorageWeedEX(0);
    check(
        ItemDefinitions.WEED_EX_CELL.useOnCrop(herbicide, crop, false)
            && herbicide.isEmpty()
            && crop.getStorageWeedEX() == 50,
        "last actual herbicide charge consumes the caller's physical cell");
    var hydration = new ItemStack(ItemDefinitions.HYDRATION_CELL);
    StackTag.update(
        hydration,
        data -> {
          data.putInt("uses", 9999);
          return data;
        });
    check(
        ItemDefinitions.HYDRATION_CELL.useOnCrop(hydration, crop, false)
            && hydration.isEmpty()
            && crop.getStorageWater() == 1,
        "last remaining hydration unit has a usable dose and truly exhausts its physical cell");
    var empty = new ItemStack(ItemDefinitions.EMPTY_CELL);
    var water = Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000);
    var replacement = new MutableObject<ItemStack>();
    var emptyItem = (ic2ce.api.fluid.Ic2FluidItem) empty.getItem();
    check(
        emptyItem.fillMb(empty, water, true, replacement) == 1000
            && empty.getCount() == 1
            && replacement.getValue().is(ItemDefinitions.WATER_CELL),
        "fixed-volume fill simulation owns its returned cell and leaves its input intact");
    var filled = replacement.getValue();
    var fluidItem = (ic2ce.api.fluid.Ic2FluidItem) filled.getItem();
    check(
        fluidItem.drainMb(filled, 999, false, replacement).isEmpty() && filled.getCount() == 1,
        "undersized fluid request cannot consume a indivisible cell");
    check(
        fluidItem.drainMb(filled, 1000, true, replacement).getAmountMb() == 1000
            && filled.getCount() == 1
            && replacement.getValue().is(ItemDefinitions.EMPTY_CELL),
        "fixed-volume drain simulation preserves original quantity and returns an owned empty"
            + " replacement");
    check(
        fluidItem.drainMb(filled, 1000, false, replacement).getAmountMb() == 1000
            && filled.isEmpty()
            && replacement.getValue().is(ItemDefinitions.EMPTY_CELL),
        "real fixed-volume drain consumes exactly one cell and preserves its empty container");
    check(
        ItemDefinitions.EMPTY_CELL.getBarWidth(empty) == 0,
        "zero-charge empty cell has a finite bounded display width");
    world.removeBlock(position, false);
    world.removeBlock(position.east(), false);
    world.removeBlock(position.east().below(), false);
    return new Result(checks, List.copyOf(failures));
  }
}
