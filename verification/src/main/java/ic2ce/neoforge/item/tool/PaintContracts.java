package ic2ce.neoforge.item.tool;

import ic2ce.neoforge.item.paint.VanillaColorBlockId;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;

/**
 * Real furniture/entity state, cross-color palette, owned container contents and denied paired
 * edits.
 */
public final class PaintContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean value, String label) {
    checks++;
    if (!value) failures.add("native paint: " + label);
  }

  public static Result run(ServerLevel world) {
    return new PaintContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(9248, 100, 9248);
    var partner = position.east();
    world.getChunkAt(position);
    world.getChunkAt(partner);
    world.setBlockAndUpdate(position.below(), Blocks.STONE.defaultBlockState());
    world.setBlockAndUpdate(partner.below(), Blocks.STONE.defaultBlockState());
    for (var family : VanillaColorBlockId.values())
      for (var pigment : DyeColor.values()) {
        var from = pigment == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE;
        var source = BlockPaintService.colorBlock(from, family);
        var target = BlockPaintService.colorBlock(pigment, family);
        var state = source.defaultBlockState();
        if (state.hasProperty(BedBlock.PART))
          state =
              state
                  .setValue(BedBlock.PART, BedPart.FOOT)
                  .setValue(BedBlock.FACING, Direction.EAST)
                  .setValue(BedBlock.OCCUPIED, false);
        if (family == VanillaColorBlockId.BED)
          world.setBlock(
              partner,
              state.setValue(BedBlock.PART, BedPart.HEAD),
              Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        world.setBlock(position, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        var entity = world.getBlockEntity(position);
        ItemStack named = new ItemStack(source);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Owned paint data"));
        if (entity != null) entity.applyComponentsFromItemStack(named);
        if (entity instanceof ShulkerBoxBlockEntity box)
          box.setItem(0, new ItemStack(Items.IRON_INGOT, 17));
        var metadata =
            entity == null ? null : entity.saveWithFullMetadata(world.registryAccess()).copy();
        int loose =
            world.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3)).size();
        boolean painted = BlockPaintService.paint(world, position, pigment, _ -> true);
        var now = world.getBlockState(position);
        var current = world.getBlockEntity(position);
        check(
            painted && now.is(target),
            family + " -> " + pigment + " commits the actual native palette target");
        boolean preserved = true;
        for (var property : state.getProperties())
          if (now.hasProperty(property))
            preserved &= value(state, property).equals(value(now, property));
        check(
            preserved,
            family + " -> " + pigment + " preserves every shared physical state property");
        if (metadata != null)
          check(
              current != null
                  && metadata.equals(current.saveWithFullMetadata(world.registryAccess())),
              family + " -> " + pigment + " retains the actual block entity's complete saved data");
        if (family == VanillaColorBlockId.BED) {
          check(
              world.getBlockState(partner).is(target)
                  && current instanceof BedBlockEntity bed
                  && bed.getColor() == pigment
                  && world.getBlockEntity(partner) instanceof BedBlockEntity other
                  && other.getColor() == pigment,
              "both physical bed halves and cached renderer colors update together");
        }
        if (current instanceof ShulkerBoxBlockEntity box)
          check(
              box.getItem(0).getCount() == 17
                  && world
                          .getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(3))
                          .size()
                      == loose,
              "shulker inventory survives color change without loose duplicate items");
        world.setBlock(
            position,
            Blocks.AIR.defaultBlockState(),
            Block.UPDATE_CLIENTS
                | Block.UPDATE_KNOWN_SHAPE
                | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);
        world.setBlock(
            partner,
            Blocks.AIR.defaultBlockState(),
            Block.UPDATE_CLIENTS
                | Block.UPDATE_KNOWN_SHAPE
                | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);
      }
    var foot =
        Blocks.WHITE_BED
            .defaultBlockState()
            .setValue(BedBlock.FACING, Direction.EAST)
            .setValue(BedBlock.PART, BedPart.FOOT);
    world.setBlock(position, foot, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    world.setBlock(
        partner,
        foot.setValue(BedBlock.PART, BedPart.HEAD),
        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    check(
        !BlockPaintService.paint(world, position, DyeColor.RED, target -> !target.equals(partner))
            && world.getBlockState(position).is(Blocks.WHITE_BED)
            && world.getBlockState(partner).is(Blocks.WHITE_BED),
        "denied permission on the second bed half preserves both original physical blocks");
    world.setBlock(
        partner, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    check(
        !BlockPaintService.paint(world, position, DyeColor.RED, _ -> true)
            && world.getBlockState(position).is(Blocks.WHITE_BED),
        "missing companion cannot consume a painting operation or destroy the remaining half");
    check(
        !BlockPaintService.canPaint(world, position.below(), DyeColor.RED),
        "ordinary stone cannot be recolored through a substring family match");
    world.removeBlock(position, false);
    world.removeBlock(position.below(), false);
    world.removeBlock(partner.below(), false);
    return new Result(checks, List.copyOf(failures));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Comparable<?> value(
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.block.state.properties.Property property) {
    return state.getValue(property);
  }
}
