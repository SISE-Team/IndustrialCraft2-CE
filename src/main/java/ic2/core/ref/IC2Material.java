package ic2.core.ref;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 26.1.2 removed block {@code Material}; these factories return fresh
 * {@link BlockBehaviour.Properties} matching the old material presets.
 * Properties are mutable, so callers must always invoke the factory.
 */
public class IC2Material {
   public static BlockBehaviour.Properties machine() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY);
   }

   public static BlockBehaviour.Properties pipe() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY);
   }

   public static BlockBehaviour.Properties cable() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK);
   }

   public static BlockBehaviour.Properties steam() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY)
         .noCollision().replaceable().liquid();
   }

   public static BlockBehaviour.Properties stone() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops();
   }

   public static BlockBehaviour.Properties stone(MapColor color) {
      return BlockBehaviour.Properties.of().mapColor(color).requiresCorrectToolForDrops();
   }

   public static BlockBehaviour.Properties wood() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD);
   }

   public static BlockBehaviour.Properties wood(MapColor color) {
      return BlockBehaviour.Properties.of().mapColor(color);
   }

   public static BlockBehaviour.Properties metal() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops();
   }

   public static BlockBehaviour.Properties decoration() {
      return BlockBehaviour.Properties.of();
   }

   public static BlockBehaviour.Properties glass() {
      return BlockBehaviour.Properties.of();
   }

   public static BlockBehaviour.Properties explosive() {
      return BlockBehaviour.Properties.of();
   }

   public static BlockBehaviour.Properties wool() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.WOOL);
   }

   public static BlockBehaviour.Properties plant() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
         .noCollision().instabreak().replaceable().sound(SoundType.GRASS);
   }

   public static BlockBehaviour.Properties leaves() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
         .strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
         .isValidSpawn((state, level, pos, type) -> false)
         .isSuffocating((state, level, pos) -> false)
         .isViewBlocking((state, level, pos) -> false);
   }
}
