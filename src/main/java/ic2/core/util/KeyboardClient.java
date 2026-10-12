package ic2.core.util;

import ic2.core.IC2;
import ic2.core.proxy.SideProxyClient;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class KeyboardClient extends Keyboard {
   private static final String keyCategory = "IC2";
   private final KeyMapping altKey = new KeyMapping("ALT Key", 342, new net.minecraft.client.KeyMapping.Category(ic2.core.IC2.getIdentifier("ic2")));
   private final KeyMapping boostKey = new KeyMapping("Boost Key", 341, new net.minecraft.client.KeyMapping.Category(ic2.core.IC2.getIdentifier("ic2")));
   private final KeyMapping modeSwitchKey = new KeyMapping("Mode Switch Key", 77, new net.minecraft.client.KeyMapping.Category(ic2.core.IC2.getIdentifier("ic2")));
   private final KeyMapping sideinventoryKey = new KeyMapping("Side Inventory Key", 67, new net.minecraft.client.KeyMapping.Category(ic2.core.IC2.getIdentifier("ic2")));
   private final KeyMapping expandinfo = new KeyMapping("Hub Expand Key", 88, new net.minecraft.client.KeyMapping.Category(ic2.core.IC2.getIdentifier("ic2")));
   private static boolean registeredKeys = false;
   private int lastKeyState = 0;

   public KeyboardClient() {
      if (!registeredKeys) {
         registeredKeys = true;
         SideProxyClient.envProxy.registerKeyBinding(this.altKey);
         SideProxyClient.envProxy.registerKeyBinding(this.boostKey);
         SideProxyClient.envProxy.registerKeyBinding(this.modeSwitchKey);
         SideProxyClient.envProxy.registerKeyBinding(this.sideinventoryKey);
         SideProxyClient.envProxy.registerKeyBinding(this.expandinfo);
      }
   }

   @Override
   public void sendKeyUpdate() {
      // 26.1.2: this class may be constructed during registration, before the
      // Minecraft instance exists; always resolve it live instead of caching.
      Minecraft mc = Minecraft.getInstance();
      Set<Keyboard.Key> keys = EnumSet.noneOf(Keyboard.Key.class);
      Screen currentScreen = mc.screen;
      if (currentScreen == null) {
         if (this.altKey.isDown()) {
            keys.add(Keyboard.Key.alt);
         }

         if (this.boostKey.isDown()) {
            keys.add(Keyboard.Key.boost);
         }

         if (mc.options.keyUp.isDown()) {
            keys.add(Keyboard.Key.forward);
         }

         if (this.modeSwitchKey.isDown()) {
            keys.add(Keyboard.Key.modeSwitch);
         }

         if (mc.options.keyJump.isDown()) {
            keys.add(Keyboard.Key.jump);
         }

         if (this.sideinventoryKey.isDown()) {
            keys.add(Keyboard.Key.sideInventory);
         }

         if (this.expandinfo.isDown()) {
            keys.add(Keyboard.Key.hubMode);
         }

         for (Keyboard.IKeyWatcher watcher : this.watchers) {
            watcher.checkForKey(keys);
         }
      }

      int currentKeyState = Keyboard.Key.toInt(keys);
      if (currentKeyState != this.lastKeyState) {
         IC2.network.get(false).initiateKeyUpdate(currentKeyState);
         super.processKeyUpdate(IC2.sideProxy.getPlayerInstance(), currentKeyState);
         this.lastKeyState = currentKeyState;
      }
   }
}
