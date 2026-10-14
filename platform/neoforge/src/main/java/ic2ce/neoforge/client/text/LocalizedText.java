package ic2ce.neoforge.client.text;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.locale.Language;

/**
 * Actual loaded native language is resolved at use time; untranslated literals without arguments
 * bypass formatting.
 */
public final class LocalizedText {
  private LocalizedText() {}

  public static String translate(String key) {
    return Language.getInstance().getOrDefault(Objects.requireNonNull(key));
  }

  public static String translate(String key, Object... arguments) {
    String value = translate(key);
    if (arguments == null || arguments.length == 0) return value;
    try {
      return String.format(Locale.ROOT, value, arguments);
    } catch (IllegalFormatException error) {
      return "Format error: " + value;
    }
  }
}
