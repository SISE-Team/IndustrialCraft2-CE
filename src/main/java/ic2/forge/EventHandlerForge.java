package ic2.forge;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.event.RetextureEvent;
import ic2.api.tile.RetexturableBlock;
import ic2.core.IC2;
import ic2.core.event.EventHandler;
import ic2.core.event.TickHandler;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent.Save;
import net.neoforged.neoforge.event.level.LevelEvent.Load;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class EventHandlerForge {
   public EventHandlerForge() {}

   @SubscribeEvent
   public void syncMachineRecipes(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
      event.sendRecipes(net.minecraft.world.item.crafting.RecipeType.CRAFTING, net.minecraft.world.item.crafting.RecipeType.SMELTING);
      event.sendRecipes(net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.entrySet().stream()
         .filter(entry -> entry.getKey().identifier().getNamespace().equals("ic2"))
         .map(java.util.Map.Entry::getValue).toList());
   }

   @SubscribeEvent
   public void serverStart(ServerStartingEvent event) {
      EventHandler.onServerStart(event.getServer());
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public void initializeUuGraph(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
      ic2.core.uu.UuIndex.instance.init(event.getServer().overworld());
   }

   @SubscribeEvent
   public void serverAboutToStart(ServerAboutToStartEvent event) {
      // 26.1.2: holder components are only bound when a world's datapacks load
      // (WorldLoader -> ReloadableServerResources#updateComponentsAndStaticRegistryTags),
      // which completes before the server initializes. IC2's in-code recipe data builds
      // ItemStacks, so it must run here: after component binding, before any level loads.
      EventHandler.onInitCommonSetup();
   }

   @SubscribeEvent
   public void onPlayerLogout(PlayerLoggedOutEvent event) {
      EventHandler.onPlayerLogout(event.getEntity());
   }

   @SubscribeEvent
   public void onWorldLoad(Load event) {
      EventHandler.onWorldLoad((Level)event.getLevel());
   }

   @SubscribeEvent
   public void onWorldUnload(Unload event) {
      EventHandler.onWorldUnload((Level)event.getLevel());
   }

   @SubscribeEvent
   public void onChunkDataLoad(net.neoforged.neoforge.event.level.ChunkDataEvent.Load event) {
      ChunkAccess chunk = event.getChunk();
      if (chunk instanceof LevelChunk levelChunk) {
         EventHandler.onChunkDataLoad(levelChunk);
      }
   }

   @SubscribeEvent
   public void onChunkSave(Save event) {
      ChunkAccess chunk = event.getChunk();
      if (chunk instanceof LevelChunk levelChunk) {
         EventHandler.onChunkSave(levelChunk);
      }
   }

   @SubscribeEvent
   public void onChunkLoad(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
      ChunkAccess chunk = event.getChunk();
      if (chunk instanceof LevelChunk levelChunk) {
         EventHandler.onChunkLoad(levelChunk);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onChunkUnload(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
      ChunkAccess chunk = event.getChunk();
      if (chunk instanceof LevelChunk levelChunk) {
         EventHandler.onChunkUnload(levelChunk);
      }
   }

   @SubscribeEvent
   public void onWorldTick(LevelTickEvent.Pre event) {
      TickHandler.onWorldTickStart(event.getLevel());
   }

   @SubscribeEvent
   public void onWorldTickEnd(LevelTickEvent.Post event) {
      TickHandler.onWorldTickEnd(event.getLevel());
   }

   @SubscribeEvent
   public void onServerTick(ServerTickEvent.Pre event) {
      TickHandler.onServerTick();
   }

   @SubscribeEvent
   public void onPlayerTick(PlayerTickEvent.Pre event) {
      EventHandler.onPlayerTick(event.getEntity());
   }

   @SubscribeEvent
   public void onFinalizeSpawn(FinalizeSpawnEvent event) {
      EventHandler.onLivingSpecialSpawn(event.getEntity());
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public void onLivingFall(LivingFallEvent event) {
      if (EventHandler.onLivingFall(event.getEntity(), (float)event.getDistance())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onPlayerLeftClickEmpty(LeftClickEmpty event) {
      // not cancelable in 26.1.2; still notify the handler
      EventHandler.onEntitySwingHand(event.getEntity(), event.getHand());
   }

   @SubscribeEvent
   public void onPlayerLeftClickBlock(LeftClickBlock event) {
      if (EventHandler.onEntitySwingHand(event.getEntity(), event.getHand())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onEntityInteract(EntityInteract event) {
      if (EventHandler.onEntityInteract(event.getEntity(), event.getHand(), event.getTarget())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
   public void onEntityAttacked(LivingIncomingDamageEvent event) {
      LivingEntity entity = event.getEntity();
      if (!EventHandler.onEntityAttacked(entity, event.getSource(), event.getAmount())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
   public void onAttackEntity(AttackEntityEvent event) {
      if (!EventHandler.onAttackEntity(event.getEntity(), event.getTarget())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onBlockStartBreak(LeftClickBlock event) {
      if (EventHandler.onBlockStartBreak(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace()) == InteractionResult.FAIL) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void beforeBlockBreak(BreakBlockEvent event) {
      Level world = (Level)event.getLevel();
      BlockPos pos = event.getPos();
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (!EventHandler.beforeBlockBreak(world, event.getPlayer(), pos, event.getState(), blockEntity)) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onGetBurnTime(FurnaceFuelBurnTimeEvent event) {
      Item item = event.getItemStack().getItem();
      if (EnvProxyForge.burnTimeRecord.containsKey(item)) {
         event.setBurnTime(EnvProxyForge.burnTimeRecord.get(item));
      }
   }

   @SubscribeEvent
   public void onRetexture(RetextureEvent event) {
      Block block = event.state.getBlock();
      if (block instanceof RetexturableBlock
         && ((RetexturableBlock)block)
            .retexture(
               event.state,
               (Level)event.getLevel(),
               event.pos,
               event.side,
               event.player,
               event.refState,
               event.refVariant,
               event.refSide,
               event.refColorMultipliers
            )) {
         event.applied = true;
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onEnergyTileLoad(EnergyTileLoadEvent event) {
      if (event.getLevel().isClientSide()) {
         IC2.log
            .warn(
               LogCategory.EnergyNet,
               "EnergyTileLoadEvent: posted for %s client-side, aborting",
               Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile))
            );
      } else {
         EnergyNet.instance.addTileUnchecked(event.tile);
      }
   }

   @SubscribeEvent
   public void onEnergyTileUnload(EnergyTileUnloadEvent event) {
      if (event.getLevel().isClientSide()) {
         IC2.log
            .warn(
               LogCategory.EnergyNet,
               "EnergyTileUnloadEvent: posted for %s client-side, aborting",
               Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile))
            );
      } else {
         EnergyNet.instance.removeTile(event.tile);
      }
   }
}
