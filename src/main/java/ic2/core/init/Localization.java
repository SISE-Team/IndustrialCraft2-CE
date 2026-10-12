package ic2.core.init;

import java.util.IllegalFormatException;
import java.util.Locale;
import net.minecraft.locale.Language;

public class Localization {
   private static final String defaultLang = "en_us";
   private static final String ic2LangKey = "ic2.";

   public static String translate(String key) {
      return translate(key, new Object[0]);
   }

   public static String translate(String key, Object... args) {
      String value = Language.getInstance().getOrDefault(key);
      try {
         return String.format(Locale.ROOT, value, args);
      } catch (IllegalFormatException error) {
         return "Format error: " + value;
      }
   }
}
