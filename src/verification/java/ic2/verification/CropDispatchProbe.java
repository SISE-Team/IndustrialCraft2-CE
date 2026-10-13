package ic2.verification;

import ic2.api.crops.Crops;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.ItemCropSeed;
import ic2.core.ref.Ic2Blocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

public final class CropDispatchProbe {
    public static Map<String,Object> run(ServerPlayer player){
        var pos=new BlockPos(96,101,96);var level=player.level();var card=Crops.instance.getCropCard("ic2","wheat");var stack=ItemCropSeed.generateItemStackFromValues(card,4,5,6,4);
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),"time set noon");
        level.setBlock(pos.above(),Blocks.AIR.defaultBlockState(),3);level.setBlock(pos.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7),3);level.setBlock(pos,Ic2Blocks.CROP_STICK.defaultBlockState().setValue(ic2.core.block.tileentity.Ic2TileEntityBlock.CROSSING_BASE,false),3);var crop=(TileEntityCrop)level.getBlockEntity(pos);
        var row=new LinkedHashMap<String,Object>();row.put("seedTag",ic2.core.util.StackTag.get(stack).toString());row.put("resolvedCard",String.valueOf(Crops.instance.getCropCard(stack)));row.put("light",crop.getLightLevel());row.put("crossing",crop.isCrossingBase());row.put("canGrow",card.canGrow(crop));
        var previous=player.getMainHandItem();player.setItemInHand(InteractionHand.MAIN_HAND,stack);var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);row.put("directItemResult",stack.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit)).toString());row.put("afterBlock",level.getBlockState(pos).toString());row.put("afterHeld",player.getMainHandItem().toString());player.setItemInHand(InteractionHand.MAIN_HAND,previous);level.removeBlock(pos,false);return row;
    }
}
