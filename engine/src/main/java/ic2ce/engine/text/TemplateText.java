package ic2ce.engine.text;

import java.util.*;

/**
 * Independent bounded template grammar; immutable expression records defer variables and
 * translation to a host context.
 */
public final class TemplateText {
  private TemplateText() {}

  public interface Context {
    String variable(String name);

    String translate(String key, List<String> arguments);
  }

  public sealed interface Expression permits Literal, Sequence, Binding, Translation {
    String evaluate(Context context);
  }

  public record Literal(String value) implements Expression {
    public Literal {
      Objects.requireNonNull(value);
    }

    @Override
    public String evaluate(Context context) {
      return value;
    }
  }

  public record Sequence(List<Expression> parts) implements Expression {
    public Sequence {
      parts = List.copyOf(parts);
    }

    @Override
    public String evaluate(Context context) {
      var output = new StringBuilder();
      for (var part : parts) {
        var value = part.evaluate(context);
        if (value == null) return null;
        output.append(value);
      }
      return output.toString();
    }
  }

  public record Binding(Expression name) implements Expression {
    public Binding {
      Objects.requireNonNull(name);
    }

    @Override
    public String evaluate(Context context) {
      var key = name.evaluate(context);
      return key == null ? null : context.variable(key);
    }
  }

  public record Translation(Expression key, List<Expression> arguments) implements Expression {
    public Translation {
      Objects.requireNonNull(key);
      arguments = List.copyOf(arguments);
    }

    @Override
    public String evaluate(Context context) {
      var name = key.evaluate(context);
      if (name == null) return null;
      var values = new ArrayList<String>(arguments.size());
      for (var expression : arguments) {
        var value = expression.evaluate(context);
        if (value == null) return null;
        values.add(value);
      }
      return context.translate(name, List.copyOf(values));
    }
  }

  public static Expression compile(String text) {
    Objects.requireNonNull(text);
    if (text.length() > 32768)
      throw new IllegalArgumentException("Native template exceeds text bound");
    var parser = new Parser(text);
    return parser.sequence((char) 0, false, 0);
  }

  private static final class Parser {
    final String text;
    int cursor, nodes;

    Parser(String text) {
      this.text = text;
    }

    private Expression node(Expression value) {
      if (++nodes > 4096)
        throw new IllegalArgumentException("Native template exceeds expression bound");
      return value;
    }

    private void literal(StringBuilder value, List<Expression> parts) {
      if (value.length() > 0) {
        parts.add(node(new Literal(value.toString())));
        value.setLength(0);
      }
    }

    Expression sequence(char terminator, boolean comma, int depth) {
      if (depth > 64) throw new IllegalArgumentException("Native template nesting exceeds bound");
      var parts = new ArrayList<Expression>();
      var value = new StringBuilder();
      while (cursor < text.length()) {
        char symbol = text.charAt(cursor);
        if (symbol == terminator || comma && symbol == ',') break;
        cursor++;
        if (symbol == '\\') {
          if (cursor == text.length())
            throw new IllegalArgumentException("Unfinished template escape");
          value.append(text.charAt(cursor++));
        } else if (symbol == '{') {
          literal(value, parts);
          var key = sequence('}', true, depth + 1);
          var arguments = new ArrayList<Expression>();
          while (cursor < text.length() && text.charAt(cursor) == ',') {
            cursor++;
            if (arguments.size() >= 256)
              throw new IllegalArgumentException("Native template argument bound");
            arguments.add(sequence('}', true, depth + 1));
          }
          if (cursor >= text.length() || text.charAt(cursor++) != '}')
            throw new IllegalArgumentException("Unfinished translation template");
          parts.add(node(new Translation(key, arguments)));
        } else if (symbol == '%') {
          if (cursor < text.length() && text.charAt(cursor) == '%') {
            cursor++;
            value.append('%');
            continue;
          }
          literal(value, parts);
          var name = sequence('%', false, depth + 1);
          if (cursor >= text.length() || text.charAt(cursor++) != '%')
            throw new IllegalArgumentException("Unfinished variable template");
          parts.add(node(new Binding(name)));
        } else value.append(symbol);
      }
      literal(value, parts);
      return parts.isEmpty()
          ? node(new Literal(""))
          : parts.size() == 1 ? parts.getFirst() : node(new Sequence(parts));
    }
  }
}
