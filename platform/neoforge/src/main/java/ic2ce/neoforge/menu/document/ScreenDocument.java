package ic2ce.neoforge.menu.document;

import ic2ce.core.util.*;
import ic2ce.neoforge.client.widget.*;
import ic2ce.neoforge.config.NativeConfigValues;
import java.io.*;
import java.util.*;
import javax.xml.stream.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Bounded native GUI document decoding with immutable children, complete text accumulation and
 * inherited conditions.
 */
public final class ScreenDocument {
  private ScreenDocument() {}

  public static GuiNode parse(Identifier id, Class<?> baseClass) {
    String resource = "/assets/" + id.getNamespace() + "/guidef/" + id.getPath() + ".xml";
    try (var stream = ScreenDocument.class.getResourceAsStream(resource)) {
      if (stream == null) throw new FileNotFoundException(resource);
      return parse(stream, baseClass);
    } catch (IOException | SAXException error) {
      throw new IllegalArgumentException("Unable to decode native GUI " + id, error);
    }
  }

  private record Frame(Node node, StringBuilder content, String tag) {}

  private static GuiNode parse(InputStream source, Class<?> type) throws SAXException, IOException {
    var factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    XMLStreamReader reader = null;
    var stack = new ArrayDeque<Frame>();
    GuiNode root = null;
    int nodes = 0, characters = 0;
    try {
      reader = factory.createXMLStreamReader(source);
      while (reader.hasNext()) {
        int event = reader.next();
        if (event == XMLStreamConstants.DTD)
          throw new SAXException("Native GUI document cannot contain a DTD");
        if (event == XMLStreamConstants.START_ELEMENT) {
          if (++nodes > 4096 || stack.size() > 64)
            throw new SAXException("Native GUI document exceeds node/depth bounds");
          String tag = reader.getLocalName().toLowerCase(Locale.ROOT);
          var attributes = new AttributesImpl();
          for (int attribute = 0; attribute < reader.getAttributeCount(); attribute++)
            attributes.addAttribute(
                "",
                reader.getAttributeLocalName(attribute),
                reader.getAttributeLocalName(attribute),
                "CDATA",
                reader.getAttributeValue(attribute));
          Node node;
          if (stack.isEmpty()) {
            if (root != null || !tag.equals("gui"))
              throw new SAXException("Native GUI requires one root gui element");
            node = root = new GuiNode(attributes, type);
          } else {
            if (!(stack.peek().node instanceof ParentNode parent))
              throw new SAXException(
                  "Native GUI leaf cannot contain an element: " + stack.peek().tag);
            node = create(tag, parent, attributes);
            parent.addNode(node);
          }
          stack.push(new Frame(node, new StringBuilder(), tag));
        } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
          String content = reader.getText();
          characters += content.length();
          if (characters > 1_048_576) throw new SAXException("Native GUI text exceeds bounds");
          if (!stack.isEmpty()) stack.peek().content.append(content);
          else if (!content.isBlank()) throw new SAXException("Native GUI text outside its root");
        } else if (event == XMLStreamConstants.END_ELEMENT) {
          if (stack.isEmpty()) throw new SAXException("Native GUI has an unmatched end element");
          var frame = stack.pop();
          String content = frame.content.toString().strip();
          if (!content.isEmpty()) frame.node.setContent(content);
        }
      }
      if (root == null || !stack.isEmpty())
        throw new SAXException("Incomplete native GUI document");
      ((ParentNode) root).freeze();
      return root;
    } catch (XMLStreamException | IllegalArgumentException error) {
      throw new SAXException("Invalid native GUI document", error);
    } finally {
      if (reader != null)
        try {
          reader.close();
        } catch (XMLStreamException ignored) {
        }
    }
  }

  private static Node create(String tag, ParentNode parent, Attributes attributes)
      throws SAXException {
    return switch (tag) {
      case "environment" -> new EnvironmentNode(parent, attributes);
      case "key" -> KeyOnlyNode.getForKey(parent, attributes);
      case "only" -> LogicalNode.getForValue(parent, attributes);
      case "tooltip" -> new TooltipNode(parent, attributes.getValue("text"));
      case "button" -> new ButtonNode(parent, attributes);
      case "energygauge" -> new EnergyGaugeNode(parent, attributes);
      case "gauge" -> new GaugeNode(parent, attributes);
      case "image" -> new ImageNode(parent, attributes);
      case "playerinventory" -> new PlayerInventoryNode(parent, attributes);
      case "slot" -> new SlotNode(parent, attributes);
      case "slotgrid" -> new SlotGridNode(parent, attributes);
      case "slothologram" -> new SlotHologramNode(parent, attributes);
      case "text" -> new TextNode(parent, attributes);
      case "fluidtank" -> new FluidTankNode(parent, attributes);
      case "fluidslot" -> new FluidSlotNode(parent, attributes);
      default -> throw new SAXException("Unknown native GUI node: " + tag);
    };
  }

  private static int dimension(int value) throws SAXException {
    if (value <= 0 || value > 4096)
      throw new SAXException("Invalid native GUI dimension: " + value);
    return value;
  }

  private static boolean inverted(Attributes attributes) throws SAXException {
    String value = attributes.getValue("inverted");
    return value != null && (value.isEmpty() || XmlUtil.parseBool(value));
  }

  private static InventoryGrid.SlotStyle style(Attributes attributes) throws SAXException {
    var style = InventoryGrid.SlotStyle.get(XmlUtil.getAttr(attributes, "style", "normal"));
    if (style == null) throw new SAXException("Unknown native slot material");
    return style;
  }

  public enum NodeType {
    gui,
    environment,
    key,
    only,
    tooltip,
    button,
    energygauge,
    gauge,
    image,
    playerinventory,
    slot,
    slotgrid,
    slothologram,
    text,
    fluidtank,
    fluidslot;

    public static NodeType get(String name) {
      try {
        return valueOf(name.toLowerCase(Locale.ROOT));
      } catch (IllegalArgumentException | NullPointerException missing) {
        return null;
      }
    }
  }

  public abstract static class Node {
    final ParentNode parent;

    Node(ParentNode parent) {
      this.parent = parent;
    }

    public abstract NodeType getType();

    public void setContent(String text) throws SAXException {
      throw new SAXException("Unexpected native GUI text in " + getType());
    }
  }

  public abstract static class ParentNode extends Node {
    private final List<Node> children = new ArrayList<>();
    private final List<Node> view = Collections.unmodifiableList(children);
    private boolean frozen;

    ParentNode(ParentNode parent) {
      super(parent);
    }

    public void addNode(Node node) {
      Objects.requireNonNull(node);
      if (frozen || node.parent != this || node == this)
        throw new IllegalArgumentException("Native GUI child ownership violation");
      children.add(node);
    }

    public Iterable<Node> getNodes() {
      return view;
    }

    public Class<?> getBaseClass() {
      return parent == null ? null : parent.getBaseClass();
    }

    private void freeze() {
      if (frozen) return;
      for (var child : children) if (child instanceof ParentNode group) group.freeze();
      frozen = true;
    }

    void addElement(DocumentScreen<?> gui, ScreenWidget<?> element) {
      var handlers = addHandlers(gui, element, new ArrayList<>());
      if (!handlers.isEmpty())
        element.withEnableHandler(
            WidgetCondition.EnableHandlers.and(handlers.toArray(WidgetCondition[]::new)));
      gui.addElement(element);
    }

    protected Collection<WidgetCondition> addHandlers(
        DocumentScreen<?> gui, ScreenWidget<?> element, Collection<WidgetCondition> handlers) {
      return parent == null ? handlers : parent.addHandlers(gui, element, handlers);
    }
  }

  public static final class GuiNode extends ParentNode {
    private final Class<?> baseClass;
    final int width, height;

    GuiNode(Attributes attributes, Class<?> type) throws SAXException {
      super(null);
      baseClass = Objects.requireNonNull(type);
      width = dimension(XmlUtil.getIntAttr(attributes, "width"));
      height = dimension(XmlUtil.getIntAttr(attributes, "height"));
    }

    @Override
    public NodeType getType() {
      return NodeType.gui;
    }

    @Override
    public Class<?> getBaseClass() {
      return baseClass;
    }
  }

  public static final class EnvironmentNode extends ParentNode {
    public final ScreenProjection environment;

    EnvironmentNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      environment = ScreenProjection.get(XmlUtil.getAttr(attributes, "name"));
      if (environment == null) throw new SAXException("Unknown native GUI environment");
    }

    @Override
    public NodeType getType() {
      return NodeType.environment;
    }
  }

  public abstract static class KeyOnlyNode extends ParentNode {
    protected final boolean inverted;

    protected KeyOnlyNode(ParentNode parent, boolean inverted) {
      super(parent);
      this.inverted = inverted;
    }

    static KeyOnlyNode getForKey(ParentNode parent, Attributes attributes) throws SAXException {
      boolean reversed = inverted(attributes);
      return switch (XmlUtil.getAttr(attributes, "key").toLowerCase(Locale.ROOT)) {
        case "shift" -> new ShiftOnlyNode(parent, reversed);
        case "control" -> new ControlOnlyNode(parent, reversed);
        case "alt" -> new AltOnlyNode(parent, reversed);
        default -> throw new SAXException("Unknown native GUI key");
      };
    }

    @Override
    public final NodeType getType() {
      return NodeType.key;
    }

    protected abstract boolean isKeyDown();

    @Override
    protected Collection<WidgetCondition> addHandlers(
        DocumentScreen<?> gui, ScreenWidget<?> element, Collection<WidgetCondition> handlers) {
      WidgetCondition condition = this::isKeyDown;
      handlers.add(inverted ? WidgetCondition.EnableHandlers.not(condition) : condition);
      return super.addHandlers(gui, element, handlers);
    }
  }

  static final class ShiftOnlyNode extends KeyOnlyNode {
    ShiftOnlyNode(ParentNode parent, boolean reversed) {
      super(parent, reversed);
    }

    @Override
    protected boolean isKeyDown() {
      return Minecraft.getInstance().hasShiftDown();
    }
  }

  static final class ControlOnlyNode extends KeyOnlyNode {
    ControlOnlyNode(ParentNode parent, boolean reversed) {
      super(parent, reversed);
    }

    @Override
    protected boolean isKeyDown() {
      return Minecraft.getInstance().hasControlDown();
    }
  }

  static final class AltOnlyNode extends KeyOnlyNode {
    AltOnlyNode(ParentNode parent, boolean reversed) {
      super(parent, reversed);
    }

    @Override
    protected boolean isKeyDown() {
      return Minecraft.getInstance().hasAltDown();
    }
  }

  public static final class LogicalNode extends ParentNode {
    final String condition;
    protected final boolean inverted;

    protected LogicalNode(ParentNode parent, String condition, boolean reversed) {
      super(parent);
      this.condition = Objects.requireNonNull(condition);
      inverted = reversed;
    }

    static LogicalNode getForValue(ParentNode parent, Attributes attributes) throws SAXException {
      return new LogicalNode(parent, XmlUtil.getAttr(attributes, "if"), inverted(attributes));
    }

    @Override
    public NodeType getType() {
      return NodeType.only;
    }

    @Override
    protected Collection<WidgetCondition> addHandlers(
        DocumentScreen<?> gui, ScreenWidget<?> element, Collection<WidgetCondition> handlers) {
      if (!(gui.getContainer().base instanceof ScreenConditions provider))
        throw new IllegalArgumentException("Native GUI conditional requires a condition provider");
      WidgetCondition condition = () -> provider.getGuiState(this.condition);
      handlers.add(inverted ? WidgetCondition.EnableHandlers.not(condition) : condition);
      return super.addHandlers(gui, element, handlers);
    }
  }

  public static final class TooltipNode extends ParentNode {
    final String tooltip;

    public TooltipNode(ParentNode parent, String text) {
      super(parent);
      tooltip = text;
    }

    @Override
    public NodeType getType() {
      return NodeType.tooltip;
    }

    @Override
    void addElement(DocumentScreen<?> gui, ScreenWidget<?> element) {
      super.addElement(gui, element.withTooltip(tooltip));
    }
  }

  public static final class ButtonNode extends Node {
    final int x, y, width, height, eventID;
    final String eventName;
    final ItemStack icon;
    final ButtonType type;
    LiveText.ITextProvider text = LiveText.of("");

    ButtonNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      width = dimension(XmlUtil.getIntAttr(attributes, "width", 16));
      height = dimension(XmlUtil.getIntAttr(attributes, "height", 16));
      String event = XmlUtil.getAttr(attributes, "event");
      int numeric = 0;
      String named = null;
      try {
        numeric = XmlUtil.parseInt(event);
      } catch (NumberFormatException missing) {
        named = event;
      }
      eventID = numeric;
      eventName = named;
      type = ButtonType.get(XmlUtil.getAttr(attributes, "type", "vanilla"));
      if (type == null) throw new SAXException("Unknown native GUI button type");
      String image = XmlUtil.getAttr(attributes, "icon", "none");
      if (image.equals("none")) icon = null;
      else
        try {
          icon = NativeConfigValues.asStack(image);
        } catch (java.text.ParseException error) {
          throw new SAXException("Unknown native button icon", error);
        }
    }

    @Override
    public NodeType getType() {
      return NodeType.button;
    }

    @Override
    public void setContent(String source) {
      text = LiveText.parse(source, parent.getBaseClass());
    }

    public enum ButtonType {
      VANILLA,
      CUSTOM,
      TRANSPARENT,
      RECIPE;

      public static ButtonType get(String name) {
        try {
          return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException missing) {
          return null;
        }
      }
    }
  }

  public static final class EnergyGaugeNode extends Node {
    public final int x, y;
    public final ElectricalGauge.EnergyGaugeStyle style;

    EnergyGaugeNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      style = ElectricalGauge.EnergyGaugeStyle.get(XmlUtil.getAttr(attributes, "style", "bolt"));
      if (style == null) throw new SAXException("Unknown native energy gauge style");
    }

    @Override
    public NodeType getType() {
      return NodeType.energygauge;
    }
  }

  public static final class GaugeNode extends Node {
    public final int x, y;
    public final String name;
    public final FillGauge.IGaugeStyle style;
    public final boolean activeLinked;

    GaugeNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      name = XmlUtil.getAttr(attributes, "name");
      style = FillGauge.GaugeStyle.get(XmlUtil.getAttr(attributes, "style", "normal"));
      if (style == null) throw new SAXException("Unknown native progress gauge style");
      activeLinked = XmlUtil.getBoolAttr(attributes, "active", false);
    }

    @Override
    public NodeType getType() {
      return NodeType.gauge;
    }
  }

  public static final class ImageNode extends Node {
    public final int x, y, width, height, baseWidth, baseHeight, u1, v1, u2, v2;
    public final Identifier src;

    ImageNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      width = XmlUtil.getIntAttr(attributes, "width", -1);
      height = XmlUtil.getIntAttr(attributes, "height", -1);
      baseWidth = XmlUtil.getIntAttr(attributes, "basewidth", "basesize", 0);
      baseHeight = XmlUtil.getIntAttr(attributes, "baseheight", "basesize", 0);
      u1 = XmlUtil.getIntAttr(attributes, "u", "u1", 0);
      v1 = XmlUtil.getIntAttr(attributes, "v", "v1", 0);
      u2 = XmlUtil.getIntAttr(attributes, "u2", -1);
      v2 = XmlUtil.getIntAttr(attributes, "v2", -1);
      String path = XmlUtil.getAttr(attributes, "src");
      if (path.isEmpty()) throw new SAXException("Empty native GUI image");
      int separator = path.indexOf(':');
      String namespace = separator < 0 ? "ic2" : path.substring(0, separator),
          file = separator < 0 ? path : path.substring(separator + 1);
      src =
          Identifier.fromNamespaceAndPath(namespace, file.endsWith(".png") ? file : file + ".png");
    }

    @Override
    public NodeType getType() {
      return NodeType.image;
    }
  }

  protected static final class PlayerInventoryNode extends Node {
    final int x, y, spacing, hotbarOffset;
    final boolean showTitle;
    final InventoryGrid.SlotStyle style;

    PlayerInventoryNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      spacing = XmlUtil.getIntAttr(attributes, "spacing", 0);
      hotbarOffset = XmlUtil.getIntAttr(attributes, "hotbaroffset", 58);
      showTitle = XmlUtil.getBoolAttr(attributes, "title", true);
      style = style(attributes);
    }

    @Override
    public NodeType getType() {
      return NodeType.playerinventory;
    }
  }

  public static class SlotNode extends Node {
    public final int x, y, index;
    public final String name;
    public final InventoryGrid.SlotStyle style;

    SlotNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      name = XmlUtil.getAttr(attributes, "name");
      index = XmlUtil.getIntAttr(attributes, "index", 0);
      if (index < 0) throw new SAXException("Negative native GUI slot index");
      style = style(attributes);
    }

    @Override
    public NodeType getType() {
      return NodeType.slot;
    }
  }

  public static final class SlotHologramNode extends SlotNode {
    public final int index, stackSizeLimit;

    SlotHologramNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent, attributes);
      index = super.index;
      stackSizeLimit = dimension(XmlUtil.getIntAttr(attributes, "stack", 64));
    }

    @Override
    public NodeType getType() {
      return NodeType.slothologram;
    }
  }

  public static final class SlotGridNode extends Node {
    public final int x, y, spacing, offset, rows, cols;
    public final String name;
    public final boolean vertical;
    public final InventoryGrid.SlotStyle style;

    SlotGridNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      name = XmlUtil.getAttr(attributes, "name");
      spacing = XmlUtil.getIntAttr(attributes, "spacing", 0);
      offset = XmlUtil.getIntAttr(attributes, "offset", 0);
      rows = XmlUtil.getIntAttr(attributes, "rows", -1);
      cols = XmlUtil.getIntAttr(attributes, "cols", -1);
      vertical = XmlUtil.getBoolAttr(attributes, "vertical", false);
      style = style(attributes);
      if (offset < 0 || rows > 4096 || cols > 4096)
        throw new SAXException("Invalid native GUI slot grid");
    }

    @Override
    public NodeType getType() {
      return NodeType.slotgrid;
    }

    public SlotGridDimension getDimension(int total) {
      int size = total - offset;
      if (size <= 0) return new SlotGridDimension(0, 0);
      if (!vertical) {
        if (cols > 0) return new SlotGridDimension(Math.max(rows, (size + cols - 1) / cols), cols);
        int height = rows > 0 ? rows : Math.max(1, (int) Math.sqrt(size));
        int width = rows > 0 ? (size + height - 1) / height : height;
        return new SlotGridDimension(rows > 0 ? height : (size + width - 1) / width, width);
      }
      if (rows > 0) return new SlotGridDimension(rows, Math.max(cols, (size + rows - 1) / rows));
      int width = cols > 0 ? cols : Math.max(1, (int) Math.sqrt(size));
      int height = cols > 0 ? (size + width - 1) / width : width;
      return new SlotGridDimension(height, cols > 0 ? width : (size + height - 1) / height);
    }

    public static final class SlotGridDimension {
      public final int rows, cols;

      public SlotGridDimension(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
      }
    }
  }

  public static final class TextNode extends Node {
    public final int x, y, width, height, xOffset, yOffset, color;
    public final boolean centerX, centerY, rightAligned, shadow;
    public final LiveLabel.TextAlignment align;
    public LiveText.ITextProvider text = LiveText.of("");

    TextNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x", 0);
      y = XmlUtil.getIntAttr(attributes, "y", 0);
      width = XmlUtil.getIntAttr(attributes, "width", -1);
      height = XmlUtil.getIntAttr(attributes, "height", -1);
      xOffset = XmlUtil.getIntAttr(attributes, "xoffset", 0);
      yOffset = XmlUtil.getIntAttr(attributes, "yoffset", 0);
      align = LiveLabel.TextAlignment.get(XmlUtil.getAttr(attributes, "align", "start"));
      if (align == null) throw new SAXException("Unknown native text alignment");
      String center =
          XmlUtil.getAttr(attributes, "center", align == LiveLabel.TextAlignment.Center ? "x" : "");
      centerX = center.indexOf('x') >= 0;
      centerY = center.indexOf('y') >= 0;
      rightAligned = XmlUtil.getBoolAttr(attributes, "right", false);
      color = XmlUtil.getIntAttr(attributes, "color", 4210752);
      shadow = attributes.getIndex("shadow") >= 0;
    }

    @Override
    public NodeType getType() {
      return NodeType.text;
    }

    @Override
    public void setContent(String source) {
      text = LiveText.parse(source, parent.getBaseClass());
    }
  }

  public static final class FluidSlotNode extends Node {
    final int x, y;
    final String name;

    FluidSlotNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      name = XmlUtil.getAttr(attributes, "name");
    }

    @Override
    public NodeType getType() {
      return NodeType.fluidslot;
    }
  }

  public static final class FluidTankNode extends Node {
    final int x, y, width, height;
    final String name;
    final boolean mirrored;
    final TankType type;

    FluidTankNode(ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      x = XmlUtil.getIntAttr(attributes, "x");
      y = XmlUtil.getIntAttr(attributes, "y");
      name = XmlUtil.getAttr(attributes, "name");
      type = TankType.get(XmlUtil.getAttr(attributes, "type", "normal"));
      if (type == null) throw new SAXException("Unknown native fluid tank style");
      width = type == TankType.PLAIN ? dimension(XmlUtil.getIntAttr(attributes, "width")) : -1;
      height = type == TankType.PLAIN ? dimension(XmlUtil.getIntAttr(attributes, "height")) : -1;
      mirrored = type == TankType.BORDERLESS && XmlUtil.getBoolAttr(attributes, "mirrored");
    }

    @Override
    public NodeType getType() {
      return NodeType.fluidtank;
    }

    public enum TankType {
      NORMAL,
      PLAIN,
      BORDERLESS;

      public static TankType get(String name) {
        try {
          return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException missing) {
          return null;
        }
      }
    }
  }
}
