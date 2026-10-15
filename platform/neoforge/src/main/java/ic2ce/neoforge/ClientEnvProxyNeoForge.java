package ic2ce.neoforge;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.core.proxy.ClientEnvProxy;
import ic2ce.neoforge.client.text.LocalizedText;
import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.*;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Independently owned registration records replay current native client APIs without exposing
 * caller-owned arrays.
 */
public final class ClientEnvProxyNeoForge implements ClientEnvProxy {
  record ScreenRegistration<H extends AbstractContainerMenu>(
      MenuType<H> type, ScreenFactory<H> factory) {
    ScreenRegistration {
      Objects.requireNonNull(type);
      Objects.requireNonNull(factory);
    }
  }

  record BlockTintRegistration(List<BlockTintSource> provider, Block... blocks) {
    BlockTintRegistration {
      provider = List.copyOf(provider);
      blocks = blocks.clone();
    }

    @Override
    public Block[] blocks() {
      return blocks.clone();
    }
  }

  record ItemTintRegistration(Identifier id, ItemTintSource provider) {}

  record BerRegistration<E extends BlockEntity, S extends BlockEntityRenderState>(
      BlockEntityType<E> type, BlockEntityRendererProvider<E, S> factory) {}

  record EntityRendererRegistration<E extends Entity>(
      EntityType<? extends E> type, EntityRendererProvider<E> factory) {}

  static final List<ScreenRegistration<?>> screenRegistrations = new CopyOnWriteArrayList<>();
  static final List<BlockTintRegistration> blockTintRegistrations = new CopyOnWriteArrayList<>();
  static final List<ItemTintRegistration> itemTintRegistrations = new CopyOnWriteArrayList<>();
  static final List<KeyMapping> keyBindingRegistrations = new CopyOnWriteArrayList<>();
  static final List<BerRegistration<?, ?>> berRegistrations = new CopyOnWriteArrayList<>();
  static final List<EntityRendererRegistration<?>> entityRendererRegistrations =
      new CopyOnWriteArrayList<>();

  @Override
  public HolderLookup.Provider getRegistryLookup() {
    var world = Minecraft.getInstance().level;
    return world == null ? null : world.registryAccess();
  }

  @Override
  public File getMinecraftDir() {
    return FMLPaths.GAMEDIR.get().toFile();
  }

  @Override
  public <H extends AbstractContainerMenu> void registerScreen(
      MenuType<H> type, ScreenFactory<H> factory) {
    synchronized (screenRegistrations) {
      if (screenRegistrations.stream().anyMatch(existing -> existing.type() == type))
        throw new IllegalArgumentException("Duplicate native screen type");
      screenRegistrations.add(new ScreenRegistration<>(type, factory));
    }
  }

  @Override
  public void registerColorProvider(BlockTintSource provider, Block... blocks) {
    blockTintRegistrations.add(
        new BlockTintRegistration(List.of(Objects.requireNonNull(provider)), blocks));
  }

  @Override
  public void registerColorProvider(ItemTintSource provider, ItemLike... items) {
    synchronized (itemTintRegistrations) {
      itemTintRegistrations.add(
          new ItemTintRegistration(
              Identifier.fromNamespaceAndPath("ic2", "item_tint_" + itemTintRegistrations.size()),
              Objects.requireNonNull(provider)));
    }
  }

  @Override
  public void registerKeyBinding(KeyMapping mapping) {
    keyBindingRegistrations.add(Objects.requireNonNull(mapping));
  }

  @Override
  public <E extends BlockEntity, S extends BlockEntityRenderState> void registerBer(
      BlockEntityType<E> type, BlockEntityRendererProvider<E, S> factory) {
    berRegistrations.add(
        new BerRegistration<>(Objects.requireNonNull(type), Objects.requireNonNull(factory)));
  }

  @Override
  public <E extends Entity> void registerEntityRenderer(
      EntityType<? extends E> type, EntityRendererProvider<E> factory) {
    entityRendererRegistrations.add(
        new EntityRendererRegistration<>(
            Objects.requireNonNull(type), Objects.requireNonNull(factory)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
      BlockEntityType<? extends E> type, BlockEntityRendererProvider<E, S> factory) {
    registerBer((BlockEntityType<E>) type, factory);
  }

  @Override
  public TextureAtlasSprite getFluidStillSprite(Ic2FluidStack fluid) {
    var atlas =
        (TextureAtlas)
            Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
    return atlas.getSprite(FluidHandler.getStillSpriteId(fluid.getFluid()));
  }

  @Override
  public int getFluidColor(Ic2FluidStack fluid) {
    return FluidHandler.getColor(fluid.getFluid());
  }

  @Override
  public String getFluidName(Ic2FluidStack fluid) {
    return LocalizedText.translate(fluid.getFluid().getFluidType().getDescriptionId());
  }
}
