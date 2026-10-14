package ic2ce.integration.jei.recipe.machine;

import ic2ce.integration.jeirei.SlotPosition;
import ic2ce.neoforge.block.*;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.menu.document.*;
import ic2ce.neoforge.processing.PoweredProcessor;
import java.util.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;

/** Independently compiled immutable JEI projection of the retained device screen document. */
public class DynamicCategory extends IORecipeCategory {
  private record Element(IDrawable drawable, SlotPosition position) {}

  protected final int xOffset, yOffset;
  private final List<Element> elements;
  private final List<SlotPosition> inputSlots, outputSlots;
  private final IDrawable background;
  private final RecipeType<IORecipeWrapper> recipeType;

  public DynamicCategory(
      MachineBlockType block, RecipeType<IORecipeWrapper> type, IGuiHelper graphics) {
    super(block);
    recipeType = Objects.requireNonNull(type);
    var body = new ArrayList<Element>();
    var inputs = new ArrayList<SlotPosition>();
    var outputs = new ArrayList<SlotPosition>();
    var nativeDevice =
        Objects.requireNonNull(block.getDummyTe(), "Missing native recipe display device");
    compile(
        graphics,
        ScreenDocument.parse(BuiltInRegistries.BLOCK.getKey(block), nativeDevice.getClass()),
        nativeDevice,
        body,
        inputs,
        outputs);
    int left = 0, top = 0, right = 16, bottom = 16;
    if (!body.isEmpty()) {
      left = Integer.MAX_VALUE;
      top = Integer.MAX_VALUE;
      right = Integer.MIN_VALUE;
      bottom = Integer.MIN_VALUE;
      for (var element : body) {
        var position = element.position();
        left = Math.min(left, position.getX());
        top = Math.min(top, position.getY());
        right = Math.max(right, Math.addExact(position.getX(), element.drawable().getWidth()));
        bottom = Math.max(bottom, Math.addExact(position.getY(), element.drawable().getHeight()));
      }
    }
    xOffset = -left;
    yOffset = -top;
    background = graphics.createBlankDrawable(Math.max(1, right - left), Math.max(1, bottom - top));
    elements = List.copyOf(body);
    inputSlots = List.copyOf(inputs);
    outputSlots = List.copyOf(outputs);
  }

  private void compile(
      IGuiHelper graphics,
      ScreenDocument.ParentNode parent,
      NativeBlockEntity device,
      List<Element> body,
      List<SlotPosition> inputs,
      List<SlotPosition> outputs) {
    for (var raw : parent.getNodes())
      switch (raw) {
        case ScreenDocument.EnvironmentNode environment -> {
          if (environment.environment == ScreenProjection.JEI)
            compile(graphics, environment, device, body, inputs, outputs);
        }
        case ScreenDocument.EnergyGaugeNode node -> {
          var properties = node.style.properties;
          gauge(graphics, body, node.x, node.y, properties, 300, true, false);
        }
        case ScreenDocument.GaugeNode node ->
            gauge(
                graphics,
                body,
                node.x,
                node.y,
                node.style.getProperties(),
                getProcessSpeed(node.name),
                false,
                node.style == FillGauge.GaugeStyle.HeatCentrifuge);
        case ScreenDocument.ImageNode node -> {
          var image =
              graphics
                  .drawableBuilder(node.src, node.u1, node.v1, node.width, node.height)
                  .setTextureSize(node.baseWidth, node.baseHeight)
                  .build();
          body.add(new Element(image, new SlotPosition(node.x, node.y)));
        }
        case ScreenDocument.SlotNode node ->
            slot(
                graphics,
                body,
                inputs,
                outputs,
                node.name,
                new SlotPosition(node.x, node.y, node.style));
        case ScreenDocument.SlotGridNode node -> {
          if (!(device instanceof InventoryBlockEntity inventory))
            throw new IllegalArgumentException("Recipe slot grid requires a native inventory");
          var slot = inventory.getInventorySlot(node.name);
          if (slot == null)
            throw new IllegalArgumentException("Unknown native recipe display slot: " + node.name);
          int count = slot.size() - node.offset;
          if (count <= 0) break;
          var grid = node.getDimension(slot.size());
          int visible = Math.min(count, Math.multiplyExact(grid.rows, grid.cols));
          int sx = Math.addExact(node.style.width, node.spacing),
              sy = Math.addExact(node.style.height, node.spacing);
          if (sx <= 0 || sy <= 0)
            throw new IllegalArgumentException("Nonpositive recipe slot-grid stride");
          for (int index = 0; index < visible; index++) {
            int column = node.vertical ? index / grid.rows : index % grid.cols,
                row = node.vertical ? index % grid.rows : index / grid.cols;
            slot(
                graphics,
                body,
                inputs,
                outputs,
                node.name,
                new SlotPosition(node.x + sx * column, node.y + sy * row, node.style));
          }
        }
        case ScreenDocument.ParentNode group ->
            compile(graphics, group, device, body, inputs, outputs);
        default -> {}
      }
  }

  private static void gauge(
      IGuiHelper graphics,
      List<Element> body,
      int x,
      int y,
      FillGauge.GaugeProperties style,
      int duration,
      boolean energy,
      boolean staticFill) {
    if (style.bgWidth > 0 && style.bgHeight > 0)
      body.add(
          new Element(
              graphics.createDrawable(
                  style.texture,
                  energy ? style.uBgInactive : style.uBgActive,
                  energy ? style.vBgInactive : style.vBgActive,
                  style.bgWidth,
                  style.bgHeight),
              new SlotPosition(x + style.bgXOffset, y + style.bgYOffset)));
    if (style.innerWidth <= 0 || style.innerHeight <= 0) return;
    var fill =
        graphics.createDrawable(
            style.texture, style.uInner, style.vInner, style.innerWidth, style.innerHeight);
    var direction =
        style.vertical
            ? (style.reverse
                ? IDrawableAnimated.StartDirection.TOP
                : IDrawableAnimated.StartDirection.BOTTOM)
            : (style.reverse
                ? IDrawableAnimated.StartDirection.RIGHT
                : IDrawableAnimated.StartDirection.LEFT);
    body.add(
        new Element(
            staticFill
                ? fill
                : graphics.createAnimatedDrawable(fill, Math.max(1, duration), direction, energy),
            new SlotPosition(x, y)));
  }

  private static void slot(
      IGuiHelper graphics,
      List<Element> body,
      List<SlotPosition> inputs,
      List<SlotPosition> outputs,
      String name,
      SlotPosition position) {
    var style = position.getStyle();
    body.add(
        new Element(
            graphics.createDrawable(
                ScreenWidget.commonTexture, style.u, style.v, style.width, style.height),
            position));
    var center = new SlotPosition(position, (style.width - 16) / 2, (style.height - 16) / 2);
    String canonical = name.toLowerCase(Locale.ROOT);
    if (canonical.contains("input")) inputs.add(center);
    else if (canonical.contains("output")) outputs.add(center);
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, IORecipeWrapper recipe, IFocusGroup focuses) {
    addRecipeSlots(builder, recipe, focuses, xOffset, yOffset);
  }

  @Override
  public void draw(
      IORecipeWrapper recipe,
      IRecipeSlotsView slots,
      GuiGraphicsExtractor graphics,
      double x,
      double y) {
    background.draw(graphics);
    for (var element : elements)
      element
          .drawable()
          .draw(graphics, element.position().getX() + xOffset, element.position().getY() + yOffset);
  }

  @Override
  public int getWidth() {
    return background.getWidth();
  }

  @Override
  public int getHeight() {
    return background.getHeight();
  }

  @Override
  public RecipeType<IORecipeWrapper> getRecipeType() {
    return recipeType;
  }

  public IDrawable getBackground() {
    return background;
  }

  @Override
  protected List<SlotPosition> getInputSlotPos() {
    return inputSlots;
  }

  @Override
  protected List<SlotPosition> getOutputSlotPos() {
    return outputSlots;
  }

  protected int getProcessSpeed(String name) {
    return "progress".equals(name) && block.getDummyTe() instanceof PoweredProcessor processor
        ? Math.max(1, processor.defaultOperationLength / 3)
        : 200;
  }
}
