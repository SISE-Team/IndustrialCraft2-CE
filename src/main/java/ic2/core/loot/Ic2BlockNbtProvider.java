package ic2.core.loot;

import com.mojang.serialization.MapCodec;
import ic2.core.block.tileentity.Ic2TileEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Loot NBT source exposing the IC2 tile-entity drop adjustment (custom name,
 * lock, charge...) as the {@code ic2:block_nbt} provider in loot tables.
 *
 * <p>26.1.2 replaced the old Serializer/LootNbtProviderType registration with
 * MapCodecs in {@code BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE}; this is a
 * constant provider, so a unit codec suffices.</p>
 */
public class Ic2BlockNbtProvider implements NbtProvider {
   public static final Ic2BlockNbtProvider BLOCK_NBT = new Ic2BlockNbtProvider();
   public static final MapCodec<Ic2BlockNbtProvider> MAP_CODEC = MapCodec.unit(BLOCK_NBT);

   @Override
   public MapCodec<? extends NbtProvider> codec() {
      return MAP_CODEC;
   }

   @Nullable
   @Override
   public Tag get(LootContext context) {
      BlockEntity blockEntity = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
      if (state == null) {
         return null;
      } else {
         if (blockEntity instanceof Ic2TileEntity tileEntity) {
            ItemStack stack = tileEntity.adjustDrop(state.getBlock().asItem().getDefaultInstance(), false);
            if (ic2.core.util.StackTag.has(stack)) {
               return ic2.core.util.StackTag.get(stack);
            }
         }

         return null;
      }
   }
}
