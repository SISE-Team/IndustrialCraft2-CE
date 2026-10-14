package ic2ce.core.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;

/** Standalone configuration implementation; the public name preserves addon compatibility. */
public class Config {
  public final String name;
  private final Config parent;
  private final Map<String, Config> sections = new LinkedHashMap<>();
  private final Map<String, Value> values = new LinkedHashMap<>();
  private String comment;
  private boolean saveWithParent = true;
  private volatile Path cachedPath;

  private record Path(String source, List<String> parts) {}

  public Config(String name) {
    this(null, name, "");
  }

  private Config(Config parent, String name, String comment) {
    this.parent = parent;
    this.name = name;
    this.comment = comment;
  }

  public Config getRoot() {
    Config section = this;
    while (section.parent != null) section = section.parent;
    return section;
  }

  private List<String> path(String source) {
    Path current = cachedPath;
    if (current == null || !current.source().equals(source)) {
      current = new Path(source, List.copyOf(tokens(source, '/')));
      cachedPath = current;
    }
    return current.parts();
  }

  private Config section(List<String> path, int count, boolean create) {
    Config current = this;
    for (int i = 0; i < count; i++) {
      String key = path.get(i);
      assert !key.isEmpty();
      Config child = current.sections.get(key);
      if (child == null) {
        if (!create) return null;
        child = new Config(current, key, "");
        current.sections.put(key, child);
      }
      current = child;
    }
    return current;
  }

  public Config getSub(String key) {
    List<String> parts = path(key);
    return section(parts, parts.size(), false);
  }

  public Config addSub(String key, String text) {
    Config child = sections.computeIfAbsent(key, k -> new Config(this, k, text));
    child.comment = text;
    return child;
  }

  public Value get(String key) {
    List<String> parts = path(key);
    Config owner = section(parts, parts.size() - 1, false);
    return owner == null ? null : owner.values.get(parts.getLast());
  }

  public void set(String key, Value value) {
    List<String> parts = path(key);
    assert parts.getLast().equals(value.name);
    section(parts, parts.size() - 1, true).values.put(parts.getLast(), value);
  }

  public <T> void set(String key, T value) {
    List<String> parts = path(key);
    String leaf = parts.getLast();
    section(parts, parts.size() - 1, true)
        .values
        .computeIfAbsent(leaf, k -> new Value(k, "", null))
        .set(value);
  }

  public Value remove(String key) {
    List<String> parts = path(key);
    return section(parts, parts.size() - 1, true).values.remove(parts.getLast());
  }

  public void clear() {
    sections.clear();
    values.clear();
  }

  public void sort() {
    Map<String, Value> sorted = new TreeMap<>(values);
    values.clear();
    values.putAll(sorted);
  }

  public Iterator<Config> sectionIterator() {
    return sections.values().iterator();
  }

  public boolean hasChildSection() {
    return !sections.isEmpty();
  }

  public int getNumberOfSections() {
    return sections.size();
  }

  public Iterator<Value> valueIterator() {
    return values.values().iterator();
  }

  public boolean isEmptySection() {
    return values.isEmpty();
  }

  public int getNumberOfConfigs() {
    return values.size();
  }

  public void setSaveWithParent(boolean enabled) {
    saveWithParent = enabled;
  }

  public void load(InputStream stream) throws IOException, ParseException {
    try (var reader = new LineNumberReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      Config current = this;
      var comments = new StringBuilder();
      String physical;
      while ((physical = reader.readLine()) != null) {
        String line = trim(physical);
        if (line.isEmpty()) continue;
        if (line.startsWith(";")) {
          if (line.equals(";---")) comments.setLength(0);
          else {
            if (!comments.isEmpty()) comments.append(System.lineSeparator());
            comments.append(line.substring(1).trim());
          }
          continue;
        }
        if (line.startsWith("[")) {
          if (!line.endsWith("]"))
            throw new ParseException(
                "section without closing bracket", reader.getLineNumber(), line);
          List<String> names = tokens(line.substring(1, line.length() - 1), '/');
          names.replaceAll(part -> unescape(part, "[];/", true));
          current = section(names, names.size(), true);
          if (!comments.isEmpty()) current.comment = comments.toString();
          comments.setLength(0);
          continue;
        }
        List<String> pair = tokens(line, '=');
        if (pair.size() != 2)
          throw new ParseException("invalid key-value pair", reader.getLineNumber(), line);
        String key = unescape(pair.getFirst(), "[];=", false);
        if (key.isEmpty()) throw new ParseException("empty key", reader.getLineNumber(), line);
        String content = pair.getLast();
        while (continued(content)) {
          String next = reader.readLine();
          if (next == null)
            throw new ParseException("unfinished continuation", reader.getLineNumber(), line);
          content = content.substring(0, content.length() - 1) + " " + next.trim();
        }
        current.set(
            key,
            new Value(
                key,
                comments.toString(),
                unescape(content, "[];=", false),
                reader.getLineNumber()));
        comments.setLength(0);
      }
    }
  }

  public void load(File file) throws IOException, ParseException {
    try (var stream = new FileInputStream(file)) {
      load(stream);
    }
  }

  public void save(OutputStream stream) throws IOException {
    try (var writer = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
      line(writer, "; " + name);
      line(writer, "; created " + DateFormat.getDateTimeInstance().format(new Date()));
      line(writer, ";---");
      var pending = new ArrayDeque<Config>();
      pending.add(this);
      while (!pending.isEmpty()) {
        Config current = pending.removeFirst();
        if (!current.values.isEmpty() || !current.comment.isEmpty() || current.sections.isEmpty()) {
          writer.newLine();
          if (current != this) {
            comments(writer, current.comment);
            var names = new ArrayDeque<String>();
            for (Config part = current; part != this; part = part.parent)
              names.addFirst(escape(part.name, "[];/", true));
            line(writer, "[" + String.join(" / ", names) + "]");
          }
          for (Value value : current.values.values()) {
            comments(writer, value.comment);
            line(
                writer,
                escape(value.name, "[];=\\", false)
                    + " = "
                    + escape(value.getString(), "[];=\\", false));
          }
        }
        var children = new ArrayList<>(current.sections.values());
        for (int i = children.size() - 1; i >= 0; i--)
          if (children.get(i).saveWithParent) pending.addFirst(children.get(i));
      }
      writer.newLine();
    }
  }

  public void save(File file) throws IOException {
    try (var stream = new FileOutputStream(file)) {
      save(stream);
    }
  }

  private static void line(BufferedWriter writer, String text) throws IOException {
    writer.write(text);
    writer.newLine();
  }

  private static void comments(BufferedWriter writer, String text) throws IOException {
    if (!text.isEmpty()) for (String part : text.split("\n")) line(writer, "; " + part);
  }

  private static boolean continued(String text) {
    int count = 0;
    for (int i = text.length() - 1; i >= 0 && text.charAt(i) == '\\'; i--) count++;
    return (count & 1) != 0;
  }

  private static List<String> tokens(String text, char separator) {
    var result = new ArrayList<String>();
    boolean quoted = false, escaped = false;
    int start = 0;
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (escaped) escaped = false;
      else if (ch == '\\') escaped = true;
      else if (ch == '"') quoted = !quoted;
      else if (ch == separator && !quoted) {
        result.add(token(text, start, i));
        start = i + 1;
      }
    }
    result.add(token(text, start, text.length()));
    return result;
  }

  private static String token(String text, int start, int end) {
    // Leading Java whitespace was accepted by the established config syntax.
    // Quotes/backslashes still delimit literal content, and trailing trim is ASCII.
    while (start < end && Character.isWhitespace(text.charAt(start))) start++;
    return text.substring(start, end).trim();
  }

  private static String trim(String text) {
    int start = 0, end = text.length();
    while (start < end && (text.charAt(start) <= ' ' || text.charAt(start) == '\ufeff')) start++;
    while (end > start && (text.charAt(end - 1) <= ' ' || text.charAt(end - 1) == '\ufeff')) end--;
    return text.substring(start, end);
  }

  private static String escape(String text, String escaped, boolean section) {
    var out = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '\n') out.append(section ? "\\n" : "\\\n");
      else {
        if (escaped.indexOf(ch) >= 0) out.append('\\');
        out.append(ch);
      }
    }
    return out.toString();
  }

  private static String unescape(String text, String escaped, boolean section) {
    var out = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '\\' && i + 1 < text.length()) {
        char next = text.charAt(i + 1);
        if (escaped.indexOf(next) >= 0) {
          out.append(next);
          i++;
          continue;
        }
        if (section && next == 'n') {
          out.append('\n');
          i++;
          continue;
        }
      }
      out.append(ch);
    }
    return out.toString();
  }

  public static class ParseException extends RuntimeException {
    private static final long serialVersionUID = 8721912755972301225L;

    public ParseException(String message, int line, String content) {
      super(describe(message, line, content));
    }

    public ParseException(String message, int line, String content, Exception cause) {
      super(describe(message, line, content), cause);
    }

    public ParseException(String message, Value value) {
      this(message, value.getLine(), value.name + " = " + value.getString());
    }

    public ParseException(String message, Value value, Exception cause) {
      this(message, value.getLine(), value.name + " = " + value.getString(), cause);
    }

    private static String describe(String message, int line, String content) {
      return message
          + (line < 0 ? " at an unknown line" : " at line " + line)
          + " ("
          + content
          + ").";
    }
  }

  public static class Value {
    public final String name;
    public String comment;
    public String value;
    private final int line;
    private ParsedNumber parsed;

    private record ParsedNumber(String source, Number number) {}

    public Value(String name, String comment, String value) {
      this(name, comment, value, -1);
    }

    private Value(String name, String comment, String value, int line) {
      this.name = name;
      this.comment = comment;
      this.value = value;
      this.line = line;
    }

    public String getString() {
      return value;
    }

    public boolean getBool() {
      return Boolean.parseBoolean(value);
    }

    public int getInt() {
      return number().intValue();
    }

    public float getFloat() {
      return number().floatValue();
    }

    public double getDouble() {
      return number().doubleValue();
    }

    public <T> void set(T value) {
      this.value = String.valueOf(value);
      parsed = null;
    }

    public int getLine() {
      return line;
    }

    private Number number() {
      if (parsed == null || !Objects.equals(parsed.source(), value)) {
        try {
          parsed = new ParsedNumber(value, NumberFormat.getInstance(Locale.US).parse(value));
        } catch (java.text.ParseException failure) {
          throw new ParseException("invalid value", this, failure);
        }
      }
      return parsed.number();
    }
  }
}
