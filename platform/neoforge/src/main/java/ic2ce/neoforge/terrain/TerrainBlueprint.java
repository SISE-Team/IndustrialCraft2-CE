package ic2ce.neoforge.terrain;

import ic2ce.api.item.ITerraformingBP;
import ic2ce.neoforge.registry.ItemDefinitions;
import ic2ce.neoforge.runtime.ProgressionService;
import ic2ce.neoforge.terrain.program.TerrainProgram;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Immutable energy/range descriptor bound to one independent terrain program. */
public class TerrainBlueprint extends Item implements ITerraformingBP {
  public final double consume;
  public final int range;
  private final TerrainProgram logic;

  public TerrainBlueprint(Properties properties, double consume, int range, TerrainProgram logic) {
    super(properties);
    if (!Double.isFinite(consume) || consume < 0 || range < 0 || range > 1024)
      throw new IllegalArgumentException("Invalid terrain blueprint bounds");
    this.consume = consume;
    this.range = range;
    this.logic = logic;
  }

  public static void init() {
    for (var item : BuiltInRegistries.ITEM)
      if (item instanceof TerrainBlueprint blueprint && blueprint.logic != null)
        blueprint.logic.init();
  }

  @Override
  public double getConsume(ItemStack stack) {
    return consume;
  }

  @Override
  public int getRange(ItemStack stack) {
    return range;
  }

  @Override
  public boolean canInsert(ItemStack stack, Player player, Level world, BlockPos position) {
    if (this == ItemDefinitions.CULTIVATION_TFBP
        && world.dimension() == Level.END
        && !world.isClientSide())
      ProgressionService.issueAchievement(player, "terraformEndCultivation");
    return true;
  }

  @Override
  public boolean terraform(ItemStack stack, Level world, BlockPos position) {
    return logic != null
        && TerrainProgram.editable(world, position)
        && logic.terraform(world, position);
  }
}
