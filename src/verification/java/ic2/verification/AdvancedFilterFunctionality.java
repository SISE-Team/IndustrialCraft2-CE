package ic2.verification;

import static ic2.verification.AllMachinesVerification.*;
import ic2.api.item.ElectricItem;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.util.StackTag;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Real transfers for damage, NBT, charge equality, comparison and range settings. */
final class AdvancedFilterFunctionality {
   record Case(String mode,String direction,Ic2TileEntity parent,InvSlot source,InvSlot recipient,ItemStack upgrade,ItemStack sample,boolean expected){}
   private final ExtendedValidation e;private final AllMachinesVerification s;
   private final List<Case> cases=new ArrayList<>();private final List<Map<String,Object>> results=new ArrayList<>();
   private long preparedAt;private int age;
   AdvancedFilterFunctionality(ExtendedValidation owner){e=owner;s=owner.s;}
   private ItemStack battery(double charge){var stack=stack("re_battery",1);ElectricItem.manager.charge(stack,charge,1,true,false);return stack;}
   void prepare(){
      preparedAt=System.nanoTime();int n=0;
      for(String direction:List.of("eject","pull"))for(String mode:List.of("ignored","damage_match","damage_reject","fuzzy_charge_ignored","exact_charge_reject","exact_match","energy_match","energy_reject","comparison_match","comparison_reject","range_match","range_reject")){
         var pos=new BlockPos(2048+(n%8)*8,201,4096+(n/8)*8);n++;
         for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){s.level.setChunkForced((pos.getX()>>4)+x,(pos.getZ()>>4)+z,true);s.level.getChunk((pos.getX()>>4)+x,(pos.getZ()>>4)+z);}
         var parent=s.helper(direction.equals("eject")?"item_buffer":"recycler",pos);var box=s.helper("iron_storage_box",pos.east());var external=(InvSlot)get(box,"inventory");
         var internal=(InvSlot)get(parent,direction.equals("eject")?"rightcontentSlot":"inputSlot");
         var filter=new ItemStack(Items.IRON_PICKAXE);filter.setDamageValue(10);var sample=filter.copy();boolean expected=!mode.endsWith("reject");var settings=new CompoundTag();
         if(mode.equals("ignored")){sample.setDamageValue(20);expected=true;}
         if(mode.startsWith("damage")){var meta=new CompoundTag();meta.putBoolean("active",true);meta.putByte("type",(byte)1);settings.put("metaSettings",meta);if(mode.endsWith("reject"))sample.setDamageValue(20);}
         if(mode.contains("charge")||mode.startsWith("energy")||mode.startsWith("comparison")||mode.startsWith("range")){
            filter=battery(5000);sample=battery(mode.endsWith("reject")?7000:5000);
         }
         if(mode.startsWith("fuzzy")||mode.startsWith("exact")){var tag=new CompoundTag();tag.putByte("type",(byte)(mode.startsWith("fuzzy")?1:2));settings.put("nbtSettings",tag);if(mode.startsWith("fuzzy"))sample=battery(7000);}
         if(mode.startsWith("energy")||mode.startsWith("comparison")||mode.startsWith("range")){
            var energy=new CompoundTag();energy.putBoolean("active",true);energy.putByte("type",(byte)(mode.startsWith("energy")?1:mode.startsWith("comparison")?2:3));
            if(mode.startsWith("comparison")){energy.putString("normal","4000");energy.putByte("normalComp",(byte)0);sample=battery(mode.endsWith("reject")?3000:5000);}
            if(mode.startsWith("range")){energy.putString("normal","4000");energy.putByte("normalComp",(byte)0);energy.putString("extra","6000");energy.putByte("extraComp",(byte)0);}
            settings.put("energySettings",energy);
         }
         var list=new ListTag();var encoded=new CompoundTag();StackTag.save(s.level.registryAccess(),filter,encoded);encoded.putByte("Slot",(byte)0);list.add(encoded);settings.put("Items",list);settings.putByte("dir",(byte)(Direction.EAST.ordinal()+1));
         var upgrade=stack(direction.equals("eject")?"advanced_ejector_upgrade":"advanced_pulling_upgrade",1);StackTag.set(upgrade,settings);
         cases.add(new Case(mode,direction,parent,direction.equals("eject")?internal:external,direction.equals("eject")?external:internal,upgrade,sample,expected));
      }
   }
   boolean tick(){
      if(age==0){if(System.nanoTime()-preparedAt<10_000_000_000L)return false;
         for(var test:cases){var upgrade=(InvSlotUpgrade)((TileEntityInventory)test.parent).getInventorySlot("upgrade");e.check(test.direction+" "+test.mode+": compatible real upgrade slot",upgrade.accepts(test.upgrade));test.source.put(test.sample.copy());upgrade.put(0,test.upgrade);upgrade.onChanged();test.parent.setChanged();}
      }
      age++;if(age<150)return false;
      for(var test:cases){boolean moved=!test.recipient.isEmpty();int total=0;for(var item:test.source)total+=item.getCount();for(var item:test.recipient)total+=item.getCount();boolean passed=moved==test.expected&&total==1;
         e.check(test.direction+" actual advanced filter "+test.mode+" and exact item conservation",passed);results.add(Map.of("direction",test.direction,"mode",test.mode,"expectedTransfer",test.expected,"actualTransfer",moved,"conservedItems",total,"passed",passed));
         s.level.setBlockAndUpdate(test.parent.getBlockPos(),Blocks.AIR.defaultBlockState());s.level.setBlockAndUpdate(test.parent.getBlockPos().east(),Blocks.AIR.defaultBlockState());
      }
      e.details.put("advancedFilterCases",results);return true;
   }
}
