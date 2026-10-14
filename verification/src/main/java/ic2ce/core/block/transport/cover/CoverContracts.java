package ic2ce.core.block.transport.cover;

import ic2ce.api.fluid.Ic2FluidStack;
import ic2ce.core.util.StackTag;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.registry.BlockDefinitions;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * Native addon cover mutation uses physical stacks, metadata and owner lifecycle. No extra registry
 * items enter the release.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = "ic2")
public final class CoverContracts {
  private static TestCover registeredCover;

  @net.neoforged.bus.api.SubscribeEvent
  public static void registerTestItem(net.neoforged.neoforge.registries.RegisterEvent event) {
    if (Boolean.getBoolean("ic2.verifyHotpaths") && event.getRegistryKey() == Registries.ITEM)
      event.register(
          Registries.ITEM,
          Identifier.fromNamespaceAndPath("ic2ce_verification", "cover"),
          () -> registeredCover = new TestCover());
  }

  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native cover: " + text);
  }

  public static Result run(ServerLevel world) {
    return new CoverContracts().exercise(world);
  }

  private static final class Holder extends NativeBlockEntity implements ICoverHolder {
    Holder() {
      super(
          BlockEntityDefinitions.MACERATOR,
          BlockPos.ZERO,
          BlockDefinitions.MACERATOR.defaultBlockState());
    }

    public Set<CoverProperty> getCoverProperties() {
      return Set.of(CoverProperty.FluidConsuming);
    }

    public boolean canPlaceCover(Level world, BlockPos pos, Direction side, ItemStack stack) {
      return true;
    }

    public void placeCover(Level world, BlockPos pos, Direction side, ItemStack stack) {}

    public boolean canRemoveCover(Level world, BlockPos pos, Direction side) {
      return true;
    }

    public void removeCover(Level world, BlockPos pos, Direction side) {}
  }

  private static final class TestCover extends Item implements ICoverItem {
    int ticks;

    TestCover() {
      super(
          new Item.Properties()
              .setId(
                  ResourceKey.create(
                      Registries.ITEM,
                      Identifier.fromNamespaceAndPath("ic2ce_verification", "cover"))));
    }

    public boolean isSuitableFor(ItemStack stack, Set<CoverProperty> properties) {
      return properties.contains(CoverProperty.FluidConsuming);
    }

    public boolean onTick(ItemStack stack, ICoverHolder holder) {
      ticks++;
      StackTag.update(
          stack,
          tag -> {
            tag.putInt("ticks", ticks);
            return tag;
          });
      return true;
    }

    public boolean allowsInput(ItemStack stack) {
      return true;
    }

    public boolean allowsInput(Ic2FluidStack stack) {
      return true;
    }

    public boolean allowsOutput(ItemStack stack) {
      return true;
    }

    public boolean allowsOutput(Ic2FluidStack stack) {
      return true;
    }
  }

  private Result exercise(ServerLevel world) {
    var holder = new Holder();
    holder.setLevel(world);
    var covers = new Covers(holder);
    var item = Objects.requireNonNull(registeredCover);
    check(
        covers.removeCover(Direction.UP).isEmpty() && !covers.hasCover(Direction.UP),
        "empty native cover removal is inert and cannot dereference null");
    covers.addCover(Direction.UP, new ItemStack(Items.STICK));
    check(
        !covers.hasCover(Direction.UP),
        "ordinary registered item cannot enter an addon cover slot");
    var offered = new ItemStack(item, 4);
    StackTag.update(
        offered,
        tag -> {
          tag.putString("owner_data", "retained");
          return tag;
        });
    covers.addCover(Direction.UP, offered);
    offered.setCount(1);
    check(
        covers.hasCover(Direction.UP)
            && covers.covers[Direction.UP.ordinal()].getCount() == 1
            && StackTag.get(offered).getByteOr("side", (byte) -1) == -1,
        "one admitted cover owns its count and side tag without editing the source stack");
    covers.onWorldTick();
    var removed = covers.removeCover(Direction.UP);
    var data = StackTag.get(removed);
    check(
        item.ticks == 1
            && data.getIntOr("ticks", 0) == 1
            && data.getStringOr("owner_data", "").equals("retained")
            && !data.contains("side"),
        "native cover tick persists state and removal strips only its owned side marker");
    check(
        covers.getCoverItem(Direction.UP) == null && covers.removeCover(Direction.UP).isEmpty(),
        "repeated cover removal has no duplicate output");
    covers.addCover(Direction.NORTH, new ItemStack(item));
    covers.readFromNbt(new CompoundTag());
    check(
        !covers.hasCover(Direction.NORTH),
        "empty saved cover set replaces the complete previous state on reload");
    var wrong =
        new ic2ce.neoforge.processing.solid.OreGrinder(
            BlockPos.ZERO, BlockDefinitions.MACERATOR.defaultBlockState());
    wrong.setLevel(world);
    var inert = new Covers(wrong);
    inert.addCover(Direction.UP, new ItemStack(item));
    inert.onWorldTick();
    check(
        !inert.hasCover(Direction.UP) && !inert.enableWorldTick(),
        "non-cover native owner cannot produce a holder cast or phantom cover tick");
    return new Result(checks, List.copyOf(failures));
  }
}
