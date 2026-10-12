package ic2.verification;

import ic2.core.Ic2Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Confirms actual client screens survive multiple render frames, not just menu construction. */
public final class ClientUiVerification {
   @SubscribeEvent
   public void clientTick(ClientTickEvent.Post event) {
      var minecraft=Minecraft.getInstance();
      if(Boolean.getBoolean("ic2.verifyAllMachines")&&AllMachinesVerification.ACTOR_ENERGY_BLOCK_POS!=null&&minecraft.player!=null&&minecraft.level!=null){
         var tile=minecraft.level.getBlockEntity(AllMachinesVerification.ACTOR_ENERGY_BLOCK_POS);
         var data=AllMachinesVerification.ACTOR_CLIENT_STATE;
         data.put("playerY",minecraft.player.getY());data.put("velocityY",minecraft.player.getDeltaMovement().y());
         data.put("onGround",minecraft.player.onGround());data.put("flying",minecraft.player.getAbilities().flying);
         if(tile instanceof ic2.core.block.machine.tileentity.TileEntityMagnetizer machine)data.put("magnetizerEU",machine.getEnergy());
      }
      if (Boolean.getBoolean("ic2.verifyAllMachines") && AllMachinesVerification.CLIENT_GUI_POS != null
         && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(AllMachinesVerification.CLIENT_GUI_POS) != null) {
         AllMachinesVerification.CLIENT_GUI_READY = true;
      }
      if (Boolean.getBoolean("ic2.verifyAllMachines") && AllMachinesVerification.CLIENT_GUI_CASE != null
         && Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen
         && screen.getMenu().containerId == AllMachinesVerification.CLIENT_GUI_CONTAINER_ID) {
         AllMachinesVerification.GUI_TICKS.merge(AllMachinesVerification.CLIENT_GUI_CASE, 1, Integer::sum);
      }
      if (Minecraft.getInstance().screen instanceof Ic2Gui<?> gui && gui.getContainer().base instanceof BlockEntity tile) {
         String id = BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock()).getPath();
         MigrationVerification.CLIENT_GUI_TICKS.merge(id, 1, Integer::sum);
      }
   }
}
