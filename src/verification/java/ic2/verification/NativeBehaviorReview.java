package ic2.verification;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.block.wiring.AbstractSplitterCableBlock;
import ic2.core.ref.Ic2Blocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Routes through current vanilla BlockState/ItemStack APIs, rather than legacy helper overloads. */
public final class NativeBehaviorReview {
    private final List<String> failures=new ArrayList<>();private int checks;
    private void check(boolean ok,String label){checks++;if(!ok)failures.add(label);}
    public static Map<String,Object> run(ServerPlayer player){return new NativeBehaviorReview().execute(player);}
    private Map<String,Object> execute(ServerPlayer player){
        var level=player.level();var pos=new BlockPos(94,101,94);level.getChunkAt(pos);
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=-1;y<=2;y++)level.setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(pos,Ic2Blocks.IRON_FENCE.defaultBlockState(),3);
        level.setBlock(pos.east(),Ic2Blocks.IRON_FENCE.defaultBlockState(),3);check(level.getBlockState(pos).getValue(CrossCollisionBlock.EAST),"iron fence connects after neighbor placement");
        level.removeBlock(pos.east(),false);check(!level.getBlockState(pos).getValue(CrossCollisionBlock.EAST),"iron fence disconnects after neighbor removal");level.removeBlock(pos,false);
        var splitter=BuiltInRegistries.BLOCK.getValue(IC2.getIdentifier("splitter_cable"));level.setBlock(pos,splitter.defaultBlockState(),3);level.setBlock(pos.east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        check(level.getBlockState(pos).getValue(AbstractSplitterCableBlock.active),"splitter joins its energy network after redstone is added");level.removeBlock(pos.east(),false);check(!level.getBlockState(pos).getValue(AbstractSplitterCableBlock.active),"splitter disconnects when redstone is removed");level.removeBlock(pos,false);
        for(var sheet:List.of(Ic2Blocks.RESIN_SHEET,Ic2Blocks.RUBBER_SHEET)){
            level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(pos,sheet.defaultBlockState(),3);level.removeBlock(pos.below(),false);check(level.isEmptyBlock(pos),BuiltInRegistries.BLOCK.getKey(sheet)+" drops when support disappears");
        }
        for(var scaffold:List.of(Ic2Blocks.WOODEN_SCAFFOLD,Ic2Blocks.IRON_SCAFFOLD,Ic2Blocks.REINFORCED_WOODEN_SCAFFOLD,Ic2Blocks.REINFORCED_IRON_SCAFFOLD)){
            level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(pos,scaffold.defaultBlockState(),3);level.removeBlock(pos.below(),false);check(level.isEmptyBlock(pos),BuiltInRegistries.BLOCK.getKey(scaffold)+" collapses when support disappears");
        }
        player.setDeltaMovement(new Vec3(1,-1,1));player.fallDistance=10;Ic2Blocks.RESIN_SHEET.defaultBlockState().entityInside(level,pos,player,InsideBlockEffectApplier.NOOP,true);check(player.getDeltaMovement().x()==0.6&&player.getDeltaMovement().y()==-0.85&&player.fallDistance==7.5,"resin sheet native entity callback slows motion and falling");
        player.fallDistance=10;Ic2Blocks.WOOL_SHEET.defaultBlockState().entityInside(level,pos,player,InsideBlockEffectApplier.NOOP,true);check(Math.abs(player.fallDistance-9.5)<0.01,"wool sheet native callback reduces fall distance");
        player.setShiftKeyDown(false);player.setDeltaMovement(new Vec3(1,-1,1));player.fallDistance=10;Ic2Blocks.WOODEN_SCAFFOLD.defaultBlockState().entityInside(level,pos,player,InsideBlockEffectApplier.NOOP,true);check(player.getDeltaMovement().y()==-0.07&&player.getDeltaMovement().x()==0.15&&player.fallDistance==0,"scaffold native callback controls climbing and falling");
        for(String id:List.of("drill","diamond_drill","iridium_drill","chainsaw")){
            var item=BuiltInRegistries.ITEM.getValue(IC2.getIdentifier(id));var stack=new ItemStack(item);ElectricItem.manager.charge(stack,10000,Integer.MAX_VALUE,true,false);
            check(stack.getMaxDamage()==0,id+" uses charge without physical tool durability");check(!stack.has(net.minecraft.core.component.DataComponents.ENCHANTABLE),id+" retains original non-enchantability");
            var state=id.equals("chainsaw")?Blocks.COBWEB.defaultBlockState():Blocks.DIRT.defaultBlockState();check(stack.isCorrectToolForDrops(state),id+" native multi-tag or shearing drop rules");
        }
        player.setDeltaMovement(Vec3.ZERO);player.fallDistance=0;level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
        return Map.of("checks",checks,"failures",failures,"mode","server_native_dispatch");
    }
}
