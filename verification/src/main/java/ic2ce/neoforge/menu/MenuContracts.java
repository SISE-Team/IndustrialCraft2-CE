package ic2ce.neoforge.menu;

import com.mojang.authlib.GameProfile;
import ic2ce.core.*;
import ic2ce.neoforge.menu.slot.EquipmentItemSlot;
import ic2ce.neoforge.menu.slot.TemplateSlot;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Real native ownership, component identity, transfer bills, ghost isolation and armor insertion.
 */
public final class MenuContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String text) {
    checks++;
    if (!ok) failures.add("native base menu: " + text);
  }

  private static final class Menu extends OwnedMenu<SimpleContainer> {
    int taken;

    Menu(Player owner, SimpleContainer storage) {
      super(MenuType.GENERIC_9x1, 191, owner.getInventory(), storage);
      addSlot(
          new Slot(storage, 0, 8, 8) {
            @Override
            public void onTake(Player actor, ItemStack stack) {
              taken += stack.getCount();
              super.onTake(actor, stack);
            }
          });
      addPlayerInventorySlots(owner.getInventory(), 178);
    }
  }

  public static Result run(ServerLevel world) {
    return new MenuContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var owner =
        FakePlayerFactory.get(
            world,
            new GameProfile(UUID.fromString("f768ea23-b740-471d-b12f-c6c66f1cfc2f"), "MenuProbe"));
    var other =
        FakePlayerFactory.get(
            world,
            new GameProfile(UUID.fromString("1717c631-9845-497f-a935-e4d4966c19b1"), "MenuOther"));
    owner.getInventory().clearContent();
    var storage = new SimpleContainer(1);
    var menu = new Menu(owner, storage);
    int hotbar = menu.slots.size() - 9;
    owner.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 3));
    check(
        menu.quickMoveStack(owner, -1).isEmpty()
            && menu.quickMoveStack(owner, menu.slots.size()).isEmpty()
            && owner.getInventory().getItem(0).getCount() == 3,
        "invalid source indices cannot crash or change physical inventory");
    check(
        menu.quickMoveStack(other, hotbar).isEmpty()
            && owner.getInventory().getItem(0).getCount() == 3,
        "another actual player cannot use this menu's physical shift transfer");
    var moved = menu.quickMoveStack(owner, hotbar);
    check(
        moved.is(Items.IRON_INGOT)
            && moved.getCount() == 3
            && storage.getItem(0).getCount() == 3
            && owner.getInventory().getItem(0).isEmpty(),
        "real native shift transfer returns original result and conserves the three-item bill");
    owner.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 4));
    menu.quickMoveStack(owner, hotbar);
    check(
        storage.getItem(0).getCount() == 7 && owner.getInventory().getItem(0).isEmpty(),
        "real native matching component stack merges exactly");
    var named = new ItemStack(Items.IRON_INGOT, 5);
    named.set(DataComponents.CUSTOM_NAME, Component.literal("independent native components"));
    owner.getInventory().setItem(0, named);
    check(
        menu.quickMoveStack(owner, hotbar).isEmpty()
            && storage.getItem(0).getCount() == 7
            && owner.getInventory().getItem(0).getCount() == 5,
        "different native name components cannot merge or disappear");
    owner.getInventory().clearContent();
    menu.quickMoveStack(owner, 0);
    check(
        storage.getItem(0).isEmpty()
            && menu.taken == 7
            && owner.getInventory().getItem(8).getCount() == 7,
        "reverse native shift reports the actual seven-item pickup quantity once");
    var listener =
        new ContainerListener() {
          public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack) {}

          public void dataChanged(AbstractContainerMenu menu, int id, int value) {}
        };
    menu.addSlotListener(listener);
    menu.addSlotListener(listener);
    check(
        menu.getListeners().size() == 1,
        "native listeners register idempotently without reflection");
    menu.removeSlotListener(listener);
    check(menu.getListeners().isEmpty(), "native listener lifecycle removes actual ownership");
    var templates = new ItemStack[] {ItemStack.EMPTY};
    int[] changes = {0};
    var ghost = new TemplateSlot(templates, 0, 8, 8, 6, index -> changes[0]++);
    menu.setCarried(new ItemStack(Items.GOLD_INGOT, 4));
    var carried = menu.getCarried().copy();
    ghost.slotClick(0, ContainerInput.PICKUP, owner, menu);
    check(
        templates[0].getCount() == 4
            && ItemStack.matches(carried, menu.getCarried())
            && changes[0] == 1,
        "ghost left-click owns its template without paying or lending the physical carried stack");
    ghost.getItem().setCount(50);
    check(templates[0].getCount() == 4, "ghost display mutation cannot edit the native template");
    ghost.slotClick(0, ContainerInput.PICKUP, owner, menu);
    check(
        templates[0].getCount() == 6 && changes[0] == 2,
        "ghost count addition saturates its declared bound");
    ghost.slotClick(0, ContainerInput.PICKUP, owner, menu);
    check(changes[0] == 2, "unchanged ghost clicks cannot repeat template-change side effects");
    ghost.slotClick(0, ContainerInput.PICKUP, other, menu);
    check(changes[0] == 2, "ghost edit rejects another player bound to the wrong native menu");
    menu.setCarried(ItemStack.EMPTY);
    ghost.slotClick(1, ContainerInput.PICKUP, owner, menu);
    check(
        templates[0].getCount() == 3 && ghost.remove(100).isEmpty(),
        "ghost right-click halves only the template and exposes no physical extraction");
    boolean bad = false;
    try {
      new TemplateSlot(templates, -1, 0, 0, 1, null);
    } catch (IndexOutOfBoundsException expected) {
      bad = true;
    }
    check(bad, "negative ghost index rejected before construction completes");
    for (var equipment :
        List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)) {
      var armor = new EquipmentItemSlot(owner.getInventory(), equipment, 8, 8);
      check(
          armor.getMaxStackSize() == 1 && !armor.mayPlace(null) && !armor.mayPlace(ItemStack.EMPTY),
          "native armor slot rejects empty input and has one physical capacity " + equipment);
    }
    var bootSlot = new EquipmentItemSlot(owner.getInventory(), EquipmentSlot.FEET, 8, 8);
    check(
        bootSlot.mayPlace(new ItemStack(Items.IRON_BOOTS))
            && !bootSlot.mayPlace(new ItemStack(Items.IRON_HELMET)),
        "actual native armor insertion follows the equipment type");
    owner.getInventory().clearContent();
    menu.setCarried(ItemStack.EMPTY);
    return new Result(checks, List.copyOf(failures));
  }
}
