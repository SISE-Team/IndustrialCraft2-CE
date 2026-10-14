package ic2ce.neoforge.registry;

import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Native physical definitions retain their own registry IDs and reject impossible recipe/armor
 * inputs.
 */
public final class DefinitionContracts {
  public record Result(int checks, List<String> failures) {}

  @SuppressWarnings("unchecked")
  public static Result run() {
    var failures = new ArrayList<String>();
    int checks = 0;
    try {
      var identifier = BlockBehaviour.Properties.class.getDeclaredField("id");
      identifier.setAccessible(true);
      var properties =
          Collections.newSetFromMap(new IdentityHashMap<BlockBehaviour.Properties, Boolean>());
      for (var entry : BuiltInRegistries.BLOCK.entrySet())
        if (entry.getKey().identifier().getNamespace().equals("ic2")) {
          var block = entry.getValue();
          checks++;
          if (!properties.add(block.properties()))
            failures.add(
                "native definitions: mutable block properties are shared across physical"
                    + " registrations: "
                    + entry.getKey());
          checks++;
          if (!entry.getKey().equals(identifier.get(block.properties())))
            failures.add(
                "native definitions: a later builder mutation replaced this block's registry ID: "
                    + entry.getKey());
        }
      var metadata = RecipeCodecs.class.getDeclaredMethod("integerFields", String[].class);
      metadata.setAccessible(true);
      var convert =
          (Function<com.google.gson.JsonObject, CompoundTag>)
              metadata.invoke(null, (Object) new String[] {"amount", "duration"});
      var result =
          convert.apply(
              JsonParser.parseString("{\"amount\":1000,\"duration\":400}").getAsJsonObject());
      checks++;
      if (result.getIntOr("amount", -1) != 1000 || result.getIntOr("duration", -1) != 400)
        failures.add("native definitions: exact recipe quantities changed in native metadata");
      for (String number : List.of("-1", "1.5", "2147483648", "\"1000\"")) {
        boolean rejected = false;
        try {
          convert.apply(
              JsonParser.parseString("{\"amount\":" + number + ",\"duration\":400}")
                  .getAsJsonObject());
        } catch (com.google.gson.JsonParseException expected) {
          rejected = true;
        }
        checks++;
        if (!rejected)
          failures.add(
              "native definitions: impossible numeric recipe metadata was admitted: " + number);
      }
      boolean rejected = false;
      try {
        ArmorProfiles.typeFor(EquipmentSlot.MAINHAND);
      } catch (IllegalArgumentException expected) {
        rejected = true;
      }
      checks++;
      if (!rejected) failures.add("native definitions: a hand slot silently became chest armor");
    } catch (ReflectiveOperationException error) {
      failures.add("native definition observations failed: " + error);
    }
    return new Result(checks, List.copyOf(failures));
  }
}
