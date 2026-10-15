package ic2ce.neoforge.diagnostics;

import ic2ce.neoforge.config.NativeConfigValues;
import ic2ce.neoforge.item.paint.VanillaColorBlockId;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.*;

/**
 * Actual native stack grammar and incremental UTF8 diagnostics retain precise quantities and split
 * characters.
 */
public final class ConfigurationContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) failures.add("native configuration: " + label);
  }

  public static Result run() {
    try {
      return new ConfigurationContracts().exercise();
    } catch (Exception error) {
      throw new IllegalStateException(error);
    }
  }

  private static final class RecordingLog extends ProjectLog {
    final List<String> lines = new ArrayList<>();

    RecordingLog() {
      super(LogManager.getLogger("IndustrialCraft2-CE.Verification"));
    }

    @Override
    public void log(DiagnosticArea category, Level level, String message) {
      lines.add(message);
    }
  }

  private Result exercise() throws Exception {
    check(
        NativeConfigValues.asStackWithAmount("minecraft:coal * 17").is(Items.COAL)
            && NativeConfigValues.asStackWithAmount("minecraft:coal * 17").getCount() == 17,
        "actual item quantity grammar accepts exact positive bill and surrounding spacing");
    check(
        NativeConfigValues.fromStackWithAmount(new ItemStack(Items.COAL, 7))
            .equals("minecraft:coal*7"),
        "actual stack quantity export roundtrips the registry identity");
    check(
        NativeConfigValues.asStack("ic2ce_verification:missing_item") == null,
        "unknown registered input retains the documented missing-item sentinel");
    for (String bad :
        List.of(
            "minecraft:coal*0",
            "minecraft:coal*-1",
            "minecraft:coal*2147483648",
            "minecraft:coal*2*3",
            "minecraft:coal*x",
            "invalid namespace:coal")) {
      boolean rejected = false;
      try {
        NativeConfigValues.asStackWithAmount(bad);
      } catch (java.text.ParseException expected) {
        rejected = true;
      }
      check(rejected, "invalid native grammar reports a meaningful parse error: " + bad);
    }
    boolean forbidden = false;
    try {
      NativeConfigValues.asStack("minecraft:coal*2");
    } catch (java.text.ParseException expected) {
      forbidden = true;
    }
    check(forbidden, "quantity suffix cannot silently enter a configuration field that forbids it");
    var input = NativeConfigValues.asRecipeInputWithAmount("Tag:minecraft:coals*3");
    check(
        input.getAmount() == 3 && input.matches(new ItemStack(Items.COAL)),
        "actual tag recipe descriptor retains independent quantity and live native tag matching");
    var fluid = NativeConfigValues.asRecipeInput("Fluid:minecraft:water");
    check(
        fluid != null && fluid.getAmount() == 1,
        "unquantified fluid descriptor uses the actual one-bucket container recipe protocol");
    var recording = new RecordingLog();
    try (var output = new Utf8LogStream(recording, DiagnosticArea.General, Level.INFO)) {
      var bytes = "中😀文\r\n\nnext".getBytes(StandardCharsets.UTF_8);
      for (byte value : bytes) {
        output.write(value);
        output.flush();
      }
    }
    check(
        recording.lines.equals(List.of("中😀文", "", "next")),
        "one-byte UTF8 writes, non-BMP text, native CRLF and empty line preserve complete"
            + " diagnostics");
    recording.lines.clear();
    var output = new Utf8LogStream(recording, DiagnosticArea.General, Level.INFO);
    output.write("x".repeat(70000).getBytes(StandardCharsets.UTF_8));
    output.close();
    output.close();
    check(
        recording.lines.size() == 2
            && recording.lines.get(0).length() == 65536
            && recording.lines.get(1).length() == 4464,
        "unterminated diagnostics stay bounded while retaining every character and idempotent"
            + " close");
    boolean closed = false;
    try {
      output.write('x');
    } catch (java.io.IOException expected) {
      closed = true;
    }
    check(closed, "closed native output stream cannot retain another unflushed buffer");
    recording.lines.clear();
    try (var malformed = new Utf8LogStream(recording, DiagnosticArea.General, Level.INFO)) {
      malformed.write(0xe4);
    }
    check(
        recording.lines.equals(List.of("�")),
        "partial final UTF8 input has a deterministic replacement instead of a thrown decoder"
            + " failure");
    check(
        !VanillaColorBlockId.WOOL.test(Blocks.WHITE_CARPET)
            && VanillaColorBlockId.WOOL.test(Blocks.WHITE_WOOL)
            && VanillaColorBlockId.SHULKER_BOX.test(Blocks.SHULKER_BOX),
        "actual paint families cannot be guessed from overlapping registry substrings");
    return new Result(checks, List.copyOf(failures));
  }
}
