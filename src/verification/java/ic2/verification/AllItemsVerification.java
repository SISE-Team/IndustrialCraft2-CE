package ic2.verification;

import com.google.gson.GsonBuilder;
import ic2.core.ref.Ic2Blocks;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Every registered block item is placed using real client packets before machine operation begins. */
public final class AllItemsVerification {
    public static volatile boolean completed;
    private static final BlockPos POS=new BlockPos(80,101,80);
    private final List<Item> blocks=new ArrayList<>();
    private final List<Map<String,Object>> placements=new ArrayList<>();
    private final Map<String,List<Map<String,Object>>> usage=new TreeMap<>();
    private final List<String> failures=new ArrayList<>();
    public final Map<String,Object> phases=new LinkedHashMap<>();
    private ServerPlayer player;private int index=-1,age,checks;
    private static volatile int current=-1;private static volatile String expectedItem;
    private static volatile boolean ready,used;
    private static volatile String clientError="";
    public void mark(Item item,String operation,String evidence){String id=BuiltInRegistries.ITEM.getKey(item).toString();if(id.startsWith("ic2:"))usage.computeIfAbsent(id,k->new ArrayList<>()).add(Map.of("operation",operation,"evidence",evidence));}
    public boolean hasUsage(Item item){return usage.containsKey(BuiltInRegistries.ITEM.getKey(item).toString());}
    public void check(boolean ok,String label){checks++;if(!ok)failures.add("case "+index+": "+label);}
    private void save(){
        var remaining=BuiltInRegistries.ITEM.keySet().stream().filter(id->id.getNamespace().equals("ic2")&&!usage.containsKey(id.toString())).map(Object::toString).sorted().toList();
        var report=new LinkedHashMap<String,Object>();report.put("registeredItems",BuiltInRegistries.ITEM.keySet().stream().filter(id->id.getNamespace().equals("ic2")).count());report.put("registeredBlockItems",blocks.size());report.put("placements",placements);report.put("usage",usage);report.put("remainingItems",remaining);report.put("checks",checks);report.put("failures",failures);report.put("placementComplete",index>=blocks.size());report.put("complete",completed&&failures.isEmpty()&&remaining.isEmpty());
        report.put("phases",phases);try{Files.writeString(Path.of("ic2-all-items-verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception e){throw new RuntimeException(e);}
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event){if(player!=null||!(event.getEntity() instanceof ServerPlayer actor))return;player=actor;player.setGameMode(GameType.SURVIVAL);player.setInvulnerable(true);
        for(var id:BuiltInRegistries.ITEM.keySet())if(id.getNamespace().equals("ic2")&&BuiltInRegistries.ITEM.getValue(id) instanceof BlockItem)blocks.add(BuiltInRegistries.ITEM.getValue(id));blocks.sort(Comparator.comparing(item->BuiltInRegistries.ITEM.getKey(item).toString()));save();}
    private void next(){
        index++;age=0;ready=false;used=false;clientError="";
        if(index>=blocks.size()){save();finishPlacement();return;}
        var level=player.level();player.closeContainer();player.stopUsingItem();player.teleportTo(80.5,101,78.5);player.setDeltaMovement(Vec3.ZERO);player.getInventory().clearContent();player.getInventory().setSelectedSlot(0);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(blocks.get(index)));player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STICK));
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){for(int y=0;y<=6;y++)level.setBlock(POS.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);level.setBlock(POS.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);}
        var item=(BlockItem)blocks.get(index);var block=item.getBlock();String id=BuiltInRegistries.ITEM.getKey(item).getPath();
        if(item instanceof ic2.core.crop.ItemCrop)level.setBlock(POS.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7),3);
        else if(!block.defaultBlockState().canSurvive(level,POS))for(var support:List.of(Blocks.GRASS_BLOCK,Blocks.DIRT,Blocks.FARMLAND,Blocks.SAND,Blocks.MYCELIUM)){level.setBlock(POS.below(),support.defaultBlockState(),3);if(block.defaultBlockState().canSurvive(level,POS))break;}
        if(id.equals("rubber_sapling"))level.setBlock(POS.below(),Blocks.GRASS_BLOCK.defaultBlockState(),3);
        if(id.equals("reactor_chamber"))level.setBlock(POS.west(),Ic2Blocks.NUCLEAR_REACTOR.defaultBlockState(),3);
        if(Set.of("reactor_access_hatch","reactor_fluid_port","reactor_redstone_port").contains(id)){
            var core=POS.west(2);for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++)if(Math.abs(x)==2||Math.abs(y)==2||Math.abs(z)==2)level.setBlock(core.offset(x,y,z),Ic2Blocks.REACTOR_VESSEL.defaultBlockState(),3);
            level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);level.setBlock(core,Ic2Blocks.NUCLEAR_REACTOR.defaultBlockState(),3);for(var direction:Direction.values())level.setBlock(core.relative(direction),Ic2Blocks.REACTOR_CHAMBER.defaultBlockState(),3);
        }
        for(var entity:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(POS).inflate(8)))entity.discard();
        expectedItem=BuiltInRegistries.ITEM.getKey(blocks.get(index)).toString();current=index;save();
    }
    private void finishPlacement(){
        // Remaining functional/crafting phases populate the same per-item ledger.
        RecipeUsageVerification.run(player,this);
        completed=true;save();
        AllMachinesVerification.ACTIVE.login(new PlayerEvent.PlayerLoggedInEvent(player));
    }
    @SubscribeEvent public void tick(ServerTickEvent.Post event){
        if(player==null||completed)return;if(index<0){if(++age>=40)next();return;}if(index>=blocks.size())return;age++;if(age==20)ready=true;if(age<50)return;
        var item=(BlockItem)blocks.get(index);var state=player.level().getBlockState(POS);boolean placed=state.is(item.getBlock());check(used,"real client placement packet issued");check(clientError.isEmpty(),"client exception "+clientError);check(placed,"block item creates its registered block: "+BuiltInRegistries.ITEM.getKey(item));check(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),"survival placement consumes exactly one item");check(player.getOffhandItem().is(Items.STICK),"unused hand is preserved");
        var row=new LinkedHashMap<String,Object>();row.put("id",BuiltInRegistries.ITEM.getKey(item).toString());row.put("placed",placed);row.put("heldAfter",player.getMainHandItem().toString());row.put("serverCreative",player.getAbilities().instabuild);row.put("used",used);row.put("clientError",clientError);row.put("state",state.toString());var tile=player.level().getBlockEntity(POS);row.put("blockEntity",tile==null?"none":tile.getClass().getName());placements.add(row);if(placed&&clientError.isEmpty())mark(item,"real_client_placement",state.toString());next();
    }
    public static final class Client {
        private boolean hidden;private int active=-1;
        @SubscribeEvent public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){var mc=net.minecraft.client.Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(!ready||completed||active==current||mc.player==null||mc.gameMode==null||mc.level==null)return;if(!BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString().equals(expectedItem))return;active=current;try{mc.setScreen(null);mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(80.5,101,80.5),Direction.UP,POS.below(),false));}catch(Throwable e){clientError=e.toString();}used=true;}
    }
}
