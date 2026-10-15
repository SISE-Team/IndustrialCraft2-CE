package ic2ce.neoforge.item.upgrade;

import com.google.common.base.Predicate;
import ic2ce.api.item.*;
import ic2ce.neoforge.item.StackTag;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

/**
 * Immutable filter compilation is weakly keyed by native component identity; candidate charge
 * remains live.
 */
public final class CompiledUpgradeFilter implements Predicate<ItemStack> {
  private static final Map<
          CustomData, Map<net.minecraft.core.HolderLookup.Provider, CompiledUpgradeFilter>>
      CACHE = Collections.synchronizedMap(new WeakHashMap<>());
  private static final CompiledUpgradeFilter EMPTY = new CompiledUpgradeFilter(new CompoundTag());
  private static final Set<String> VOLATILE = Set.of("charge", "energy", "damage", "advDmg");

  private record Prototype(
      Item item, int damage, double charge, CompoundTag data, CompoundTag fuzzy) {}

  private final Map<Item, List<Prototype>> byItem;
  private final NumericFilter damage, energy;
  private final MetadataMatch nbt;
  private final TagKey<Item> materialTag;

  public static CompiledUpgradeFilter of(ItemStack stack) {
    var data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return EMPTY;
    var lookup =
        ic2ce.neoforge.runtime.ProjectRuntime.envProxy == null
            ? null
            : ic2ce.neoforge.runtime.ProjectRuntime.envProxy.getRegistryLookup();
    if (lookup == null) return new CompiledUpgradeFilter(data.copyTag());
    synchronized (CACHE) {
      var generation = CACHE.computeIfAbsent(data, ignored -> new WeakHashMap<>());
      var result = generation.get(lookup);
      if (result == null) {
        result = new CompiledUpgradeFilter(data.copyTag());
        generation.put(lookup, result);
      }
      return result;
    }
  }

  private CompiledUpgradeFilter(CompoundTag source) {
    damage = new NumericFilter(source.getCompoundOrEmpty("metaSettings"));
    energy = new NumericFilter(source.getCompoundOrEmpty("energySettings"));
    nbt =
        MetadataMatch.getFromNBT(
            source.getCompoundOrEmpty("nbtSettings").getByteOr("type", (byte) 0));
    var tagSettings = source.getCompoundOrEmpty("oreSettings");
    var material =
        tagSettings.getBooleanOr("active", false)
            ? Identifier.tryParse(tagSettings.getStringOr("tag", ""))
            : null;
    materialTag = material == null ? null : TagKey.create(Registries.ITEM, material);
    var entries = new IdentityHashMap<Item, List<Prototype>>();
    var saved = source.getListOrEmpty("Items");
    for (int index = 0; index < saved.size(); index++) {
      var entry = saved.getCompound(index).orElseGet(CompoundTag::new);
      int slot = entry.getByteOr("Slot", (byte) -1);
      if (slot < 0 || slot >= 9) continue;
      var stack = StackTag.parseHolder(entry);
      if (stack.isEmpty()) continue;
      var tag = StackTag.get(stack);
      entries
          .computeIfAbsent(stack.getItem(), ignored -> new ArrayList<>())
          .add(
              new Prototype(
                  stack.getItem(),
                  stack.getDamageValue(),
                  ElectricItem.manager.getCharge(stack),
                  tag,
                  clean(tag)));
    }
    entries.replaceAll((item, list) -> List.copyOf(list));
    byItem = Collections.unmodifiableMap(entries);
  }

  private static CompoundTag clean(CompoundTag data) {
    if (data == null) return null;
    var result = data.copy();
    for (var key : VOLATILE) result.remove(key);
    return result.isEmpty() ? null : result;
  }

  @Override
  public boolean apply(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    if (materialTag != null && !stack.is(materialTag)) return false;
    boolean directDamage = damage.active && damage.comparison == FilterMode.DIRECT,
        directEnergy = energy.active && energy.comparison == FilterMode.DIRECT;
    if (!damage.comparison.ignoreFilters() && !damage.doComparison(stack.getDamageValue()))
      return false;
    double charge = 0;
    if (energy.active) {
      if (!(stack.getItem() instanceof IElectricItem)) return false;
      charge = ElectricItem.manager.getCharge(stack);
      if (!energy.comparison.ignoreFilters()
          && !energy.doComparison((int) Math.min(Integer.MAX_VALUE, charge))) return false;
    }
    var alternatives = byItem.get(stack.getItem());
    if (alternatives == null)
      return byItem.isEmpty()
          && !directDamage
          && !directEnergy
          && (damage.active || energy.active || materialTag != null);
    var tag = nbt.enabled() ? StackTag.get(stack) : null;
    var fuzzy = nbt == MetadataMatch.FUZZY ? clean(tag) : null;
    for (var prototype : alternatives) {
      if (directDamage && stack.getDamageValue() != prototype.damage
          || directEnergy && Math.abs(charge - prototype.charge) >= 1e-5) continue;
      if (nbt == MetadataMatch.EXACT && !Objects.equals(tag, prototype.data)
          || nbt == MetadataMatch.FUZZY && !Objects.equals(fuzzy, prototype.fuzzy)) continue;
      return true;
    }
    return false;
  }
}
