package ic2.core.proxy;

import ic2.core.fluid.Ic2FluidStack;
import java.io.File;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ClientEnvProxy {
   net.minecraft.core.HolderLookup.Provider getRegistryLookup();
   <H extends AbstractContainerMenu> void registerScreen(MenuType<H> var1, ClientEnvProxy.ScreenFactory<H> var2);

   File getMinecraftDir();

   void registerColorProvider(BlockTintSource var1, Block... var2);

   void registerColorProvider(ItemTintSource var1, ItemLike... var2);

   void registerKeyBinding(KeyMapping var1);

   <E extends BlockEntity, S extends BlockEntityRenderState> void registerBer(BlockEntityType<E> var1, BlockEntityRendererProvider<E, S> var2);

   /**
    * 26.1.2 item model properties are JSON driven; the old code registration is
    * gone. This is retained as a no-op for the legacy call sites.
    */
   @FunctionalInterface
   interface ModelPredicateFunction {
      float get(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.entity.LivingEntity entity, int seed);
   }

   default void registerModelPredicateProvider(Identifier var1, ModelPredicateFunction var2) {
   }

   default void registerModelPredicateProvider(net.minecraft.world.item.Item var1, Identifier var2, ModelPredicateFunction var3) {
   }

   <E extends Entity> void registerEntityRenderer(EntityType<? extends E> var1, EntityRendererProvider<E> var2);

   <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends E> var1, BlockEntityRendererProvider<E, S> var2);

   TextureAtlasSprite getFluidStillSprite(Ic2FluidStack var1);

   int getFluidColor(Ic2FluidStack var1);

   String getFluidName(Ic2FluidStack var1);

   default ClientEnvProxy.QuadData getQuadData(BakedQuad quad) {
      final float[] positions = new float[12];
      for (int i = 0; i < 4; i++) {
         positions[i * 3] = quad.position(i).x();
         positions[i * 3 + 1] = quad.position(i).y();
         positions[i * 3 + 2] = quad.position(i).z();
      }

      return new ClientEnvProxy.QuadData() {
         @Override
         public float[] positions() {
            return positions;
         }

         @Override
         public float[] uvs() {
            return new float[8];
         }

         @Override
         public int tint() {
            return quad.materialInfo().tintIndex();
         }

         @Override
         public TextureAtlasSprite sprite() {
            return quad.materialInfo().sprite();
         }
      };
   }

   public interface QuadData {
      float[] positions();

      float[] uvs();

      int tint();

      TextureAtlasSprite sprite();
   }

   public interface ScreenFactory<H extends AbstractContainerMenu> {
      AbstractContainerScreen<H> create(H var1, Inventory var2, Component var3);
   }
}
