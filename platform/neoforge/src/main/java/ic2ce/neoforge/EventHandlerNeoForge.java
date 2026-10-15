package ic2ce.neoforge;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.energy.event.*;
import ic2ce.api.event.RetextureEvent;
import ic2ce.api.item.ElectricItem;
import ic2ce.api.tile.RetexturableBlock;
import ic2ce.core.event.*;
import ic2ce.neoforge.item.armor.*;
import ic2ce.neoforge.item.tool.NanoBlade;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.*;

/**
 * Native event adaptation preserves cancellation and commits gameplay costs only at the appropriate
 * native phase.
 */
public final class EventHandlerNeoForge {
  @SubscribeEvent
  public void tagsUpdated(TagsUpdatedEvent event) {
    ic2ce.neoforge.recipe.processing.OrderedMachineRecipes.invalidateTagCandidates();
    ic2ce.neoforge.recipe.input.FluidContentsInput.invalidateContainers();
  }

  @SubscribeEvent
  public void syncMachineRecipes(OnDatapackSyncEvent event) {
    event.sendRecipes(
        net.minecraft.world.item.crafting.RecipeType.CRAFTING,
        net.minecraft.world.item.crafting.RecipeType.SMELTING);
    event.sendRecipes(
        BuiltInRegistries.RECIPE_TYPE.entrySet().stream()
            .filter(entry -> entry.getKey().identifier().getNamespace().equals("ic2"))
            .map(Map.Entry::getValue)
            .toList());
  }

  @SubscribeEvent
  public void serverStart(ServerStartingEvent event) {
    EventHandler.onServerStart(event.getServer());
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void initializeUuGraph(ServerStartedEvent event) {
    ic2ce.neoforge.processing.uu.UuIndex.instance.init(event.getServer().overworld());
  }

  @SubscribeEvent
  public void beforeServer(ServerAboutToStartEvent event) {
    EventHandler.onInitCommonSetup();
  }

  @SubscribeEvent
  public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
    EventHandler.onPlayerLogout(event.getEntity());
  }

  @SubscribeEvent
  public void worldLoad(LevelEvent.Load event) {
    if (event.getLevel() instanceof Level level) EventHandler.onWorldLoad(level);
  }

  @SubscribeEvent
  public void worldUnload(LevelEvent.Unload event) {
    if (event.getLevel() instanceof Level level) EventHandler.onWorldUnload(level);
  }

  @SubscribeEvent
  public void chunkDataLoad(ChunkDataEvent.Load event) {
    if (event.getChunk() instanceof LevelChunk chunk) EventHandler.onChunkDataLoad(chunk);
  }

  @SubscribeEvent
  public void chunkSave(ChunkDataEvent.Save event) {
    if (event.getChunk() instanceof LevelChunk chunk) EventHandler.onChunkSave(chunk);
  }

  @SubscribeEvent
  public void chunkLoad(ChunkEvent.Load event) {
    if (event.getChunk() instanceof LevelChunk chunk) EventHandler.onChunkLoad(chunk);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void chunkUnload(ChunkEvent.Unload event) {
    if (event.getChunk() instanceof LevelChunk chunk) EventHandler.onChunkUnload(chunk);
  }

  @SubscribeEvent
  public void worldTick(LevelTickEvent.Pre event) {
    TickHandler.onWorldTickStart(event.getLevel());
  }

  @SubscribeEvent
  public void worldTickEnd(LevelTickEvent.Post event) {
    TickHandler.onWorldTickEnd(event.getLevel());
  }

  @SubscribeEvent
  public void syncCropChunk(ChunkWatchEvent.Sent event) {
    if (event.getPlayer().level() != event.getLevel()) return;
    for (var tile : event.getChunk().getBlockEntities().values())
      if (tile instanceof ic2ce.neoforge.agriculture.CropPlot && !tile.isRemoved())
        ic2ce.neoforge.runtime.ProjectRuntime.network
            .get(true)
            .sendInitialData(tile, event.getPlayer());
  }

  @SubscribeEvent
  public void serverTick(ServerTickEvent.Pre event) {
    TickHandler.onServerTick();
  }

  @SubscribeEvent
  public void playerTick(PlayerTickEvent.Pre event) {
    EventHandler.onPlayerTick(event.getEntity());
  }

  @SubscribeEvent
  public void spawn(FinalizeSpawnEvent event) {
    EventHandler.onLivingSpecialSpawn(event.getEntity());
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void fall(LivingFallEvent event) {
    if (EventHandler.onLivingFall(event.getEntity(), (float) event.getDistance()))
      event.setCanceled(true);
  }

  @SubscribeEvent
  public void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
    EventHandler.onEntitySwingHand(event.getEntity(), event.getHand());
  }

  @SubscribeEvent
  public void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    if (EventHandler.onEntitySwingHand(event.getEntity(), event.getHand())
        || EventHandler.onBlockStartBreak(
                event.getEntity(),
                event.getLevel(),
                event.getHand(),
                event.getPos(),
                event.getFace())
            == InteractionResult.FAIL) event.setCanceled(true);
  }

  @SubscribeEvent
  public void entityInteract(PlayerInteractEvent.EntityInteract event) {
    if (EventHandler.onEntityInteract(event.getEntity(), event.getHand(), event.getTarget()))
      event.setCanceled(true);
  }

  @SubscribeEvent
  public void attack(AttackEntityEvent event) {
    if (!EventHandler.onAttackEntity(event.getEntity(), event.getTarget())) event.setCanceled(true);
  }

  @SubscribeEvent
  public void beforeBlockBreak(BreakBlockEvent event) {
    if (event.getLevel() instanceof Level level
        && !EventHandler.beforeBlockBreak(
            level,
            event.getPlayer(),
            event.getPos(),
            event.getState(),
            level.getBlockEntity(event.getPos()))) event.setCanceled(true);
  }

  @SubscribeEvent
  public void fuel(FurnaceFuelBurnTimeEvent event) {
    var ticks = EnvProxyNeoForge.burnTimeRecord.get(event.getItemStack().getItem());
    if (ticks != null) event.setBurnTime(ticks);
  }

  @SubscribeEvent
  public void retexture(RetextureEvent event) {
    if (event.getLevel() instanceof Level level
        && event.state.getBlock() instanceof RetexturableBlock block
        && block.retexture(
            event.state,
            level,
            event.pos,
            event.side,
            event.player,
            event.refState,
            event.refVariant,
            event.refSide,
            event.refColorMultipliers)) {
      event.applied = true;
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void energyLoad(EnergyTileLoadEvent event) {
    if (!event.getLevel().isClientSide()) EnergyNet.instance.addTileUnchecked(event.tile);
  }

  @SubscribeEvent
  public void energyUnload(EnergyTileUnloadEvent event) {
    if (!event.getLevel().isClientSide()) EnergyNet.instance.removeTile(event.tile);
  }

  @SubscribeEvent
  public void completedDamage(LivingDamageEvent.Post event) {
    if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return;
    float applied =
        event.getInflictedDamage()
            + event.getReduction(DamageContainer.Reduction.ARMOR)
            + event.getReduction(DamageContainer.Reduction.ENCHANTMENTS)
            + event.getReduction(DamageContainer.Reduction.MOB_EFFECTS);
    if (Float.isFinite(applied) && applied > 0)
      EventHandler.onDamageApplied(event.getEntity(), event.getSource(), applied);
  }

  @SubscribeEvent
  public void itemAttributes(ItemAttributeModifierEvent event) {
    var stack = event.getItemStack();
    if (stack.getItem() instanceof ChargedArmor armor
        && ElectricItem.manager.canUse(stack, armor.getEnergyPerDamage())) {
      int[] protection =
          switch (armor) {
            case NanoArmor ignored -> NanoArmor.CHARGED_PROTECTION;
            case QuantumArmor ignored -> QuantumArmor.CHARGED_PROTECTION;
            default -> null;
          };
      if (protection != null) {
        var slot = armor.getArmorSlot();
        event.replaceModifier(
            Attributes.ARMOR,
            new AttributeModifier(
                ProjectRuntime.getIdentifier("charged_armor/" + slot.getName()),
                protection[slot.getIndex()],
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.bySlot(slot));
      }
    }
    if (stack.getItem() instanceof NanoBlade blade) {
      event.removeModifier(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID);
      event.removeModifier(Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID);
      for (var entry : blade.getDefaultAttributeModifiers(stack).modifiers())
        event.replaceModifier(entry.attribute(), entry.modifier(), entry.slot());
    }
  }
}
