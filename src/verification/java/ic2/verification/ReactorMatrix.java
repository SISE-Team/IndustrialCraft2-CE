package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.api.reactor.IReactorComponent;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.item.reactor.AbstractDamageableReactorComponent;
import ic2.core.item.reactor.ItemReactorUranium;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Actual placed reactors: component pairs, rotation, heat, and worn inputs. */
final class ReactorMatrix {
   record Case(String first,String second,int heat,boolean vertical,boolean worn){}
   private final ExtendedValidation e;private final AllMachinesVerification s;
   private final List<Case> cases=new ArrayList<>();
   private final List<BlockPos> positions=new ArrayList<>();
   private final List<Map<String,Object>> results=new ArrayList<>();
   private final Map<BlockPos,Case> running=new LinkedHashMap<>();
   private final Map<BlockPos,TileEntityNuclearReactorElectric> reactors=new LinkedHashMap<>();
   private int age,index;
   ReactorMatrix(ExtendedValidation owner){e=owner;s=owner.s;}
   void prepare(){
      List<String> items=BuiltInRegistries.ITEM.keySet().stream().filter(id->id.getNamespace().equals("ic2")&&BuiltInRegistries.ITEM.getValue(id) instanceof IReactorComponent).map(id->id.getPath()).sorted().toList();
      e.details.put("reactorComponentTypes",items);
      for(int a=0;a<items.size();a++)for(int b=a;b<items.size();b++)
         for(int heat:List.of(0,5000,9000))for(boolean vertical:List.of(false,true))for(boolean worn:List.of(false,true))cases.add(new Case(items.get(a),items.get(b),heat,vertical,worn));
      e.details.put("reactorPairScenarios",cases.size());
      for(int n=0;n<24;n++){
         // IC2's original nuclear rays pass through unbreakable bedrock. Keep
         // each real reactor beyond the maximum configured blast radius.
         var pos=new BlockPos(1024+(n%6)*256,181,1024+(n/6)*256);positions.add(pos);
         for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)s.level.setChunkForced((pos.getX()>>4)+x,(pos.getZ()>>4)+z,true);
         for(int x=-4;x<=4;x++)for(int y=-4;y<=4;y++)for(int z=-4;z<=4;z++)s.level.setBlockAndUpdate(pos.offset(x,y,z),Math.abs(x)==4||Math.abs(y)==4||Math.abs(z)==4?Blocks.BEDROCK.defaultBlockState():Blocks.AIR.defaultBlockState());
      }
   }
   private ItemStack component(String id,boolean worn){
      var item=stack(id,1);
      if(worn&&item.getItem() instanceof AbstractDamageableReactorComponent damageable)damageable.setUse(item,(int)(((Number)get(damageable,"maxUse")).intValue()*0.9));
      return item;
   }
   boolean tick(){
      age++;
      if(age%100==1){
         if(index>=cases.size()){e.details.put("reactorCases",results);return true;}
         running.clear();reactors.clear();
         for(var pos:positions){
            if(index>=cases.size())break;
            var test=cases.get(index++);running.put(pos,test);
            // The shell contains deliberate hot-reactor failures in this world.
            for(Direction dir:Direction.values())s.level.setBlockAndUpdate(pos.relative(dir),Blocks.AIR.defaultBlockState());
            var reactor=(TileEntityNuclearReactorElectric)s.helper("nuclear_reactor",pos);
            for(Direction dir:Direction.values())s.helper("reactor_chamber",pos.relative(dir));
            reactor.setItemAt(2,2,component(test.first,test.worn));
            reactor.setItemAt(test.vertical?2:3,test.vertical?3:2,component(test.second,test.worn));
            reactor.setHeat(test.heat);reactor.setChanged();
            s.level.setBlockAndUpdate(pos.above(2),Blocks.REDSTONE_BLOCK.defaultBlockState());
            reactors.put(pos,reactor);
         }
      }
      if(age%100==85){
         for(var entry:running.entrySet()){
            var pos=entry.getKey();var test=entry.getValue();var reactor=reactors.get(pos);
            boolean exists=s.level.getBlockEntity(pos)==reactor;
            var result=new LinkedHashMap<String,Object>();result.put("first",test.first);result.put("second",test.second);result.put("initialHeat",test.heat);result.put("vertical",test.vertical);result.put("worn",test.worn);result.put("meltdown",!exists);
            String label=test.toString();
            result.put("finalHeat",reactor.getHeat());result.put("finalMaxHeat",reactor.getMaxHeat());
            if(exists){
               e.check(label+": valid finite output and nonnegative core heat",Float.isFinite(reactor.output)&&reactor.output>=0&&reactor.getHeat()>=0);
               e.check(label+": coolant volume within tank bounds",reactor.inputTank.getFluidAmount()>=0&&reactor.inputTank.getFluidAmount()<=reactor.inputTank.getCapacity()&&reactor.outputTank.getFluidAmount()>=0&&reactor.outputTank.getFluidAmount()<=reactor.outputTank.getCapacity());
               for(int x=0;x<9;x++)for(int y=0;y<6;y++){
                  var item=reactor.getItemAt(x,y);
                  if(item!=null&&!item.isEmpty()&&item.getItem() instanceof AbstractDamageableReactorComponent component){
                     var tag=ic2.core.util.StackTag.get(item);int wear=tag==null?0:tag.getIntOr("use",0);int limit=((Number)get(component,"maxUse")).intValue();
                     e.check(label+": component wear in bounds at "+x+","+y,wear>=0&&wear<=limit);
                  }
               }
               result.put("outputEU",reactor.getOfferedEnergy());result.put("finalHeat",reactor.getHeat());
               var first=stack(test.first,1).getItem();var second=stack(test.second,1).getItem();
               if(!test.worn&&test.heat==0&&first instanceof ItemReactorUranium a&&second instanceof ItemReactorUranium b&&!(a instanceof ic2.core.item.reactor.ItemReactorMOX)&&!(b instanceof ic2.core.item.reactor.ItemReactorMOX)){
                  double expected=5.0*(a.numberOfCells*(1+a.numberOfCells/2)+b.numberOfCells*(1+b.numberOfCells/2)+a.numberOfCells+b.numberOfCells);
                  e.check(label+": adjacent uranium rods match independent pulse output",Math.abs(reactor.getOfferedEnergy()-expected)<0.001);result.put("expectedEU",expected);
               }
            }else e.check(label+": meltdown only from seeded hot operating reactor",test.heat>=5000);
            e.check(label+": outer bedrock containment remains",s.level.getBlockState(pos.east(4)).is(Blocks.BEDROCK)&&s.level.getBlockState(pos.west(4)).is(Blocks.BEDROCK));
            s.level.setBlockAndUpdate(pos.above(2),Blocks.AIR.defaultBlockState());results.add(result);
         }
      }
      if(age%100==99){for(var pos:positions){s.level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());for(Direction dir:Direction.values())s.level.setBlockAndUpdate(pos.relative(dir),Blocks.AIR.defaultBlockState());}}
      return false;
   }
}
