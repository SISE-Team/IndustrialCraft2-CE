package ic2ce.api.energy;

import ic2ce.api.crops.BaseSeed;
import ic2ce.api.energy.tile.*;
import ic2ce.api.item.*;
import ic2ce.neoforge.agriculture.CropCatalog;
import ic2ce.neoforge.item.tool.ToolItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Addon-facing defaults are checked against separate capacity/payment/face oracles. */
public final class PublicProtocolContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    int[] committed = {0};
    var heat =
        new IHeatSource() {
          public int maxrequestHeatTick(Direction face) {
            return 40;
          }

          public int requestHeat(Direction face, int request) {
            committed[0] += request;
            return request;
          }
        };
    for (int request : new int[] {Integer.MIN_VALUE, -1, 0, 1, 17, 40, 41, Integer.MAX_VALUE}) {
      int expected = Math.min(40, Math.max(0, request));
      checks++;
      if (heat.drawHeat(Direction.EAST, request, true) != expected || committed[0] != 0)
        failures.add(
            "public heat: prediction escaped the physical requested/capacity bill: " + request);
    }
    checks++;
    if (heat.drawHeat(Direction.EAST, 17, false) != 17 || committed[0] != 17)
      failures.add("public heat: committing draw changed the admitted payment");
    checks++;
    if (heat.drawHeat(null, 20, false) != 0 || committed[0] != 17)
      failures.add("public heat: absent contact committed a payment");
    for (var side : Direction.values())
      for (var own : DyeColor.values())
        for (var other : DyeColor.values()) {
          Direction[] requested = {null};
          IColoredEnergyTile left = face -> own;
          IColoredEnergyTile right =
              face -> {
                requested[0] = face;
                return other;
              };
          checks++;
          if (left.canInteractWith(right, side) != (own == other)
              || requested[0] != side.getOpposite())
            failures.add("public pigment: contact did not compare actual opposite faces");
        }
    IColoredEnergyTile uncolored = face -> null;
    checks++;
    if (!uncolored.canInteractWith((IColoredEnergyTile) face -> DyeColor.RED, Direction.UP)
        || uncolored.canInteractWith(null, Direction.UP))
      failures.add("public pigment: wildcard or absent-neighbor admission failed");
    var drill =
        new IMiningDrill() {
          public int energyUse(ItemStack stack, Level world, BlockPos pos, BlockState state) {
            return 1;
          }

          public int breakTime(ItemStack stack, Level world, BlockPos pos, BlockState state) {
            return 1;
          }

          public boolean breakBlock(ItemStack stack, Level world, BlockPos pos, BlockState state) {
            return false;
          }
        };
    var stack = new ItemStack(ToolItems.DIAMOND_DRILL);
    ElectricItem.manager.charge(stack, 1000, Integer.MAX_VALUE, true, false);
    double before = ElectricItem.manager.getCharge(stack);
    for (double bill :
        new double[] {-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
      checks++;
      if (drill.tryUsePower(stack, bill) || ElectricItem.manager.getCharge(stack) != before)
        failures.add("public drill: invalid bill changed physical charge");
    }
    checks++;
    if (!drill.tryUsePower(stack, 0)
        || ElectricItem.manager.getCharge(stack) != before
        || drill.tryUsePower(ItemStack.EMPTY, 0))
      failures.add("public drill: zero payment or empty physical stack admission failed");
    checks++;
    if (!drill.tryUsePower(stack, 100) || ElectricItem.manager.getCharge(stack) != before - 100)
      failures.add("public drill: finite bill did not pay exactly once");
    var crop = CropCatalog.cropWheat;
    var seed = new BaseSeed(crop, 0, 0, 31, 7);
    checks++;
    if (seed.size != 0 || seed.statGain != 31)
      failures.add("public seed: valid germination genotype rejected");
    for (int trait : new int[] {-1, 32, Integer.MAX_VALUE}) {
      boolean rejected = false;
      try {
        new BaseSeed(crop, 0, trait, 0, 0);
      } catch (IllegalArgumentException expected) {
        rejected = true;
      }
      checks++;
      if (!rejected) failures.add("public seed: invalid trait entered immutable registration");
    }

    for (double power : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
      boolean rejected = false;
      try {
        new ic2ce.api.recipe.ISemiFluidFuelManager.BurnProperty(1, power);
      } catch (IllegalArgumentException expected) {
        rejected = true;
      }
      checks++;
      if (!rejected)
        failures.add("public fuel: invalid finite electrical fluid-fuel property was admitted");
    }
    boolean rejected = false;
    try {
      new ic2ce.api.recipe.IFluidHeatManager.BurnProperty(0, 1);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    checks++;
    if (!rejected) failures.add("public heat: zero physical fuel volume was admitted");
    rejected = false;
    try {
      new ic2ce.api.recipe.ILiquidHeatExchangerManager.HeatExchangeProperty(
          net.minecraft.world.level.material.Fluids.EMPTY, 1);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    checks++;
    if (!rejected) failures.add("public exchange: empty native output fluid was admitted");
    var fermentation =
        new ic2ce.api.recipe.IFermenterRecipeManager.FermentationProperty(
            1, 1, net.minecraft.world.level.material.Fluids.WATER, 2);
    var output = fermentation.getOutput();
    output.setAmountMb(1);
    checks++;
    if (fermentation.getOutput().getAmountMb() != 2)
      failures.add("public fermentation: borrowed output mutated the physical recipe");
    var choices = new ArrayList<ItemStack>();
    choices.add(ItemStack.EMPTY);
    choices.add(new ItemStack(net.minecraft.world.item.Items.COAL));
    choices.add(new ItemStack(net.minecraft.world.item.Items.COAL, 2));
    var input =
        new ic2ce.api.recipe.IRecipeInput() {
          public boolean matches(ItemStack value) {
            return value.is(net.minecraft.world.item.Items.COAL);
          }

          public int getAmount() {
            return 2;
          }

          public List<ItemStack> getInputs() {
            return choices;
          }
        };
    var ingredient = input.getIngredient();
    choices.clear();
    checks++;
    if (!ingredient.test(new ItemStack(net.minecraft.world.item.Items.COAL))
        || ingredient.test(new ItemStack(net.minecraft.world.item.Items.STICK)))
      failures.add(
          "public input: actual native display retained an empty item or borrowed mutable candidate"
              + " list");
    rejected = false;
    try {
      input.getIngredient();
    } catch (IllegalStateException expected) {
      rejected = true;
    }
    checks++;
    if (!rejected) failures.add("public input: empty native candidate display was admitted");
    return new Result(checks, List.copyOf(failures));
  }
}
