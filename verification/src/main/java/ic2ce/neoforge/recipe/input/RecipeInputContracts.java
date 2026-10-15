package ic2ce.neoforge.recipe.input;

import com.google.gson.*;
import ic2ce.api.recipe.*;
import ic2ce.neoforge.recipe.codec.DeferredInput;
import ic2ce.neoforge.recipe.codec.RecipeValueCodec;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

/** Real native component, container, public-output and registry-backed wire contracts. */
public final class RecipeInputContracts {
  public record Result(int checks, List<String> failures) {}

  private final List<String> failures = new ArrayList<>();
  private int checks;

  private void check(boolean pass, String text) {
    checks++;
    if (!pass) failures.add("native recipe input: " + text);
  }

  private void rejects(Runnable operation, String text) {
    boolean rejected = false;
    try {
      operation.run();
    } catch (IllegalArgumentException error) {
      rejected = true;
    }
    check(rejected, text);
  }

  public static Result run(ServerLevel world) {
    try {
      return new RecipeInputContracts().exercise(world);
    } catch (RuntimeException error) {
      error.printStackTrace();
      throw error;
    }
  }

  private Result exercise(ServerLevel world) {
    var factory = new InputFactory();
    var prototype = new ItemStack(Items.IRON_INGOT, 3);
    prototype.set(DataComponents.CUSTOM_NAME, Component.literal("native recipe component"));
    var exact = new ExactStackInput(prototype);
    prototype.setCount(9);
    prototype.remove(DataComponents.CUSTOM_NAME);
    check(
        exact.getAmount() == 3 && !exact.matches(prototype),
        "constructor owns native identity and quantity");
    var matching = exact.prototype();
    matching.setCount(1);
    check(
        exact.matches(matching) && exact.getIngredient().test(matching),
        "predicate and native placement retain all components independent of subject count");
    var display = exact.getInputs();
    display.getFirst().remove(DataComponents.CUSTOM_NAME);
    display.getFirst().setCount(12);
    check(
        exact.getInputs().getFirst().getCount() == 3 && exact.matches(matching),
        "display mutation cannot change a compiled bill");
    var identical = new ExactStackInput(exact.prototype());
    check(
        exact.equals(identical) && exact.hashCode() == identical.hashCode(),
        "exact identity equality and hash are coherent");
    rejects(() -> factory.forStack(new ItemStack(Items.IRON_INGOT), 0), "zero quantity rejected");
    rejects(
        () -> factory.forIngredient(Ingredient.of(Items.IRON_INGOT), -1),
        "negative native ingredient quantity rejected");
    var absent = factory.forTag("ic2:deliberately_absent_contract_tag", 1);
    check(
        !absent.matches(matching) && absent.getInputs().isEmpty(),
        "missing material tags safely retain a live negative predicate");
    IRecipeInput[] alternatives = {factory.forItem(Items.GOLD_INGOT), exact};
    var either = new AlternativeInputs(4, alternatives);
    alternatives[0] = factory.forItem(Items.DIAMOND);
    check(
        either.matches(new ItemStack(Items.GOLD_INGOT))
            && !either.matches(new ItemStack(Items.DIAMOND)),
        "alternative structure owns caller array");
    check(
        !either.equals(new AlternativeInputs(1, either.alternatives())),
        "different alternative bills are unequal");
    var empty = factory.forFluidContainer(null, 1000);
    var water = factory.forFluidContainer(Fluids.WATER, 1000);
    check(
        !empty.matches(new ItemStack(Items.DIAMOND)) && empty.matches(new ItemStack(Items.BUCKET)),
        "empty fluid input rejects noncontainers and accepts actual empty bucket");
    check(
        water.matches(new ItemStack(Items.WATER_BUCKET))
            && !water.matches(new ItemStack(Items.LAVA_BUCKET)),
        "exact fluid quantity and identity checked");
    check(
        !factory.forFluidContainer(Fluids.WATER, 1001).matches(new ItemStack(Items.WATER_BUCKET)),
        "insufficient actual fluid rejects input");
    var samples = FluidContentsInput.getFluidContainer(Fluids.WATER);
    check(
        samples.stream().anyMatch(stack -> stack.is(Items.WATER_BUCKET)),
        "native container catalog includes water bucket");
    if (!samples.isEmpty()) samples.getFirst().setCount(99);
    check(
        FluidContentsInput.getFluidContainer(Fluids.WATER).stream()
            .allMatch(stack -> stack.getCount() == 1),
        "container display catalog never lends mutable templates");
    var itemJson = exact.toJson();
    var jsonInput = RecipeValueCodec.parseInput(itemJson);
    itemJson.getAsJsonObject().addProperty("count", 55);
    check(
        jsonInput.getAmount() == 3
            && jsonInput.matches(matching)
            && !jsonInput.matches(new ItemStack(Items.IRON_INGOT)),
        "JSON snapshot retains full native component matching and count");
    var taggedData = new CompoundTag();
    taggedData.putString("key", "value");
    var legacy = new JsonObject();
    legacy.addProperty("item", "minecraft:iron_ingot");
    legacy.add("nbt", JsonParser.parseString("{\"key\":\"value\"}"));
    var legacyInput = RecipeValueCodec.parseInput(legacy);
    var tagged = new ItemStack(Items.IRON_INGOT);
    ic2ce.neoforge.item.StackTag.set(tagged, taggedData);
    check(
        legacyInput.matches(tagged) && !legacyInput.matches(new ItemStack(Items.IRON_INGOT)),
        "legacy nbt key is parsed from its actual field");
    check(
        RecipeValueCodec.parseInput(either.toJson()).getAmount() == 4,
        "combination JSON preserves outer quantity");
    check(
        RecipeValueCodec.parseInput(new ExactStackInput(new ItemStack(Items.DIAMOND)).toJson())
            .matches(new ItemStack(Items.DIAMOND)),
        "component-free exact JSON avoids null-tag conversion");
    var metadata = new CompoundTag();
    metadata.putInt("heat", 15);
    var outputStack = new ItemStack(Items.GOLD_INGOT, 2);
    var output = new RecipeOutput(metadata, outputStack);
    metadata.putInt("heat", 99);
    outputStack.setCount(64);
    var expectedMetadata = new CompoundTag();
    expectedMetadata.putInt("heat", 15);
    var same = new RecipeOutput(expectedMetadata, new ItemStack(Items.GOLD_INGOT, 2));
    check(
        output.equals(same)
            && output.hashCode() == same.hashCode()
            && !output.equals(new RecipeOutput(expectedMetadata, new ItemStack(Items.DIAMOND, 2))),
        "legacy equality matches identical output and owns metadata/stacks");
    for (var input :
        List.of(
            exact,
            either,
            (InputDescriptor) water,
            (InputDescriptor) empty,
            (InputDescriptor) absent)) {
      var buffer =
          new RegistryFriendlyByteBuf(
              Unpooled.buffer(),
              world.registryAccess(),
              net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
      try {
        RecipeValueCodec.writeInput(buffer, input);
        var decoded = RecipeValueCodec.readInput(buffer);
        check(
            decoded.getAmount() == input.getAmount()
                && decoded.matches(matching) == input.matches(matching)
                && decoded.matches(new ItemStack(Items.WATER_BUCKET))
                    == input.matches(new ItemStack(Items.WATER_BUCKET))
                && !buffer.isReadable(),
            "native wire round trip " + input);
      } finally {
        buffer.release();
      }
    }
    var bad =
        new RegistryFriendlyByteBuf(
            Unpooled.buffer(),
            world.registryAccess(),
            net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
    try {
      bad.writeByte(3);
      bad.writeVarInt(Integer.MAX_VALUE);
      rejects(
          () -> RecipeValueCodec.readInput(bad),
          "oversized alternative packet rejected before allocation");
    } finally {
      bad.release();
    }
    var cycle = new DeferredInput[1];
    cycle[0] =
        new DeferredInput(
            () -> {
              cycle[0].unwrap();
              return exact;
            });
    boolean recursion = false;
    try {
      cycle[0].unwrap();
    } catch (IllegalStateException expected) {
      recursion = true;
    }
    check(recursion, "recursive deferred resolution rejects cleanly");
    var resolutions = new java.util.concurrent.atomic.AtomicInteger();
    var lazy =
        new DeferredInput(
            () -> {
              resolutions.incrementAndGet();
              return exact;
            });
    var tasks = new ArrayList<java.util.concurrent.CompletableFuture<IRecipeInput>>();
    for (int index = 0; index < 16; index++)
      tasks.add(java.util.concurrent.CompletableFuture.supplyAsync(lazy::unwrap));
    for (var task : tasks)
      check(task.join() == exact, "concurrent deferred input publishes one actual delegate");
    check(resolutions.get() == 1, "native deferred supplier called exactly once");
    return new Result(checks, List.copyOf(failures));
  }
}
