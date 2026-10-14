package ic2ce.core.util;

import com.mojang.authlib.GameProfile;
import java.util.Objects;
import net.minecraft.nbt.*;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * Native saved profiles replace identity fields atomically and preserve unrelated surrounding owner
 * data.
 */
public final class ProfileUtil {
  private ProfileUtil() {}

  @Nullable
  public static GameProfile read(CompoundTag data) {
    return data == null
        ? null
        : ExtraCodecs.STORED_GAME_PROFILE
            .codec()
            .parse(NbtOps.INSTANCE, data)
            .result()
            .orElse(null);
  }

  public static CompoundTag write(CompoundTag target, GameProfile profile) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(profile);
    var encoded =
        ExtraCodecs.STORED_GAME_PROFILE.codec().encodeStart(NbtOps.INSTANCE, profile).getOrThrow();
    if (!(encoded instanceof CompoundTag snapshot))
      throw new IllegalStateException("Native profile codec produced a noncompound snapshot");
    // Native profile format keys are inspected as data; no legacy Java implementation is embedded.
    target.remove("id");
    target.remove("name");
    target.remove("properties");
    target.remove("Id");
    target.remove("Name");
    target.remove("Properties");
    target.merge(snapshot.copy());
    return target;
  }
}
