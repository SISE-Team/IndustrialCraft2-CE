package ic2ce.neoforge.client;

import ic2ce.api.fluid.Ic2FluidStack;
import java.io.File;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;

/**
 * Native client boundary registers real current screens/renderers and exposes independently owned
 * model snapshots.
 */
public interface ClientEnvProxy {
  net.minecraft.core.HolderLookup.Provider getRegistryLookup();

  <H extends AbstractContainerMenu> void registerScreen(MenuType<H> type, ScreenFactory<H> factory);

  File getMinecraftDir();

  void registerColorProvider(BlockTintSource provider, Block... blocks);

  void registerColorProvider(ItemTintSource provider, ItemLike... items);

  void registerKeyBinding(KeyMapping mapping);

  <E extends BlockEntity, S extends BlockEntityRenderState> void registerBer(
      BlockEntityType<E> type, BlockEntityRendererProvider<E, S> factory);

  <E extends Entity> void registerEntityRenderer(
      EntityType<? extends E> type, EntityRendererProvider<E> factory);

  <E extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
      BlockEntityType<? extends E> type, BlockEntityRendererProvider<E, S> factory);

  TextureAtlasSprite getFluidStillSprite(Ic2FluidStack fluid);

  int getFluidColor(Ic2FluidStack fluid);

  String getFluidName(Ic2FluidStack fluid);

  default QuadData getQuadData(BakedQuad quad) {
    float[] positions = new float[12], uvs = new float[8];
    for (int vertex = 0; vertex < 4; vertex++) {
      var position = quad.position(vertex);
      positions[3 * vertex] = position.x();
      positions[3 * vertex + 1] = position.y();
      positions[3 * vertex + 2] = position.z();
      uvs[2 * vertex] =
          net.minecraft.client.model.geom.builders.UVPair.unpackU(quad.packedUV(vertex));
      uvs[2 * vertex + 1] =
          net.minecraft.client.model.geom.builders.UVPair.unpackV(quad.packedUV(vertex));
    }
    return new Snapshot(
        positions, uvs, quad.materialInfo().tintIndex(), quad.materialInfo().sprite());
  }

  record Snapshot(float[] positionValues, float[] uvValues, int tint, TextureAtlasSprite sprite)
      implements QuadData {
    public Snapshot {
      positionValues = positionValues.clone();
      uvValues = uvValues.clone();
    }

    @Override
    public float[] positions() {
      return positionValues.clone();
    }

    @Override
    public float[] uvs() {
      return uvValues.clone();
    }

    @Override
    public float[] positionValues() {
      return positionValues.clone();
    }

    @Override
    public float[] uvValues() {
      return uvValues.clone();
    }
  }

  interface QuadData {
    float[] positions();

    float[] uvs();

    int tint();

    TextureAtlasSprite sprite();
  }

  @FunctionalInterface
  interface ScreenFactory<H extends AbstractContainerMenu> {
    AbstractContainerScreen<H> create(H menu, Inventory inventory, Component title);
  }
}
