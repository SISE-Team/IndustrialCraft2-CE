package ic2ce.core.proxy;

import ic2ce.api.tile.IRotorProvider;
import ic2ce.neoforge.audio.AudioService;
import ic2ce.core.util.Keyboard;
import java.io.File;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface SideProxy {
   void preInit();

   void onPostInit();

   AudioService getSoundManager();

   Keyboard getKeyboard();

   boolean isSimulating();

   boolean isRendering();

   void requestTick(boolean var1, Runnable var2);

   void onServerAvailable(MinecraftServer var1);

   void displayError(String var1, Object... var2);

   void displayError(Exception var1, String var2, Object... var3);

   void playSoundSp(SoundEvent var1, SoundSource var2, float var3, float var4);

   void playSoundOnce(Entity var1, SoundEvent var2, float var3, float var4);

   Player getPlayerInstance();

   Level getWorld(MinecraftServer var1, Identifier var2);

   Level getPlayerWorld();

   RecipeManager getRecipeManager();

   File getMinecraftDir();

   void messagePlayer(Player var1, String var2, Object... var3);

   <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> var1);
}
