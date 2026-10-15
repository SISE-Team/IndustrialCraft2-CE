package ic2ce.neoforge.world.loot;

import com.mojang.serialization.MapCodec;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

/**
 * Registers IC2's loot NBT providers in the 26.1.2 MapCodec registry
 * ({@code BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE}); loot tables reference
 * them as {@code "type": "ic2:block_nbt"}.
 */
public final class Ic2LootNbtProviderTypes {
   public static final MapCodec<Ic2BlockNbtProvider> BLOCK_NBT = register("block_nbt", Ic2BlockNbtProvider.MAP_CODEC);

   private Ic2LootNbtProviderTypes() {}

   private static <T extends NbtProvider> MapCodec<T> register(String id, MapCodec<T> codec) {
      return ic2ce.neoforge.EnvProxyNeoForge.registerLootProvider(ProjectRuntime.getIdentifier(id), codec);
   }

   /** Called during mod init so the provider codec is registered before datapack load. */
   public static void registerAll() {
   }
}
