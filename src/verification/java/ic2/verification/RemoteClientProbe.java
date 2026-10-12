package ic2.verification;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Records real remote-client frames and translations without changing gameplay. */
public final class RemoteClientProbe {
   private final Map<Integer,Integer> menuFrames=new LinkedHashMap<>();
   private final Map<Integer,String> menuTitles=new LinkedHashMap<>();
   private final Map<Integer,Integer> menuClicks=new LinkedHashMap<>();
   private int ticks;
   private boolean languageAudited;
   @SubscribeEvent public void click(net.neoforged.neoforge.client.event.InputEvent.MouseButton.Post event) {
      var mc=Minecraft.getInstance();
      if(event.getButton()==0&&event.getAction()==1&&mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)
         menuClicks.merge(screen.getMenu().containerId,1,Integer::sum);
   }
   @SubscribeEvent public void frame(RenderFrameEvent.Post event) {
      var mc=Minecraft.getInstance();
      if(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen){
         menuFrames.merge(screen.getMenu().containerId,1,Integer::sum);
         menuTitles.put(screen.getMenu().containerId,screen.getTitle().getString());
      }
   }
   @SubscribeEvent public void tick(ClientTickEvent.Post event) {
      var mc=Minecraft.getInstance();ticks++;
      if(mc.player==null||mc.level==null||ticks%10!=0)return;
      if(!languageAudited){auditLanguage(mc);languageAudited=true;}
      Map<String,Object> result=new LinkedHashMap<>();
      result.put("updatedAt",System.currentTimeMillis());result.put("player",mc.player.getName().getString());
      result.put("connected",true);result.put("language",mc.options.languageCode);
      result.put("menuFrames",menuFrames);result.put("menuTitles",menuTitles);
      result.put("menuClicks",menuClicks);
      List<String> loaded=new ArrayList<>();
      for(int i=0;i<104;i++){
         var pos=new BlockPos(168+(i%12)*16,181,168+(i/12)*16);
         if(mc.level.getBlockEntity(pos)!=null)loaded.add(pos.getX()+","+pos.getY()+","+pos.getZ());
      }
      result.put("loadedMachinePositions",loaded);
      result.put("playerY",mc.player.getY());result.put("velocityY",mc.player.getDeltaMovement().y());
      result.put("playerX",mc.player.getX());result.put("playerZ",mc.player.getZ());
      result.put("onGround",mc.player.onGround());result.put("flying",mc.player.getAbilities().flying);
      var magnetizer=mc.level.getBlockEntity(new BlockPos(216,181,232));
      if(magnetizer instanceof ic2.core.block.machine.tileentity.TileEntityMagnetizer te)result.put("magnetizerEU",te.getEnergy());
      result.put("heapUsed",Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
      result.put("heapCommitted",Runtime.getRuntime().totalMemory());
      result.put("fps",mc.getFps());
      var recipeCounts=new TreeMap<String,Integer>();
      for(var id:BuiltInRegistries.RECIPE_TYPE.keySet())if(id.getNamespace().equals("ic2"))
         recipeCounts.put(id.toString(),ic2.core.recipe.v2.RecipeManagerGetter.clientRecipes().byType((net.minecraft.world.item.crafting.RecipeType)BuiltInRegistries.RECIPE_TYPE.getValue(id)).size());
      result.put("recipeCounts",recipeCounts);
      write(Path.of("remote-client-probe.json"),result);
   }
   private void auditLanguage(Minecraft mc){
      Map<String,Object> result=new LinkedHashMap<>();List<String> failures=new ArrayList<>();List<Map<String,Object>> items=new ArrayList<>();
      Set<String> keys=new TreeSet<>();
      try(var input=getClass().getClassLoader().getResourceAsStream("ic2-verification-language-keys.json")){
         if(input!=null)keys.addAll(new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8),java.util.List.class));
      }catch(Exception error){failures.add("key manifest: "+error);}
      for(var id:BuiltInRegistries.ITEM.keySet())if(id.getNamespace().equals("ic2")){
         var item=BuiltInRegistries.ITEM.getValue(id);String key=item.getDescriptionId();keys.add(key);
         if(key.startsWith("item.ic2."))keys.add(key.replace("item.ic2.","container.ic2."));
         var row=new LinkedHashMap<String,Object>();row.put("id",id.toString());row.put("key",key);
         try{
            var stack=new ItemStack(item);row.put("name",stack.getHoverName().getString());
            var lines=stack.getTooltipLines(Item.TooltipContext.of(mc.level,mc.player),mc.player,TooltipFlag.NORMAL).stream().map(t->t.getString()).toList();row.put("tooltip",lines);
            for(String text:lines)if(text.startsWith("Format error:")||text.matches(".*\\b(?:ic2|item\\.ic2|block\\.ic2|container\\.ic2)\\.[A-Za-z][A-Za-z0-9_.]*.*"))failures.add(id+": "+text);
         }catch(Throwable error){failures.add(id+": "+error);}
         items.add(row);
      }
      for(var id:BuiltInRegistries.BLOCK.keySet())if(id.getNamespace().equals("ic2"))keys.add(BuiltInRegistries.BLOCK.getValue(id).getDescriptionId());
      int variants=0;
      int cropCards=ic2.api.crops.Crops.instance.getCrops().size();
      if(cropCards!=37)failures.add("remote client crop-card catalog: expected 37, got "+cropCards);
      for(var id:BuiltInRegistries.ITEM.keySet())if(id.getNamespace().equals("ic2")){
         var item=BuiltInRegistries.ITEM.getValue(id);var stacks=new ArrayList<ItemStack>();
         try{
            item.getClass().getMethod("addCreativeVariants",java.util.function.Consumer.class)
               .invoke(item,(java.util.function.Consumer<ItemStack>)stacks::add);
         }catch(NoSuchMethodException ignored){}catch(Exception error){failures.add(id+" creative variants: "+error);}
         for(var stack:stacks){
            variants++;
            try{
               for(var line:stack.getTooltipLines(Item.TooltipContext.of(mc.level,mc.player),mc.player,TooltipFlag.NORMAL)){
                  String value=line.getString();if(value.startsWith("Format error:")||value.matches(".*\\b(?:ic2|item\\.ic2|block\\.ic2|container\\.ic2)\\.[A-Za-z][A-Za-z0-9_.]*.*"))failures.add(id+" creative variant: "+value);
               }
            }catch(Throwable error){failures.add(id+" creative tooltip: "+error);}
         }
      }
      var memory=new ItemStack(ic2.core.ref.Ic2Items.CRYSTAL_MEMORY);
      ((ic2.core.item.ItemCrystalMemory)memory.getItem()).writecontentsTag(mc.level,memory,new ItemStack(net.minecraft.world.item.Items.IRON_INGOT,3));
      var recorded=((ic2.core.item.ItemCrystalMemory)memory.getItem()).readItemStack(memory);
      if(!recorded.is(net.minecraft.world.item.Items.IRON_INGOT)||recorded.getCount()!=3)failures.add("remote client crystal-memory NBT cannot restore stored item");
      var chatChecks=new ArrayList<Map<String,Object>>();
      try{
         var field=net.minecraft.client.gui.components.ChatComponent.class.getDeclaredField("allMessages");field.setAccessible(true);
         for(String key:List.of("ic2.nightvision.enabled","ic2.nightvision.disabled","ic2.frequency_transmitter.unlinked","ic2.EUStorage.gui.mod.redstone0","ic2.blockChargepad.gui.mod.redstone0")){
            ic2.core.IC2.sideProxy.messagePlayer(mc.player,key);
            var messages=(List<net.minecraft.client.multiplayer.chat.GuiMessage>)field.get(mc.gui.getChat());String actual=messages.getFirst().content().getString();
            String expected=ic2.core.init.Localization.translate(key);boolean passed=actual.equals(expected);
            chatChecks.add(Map.of("key",key,"actual",actual,"expected",expected,"passed",passed));if(!passed)failures.add("local IC2 chat untranslated: "+key);
         }
      }catch(Exception error){failures.add("client chat translation probe: "+error);}
      for(String key:keys)if(!Language.getInstance().has(key))failures.add("missing runtime key: "+key);
      result.put("language",mc.options.languageCode);result.put("runtimeKeys",keys.size());result.put("items",items);result.put("failures",failures);
      result.put("creativeVariants",variants);result.put("remoteCrystalMemoryRoundtrip",recorded.is(net.minecraft.world.item.Items.IRON_INGOT)&&recorded.getCount()==3);
      result.put("cropCards",cropCards);
      result.put("clientChatChecks",chatChecks);
      result.put("complete",failures.isEmpty());write(Path.of("runtime-language-audit.json"),result);
   }
   private void write(Path path,Object data){
      try{
         Path temporary=path.resolveSibling(path.getFileName()+".tmp");
         Files.writeString(temporary,new GsonBuilder().setPrettyPrinting().create().toJson(data));
         Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
      }catch(java.io.IOException transientFileLock){
         // Windows readers can briefly prevent replacement; the next tick retries.
      }
   }
}
