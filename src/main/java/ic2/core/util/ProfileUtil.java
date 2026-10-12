package ic2.core.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

/** Replaces the removed NbtUtils#readGameProfile/writeGameProfile helpers. */
public final class ProfileUtil {
   private ProfileUtil() {}

   @Nullable
   public static GameProfile read(CompoundTag tag) {
      return ExtraCodecs.STORED_GAME_PROFILE
.codec()
.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag)
         .result().orElse(null);
   }

   public static CompoundTag write(CompoundTag tag, GameProfile profile) {
      ExtraCodecs.STORED_GAME_PROFILE
.codec()
.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, profile)
         .result()
         .ifPresent(encoded -> tag.merge((CompoundTag)encoded));
      return tag;
   }
}
