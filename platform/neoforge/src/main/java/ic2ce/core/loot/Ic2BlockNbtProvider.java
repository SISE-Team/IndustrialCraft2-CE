package ic2ce.core.loot;

import com.mojang.serialization.MapCodec;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.block.NativeBlockEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.jspecify.annotations.Nullable;

/**
 * Native loot projection reads only the actual matching block entity's independently adjusted
 * physical drop metadata.
 */
public final class Ic2BlockNbtProvider implements NbtProvider {
  public static final Ic2BlockNbtProvider BLOCK_NBT = new Ic2BlockNbtProvider();
  public static final MapCodec<Ic2BlockNbtProvider> MAP_CODEC = MapCodec.unit(BLOCK_NBT);

  @Override
  public MapCodec<? extends NbtProvider> codec() {
    return MAP_CODEC;
  }

  @Override
  public @Nullable Tag get(LootContext context) {
    var state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
    var entity = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (state == null
        || !(entity instanceof NativeBlockEntity tile)
        || tile.getBlockState().getBlock() != state.getBlock()) return null;
    var stack = tile.adjustDrop(state.getBlock().asItem().getDefaultInstance(), false);
    return stack == null || stack.isEmpty() ? null : StackTag.get(stack);
  }
}
