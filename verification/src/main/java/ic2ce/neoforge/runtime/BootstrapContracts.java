package ic2ce.neoforge.runtime;

import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.terrain.MaterialValuation;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;

/**
 * Actual configuration file replacement and owned registry display data remain coherent across
 * reloading.
 */
public final class BootstrapContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run(ServerLevel world) {
    var failures = new ArrayList<String>();
    int checks = 0;
    var current = ProjectConfiguration.get();
    double factor =
        NativeConfigValues.getDouble(current, "balance/energyRetainedInStorageBlockDrops");
    ProjectConfiguration.save();
    ProjectConfiguration.load();
    checks++;
    if (ProjectConfiguration.get() == current
        || NativeConfigValues.getDouble(
                ProjectConfiguration.get(), "balance/energyRetainedInStorageBlockDrops")
            != factor)
      failures.add(
          "bootstrap: native atomic configuration save/reload did not publish a complete equivalent"
              + " new generation");
    checks++;
    if (NativeConfigValues.getString(ProjectConfiguration.get(), "balance/recyclerBlacklist")
        .contains("IC2:"))
      failures.add(
          "bootstrap: default legacy uppercase scaffold identifier was not upgraded before runtime"
              + " input parsing");
    var icons = new CategoryIcon(CreativeCategory.COMBAT);
    var shown = icons.get();
    shown.setCount(17);
    shown.remove(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
    var next = icons.get();
    checks++;
    if (next.getCount() != 1 || !next.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
      failures.add("bootstrap: borrowed creative icon mutations corrupt the cached prototype");
    var seed = new ItemStack(Items.AMETHYST_SHARD);
    MaterialValuation.add(seed, 2);
    MaterialValuation.add(seed, 7);
    MaterialValuation.add(seed, 3);
    checks++;
    if (MaterialValuation.get(new ItemStack(Items.AMETHYST_SHARD, 3)) != 21)
      failures.add(
          "bootstrap: concurrent material value registry cannot retain the highest admitted unit"
              + " value");
    MaterialValuation.add(seed, Integer.MAX_VALUE);
    checks++;
    if (MaterialValuation.get(new ItemStack(Items.AMETHYST_SHARD, 2)) != Integer.MAX_VALUE
        || MaterialValuation.get(List.of(seed, seed)) != Integer.MAX_VALUE)
      failures.add(
          "bootstrap: native material quantity arithmetic wraps positive value into a negative"
              + " total");
    var actor = AutomationActors.get(world);
    ProgressionService.issueAchievement(actor, "ic2ce_verification:missing");
    checks++;
    if (actor.level() != world)
      failures.add("bootstrap: progression lookup changes automation world identity");
    return new Result(checks, List.copyOf(failures));
  }
}
