package ic2ce.verification;

import static ic2ce.verification.AllMachinesVerification.*;

import ic2ce.api.upgrade.*;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.inventory.UpgradeInventory;
import ic2ce.neoforge.item.upgrade.MachineUpgrade;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Every upgrade, accepted machine, quantity boundary, and legal two-slot pair. */
final class UpgradeMatrix {
  private final ExtendedValidation e;
  private final AllMachinesVerification s;
  private final Map<String, NativeBlockEntity> targets = new LinkedHashMap<>();
  private final Map<String, UpgradeInventory> slots = new LinkedHashMap<>();
  private final List<String> upgrades = new ArrayList<>();
  private final List<List<ItemStack>> variants = new ArrayList<>();
  private final List<Map<String, Object>> records = new ArrayList<>();
  private int age, index;
  private final List<String> touched = new ArrayList<>();
  private final List<String> preparation = new ArrayList<>();
  private int preparationIndex;

  UpgradeMatrix(ExtendedValidation owner) {
    e = owner;
    s = owner.s;
  }

  void prepare() {
    for (var id : BuiltInRegistries.ITEM.keySet())
      if (id.getNamespace().equals("ic2")
          && BuiltInRegistries.ITEM.getValue(id) instanceof MachineUpgrade)
        upgrades.add(id.getPath());
    Collections.sort(upgrades);
    e.check("all 11 upgrade variants registered", upgrades.size() == 11);
    for (var entry : s.machines.entrySet())
      if (entry.getValue() instanceof IUpgradableBlock) preparation.add(entry.getKey());
    for (String id : upgrades)
      for (int count : List.of(1, 2, 64)) variants.add(List.of(stack(id, count)));
    for (int a = 0; a < upgrades.size(); a++)
      for (int b = a; b < upgrades.size(); b++) {
        variants.add(List.of(stack(upgrades.get(a), 1), stack(upgrades.get(b), 1)));
        if (a != b) variants.add(List.of(stack(upgrades.get(b), 1), stack(upgrades.get(a), 1)));
      }
    variants.add(List.of(stack("overclocker_upgrade", 64), stack("energy_storage_upgrade", 64)));
    variants.add(List.of(stack("overclocker_upgrade", 64), stack("transformer_upgrade", 64)));
    for (int mask = 1; mask < 16; mask++) {
      List<ItemStack> combination = new ArrayList<>();
      String[] ids = {
        "overclocker_upgrade",
        "transformer_upgrade",
        "energy_storage_upgrade",
        "redstone_inverter_upgrade"
      };
      for (int bit = 0; bit < 4; bit++)
        if ((mask & (1 << bit)) != 0) combination.add(stack(ids[bit], 1));
      variants.add(combination);
    }
    e.details.put("upgradeTypes", upgrades);
    e.details.put("upgradeGlobalVariants", variants.size());
  }

  boolean tick() {
    // Generate one remote device site per native tick so real clients receive keepalives.
    if (preparationIndex < preparation.size()) {
      int i = preparationIndex++;
      String name = preparation.get(i);
      var pos = new BlockPos(512 + (i % 8) * 16, 201, 176 + (i / 8) * 16);
      for (int x = -1; x <= 1; x++)
        for (int z = -1; z <= 1; z++)
          s.level.setChunkForced((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z, true);
      var tile = s.helper(name, pos);
      var inventory = (ic2ce.neoforge.block.InventoryBlockEntity) tile;
      if (inventory.getInventorySlot("upgrade") instanceof UpgradeInventory upgradeSlot) {
        targets.put(name, tile);
        slots.put(name, upgradeSlot);
        if (tile.hasComponent(ElectricalBuffer.class)) s.helper("creative_generator", pos.west());
      }
      e.details.put("upgradePreparedSites", preparationIndex);
      e.details.put("upgradeCapableMachineTypes", targets.size());
      return false;
    }
    age++;
    if (age == 20) {
      for (var entry : targets.entrySet())
        for (String id : upgrades) {
          var item = stack(id, 1);
          var machine = (IUpgradableBlock) entry.getValue();
          var type = ((MachineUpgrade) item.getItem()).type;
          Set<UpgradableProperty> props = machine.getUpgradableProperties();
          boolean expected =
              switch (type) {
                case overclocker ->
                    props.contains(UpgradableProperty.Processing)
                        || props.contains(UpgradableProperty.Augmentable);
                case transformer -> props.contains(UpgradableProperty.Transformer);
                case energy_storage -> props.contains(UpgradableProperty.EnergyStorage);
                case ejector, advanced_ejector -> props.contains(UpgradableProperty.ItemProducing);
                case pulling, advanced_pulling -> props.contains(UpgradableProperty.ItemConsuming);
                case fluid_ejector -> props.contains(UpgradableProperty.FluidProducing);
                case fluid_pulling -> props.contains(UpgradableProperty.FluidConsuming);
                case redstone_inverter -> props.contains(UpgradableProperty.RedstoneSensitive);
                case remote_interface -> props.contains(UpgradableProperty.RemotelyAccessible);
              };
          e.check(
              entry.getKey() + ": suitability " + id,
              slots.get(entry.getKey()).accepts(item) == expected);
        }
      var parent = (IUpgradableBlock) targets.values().iterator().next();
      var remote = (IRemoteAccessUpgrade) stack("remote_interface_upgrade", 1).getItem();
      for (int count : List.of(1, 2, 8, 64))
        e.check(
            "remote range matches 2^count and saturates: " + count,
            remote.getRangeAmplification(stack("remote_interface_upgrade", count), parent, 8)
                == (int) Math.min(Integer.MAX_VALUE, 8 * Math.pow(2, count)));
    }
    if (age < 20) return false;
    if ((age - 20) % 30 == 0) {
      if (index >= variants.size()) {
        cleanup();
        e.details.put("upgradeCases", records);
        return true;
      }
      touched.clear();
      var variant = variants.get(index++);
      for (var entry : targets.entrySet()) {
        String id = entry.getKey();
        var tile = entry.getValue();
        var slot = slots.get(id);
        slot.clear();
        if (variant.size() > slot.size() || variant.stream().anyMatch(item -> !slot.accepts(item)))
          continue;
        for (int n = 0; n < variant.size(); n++) slot.put(n, variant.get(n).copy());
        slot.onChanged();
        tile.setChanged();
        touched.add(id);
        var row = new LinkedHashMap<String, Object>();
        row.put("machine", id);
        row.put("variant", variant.stream().map(Object::toString).toList());
        row.put("timeMultiplier", slot.processTimeMultiplier);
        row.put("demandMultiplier", slot.energyDemandMultiplier);
        row.put("augmentation", slot.augmentation);
        row.put("extraTier", slot.extraTier);
        row.put("extraStorage", slot.extraEnergyStorage);
        if (tile.hasComponent(ElectricalBuffer.class)) {
          var energy = tile.getComponent(ElectricalBuffer.class);
          row.put("capacity", energy.getCapacity());
          e.check(
              id
                  + " "
                  + row.get("variant")
                  + ": energy capacity and storage remain nonnegative and finite",
              Double.isFinite(energy.getCapacity())
                  && energy.getCapacity() >= 0
                  && energy.getEnergy() >= 0);
        }
        e.check(
            id + " " + row.get("variant") + ": multipliers remain finite and positive",
            Double.isFinite(slot.processTimeMultiplier)
                && slot.processTimeMultiplier > 0
                && Double.isFinite(slot.energyDemandMultiplier)
                && slot.energyDemandMultiplier > 0);
        records.add(row);
      }
    }
    if ((age - 20) % 30 == 25) {
      for (String id : touched) {
        var tile = targets.get(id);
        var saved = tile.saveWithFullMetadata(s.level.registryAccess());
        var copy =
            net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                tile.getBlockPos(), tile.getBlockState(), saved, s.level.registryAccess());
        e.check(
            id + " variant " + index + ": survives real ticks and serialized entity type",
            s.level.getBlockEntity(tile.getBlockPos()) == tile
                && copy != null
                && copy.getType() == tile.getType());
      }
    }
    return false;
  }

  private void cleanup() {
    for (var tile : targets.values()) {
      s.level.setBlockAndUpdate(
          tile.getBlockPos(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
      s.level.setBlockAndUpdate(
          tile.getBlockPos().west(),
          net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
    }
  }
}
