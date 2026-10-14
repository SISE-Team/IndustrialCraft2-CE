package ic2ce.verification;

import ic2ce.api.crops.Crops;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.agriculture.item.EncodedSeed;
import ic2ce.neoforge.registry.BlockDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

public final class CropDispatchProbe {
  public static Map<String, Object> run(ServerPlayer player) {
    var pos = new BlockPos(96, 101, 96);
    var level = player.level();
    var card = Crops.instance.getCropCard("ic2", "wheat");
    var stack = EncodedSeed.generateItemStackFromValues(card, 4, 5, 6, 4);
    level
        .getServer()
        .getCommands()
        .performPrefixedCommand(
            level.getServer().createCommandSourceStack().withSuppressedOutput(), "time set noon");
    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
    level.setBlock(
        pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7), 3);
    level.setBlock(
        pos,
        BlockDefinitions.CROP_STICK
            .defaultBlockState()
            .setValue(ic2ce.neoforge.block.MachineBlockType.CROSSING_BASE, false),
        3);
    var crop = (CropPlot) level.getBlockEntity(pos);
    var row = new LinkedHashMap<String, Object>();
    row.put("seedTag", ic2ce.core.util.StackTag.get(stack).toString());
    row.put("resolvedCard", String.valueOf(Crops.instance.getCropCard(stack)));
    row.put("light", crop.getLightLevel());
    row.put("crossing", crop.isCrossingBase());
    row.put("canGrow", card.canGrow(crop));
    var previous = player.getMainHandItem();
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    row.put(
        "directItemResult",
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).toString());
    row.put("afterBlock", level.getBlockState(pos).toString());
    row.put("afterHeld", player.getMainHandItem().toString());
    player.setItemInHand(InteractionHand.MAIN_HAND, previous);
    level.removeBlock(pos, false);
    return row;
  }
}
