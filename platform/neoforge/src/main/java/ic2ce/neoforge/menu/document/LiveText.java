package ic2ce.neoforge.menu.document;

import com.google.common.base.Supplier;
import ic2ce.engine.text.TemplateText;
import ic2ce.neoforge.client.text.LocalizedText;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;

/**
 * Native text adapter uses immutable compiled expressions and per-class public-member handles
 * rather than reflection each frame.
 */
public final class LiveText {
  private LiveText() {}

  public interface ITextProvider {
    String getOptional(Object base, Map<String, ITextProvider> tokens);

    default String get(Object base, Map<String, ITextProvider> tokens) {
      var result = getOptional(base, tokens);
      return result == null ? "ERROR" : result;
    }
  }

  public static ITextProvider of(String text) {
    Objects.requireNonNull(text);
    return (_, _) -> text;
  }

  public static ITextProvider of(Supplier<String> value) {
    Objects.requireNonNull(value);
    return (_, _) -> value.get();
  }

  public static ITextProvider of(Component text) {
    Objects.requireNonNull(text);
    return (_, _) -> text.getString();
  }

  public static ITextProvider ofTranslated(String key) {
    Objects.requireNonNull(key);
    return (_, _) -> LocalizedText.translate(key);
  }

  public static Map<String, ITextProvider> emptyTokens() {
    return Map.of();
  }

  public static ITextProvider parse(String source, Class<?> type) {
    try {
      return new Compiled(TemplateText.compile(source), type);
    } catch (IllegalArgumentException error) {
      return of("ERROR: " + error.getMessage());
    }
  }

  private record Compiled(TemplateText.Expression expression, Class<?> declared)
      implements ITextProvider {
    @Override
    public String getOptional(Object base, Map<String, ITextProvider> tokens) {
      var values = tokens == null ? Map.<String, ITextProvider>of() : tokens;
      return expression.evaluate(
          new TemplateText.Context() {
            public String variable(String name) {
              var supplied = values.get(name);
              if (supplied != null) return supplied.getOptional(base, values);
              if (!name.startsWith("base.") || name.length() > 4096) return null;
              Object subject = base;
              Class<?> type = subject == null ? declared : subject.getClass();
              if (type == null) return null;
              String[] path = name.substring(5).split("\\.", -1);
              if (path.length > 64) return null;
              for (var part : path) {
                if (part.isEmpty()) return null;
                Class<?> ownerType = type;
                var member =
                    MEMBERS.get(ownerType).computeIfAbsent(part, key -> lookup(ownerType, key));
                if (member.isEmpty()) return null;
                subject = member.get().read(subject);
                if (subject == null) return null;
                type = subject.getClass();
              }
              return subject.toString();
            }

            public String translate(String key, List<String> arguments) {
              return LocalizedText.translate(key, arguments.toArray());
            }
          });
    }
  }

  private record Member(MethodHandle handle, boolean shared) {
    Object read(Object owner) {
      if (owner == null && !shared) return null;
      try {
        return shared ? handle.invoke() : handle.invoke(owner);
      } catch (Throwable error) {
        if (error instanceof Error fatal) throw fatal;
        throw new IllegalStateException("Unable to evaluate native GUI member", error);
      }
    }
  }

  private static final ClassValue<ConcurrentHashMap<String, Optional<Member>>> MEMBERS =
      new ClassValue<>() {
        @Override
        protected ConcurrentHashMap<String, Optional<Member>> computeValue(Class<?> type) {
          return new ConcurrentHashMap<>();
        }
      };

  private static Optional<Member> lookup(Class<?> type, String name) {
    try {
      MethodHandle handle;
      boolean shared;
      if (name.endsWith("()")) {
        var method = type.getMethod(name.substring(0, name.length() - 2));
        if (method.getReturnType() == void.class) return Optional.empty();
        shared = Modifier.isStatic(method.getModifiers());
        if (!method.trySetAccessible()) return Optional.empty();
        handle = MethodHandles.lookup().unreflect(method);
      } else {
        var field = type.getField(name);
        shared = Modifier.isStatic(field.getModifiers());
        if (!field.trySetAccessible()) return Optional.empty();
        handle = MethodHandles.lookup().unreflectGetter(field);
      }
      handle =
          handle.asType(
              shared
                  ? MethodType.methodType(Object.class)
                  : MethodType.methodType(Object.class, Object.class));
      return Optional.of(new Member(handle, shared));
    } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException missing) {
      return Optional.empty();
    }
  }
}
