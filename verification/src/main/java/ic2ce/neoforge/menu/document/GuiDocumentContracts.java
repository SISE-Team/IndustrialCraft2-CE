package ic2ce.neoforge.menu.document;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.xml.sax.SAXException;

/**
 * Every retained GUI asset is decoded; split text, immutable children and malformed structural
 * inputs have independent fixtures.
 */
public final class GuiDocumentContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native GUI document: " + label);
  }

  public static Result run() {
    return new GuiDocumentContracts().exercise();
  }

  private ScreenDocument.GuiNode parse(String source) throws Exception {
    var method = ScreenDocument.class.getDeclaredMethod("parse", InputStream.class, Class.class);
    method.setAccessible(true);
    try {
      return (ScreenDocument.GuiNode)
          method.invoke(
              null,
              new ByteArrayInputStream(source.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
              GuiDocumentContracts.class);
    } catch (java.lang.reflect.InvocationTargetException error) {
      if (error.getCause() instanceof Exception actual) throw actual;
      throw error;
    }
  }

  private Result exercise() {
    try {
      var path =
          java.nio.file.Path.of(
              ScreenDocument.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      int count = 0;
      try (var zip = new ZipFile(path.toFile())) {
        for (var entry : Collections.list(zip.entries()))
          if (entry.getName().startsWith("assets/ic2/guidef/")
              && entry.getName().endsWith(".xml")) {
            String name =
                entry
                    .getName()
                    .substring("assets/ic2/guidef/".length(), entry.getName().length() - 4);
            var document =
                ScreenDocument.parse(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("ic2", name),
                    GuiDocumentContracts.class);
            check(
                document.width > 0 && document.height > 0,
                "actual retained GUI asset decodes: " + name);
            count++;
          }
      }
      check(count == 48, "full retained native GUI material catalog was visited");
      var document = parse("<gui width='178' height='166'><text>12<![CDATA[%]]>name%</text></gui>");
      var node = (ScreenDocument.TextNode) document.getNodes().iterator().next();
      check(
          node.text.get(null, Map.of("name", LiveText.of("Machine"))).equals("12Machine"),
          "split XML character/CDATA events preserve the entire compiled template");
      boolean immutable = false;
      try {
        ((Collection<?>) document.getNodes()).clear();
      } catch (UnsupportedOperationException expected) {
        immutable = true;
      }
      check(immutable, "decoded document children cannot be erased through their published view");
      for (var source :
          List.of(
              "<gui width='0' height='166'/>",
              "<gui width='178' height='166'><text><text/></text></gui>",
              "<!DOCTYPE gui [<!ENTITY sample SYSTEM 'file:///not-a-real-probe-file'>]><gui"
                  + " width='178' height='166'><text>&sample;</text></gui>",
              "<gui width='178' height='166'><slotgrid x='1' y='1' name='test'"
                  + " offset='-1'/></gui>")) {
        boolean rejected = false;
        try {
          parse(source);
        } catch (SAXException expected) {
          rejected = true;
        }
        check(rejected, "invalid native GUI structure is rejected before projection");
      }
      var empty =
          parse(
              "<gui width='178' height='166'><button x='1' y='2' event='0'/><slotgrid x='1' y='2'"
                  + " name='test' cols='3'/></gui>");
      var iterator = empty.getNodes().iterator();
      var button = (ScreenDocument.ButtonNode) iterator.next();
      var grid = (ScreenDocument.SlotGridNode) iterator.next();
      check(
          button.text.get(null, Map.of()).isEmpty(),
          "empty native button content has a valid inert text provider");
      var dimensions = grid.getDimension(7);
      check(
          dimensions.rows == 3 && dimensions.cols == 3 && grid.getDimension(0).rows == 0,
          "actual final partial grid rows and empty layouts use bounded native dimensions");
    } catch (Throwable error) {
      check(false, "document probe exception: " + error);
    }
    return new Result(checks, List.copyOf(failures));
  }
}
