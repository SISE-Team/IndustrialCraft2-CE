package ic2ce.neoforge.menu.document;

import java.util.Objects;
import org.xml.sax.*;

/**
 * Native XML attributes retain strict booleans while accepting full 32-bit material colors without
 * narrowing errors.
 */
public final class XmlUtil {
  private XmlUtil() {}

  public static String getAttr(Attributes attributes, String name) throws SAXException {
    var value = Objects.requireNonNull(attributes).getValue(name);
    if (value == null) throw new SAXException("Missing XML attribute: " + name);
    return value;
  }

  public static String getAttr(Attributes attributes, String name, String fallback) {
    var value = Objects.requireNonNull(attributes).getValue(name);
    return value == null ? fallback : value;
  }

  public static boolean getBoolAttr(Attributes attributes, String name) throws SAXException {
    return parseBool(getAttr(attributes, name));
  }

  public static boolean getBoolAttr(Attributes attributes, String name, boolean fallback)
      throws SAXException {
    var value = attributes.getValue(name);
    return value == null ? fallback : parseBool(value);
  }

  public static boolean parseBool(String text) throws SAXException {
    return switch (text) {
      case "true" -> true;
      case "false" -> false;
      default -> throw new SAXException("Invalid XML boolean: " + text);
    };
  }

  public static int getIntAttr(Attributes attributes, String name) throws SAXException {
    return parseInt(getAttr(attributes, name));
  }

  public static int getIntAttr(Attributes attributes, String name, int fallback) {
    var value = attributes.getValue(name);
    return value == null ? fallback : parseInt(value);
  }

  public static int getIntAttr(
      Attributes attributes, String primary, String secondary, int fallback) {
    var value = attributes.getValue(primary);
    if (value == null) value = attributes.getValue(secondary);
    return value == null ? fallback : parseInt(value);
  }

  public static int parseInt(String text) {
    Objects.requireNonNull(text);
    if (text.startsWith("#") || text.startsWith("0x")) {
      int prefix = text.startsWith("#") ? 1 : 2;
      long value = Long.parseLong(text.substring(prefix), 16);
      if (value < 0 || value > 0xffffffffL)
        throw new NumberFormatException("XML color exceeds 32-bit value");
      return (int) value;
    }
    return Integer.parseInt(text);
  }
}
