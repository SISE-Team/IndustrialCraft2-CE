package ic2ce.neoforge.item.upgrade;

import ic2ce.api.network.ClientModifiable;
import ic2ce.neoforge.ProjectIds;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.handheld.menu.HandheldMenu;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.menu.document.DocumentMenus;
import ic2ce.neoforge.menu.document.HandheldDocumentMenu;
import ic2ce.neoforge.menu.document.ScreenConditions;
import ic2ce.neoforge.menu.document.ScreenDocument;
import ic2ce.neoforge.menu.document.TemplateStorage;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.network.GuiSynced;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;

/** Nine owned ghost filters and independently persisted damage, energy and metadata controls. */
public class AdvancedFilterInventory extends HandheldStorage
    implements TemplateStorage, ScreenConditions {
  @GuiSynced protected boolean meta, energy;
  @ClientModifiable protected MetadataMatch nbt;

  private static ItemStack actual(Player owner, ItemStack requested) {
    if (!owner.level().isClientSide()
        && owner.containerMenu instanceof HandheldMenu<?> menu
        && menu.base instanceof FilterSubmenu) {
      addMaintainedPlayer(owner);
      return menu.base.getContainerStack();
    }
    return requested;
  }

  public AdvancedFilterInventory(Player owner, InteractionHand hand, ItemStack stack) {
    super(owner, hand, actual(owner, stack), 9);
    var data = StackTag.getOrCreate(containerStack);
    meta = readTag(data, "meta");
    energy = readTag(data, "energy");
    nbt = MetadataMatch.getFromNBT(getTag(data, "nbt").getByteOr("type", (byte) 0));
  }

  @Override
  protected void save() {
    super.save();
    if (player.level().isClientSide() || !stillValid(player)) return;
    StackTag.update(
        containerStack,
        data -> {
          writeTag(data, "meta", meta);
          writeTag(data, "energy", energy);
          var metadata = getTag(data, "nbt");
          metadata.putBoolean("active", nbt.enabled());
          metadata.putByte("type", nbt.getForNBT());
          data.put("nbtSettings", metadata);
          return data;
        });
  }

  public static CompoundTag getTag(CompoundTag data, String key) {
    return data == null ? new CompoundTag() : data.getCompoundOrEmpty(key + "Settings");
  }

  protected static boolean readTag(CompoundTag data, String key) {
    return getTag(data, key).getBooleanOr("active", false);
  }

  protected static void writeTag(CompoundTag data, String key, boolean active) {
    var field = getTag(data, key);
    field.putBoolean("active", active);
    data.put(key + "Settings", field);
  }

  static IHasGui delegate(Player owner, InteractionHand hand, ItemStack stack, int id) {
    var base = new AdvancedFilterInventory(owner, hand, stack);
    return switch (id) {
      case 0 -> new NumericFilterEditor(base, "meta");
      case 2 -> new NumericFilterEditor(base, "energy");
      case 3 -> new MaterialTagEditor(base);
      default -> null;
    };
  }

  @Override
  public OwnedMenu<?> createServerScreenHandler(int id, Player owner) {
    return HandheldDocumentMenu.create(
        DocumentMenus.ADVANCED_UPGRADE, id, owner.getInventory(), this, getNode());
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return HandheldDocumentMenu.create(
        DocumentMenus.ADVANCED_UPGRADE, id, inventory, this, getNode());
  }

  protected ScreenDocument.GuiNode getNode() {
    return ScreenDocument.parse(
        ProjectIds.id("advanced_upgrade"), AdvancedFilterInventory.class);
  }

  Player getPlayer() {
    return player;
  }

  InteractionHand getHand() {
    return hand;
  }

  @Override
  public ItemStack[] getStacksForName(String key) {
    if (!key.equals("filter")) throw new IllegalArgumentException("Unknown filter array: " + key);
    return inventory;
  }

  @Override
  public boolean getGuiState(String key) {
    return switch (key) {
      case "meta" -> meta;
      case "energy" -> energy || nbt == MetadataMatch.EXACT;
      case "nbt" -> nbt.enabled();
      case "dev" -> true;
      default -> false;
    };
  }

  @Override
  public void onEvent(String event) {
    switch (event) {
      case "meta" -> {
        meta = !meta;
        save();
      }
      case "energy" -> {
        energy = !energy;
        save();
      }
      case "nbt" -> {
        nbt = MetadataMatch.VALUES[(nbt.ordinal() + 1) % MetadataMatch.VALUES.length];
        save();
      }
      case "metaDev" -> new NumericFilterEditor(this, "meta").openManagedItem(player, hand, 0);
      case "energyDev" -> new NumericFilterEditor(this, "energy").openManagedItem(player, hand, 2);
      case "ore", "oreDev" -> new MaterialTagEditor(this).openManagedItem(player, hand, 3);
      default -> super.onEvent(event);
    }
  }
}
