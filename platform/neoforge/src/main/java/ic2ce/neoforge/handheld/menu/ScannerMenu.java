package ic2ce.neoforge.handheld.menu;

import ic2ce.engine.value.ValueTuples;
import ic2ce.neoforge.handheld.ScannerSession;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Read-only scanner result publication owns every prototype and the result generation. */
public class ScannerMenu extends HandheldMenu<ScannerSession> {
  public List<ValueTuples.T2<ItemStack, Integer>> scanResults = List.of();

  public ScannerMenu(int id, Inventory inventory, ScannerSession scanner) {
    super(MenuDefinitions.SCANNER, id, scanner);
    addPlayerInventorySlots(inventory, 231);
  }

  public void setResults(List<ValueTuples.T2<ItemStack, Integer>> result) {
    var owned = new ArrayList<ValueTuples.T2<ItemStack, Integer>>();
    for (var row : result)
      if (row.a != null && !row.a.isEmpty() && row.b != null && row.b > 0)
        owned.add(new ValueTuples.T2<>(row.a.copy(), row.b));
    scanResults = List.copyOf(owned);
    if (!player.level().isClientSide())
      ProjectRuntime.network.get(true).sendContainerField(this, "scanResults");
  }
}
