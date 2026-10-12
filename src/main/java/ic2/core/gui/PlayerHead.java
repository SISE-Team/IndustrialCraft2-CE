package ic2.core.gui;

import com.mojang.authlib.GameProfile;
import ic2.core.Ic2Gui;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHead extends ItemImage {
   private static final Map<GameProfile, ItemStack> IMAGE_MAKER = Collections.synchronizedMap(new WeakHashMap<>());
   private final GameProfile player;

   public PlayerHead(Ic2Gui<?> gui, int x, int y, GameProfile player) {
      super(gui, x, y, new PlayerHead.PlayerHeadSupplier(player));
      this.player = player;
   }

   @Override
   protected List<Component> getToolTip() {
      List<Component> tooltip = super.getToolTip();
      if (StringUtils.isNotBlank(this.player.name())) {
         tooltip.add(Component.literal(this.player.name()));
      }

      return tooltip;
   }

   private static final class PlayerHeadSupplier implements Supplier<ItemStack> {
      private final GameProfile profile;

      PlayerHeadSupplier(GameProfile profile) {
         this.profile = profile;
      }

      public ItemStack get() {
         // 26.1.2: SkullBlockEntity.updateGameprofile removed; ResolvableProfile handles resolution.
         CompletableFuture<GameProfile> future = CompletableFuture.completedFuture(this.profile);

         try {
            return PlayerHead.IMAGE_MAKER.computeIfAbsent(future.get(), resolvedProfile -> {
               ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
               ic2.core.util.StackTag.update(skull, ic2Data -> { ic2Data.put("SkullOwner", ic2.core.util.ProfileUtil.write(new CompoundTag(), resolvedProfile)); return ic2Data; });
               return skull;
            });
         } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
         }
      }
   }
}
