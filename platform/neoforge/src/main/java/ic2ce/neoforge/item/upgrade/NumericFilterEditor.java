package ic2ce.neoforge.item.upgrade;

import ic2ce.api.network.ClientModifiable;
import ic2ce.core.*;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.handheld.menu.HandheldMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.registry.MenuDefinitions;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.*;
import net.neoforged.api.distmarker.*;

/**
 * Independent value/range menu compiles normalized numeric fields and persists them before native
 * close.
 */
public class NumericFilterEditor extends FilterSubmenu {
  protected final FilterMode initialComparisonType;
  protected final String initialNormalBox, initialExtraBox;
  protected final IntegerRelation initialNormalSetting, initialExtraSetting;

  public NumericFilterEditor(AdvancedFilterInventory parent, String key) {
    super(parent, key);
    if (!key.equals("meta") && !key.equals("energy"))
      throw new IllegalArgumentException("Unknown value filter");
    var settings = new NumericFilter(getNBT());
    initialComparisonType = settings.comparison;
    initialNormalBox = settings.mainBox;
    initialExtraBox = settings.extraBox;
    initialNormalSetting = settings.main;
    initialExtraSetting = settings.extra;
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return new ContainerValueConfig(id);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ContainerValueConfig(id);
  }

  public class ContainerValueConfig extends HandheldMenu<NumericFilterEditor> {
    @ClientModifiable protected FilterMode comparisonType = initialComparisonType;
    @ClientModifiable protected String normalBox = initialNormalBox, extraBox = initialExtraBox;
    @ClientModifiable
    protected IntegerRelation normalSetting = initialNormalSetting,
        extraSetting = initialExtraSetting;

    public ContainerValueConfig(int id) {
      super(MenuDefinitions.ADVANCED_UPGRADE_VALUE_CONFIG, id, NumericFilterEditor.this);
      addPlayerInventorySlots(player.getInventory(), 166);
      for (int slot = 0; slot < 9; slot++)
        addSlot(new TemplateSlot(inventory, slot, 8 + slot * 18, 8, 1, makeSaveCallback()));
    }

    @Override
    public void removed(Player actor) {
      if (!actor.level().isClientSide()) {
        var data = getNBT();
        var type = comparisonType == null ? FilterMode.IGNORED : comparisonType;
        var first = numeric(normalBox);
        var second = numeric(extraBox);
        var lower = normalSetting == null ? IntegerRelation.DEFAULT : normalSetting;
        var upper = extraSetting == null ? IntegerRelation.DEFAULT : extraSetting;
        if (type == FilterMode.RANGE && first.isEmpty() && !second.isEmpty()) {
          first = second;
          lower = upper;
          second = "";
          type = FilterMode.COMPARISON;
        } else if (type == FilterMode.RANGE && second.isEmpty()) type = FilterMode.COMPARISON;
        if (type == FilterMode.COMPARISON && first.isEmpty()) type = FilterMode.DIRECT;
        data.remove("normal");
        data.remove("extra");
        data.remove("normalComp");
        data.remove("extraComp");
        if (type == FilterMode.COMPARISON || type == FilterMode.RANGE) {
          data.putString("normal", first);
          data.putByte("normalComp", lower.getForNBT());
        }
        if (type == FilterMode.RANGE) {
          data.putString("extra", second);
          data.putByte("extraComp", upper.getForNBT());
        }
        data.putByte("type", type.getForNBT());
        data.putBoolean("active", type.enabled());
        writeNBT(data);
      }
      super.removed(actor);
    }

    private static String numeric(String value) {
      if (value == null || value.isEmpty() || value.length() > 11) return "";
      try {
        int number = Integer.parseInt(value);
        return number < 0 ? "" : Integer.toString(number);
      } catch (NumberFormatException ignored) {
        return "";
      }
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class GuiValueConfig extends BorderedScreen<ContainerValueConfig> {
    public GuiValueConfig(ContainerValueConfig container, Inventory inventory, Component title) {
      super(container, inventory, title);
      addElement(container.base.getBackButton(this, 10, 62));
      addElement(
          new NativeButton(
                  this,
                  10,
                  25,
                  75,
                  15,
                  ignored -> {
                    container.comparisonType =
                        FilterMode.VALUES[
                            (container.comparisonType.ordinal() + 1) % FilterMode.VALUES.length];
                    sync("comparisonType");
                  })
              .withText(() -> LocalizedText.translate(container.comparisonType.name)));
      addElement(
          new NativeButton(
                  this,
                  75,
                  43,
                  17,
                  15,
                  ignored -> {
                    container.normalSetting =
                        IntegerRelation.VALUES[
                            (container.normalSetting.ordinal() + 1)
                                % IntegerRelation.VALUES.length];
                    sync("normalSetting");
                  })
              .withEnableHandler(() -> !container.comparisonType.ignoreFilters())
              .withText(() -> container.normalSetting.symbol));
      addElement(
          new NativeButton(
                  this,
                  105,
                  43,
                  17,
                  15,
                  ignored -> {
                    container.extraSetting =
                        IntegerRelation.VALUES[
                            (container.extraSetting.ordinal() + 1) % IntegerRelation.VALUES.length];
                    sync("extraSetting");
                  })
              .withEnableHandler(() -> container.comparisonType == FilterMode.RANGE)
              .withText(() -> container.extraSetting.symbol));
      addElement(
          new TextInput(this, 10, 43, 60, 15, container.normalBox)
              .withTextValidator(GuiValueConfig::numeric)
              .withEnableHandler(() -> !container.comparisonType.ignoreFilters())
              .withTextWatcher(
                  (oldValue, value) -> {
                    container.normalBox = value;
                    sync("normalBox");
                  }));
      addElement(
          new TextInput(this, 125, 43, 40, 15, container.extraBox)
              .withTextValidator(GuiValueConfig::numeric)
              .withEnableHandler(() -> container.comparisonType == FilterMode.RANGE)
              .withTextWatcher(
                  (oldValue, value) -> {
                    container.extraBox = value;
                    sync("extraBox");
                  }));
      addElement(new InventoryGrid(this, 7, 7, 9, 1, InventoryGrid.SlotStyle.Normal));
      addElement(new InventoryGrid(this, 7, 83, 9, 3, InventoryGrid.SlotStyle.Normal));
      addElement(new InventoryGrid(this, 7, 141, 9, 1, InventoryGrid.SlotStyle.Normal));
    }

    private void sync(String field) {
      ProjectRuntime.network.get(false).sendContainerField(getContainer(), field);
    }

    private static boolean numeric(String value) {
      if (value.isEmpty()) return true;
      if (value.length() > 10) return false;
      try {
        return Integer.parseInt(value) >= 0;
      } catch (NumberFormatException ignored) {
        return false;
      }
    }
  }
}
