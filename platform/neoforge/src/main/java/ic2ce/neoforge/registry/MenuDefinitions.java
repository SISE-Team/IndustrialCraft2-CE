package ic2ce.neoforge.registry;

import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.agriculture.AgricultureMenus;
import ic2ce.neoforge.crafting.CraftingMenus;
import ic2ce.neoforge.energy.EnergyMenus;
import ic2ce.neoforge.handheld.HandheldMenus;
import ic2ce.neoforge.item.upgrade.UpgradeMenus;
import ic2ce.neoforge.kinetic.KineticMenus;
import ic2ce.neoforge.menu.document.DocumentMenus;
import ic2ce.neoforge.mining.MiningMenus;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.personal.PersonalMenus;
import ic2ce.neoforge.power.PowerMenus;
import ic2ce.neoforge.processing.ProcessingMenus;
import ic2ce.neoforge.reactor.ReactorMenus;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.thermal.ThermalMenus;
import ic2ce.neoforge.transport.TransportMenus;
import ic2ce.neoforge.utility.UtilityMenus;
import java.io.IOException;
import java.util.function.BiFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MenuDefinitions {
   /** Loads every domain menu holder. */
   public static void init() {
      DocumentMenus.init();
      PowerMenus.init();
      ThermalMenus.init();
      KineticMenus.init();
      ReactorMenus.init();
      ProcessingMenus.init();
      TransportMenus.init();
      UtilityMenus.init();
      MiningMenus.init();
      AgricultureMenus.init();
      PersonalMenus.init();
      EnergyMenus.init();
      CraftingMenus.init();
      UpgradeMenus.init();
      HandheldMenus.init();
   }

   public static <T extends AbstractContainerMenu> MenuType<T> register(String name, BiFunction<Integer, Inventory, T> factory) {
      return EnvProxy.get().registerScreenHandler(ProjectIds.id(name), factory);
   }

   private static <T extends AbstractContainerMenu> MenuType<T> registerExtended(String name, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory) {
      return EnvProxy.get().registerExtendedScreenHandler(ProjectIds.id(name), factory);
   }

   public static <T extends AbstractContainerMenu> MenuType<T> registerManagedBe(String name){return ic2ce.neoforge.menu.ManagedMenus.block(name);}

   public static <T extends AbstractContainerMenu> MenuType<T> registerManagedItem(String name){return ic2ce.neoforge.menu.ManagedMenus.item(name);}

   public static void writeManagedBeData(BlockEntity entity,GrowingBuffer buffer)throws IOException{ic2ce.neoforge.menu.ManagedMenus.writeBlock(entity,buffer);}

   public static void writeManagedItemData(Player player,InteractionHand hand,Integer nestedId,GrowingBuffer buffer){ic2ce.neoforge.menu.ManagedMenus.writeItem(player,hand,nestedId,buffer);}
}
