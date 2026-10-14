package ic2ce.neoforge.menu.document;

import ic2ce.api.network.INetworkClientTileEntityEventListener;
import ic2ce.core.block.IInventorySlotHolder;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.component.*;
import ic2ce.neoforge.handheld.HandheldStorage;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

/**
 * Independent client projection of the owned native GUI document; server events and handheld
 * prediction remain distinct.
 */
public class DocumentScreen<T extends Container> extends BorderedScreen<DocumentMenu<T>> {
  public static <T extends Container> DocumentScreen<T> create(
      DocumentMenu<T> menu, Inventory inventory, Component title) {
    return new DocumentScreen<>(menu, inventory, title);
  }

  protected DocumentScreen(DocumentMenu<T> menu, Inventory inventory, Component title) {
    super(menu, inventory, title, menu.guiNode.width, menu.guiNode.height);
    project(inventory, menu.guiNode);
  }

  private <B> B base(Class<B> contract) {
    if (!contract.isInstance(menu.base))
      throw new IllegalArgumentException(
          "Native GUI requires "
              + contract.getSimpleName()
              + ": "
              + menu.base.getClass().getName());
    return contract.cast(menu.base);
  }

  private FluidPorts fluids() {
    var entity = base(NativeBlockEntity.class);
    if (!entity.hasComponent(FluidPorts.class))
      throw new IllegalArgumentException("Native GUI requires actual fluid ports");
    return entity.getComponent(FluidPorts.class);
  }

  private void project(Inventory inventory, ScreenDocument.ParentNode parent) {
    for (var node : parent.getNodes()) {
      if (node instanceof ScreenDocument.EnvironmentNode environment
          && environment.environment != ScreenProjection.GAME) continue;
      ScreenWidget<?> widget =
          switch (node) {
            case ScreenDocument.ButtonNode button -> button(button);
            case ScreenDocument.EnergyGaugeNode gauge -> {
              var entity = base(NativeBlockEntity.class);
              if (!entity.hasComponent(ElectricalBuffer.class))
                throw new IllegalArgumentException("Native GUI requires an actual energy ledger");
              yield new ElectricalGauge(this, gauge.x, gauge.y, entity, gauge.style);
            }
            case ScreenDocument.GaugeNode gauge -> {
              var provider = base(ScreenValues.class);
              if (gauge.activeLinked && !(provider instanceof ScreenValues.IActiveGuiValueProvider))
                throw new IllegalArgumentException(
                    "Native gauge requires an actual active-state provider");
              yield new DeviceGauge(this, gauge.x, gauge.y, provider, gauge.name, gauge.style) {
                @Override
                protected boolean isActive(double ratio) {
                  return gauge.activeLinked
                      ? ((ScreenValues.IActiveGuiValueProvider) provider).isGuiValueActive(name)
                      : super.isActive(ratio);
                }
              };
            }
            case ScreenDocument.ImageNode image ->
                ResourceImage.create(
                    this,
                    image.x,
                    image.y,
                    image.width,
                    image.height,
                    image.src,
                    image.baseWidth,
                    image.baseHeight,
                    image.u1,
                    image.v1,
                    image.u2,
                    image.v2);
            case ScreenDocument.PlayerInventoryNode slots -> {
              parent.addElement(
                  this,
                  new InventoryGrid(this, slots.x, slots.y, 9, 3, slots.style, 0, slots.spacing));
              parent.addElement(
                  this,
                  new InventoryGrid(
                      this,
                      slots.x,
                      slots.y + slots.hotbarOffset,
                      9,
                      1,
                      slots.style,
                      0,
                      slots.spacing));
              if (slots.showTitle)
                parent.addElement(
                    this,
                    LiveLabel.create(
                        this,
                        slots.x + 1,
                        slots.y - 10,
                        LiveText.of(inventory.getName()),
                        4210752,
                        false));
              yield null;
            }
            case ScreenDocument.SlotNode slot ->
                new InventoryGrid(this, slot.x, slot.y, 1, 1, slot.style);
            case ScreenDocument.SlotGridNode grid -> {
              var group = base(IInventorySlotHolder.class).getInventorySlot(grid.name);
              if (group == null)
                throw new IllegalArgumentException("Unknown native GUI inventory: " + grid.name);
              var size = grid.getDimension(group.size());
              yield size.rows == 0 || size.cols == 0
                  ? null
                  : new InventoryGrid(
                      this, grid.x, grid.y, size.cols, size.rows, grid.style, 0, grid.spacing);
            }
            case ScreenDocument.TextNode text -> label(text);
            case ScreenDocument.FluidTankNode tank -> {
              var contents = fluids().getFluidTank(tank.name);
              if (contents == null)
                throw new IllegalArgumentException("Unknown native GUI tank: " + tank.name);
              yield switch (tank.type) {
                case NORMAL -> ReservoirGauge.createNormal(this, tank.x, tank.y, contents);
                case PLAIN ->
                    ReservoirGauge.createPlain(
                        this, tank.x, tank.y, tank.width, tank.height, contents);
                case BORDERLESS ->
                    ReservoirGauge.createBorderless(this, tank.x, tank.y, contents, tank.mirrored);
              };
            }
            case ScreenDocument.FluidSlotNode slot -> {
              var contents = fluids().getFluidTank(slot.name);
              if (contents == null)
                throw new IllegalArgumentException("Unknown native GUI fluid slot: " + slot.name);
              yield ReservoirSlot.createFluidSlot(this, slot.x, slot.y, contents);
            }
            default -> null;
          };
      if (widget != null) parent.addElement(this, widget);
      if (node instanceof ScreenDocument.ParentNode branch) project(inventory, branch);
    }
  }

  private ActionButton<?> button(ScreenDocument.ButtonNode node) {
    if (node.type != ScreenDocument.ButtonNode.ButtonType.RECIPE
        && !(menu.base instanceof INetworkClientTileEntityEventListener)
        && !isHandHeldGUI())
      throw new IllegalArgumentException("Native GUI button requires a server event receiver");
    ActionButton<?> button =
        switch (node.type) {
          case VANILLA ->
              new NativeButton(
                  this,
                  node.x,
                  node.y,
                  node.width,
                  node.height,
                  createEventSender(node.eventID, node.eventName));
          case CUSTOM, TRANSPARENT ->
              new AtlasButton(
                  this,
                  node.x,
                  node.y,
                  node.width,
                  node.height,
                  createEventSender(node.eventID, node.eventName));
          case RECIPE ->
              RecipeOverlay.canUse() && node.eventName != null
                  ? new RecipeOverlay(
                      this, node.x, node.y, node.width, node.height, node.eventName.split(",[ ]*"))
                  : null;
        };
    if (button == null) return null;
    var text =
        node.type == ScreenDocument.ButtonNode.ButtonType.RECIPE ? LiveText.of("") : node.text;
    if (node.icon == null)
      button.withText(() -> text.get(menu.base, Map.of("name", LiveText.of(title))));
    else {
      button.withIcon(() -> node.icon.copy());
      button.withTooltip(text.get(menu.base, Map.of("name", LiveText.of(title))));
    }
    return button;
  }

  private LiveLabel label(ScreenDocument.TextNode text) {
    int x =
        switch (text.align) {
          case Start -> text.x;
          case Center -> text.x + imageWidth / 2;
          case End -> text.x + imageWidth;
        };
    return text.rightAligned
        ? LiveLabel.createRightAligned(
            this,
            x,
            text.y,
            text.width,
            text.height,
            text.text,
            text.color,
            text.shadow,
            text.xOffset,
            text.yOffset,
            text.centerX,
            text.centerY)
        : LiveLabel.create(
            this,
            x,
            text.y,
            text.width,
            text.height,
            text.text,
            text.color,
            text.shadow,
            text.xOffset,
            text.yOffset,
            text.centerX,
            text.centerY);
  }

  protected ClickAction createEventSender(int event, String named) {
    if (isHandHeldGUI()) {
      String name = named == null ? Integer.toString(event) : named;
      return ignored -> {
        ProjectRuntime.network.get(false).sendContainerEvent(menu, name);
        ((HandheldStorage) menu.base).onEvent(name);
      };
    }
    if (named != null)
      throw new IllegalArgumentException("Native block GUI requires a numeric event identifier");
    return createEventSender(event);
  }

  protected boolean isHandHeldGUI() {
    return menu.base instanceof HandheldStorage;
  }

  @Override
  public void addElement(ScreenWidget<?> element) {
    super.addElement(element);
  }
}
