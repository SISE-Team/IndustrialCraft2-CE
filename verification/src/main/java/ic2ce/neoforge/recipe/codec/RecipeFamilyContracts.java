package ic2ce.neoforge.recipe.codec;

import ic2ce.api.recipe.*;
import ic2ce.neoforge.recipe.crafting.PositionedCrafting;
import ic2ce.neoforge.recipe.crafting.UnorderedCrafting;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.network.connection.ConnectionType;

/**
 * Every loaded IC2 native recipe serializes back to JSON and decodes through actual registry
 * codecs.
 */
public final class RecipeFamilyContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean pass, String text) {
    checks++;
    if (!pass) failures.add("native recipe family: " + text);
  }

  public static Result run(ServerLevel world) {
    return new RecipeFamilyContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var ops =
        world
            .registryAccess()
            .createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
    int recipes = 0;
    for (var holder : world.getServer().getRecipeManager().getRecipes()) {
      if (!holder.id().identifier().getNamespace().equals("ic2")) continue;
      var recipe = holder.value();
      recipes++;
      try {
        if (recipe instanceof PositionedCrafting || recipe instanceof UnorderedCrafting) {
          var context =
              net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(world);
          var ingredients =
              recipe instanceof PositionedCrafting shape
                  ? shape.getIngredients().stream().flatMap(Optional::stream).toList()
                  : Arrays.stream(((UnorderedCrafting) recipe).input)
                      .map(IRecipeInput::getIngredient)
                      .toList();
          boolean available =
              ingredients.stream()
                  .allMatch(input -> !input.display().resolveForFirstStack(context).isEmpty());
          if (available && !(recipe instanceof PositionedCrafting shape && shape.assemblyMask != 0))
            check(
                !recipe.placementInfo().isImpossibleToPlace(),
                holder.id()
                    + " runtime native crafting placement observes published material holders");
        }
        var encoded = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
        var decoded = Recipe.CODEC.parse(ops, encoded).getOrThrow();
        var wire =
            new RegistryFriendlyByteBuf(
                Unpooled.buffer(), world.registryAccess(), ConnectionType.NEOFORGE);
        try {
          Recipe.STREAM_CODEC.encode(wire, recipe);
          var remote = Recipe.STREAM_CODEC.decode(wire);
          check(
              remote.getType() == recipe.getType()
                  && remote.getSerializer() == recipe.getSerializer()
                  && !wire.isReadable(),
              holder.id()
                  + " native full recipe wire is consumed exactly and retains its registered"
                  + " protocol");
        } finally {
          wire.release();
        }
        check(
            decoded.getType() == recipe.getType()
                && decoded.getSerializer() == recipe.getSerializer(),
            holder.id() + " JSON retains its actual registered type and serializer");
        if (recipe instanceof RecipeHolder<?, ?> source
            && decoded instanceof RecipeHolder<?, ?> target) {
          check(
              Objects.equals(source.recipe().getMetaData(), target.recipe().getMetaData()),
              holder.id() + " native JSON metadata survives");
          if (source.recipe().getInput() instanceof IRecipeInput before
              && target.recipe().getInput() instanceof IRecipeInput after) {
            check(
                before.getAmount() == after.getAmount(),
                holder.id() + " native input quantity survives");
            for (var prototype : before.getInputs())
              check(
                  before.matches(prototype) == after.matches(prototype),
                  holder.id() + " JSON matching survives on " + prototype);
          }
          if (source.recipe() instanceof MachineRecipeWeighted<?> before
              && target.recipe() instanceof MachineRecipeWeighted<?> after)
            check(
                before
                    .getOutputWeighted()
                    .getWeights()
                    .equals(after.getOutputWeighted().getWeights()),
                holder.id() + " owned probability table survives");
        } else if (recipe instanceof PositionedCrafting before
            && decoded instanceof PositionedCrafting after)
          check(
              before.inputWidth == after.inputWidth
                  && before.inputHeight == after.inputHeight
                  && before.assemblyMask == after.assemblyMask
                  && ItemStack.matches(before.getOutput(), after.getOutput()),
              holder.id() + " exact shaped/UU layout and native result survive");
        else if (recipe instanceof UnorderedCrafting before
            && decoded instanceof UnorderedCrafting after)
          check(
              before.input.length == after.input.length
                  && ItemStack.matches(before.getOutput(), after.getOutput()),
              holder.id() + " native unordered recipe survives");
      } catch (RuntimeException error) {
        failures.add("native recipe JSON " + holder.id() + ": " + error);
      }
    }
    check(recipes > 300, "all loaded native IC2 recipes included: " + recipes);
    var bulkJson = new com.google.gson.JsonObject();
    bulkJson.addProperty("item", "minecraft:iron_ingot");
    bulkJson.addProperty("count", 112);
    var bulk = RecipeValueCodec.parseOutput(bulkJson);
    check(
        bulk.is(Items.IRON_INGOT) && bulk.getCount() == 112,
        "machine batch output retains counts exceeding a single inventory stack");
    var emptyWeighted = new MachineRecipeWeighted<>("input", new RecipeOutputWeighted());
    check(
        emptyWeighted.getOutput().isEmpty(),
        "empty weighted table returns no native item rather than a null list element");
    var stack = new ItemStack(Items.IRON_INGOT, 2);
    var weighted = new RecipeOutputWeighted().addOutput(stack, 1);
    stack.setCount(30);
    var shown = weighted.getOutputs().getFirst();
    shown.setCount(64);
    var chosen = weighted.drawOutput();
    check(chosen.getCount() == 2, "weighted table owns registration and display values");
    chosen.setCount(99);
    check(weighted.drawOutput().getCount() == 2, "weighted draw is an owned physical result");
    var metadata = new CompoundTag();
    metadata.putInt("heat", 20);
    var machine = new MachineRecipe<>("input", "output", metadata);
    metadata.putInt("heat", 99);
    machine.getMetaData().putInt("heat", 999);
    check(
        machine.getMetaData().getIntOr("heat", 0) == 20,
        "recipe metadata is owned on entry and query");
    var fluidData = new CompoundTag();
    fluidData.putString("FluidName", "minecraft:water");
    fluidData.putInt("Amount", 750);
    var components = new CompoundTag();
    components.putString("minecraft:custom_name", "Native fluid contract");
    fluidData.put("Components", components);
    var fluid = ic2ce.api.fluid.Ic2FluidStack.read(fluidData);
    check(
        fluid != null
            && fluid.hasExactFluid(
                RecipeValueCodec.parseFluidStack(RecipeValueCodec.fluidStackToJson(fluid))),
        "fluid JSON retains non-custom-data native components");
    var buffer =
        new RegistryFriendlyByteBuf(
            Unpooled.buffer(), world.registryAccess(), ConnectionType.NEOFORGE);
    try {
      RecipeValueCodec.writeFluidStack(buffer, fluid);
      var restored = RecipeValueCodec.readFluidStack(buffer);
      check(
          restored.getAmountMb() == 750 && restored.hasExactFluid(fluid) && !buffer.isReadable(),
          "native recipe fluid wire preserves every component and its physical bill");
    } finally {
      buffer.release();
    }
    return new Result(checks, List.copyOf(failures));
  }
}
