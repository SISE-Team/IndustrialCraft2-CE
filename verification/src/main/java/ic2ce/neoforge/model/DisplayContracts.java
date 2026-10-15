package ic2ce.neoforge.model;

import ic2ce.api.item.*;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.item.upgrade.MachineUpgrade;
import ic2ce.neoforge.registry.ItemDefinitions;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/**
 * Real initialized native model properties preserve actual charge and immutable upgrade-tag
 * generations.
 */
public final class DisplayContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    var client = Minecraft.getInstance();
    var property = new ChargeProperty();
    var battery = new ItemStack(ItemDefinitions.RE_BATTERY);
    checks++;
    if (property.get(battery, client.level, client.player, 0) != 0)
      failures.add("native model: blank battery publishes nonzero charge display");
    double maximum = ((IElectricItem) battery.getItem()).getMaxCharge(battery);
    ElectricItem.manager.charge(battery, maximum * .5, Integer.MAX_VALUE, true, false);
    checks++;
    if (Math.abs(property.get(battery, client.level, client.player, 0) - .5) > 1e-6)
      failures.add(
          "native model: actual half-charged native ledger does not publish its finite ratio");
    ElectricItem.manager.charge(battery, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false);
    checks++;
    if (property.get(battery, client.level, client.player, 0) != 1)
      failures.add("native model: actual charged battery display escapes [0,1]");
    var upgrade = new ItemStack(ItemDefinitions.EJECTOR_UPGRADE);
    var directions = Direction.values();
    for (int index = 0; index < directions.length; index++) {
      int encoded = index + 1;
      StackTag.update(
          upgrade,
          tag -> {
            tag.putInt("dir", encoded);
            return tag;
          });
      var side = directions[index];
      var selection = new StateProperty(side.getSerializedName());
      checks++;
      if (MachineUpgrade.getDirection(upgrade) != side
          || selection.get(upgrade, client.level, client.player, 0) != 1)
        failures.add(
            "native model: immutable directional tag generation did not update selection: " + side);
    }
    StackTag.update(
        upgrade,
        tag -> {
          tag.putInt("dir", 0);
          return tag;
        });
    checks++;
    if (MachineUpgrade.getDirection(upgrade) != null
        || new StateProperty("down").get(upgrade, client.level, client.player, 0) != 0)
      failures.add(
          "native model: cleared actual upgrade direction retained a stale cached selection");
    var named = new ItemStack(Items.STICK);
    StackTag.update(
        named,
        tag -> {
          tag.putInt("dir", 1);
          return tag;
        });
    checks++;
    if (new StateProperty("down").get(named, client.level, client.player, 0) != 0)
      failures.add("native model: unrelated physical item can spoof an upgrade display protocol");
    var state = Blocks.OAK_LOG.defaultBlockState();
    checks++;
    if (!ic2ce.neoforge.model.ModelUtil.getVariant(state)
        .equals(ic2ce.neoforge.world.surface.StateVariants.getVariantString(state)))
      failures.add("native model: render and persisted world variant text diverge");
    var quads =
        ic2ce.neoforge.model.ModelUtil.getQuads(
            Blocks.STONE.defaultBlockState(),
            Direction.UP,
            net.minecraft.util.RandomSource.create(25));
    boolean immutable = false;
    try {
      quads.clear();
    } catch (UnsupportedOperationException expected) {
      immutable = true;
    }
    checks++;
    if (!immutable)
      failures.add("native model: exported quad list can edit its renderer-owned projection");

    var nativeModel =
        client.getModelManager().getBlockStateModelSet().get(Blocks.STONE.defaultBlockState());
    var modelSource =
        new HashMap<
            net.minecraft.world.level.block.state.BlockState,
            net.minecraft.client.renderer.block.dispatch.BlockStateModel>();
    modelSource.put(Blocks.STONE.defaultBlockState(), nativeModel);
    var projection = new ObscuredWallModel(nativeModel, modelSource);
    modelSource.clear();
    checks++;
    if (projection.models().size() != 1)
      failures.add("native wall: source baking-map mutation changed a published model generation");
    immutable = false;
    try {
      projection.models().clear();
    } catch (UnsupportedOperationException expected) {
      immutable = true;
    }
    checks++;
    if (!immutable) failures.add("native wall: exported model generation is mutable");
    checks++;
    if (ObscuredWallModel.multiply(0xffabcdef, 0xffffff) != 0xffabcdef
        || ObscuredWallModel.multiply(0x7fffffff, 0x123456) != 0x7f123456)
      failures.add("native wall: captured RGB projection changed alpha or channels");
    int[] tint = {0x123456};
    var descriptor =
        new ic2ce.neoforge.world.surface.SurfaceMap.ObscurationData(
            Blocks.STONE.defaultBlockState(), "normal", Direction.NORTH, tint);
    var faces = new ic2ce.neoforge.world.surface.SurfaceMap.ObscurationData[6];
    faces[Direction.NORTH.ordinal()] = descriptor;
    var published =
        new ic2ce.neoforge.world.block.ImprintedWall.WallRenderState(DyeColor.WHITE, faces);
    descriptor.colorMultipliers[0] = 0;
    faces[Direction.NORTH.ordinal()] = null;
    checks++;
    if (published.obscurations[Direction.NORTH.ordinal()].colorMultipliers[0] != 0x123456)
      failures.add("native wall: a caller descriptor changed the cached surface geometry key");
    return new Result(checks, List.copyOf(failures));
  }
}
