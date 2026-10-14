package ic2ce.neoforge.cable;

import ic2ce.engine.value.ValueTuples;
import ic2ce.neoforge.item.paint.PigmentPalette;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.world.item.DyeColor;

/**
 * Independent native dye identities, saved finish names and physical insulation boundary oracles.
 */
public final class MaterialLookupContracts {
  public record Result(int checks, List<String> failures) {}

  public static Result run() {
    int checks = 0;
    var failures = new ArrayList<String>();
    for (var dye : DyeColor.values()) {
      var pigment = PigmentPalette.get(dye);
      checks++;
      if (pigment == null
          || pigment.dyeColor != dye
          || PigmentPalette.byColor(pigment.color) != pigment)
        failures.add("material lookup: actual native pigment/RGB identity did not round-trip");
      var hard = FoamFinish.getHard(dye);
      checks++;
      if (hard == null
          || !hard.isHard()
          || hard.getColor() != dye
          || FoamFinish.get(hard.getSerializedName()) != hard)
        failures.add("material lookup: native pigment did not preserve its saved hardened finish");
    }
    checks++;
    if (!FoamFinish.SOFT.isSoft()
        || FoamFinish.SOFT.isHard()
        || FoamFinish.get("unknown") != null
        || PigmentPalette.byColor(-1) != null)
      failures.add("material lookup: uncured or unknown material was admitted as a hard pigment");
    var oldPigment = PigmentPalette.values[0];
    var oldFinish = FoamFinish.VALUES[0];
    try {
      PigmentPalette.values[0] = null;
      FoamFinish.VALUES[0] = null;
      checks++;
      if (PigmentPalette.get(DyeColor.BLACK) != PigmentPalette.BLACK
          || FoamFinish.get("soft") != FoamFinish.SOFT)
        failures.add(
            "material lookup: borrowed compatibility arrays changed private lookup generations");
    } finally {
      PigmentPalette.values[0] = oldPigment;
      FoamFinish.VALUES[0] = oldFinish;
    }
    for (var material : ConductorMaterial.values()) {
      for (int depth = 0; depth <= material.maxInsulation; depth++) {
        checks++;
        if (material.getThickness(depth) != material.thickness + depth / 8f)
          failures.add("material lookup: physical insulation radius differs from rating geometry");
      }
      for (int depth : new int[] {-1, material.maxInsulation + 1}) {
        boolean rejected = false;
        try {
          material.getThickness(depth);
        } catch (IllegalArgumentException expected) {
          rejected = true;
        }
        checks++;
        if (!rejected)
          failures.add("material lookup: unsupported insulation entered native geometry");
      }
    }
    var map = new LinkedHashMap<String, Integer>();
    map.put("first", 1);
    var projection = ValueTuples.fromMap(map);
    map.put("second", 2);
    checks++;
    if (projection.size() != 1 || !projection.getFirst().equals(new ValueTuples.T2<>("first", 1)))
      failures.add(
          "structural values: map projection retained mutable structure or lost pair value"
              + " identity");
    boolean immutable = false;
    try {
      projection.clear();
    } catch (UnsupportedOperationException expected) {
      immutable = true;
    }
    checks++;
    if (!immutable)
      failures.add("structural values: escaped projection can replace owned pair structure");
    return new Result(checks, List.copyOf(failures));
  }
}
