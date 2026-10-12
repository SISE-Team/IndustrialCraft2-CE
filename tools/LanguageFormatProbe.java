import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Uses Minecraft's numeric-format normalization, then the real Java formatter. */
class LanguageFormatProbe {
    public static void main(String[] args) throws Exception {
        int checked=0,failed=0;
        Pattern numeric=Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
        Pattern argument=Pattern.compile("%(?:(\\d+)\\$)?s");
        for(String line:Files.readAllLines(Path.of(args[0]),StandardCharsets.UTF_8)) {
            String[] parts=line.split("\\t",3);
            String value=new String(Base64.getDecoder().decode(parts[2]),StandardCharsets.UTF_8);
            String normalized=numeric.matcher(value).replaceAll("%$1s");
            Object[] values=new Object[32];Arrays.fill(values,"VALUE");
            try { String.format(Locale.ROOT,normalized,values); }
            catch(IllegalFormatException e) {failed++;System.out.println("FAIL\t"+parts[0]+"\t"+parts[1]+"\t"+e);}
            checked++;
        }
        System.out.println("checked="+checked+" failed="+failed);
        if(failed>0)System.exit(1);
    }
}
