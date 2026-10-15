package ic2ce.neoforge.item.upgrade;

import ic2ce.api.network.ClientModifiable;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.handheld.menu.HandheldMenu;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import ic2ce.neoforge.runtime.SideServices;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.*;
import net.neoforged.api.distmarker.*;

/**
 * Material-tag editor lists the real current prototype tags and persists a validated native
 * identifier.
 */
public class MaterialTagEditor extends FilterSubmenu {
  public MaterialTagEditor(AdvancedFilterInventory parent) {
    super(parent, "ore");
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player actor) {
    return new ContainerEditOre(id);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new ContainerEditOre(id);
  }

  public class ContainerEditOre extends HandheldMenu<MaterialTagEditor> {
    @ClientModifiable public String selectedTag = getNBT().getStringOr("tag", "");

    public ContainerEditOre(int id) {
      super(UpgradeMenus.ADVANCED_UPGRADE_EDIT_ORE, id, MaterialTagEditor.this);
      addPlayerInventorySlots(player.getInventory(), 200);
      for (int slot = 0; slot < 9; slot++)
        addSlot(new TemplateSlot(inventory, slot, 8 + slot * 18, 8, 1, makeSaveCallback()));
    }

    @Override
    public void removed(Player actor) {
      if (!actor.level().isClientSide()) {
        var id =
            selectedTag != null && selectedTag.length() <= 256
                ? Identifier.tryParse(selectedTag)
                : null;
        var data = getNBT();
        data.putBoolean("active", id != null);
        if (id == null) data.remove("tag");
        else data.putString("tag", id.toString());
        writeNBT(data);
      }
      super.removed(actor);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class GuiEditOre extends BorderedScreen<ContainerEditOre> {
    public GuiEditOre(ContainerEditOre container, Inventory inventory, Component title) {
      super(container, inventory, title, 200);
      addElement(container.base.getBackButton(this, 10, 96));
      var field = new TextInput(this, 10, 75, 155, 15, container.selectedTag);
      field.setMaxTextLength(256);
      field.withTextWatcher(
          (oldValue, value) -> {
            container.selectedTag = value;
            sync();
          });
      addElement(field);
      addElement(
          new NativeButton(this, 115, 96, 50, 15, ignored -> field.setText(""))
              .withText(LocalizedText.translate("ic2.gui.clear")));
      var entries = new java.util.TreeSet<String>();
      for (int slot = 0; slot < container.base.getContainerSize(); slot++) {
        var prototype = container.base.getItem(slot);
        if (!prototype.isEmpty())
          BuiltInRegistries.ITEM
              .wrapAsHolder(prototype.getItem())
              .tags()
              .forEach(tag -> entries.add(tag.location().toString()));
      }
      var rows = new java.util.ArrayList<ClickableRows.IListItem>();
      for (var value : entries)
        rows.add(
            new ClickableRows.IListItem() {
              @Override
              public void draw(
                  net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                  int x,
                  int y,
                  int width,
                  int height,
                  int mouseX,
                  int mouseY) {
                GuiEditOre.this.drawString(
                    graphics,
                    x + 2,
                    y + 1,
                    value,
                    value.equals(container.selectedTag) ? 0xFFFF55 : 0xFFFFFF,
                    false);
              }

              @Override
              public boolean onClick(PointerButton button, int x, int y) {
                field.setText(value);
                return true;
              }
            });
      addElement(new ClickableRows(this, 10, 30, 155, 40, rows));
      addElement(new InventoryGrid(this, 7, 7, 9, 1, InventoryGrid.SlotStyle.Normal));
      addElement(new InventoryGrid(this, 7, 117, 9, 3, InventoryGrid.SlotStyle.Normal));
      addElement(new InventoryGrid(this, 7, 175, 9, 1, InventoryGrid.SlotStyle.Normal));
    }

    private void sync() {
      SideServices.network(false).sendContainerField(getContainer(), "selectedTag");
    }
  }
}
