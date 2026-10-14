package ic2ce.neoforge.processing.solid;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.api.recipe.IRecipeInput;
import ic2ce.api.recipe.Recipes;
import ic2ce.api.upgrade.UpgradableProperty;
import ic2ce.core.network.GrowingBuffer;
import ic2ce.core.profile.NotClassic;
import ic2ce.neoforge.inventory.ItemRecipeInput;
import ic2ce.neoforge.menu.OwnedMenu;
import ic2ce.neoforge.processing.PoweredProcessor;
import ic2ce.neoforge.processing.menu.MetalFormingMenu;
import ic2ce.neoforge.registry.BlockEntityDefinitions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extrusion, rolling and cutting bind different managers and never carry progress between modes.
 */
@NotClassic
public class MetalFormer extends PoweredProcessor<IRecipeInput, Collection<ItemStack>, ItemStack>
    implements INetworkClientTileEntityEventListener {
  private int mode;
  public static final int EventSwitch = 0;

  public MetalFormer(BlockPos position, BlockState state) {
    super(BlockEntityDefinitions.METAL_FORMER, position, state, 10, 200, 1);
    inputSlot = new ItemRecipeInput(this, "input", 1, Recipes.metalformerExtruding);
  }

  @Override
  public void load(CompoundTag data) {
    super.load(data);
    int saved = data.getIntOr("mode", 0);
    setMode(saved >= 0 && saved < 3 ? saved : 0);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    data.putInt("mode", mode);
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int selected) {
    var manager =
        switch (selected) {
          case 0 -> Recipes.metalformerExtruding;
          case 1 -> Recipes.metalformerRolling;
          case 2 -> Recipes.metalformerCutting;
          default -> throw new IllegalArgumentException("Invalid metal former mode: " + selected);
        };
    inputSlot.setRecipeManager(manager);
    if (selected != mode) {
      progress = 0;
      guiProgress = 0;
      recipeResult = null;
      processResult = null;
    }
    mode = selected;
  }

  private void cycleMode() {
    setMode((mode + 1) % 3);
    setChanged();
  }

  @Override
  public void onNetworkEvent(Player player, int event) {
    if (event == EventSwitch) cycleMode();
  }

  @Override
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (field.equals("mode")) setMode(mode);
  }

  @Override
  public OwnedMenu<MetalFormer> createServerScreenHandler(int id, Player player) {
    return new MetalFormingMenu(id, player.getInventory(), this);
  }

  @Override
  public OwnedMenu<?> createClientScreenHandler(int id, Inventory inventory, GrowingBuffer data) {
    return new MetalFormingMenu(id, inventory, this);
  }

  @Override
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(
        UpgradableProperty.Processing,
        UpgradableProperty.Transformer,
        UpgradableProperty.EnergyStorage,
        UpgradableProperty.ItemConsuming,
        UpgradableProperty.ItemProducing);
  }

  @Override
  protected boolean directRecipeOutputPreview() {
    return getClass() == MetalFormer.class;
  }
}
