package ic2.forge;

import com.mojang.serialization.MapCodec;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.init.Localization;
import ic2.core.proxy.ClientEnvProxy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.fml.loading.FMLPaths;

public final class ClientEnvProxyForge implements ClientEnvProxy {
   @Override
   public net.minecraft.core.HolderLookup.Provider getRegistryLookup() {
      var mc = ic2.core.proxy.SideProxyClient.mc;
      return mc != null && mc.level != null ? mc.level.registryAccess() : null;
   }
   record ScreenRegistration<H extends AbstractContainerMenu>(MenuType<H> type, ClientEnvProxy.ScreenFactory<H> factory) {
   }

   record BlockTintRegistration(List<BlockTintSource> provider, Block... blocks) {
   }

   record BerRegistration<E extends BlockEntity, S extends BlockEntityRenderState>(BlockEntityType<E> type, BlockEntityRendererProvider<E, S> factory) {
   }

   record EntityRendererRegistration<E extends Entity>(EntityType<? extends E> type, EntityRendererProvider<E> factory) {
   }

   static final List<ScreenRegistration<?>> screenRegistrations = new ArrayList<>();
   static final List<BlockTintRegistration> blockTintRegistrations = new ArrayList<>();
   static final List<ItemTintSource> itemTintProviders = new ArrayList<>();
   static final List<KeyMapping> keyBindingRegistrations = new ArrayList<>();
   static final List<BerRegistration<?, ?>> berRegistrations = new ArrayList<>();
   static final List<EntityRendererRegistration<?>> entityRendererRegistrations = new ArrayList<>();
   private static int tintId = 0;

   static List<ItemTintSource> itemTintProviders() {
      return itemTintProviders;
   }

   static Identifier nextTintId() {
      return Identifier.fromNamespaceAndPath("ic2", "item_tint_" + tintId++);
   }

   @Override
   public <H extends AbstractContainerMenu> void registerScreen(MenuType<H> type, ClientEnvProxy.ScreenFactory<H> factory) {
      screenRegistrations.add(new ScreenRegistration<>(type, factory));
   }

   @Override
   public File getMinecraftDir() {
      return FMLPaths.GAMEDIR.get().toFile();
   }

   @Override
   public void registerColorProvider(BlockTintSource provider, Block... blocks) {
      blockTintRegistrations.add(new BlockTintRegistration(List.of(provider), blocks));
   }

   @Override
   public void registerColorProvider(ItemTintSource provider, ItemLike... items) {
      // 26.1.2 registers item tints by id with a codec; synthetic ids are assigned at replay.
      itemTintProviders.add(provider);
   }

   @Override
   public void registerKeyBinding(KeyMapping keyBinding) {
      keyBindingRegistrations.add(keyBinding);
   }

   @Override
   public <E extends BlockEntity, S extends BlockEntityRenderState> void registerBer(BlockEntityType<E> type, BlockEntityRendererProvider<E, S> rendererFactory) {
      berRegistrations.add(new BerRegistration<>(type, rendererFactory));
   }

   @Override
   public <E extends Entity> void registerEntityRenderer(EntityType<? extends E> type, EntityRendererProvider<E> factory) {
      entityRendererRegistrations.add(new EntityRendererRegistration<>(type, factory));
   }

   @Override
   public <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends E> type, BlockEntityRendererProvider<E, S> factory) {
      berRegistrations.add(new BerRegistration<>((BlockEntityType<E>)type, (BlockEntityRendererProvider<E, S>)factory));
   }

   @Override
   public TextureAtlasSprite getFluidStillSprite(Ic2FluidStack stack) {
      Identifier id = ic2.core.fluid.FluidHandler.getStillSpriteId(stack.getFluid());
      return ((net.minecraft.client.renderer.texture.TextureAtlas)ic2.core.proxy.SideProxyClient.mc.getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)).getSprite(id);
   }

   @Override
   public int getFluidColor(Ic2FluidStack stack) {
      return ic2.core.fluid.FluidHandler.getColor(stack.getFluid());
   }

   @Override
   public String getFluidName(Ic2FluidStack stack) {
      return Localization.translate(stack.getFluid().getFluidType().getDescriptionId());
   }
}
