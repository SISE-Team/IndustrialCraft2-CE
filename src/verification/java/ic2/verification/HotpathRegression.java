package ic2.verification;

import com.google.gson.GsonBuilder;
import ic2.api.recipe.*;
import ic2.core.recipe.*;
import ic2.core.recipe.input.*;
import ic2.core.recipe.v2.LazyRecipeInput;
import ic2.core.util.Config;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Baseline-compatible native regressions for recipe candidates, config reads and reloads. */
public final class HotpathRegression {
    private final List<String> failures = new ArrayList<>();
    private final Map<String, Integer> groups = new LinkedHashMap<>();
    private MinecraftServer server;
    private CompletableFuture<Void> reload;
    private int stage, ticks, checks;
    private boolean finished;
    private ProbeManager tagged;
    private Object previousManager;
    private Path tagFile;

    private void check(boolean condition, String label) {
        checks++;
        if (!condition) failures.add(label);
    }
    private void group(String name, Runnable action) {
        int before = checks;
        try { action.run(); } catch (Throwable error) { failures.add(name + ": " + error); }
        groups.put(name, checks - before);
    }

    @SubscribeEvent public void started(ServerStartedEvent event) {
        server = event.getServer();
        group("recipeCandidates", this::recipes);
        group("recipeReplacement", this::replacement);
        group("configPaths", this::config);
        EnergyEffectsRegression.prepare(server.overworld());
        try {
            previousManager = Recipes.macerator.get(server.overworld());
            Path pack = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("ic2-hotpath-probe");
            Files.createDirectories(pack);
            Files.writeString(pack.resolve("pack.mcmeta"), "{\"pack\":{\"description\":\"IC2 hotpath reload regression\",\"min_format\":[101,1],\"max_format\":[101,1]}}");
            tagFile = pack.resolve("data/ic2/tags/item/performance_probe.json");
            Files.createDirectories(tagFile.getParent());
            Files.writeString(tagFile, "{\"replace\":true,\"values\":[\"minecraft:raw_iron\"]}");
            server.getPackRepository().reload();
            var selected = new ArrayList<>(server.getPackRepository().getSelectedIds());
            selected.add("file/ic2-hotpath-probe");
            reload = server.reloadResources(selected);
        } catch (Throwable error) { failures.add("start native reload: " + error); finish(); }
    }

    @SubscribeEvent public void tick(ServerTickEvent.Post event) {
        if (server == null || finished) return;
        if (++ticks > 1200) { failures.add("native reload timeout"); finish(); return; }
        if (reload == null || !reload.isDone()) return;
        try {
            reload.join();
            if (stage == 0) {
                group("cableEffects", () -> {
                    var result = EnergyEffectsRegression.run(server.overworld());
                    checks += result.checks(); failures.addAll(result.failures());
                });
                var ingredient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("ic2", "performance_probe"))));
                tagged = new ProbeManager();
                tagged.add(lazy(ingredient), "tagged");
                check("tagged".equals(tagged.find(new ItemStack(Items.RAW_IRON))), "first reload tag accepts iron");
                check(tagged.find(new ItemStack(Items.DIAMOND)) == null, "cache initial negative tag lookup");
                check(previousManager != Recipes.macerator.get(server.overworld()), "native recipe map replaces manager");
                previousManager = Recipes.macerator.get(server.overworld());
                check(Recipes.macerator.get(server.overworld()).apply(new ItemStack(Items.RAW_IRON), false) != null, "native processing recipe after first reload");
                Files.writeString(tagFile, "{\"replace\":true,\"values\":[\"minecraft:diamond\"]}");
                stage = 1;
                reload = server.reloadResources(server.getPackRepository().getSelectedIds());
            } else {
                check(tagged.find(new ItemStack(Items.RAW_IRON)) == null, "tag reload removes cached positive");
                check("tagged".equals(tagged.find(new ItemStack(Items.DIAMOND))), "tag reload invalidates cached negative");
                check(previousManager != Recipes.macerator.get(server.overworld()), "second native recipe map replaces manager");
                check(Recipes.macerator.get(server.overworld()).apply(new ItemStack(Items.RAW_IRON), false) != null, "native processing recipe after second reload");
                groups.put("nativeReload", 8);
                finish();
            }
        } catch (Throwable error) { failures.add("native reload stage=" + stage + ": " + error); finish(); }
    }

    private static LazyRecipeInput lazy(Ingredient ingredient) {
        var input = new LazyRecipeInput(() -> new RecipeInputIngredient(ingredient, 1));
        input.unwrap();
        return input;
    }

    private void recipes() {
        var manager = new ProbeManager();
        boolean[] enabled = {true};
        var custom = new PredicateInput(stack -> enabled[0] && stack.is(Items.IRON_INGOT));
        manager.add(custom, "custom-first");
        var vanilla = lazy(Ingredient.of(Items.IRON_INGOT));
        manager.add(vanilla, "vanilla-second");
        for (int i = 0; i < 40; i++) {
            enabled[0] = i % 2 == 0;
            check((enabled[0] ? "custom-first" : "vanilla-second").equals(manager.find(new ItemStack(Items.IRON_INGOT))), "preserve fallback order and dynamic predicate " + i);
            check(manager.find(new ItemStack(Items.DIAMOND)) == null, "repeated negative lookup " + i);
        }
        manager.add(lazy(Ingredient.of(Items.DIAMOND)), "new-diamond");
        check("new-diamond".equals(manager.find(new ItemStack(Items.DIAMOND))), "addition invalidates negative cache");
        manager.remove(vanilla);
        enabled[0] = false;
        check(manager.find(new ItemStack(Items.IRON_INGOT)) == null, "removal invalidates cached candidate");
        var iterator = manager.getRecipes().iterator();
        while (iterator.hasNext()) if (iterator.next().getOutput().equals("new-diamond")) iterator.remove();
        check(manager.find(new ItemStack(Items.DIAMOND)) == null, "iterator removal invalidates candidate");

        var order = new ProbeManager();
        order.add(lazy(Ingredient.of(Items.IRON_INGOT)), "deferred-first");
        order.add(new PredicateInput(stack -> stack.is(Items.IRON_INGOT)), "custom-second");
        check("deferred-first".equals(order.find(new ItemStack(Items.IRON_INGOT))), "deferred precedes later custom input");
        order.add(new RecipeInputItemStack(new ItemStack(Items.IRON_INGOT)), "existing-exact-cache");
        check("existing-exact-cache".equals(order.find(new ItemStack(Items.IRON_INGOT))), "retain existing exact-cache precedence");

        var named = new ItemStack(Items.IRON_INGOT);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        var components = new ProbeManager();
        var exact = new LazyRecipeInput(() -> new PredicateInput(stack -> stack.is(Items.IRON_INGOT)
            && Objects.equals(named.get(DataComponents.CUSTOM_NAME), stack.get(DataComponents.CUSTOM_NAME)))); exact.unwrap();
        components.add(exact, "named");
        check(components.find(new ItemStack(Items.IRON_INGOT)) == null, "component mismatch remains negative");
        check("named".equals(components.find(named.copy())), "same item with matching component stays eligible");
        check(components.find(new ItemStack(Items.IRON_INGOT)) == null, "component positive does not bypass later matching");

        var customIngredient = new Ingredient(new ICustomIngredient() {
            public boolean test(ItemStack stack) { return enabled[0] && stack.is(Items.GOLD_INGOT); }
            public java.util.stream.Stream<Holder<Item>> items() { return java.util.stream.Stream.of(Items.GOLD_INGOT.builtInRegistryHolder()); }
            public boolean isSimple() { return false; }
            public IngredientType<?> getType() { throw new UnsupportedOperationException("verification only"); }
        });
        var dynamic = new ProbeManager(); dynamic.add(lazy(customIngredient), "dynamic");
        enabled[0] = false;
        check(dynamic.find(new ItemStack(Items.GOLD_INGOT)) == null, "custom ingredient initially rejects");
        enabled[0] = true;
        check("dynamic".equals(dynamic.find(new ItemStack(Items.GOLD_INGOT))), "custom ingredient is never negatively filtered");

        @SuppressWarnings("unchecked")
        var customHolders = (net.minecraft.core.HolderSet<Item>)java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{net.minecraft.core.HolderSet.class}, (proxy, method, args) -> switch (method.getName()) {
                case "isImmediatelyResolvable" -> false;
                case "contains" -> enabled[0] && args[0] == Items.COAL.builtInRegistryHolder();
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "verification dynamic holder set";
                default -> throw new UnsupportedOperationException(method.getName());
            });
        var holderManager = new ProbeManager(); holderManager.add(lazy(Ingredient.of(customHolders)), "holders");
        enabled[0] = false;
        check(holderManager.find(new ItemStack(Items.COAL)) == null, "custom holder set initially rejects");
        enabled[0] = true;
        check("holders".equals(holderManager.find(new ItemStack(Items.COAL))), "custom holder set remains dynamic without tag event");

        var deferred = new ProbeManager();
        deferred.add(new PredicateInput(stack -> true), "first");
        deferred.add(new LazyRecipeInput(() -> { throw new AssertionError("forced unused lazy parse"); }), "unused");
        check("first".equals(deferred.find(new ItemStack(Items.COAL))), "unresolved lazy input is not forced by index creation");

        var random = new Random(20261013L);
        var finite = new ProbeManager();
        Item[] items = {Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.COAL, Items.COPPER_INGOT, Items.RAW_IRON};
        var entries = new ArrayList<Map.Entry<IRecipeInput, String>>();
        for (int i = 0; i < 48; i++) {
            Item item = items[random.nextInt(items.length)];
            IRecipeInput input = i % 4 == 0 ? new PredicateInput(stack -> stack.is(item) && stack.getCount() % 3 == 0) : lazy(Ingredient.of(item));
            String output = "case-" + i; finite.add(input, output); entries.add(Map.entry(input, output));
        }
        for (Item item : items) for (int count = 1; count <= 64; count++) {
            var stack = new ItemStack(item, count);
            String expected = entries.stream().filter(e -> e.getKey().matches(stack)).map(Map.Entry::getValue).findFirst().orElse(null);
            check(Objects.equals(expected, finite.find(stack)), "ordered linear oracle " + item + "/" + count);
        }

        var concurrent = new ProbeManager();
        var queries = BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR).limit(128).toList();
        for (int i = 0; i < 32; i++) concurrent.add(lazy(Ingredient.of(queries.get(i))), "item-" + i);
        try (var executor = Executors.newFixedThreadPool(8, Thread.ofPlatform().factory())) {
            var barrier = new CyclicBarrier(8);
            List<Callable<Boolean>> jobs = new ArrayList<>();
            for (int worker = 0; worker < 8; worker++) {
                int offset = worker * 17;
                jobs.add(() -> {
                    barrier.await();
                    for (int i = 0; i < 10000; i++) {
                        int index = (i + offset) % queries.size();
                        String expected = index < 32 ? "item-" + index : null;
                        if (!Objects.equals(expected, concurrent.find(new ItemStack(queries.get(index))))) return false;
                    }
                    return true;
                });
            }
            for (var result : executor.invokeAll(jobs)) check(result.get(), "concurrent cold candidate cache lookup");
        } catch (Exception error) { throw new IllegalStateException(error); }
    }

    private void replacement() {
        for (boolean objectOverload : List.of(false, true)) {
            var manager = new BoundedBasic();
            manager.addRecipe(lazy(Ingredient.of(Items.RAW_IRON)), List.of(new ItemStack(Items.COAL)), null, false);
            var replacement = lazy(Ingredient.of(Items.RAW_IRON));
            try {
                boolean accepted = objectOverload
                    ? manager.addRecipe(new MachineRecipe<>(replacement, List.of(new ItemStack(Items.DIAMOND))), true)
                    : manager.addRecipe(replacement, List.of(new ItemStack(Items.DIAMOND)), null, true);
                var result = manager.apply(new ItemStack(Items.RAW_IRON), false);
                check(accepted && result != null && result.getOutput().iterator().next().is(Items.DIAMOND), "replace distinct input object overload=" + objectOverload);
                check(java.util.stream.StreamSupport.stream(manager.getRecipes().spliterator(), false).count() == 1, "replace leaves one recipe overload=" + objectOverload);
            } catch (Throwable error) { check(false, "bounded replacement overload=" + objectOverload + ": " + error); }
        }
        var list = new BoundedList();
        var first = lazy(Ingredient.of(Items.COAL)); var second = lazy(Ingredient.of(Items.COAL));
        list.addRecipe(first, "first", null, false);
        try {
            list.addRecipe(second, "second", null, true);
            check(list.getInputs().size() == 1 && list.getInputs().getFirst() == second, "list replacement removes old object");
        } catch (Throwable error) { check(false, "bounded list replacement: " + error); }
    }

    private void config() {
        var config = new Config("probe");
        for (String key : List.of("simple", "misc/option", " leading / trailing ", "quoted/\"a/b\"", "escaped/a\\/b", "deep/a/b/c")) {
            for (int value = 0; value < 12; value++) {
                config.set(key, value);
                check(config.get(key).getInt() == value, "path value remains live " + key);
                check(config.get(new String(key)).getInt() == value, "equal noninterned path " + key);
                config.get(key).set(value + 1);
                check(config.get(key).getInt() == value + 1, "direct config value mutation " + key);
            }
            config.remove(key); check(config.get(key) == null, "path removal " + key);
            config.set(key, 19); check(config.get(key).getInt() == 19, "path reinsert " + key);
            config.clear(); check(config.get(key) == null, "path clear " + key);
        }
        config.set("misc/a", 11); config.set("misc/b", 22);
        try (var executor = Executors.newFixedThreadPool(8, Thread.ofPlatform().factory())) {
            List<Callable<Boolean>> jobs = new ArrayList<>();
            for (int worker = 0; worker < 8; worker++) jobs.add(() -> {
                for (int i = 0; i < 10000; i++) if (config.get(i % 2 == 0 ? "misc/a" : "misc/b").getInt() != (i % 2 == 0 ? 11 : 22)) return false;
                return true;
            });
            for (var result : executor.invokeAll(jobs)) check(result.get(), "concurrent read-only path cache");
        } catch (Exception error) { throw new IllegalStateException(error); }
    }

    private void finish() {
        finished = true;
        try { Files.writeString(Path.of("ic2-hotpath-regression.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("complete", failures.isEmpty(), "checks", checks, "failures", failures, "groups", groups, "nativeReloads", stage + 1))); }
        catch (Exception error) { throw new IllegalStateException(error); }
        server.halt(false);
    }

    private static final class ProbeManager extends MachineRecipeHelper<IRecipeInput, String> {
        protected IRecipeInput getForInput(IRecipeInput input) { return input; }
        void add(IRecipeInput input, String output) { var recipe = new MachineRecipe<>(input, output); recipes.put(input, recipe); addToCache(recipe); }
        void remove(IRecipeInput input) { recipes.remove(input); removeCachedRecipes(input); }
        String find(ItemStack input) { var result = getRecipe(input); return result == null ? null : result.getOutput(); }
    }
    private record PredicateInput(Predicate<ItemStack> predicate) implements IRecipeInput {
        public boolean matches(ItemStack stack) { return predicate.test(stack); }
        public int getAmount() { return 1; }
        public List<ItemStack> getInputs() { return List.of(); }
    }
    private static final class BoundedBasic extends BasicMachineRecipeManager {
        private int calls;
        @Override protected MachineRecipe<IRecipeInput, Collection<ItemStack>> getRecipe(ItemStack input) {
            if (++calls > 64) throw new IllegalStateException("replacement loop exceeded 64 lookups");
            return super.getRecipe(input);
        }
    }
    private static final class BoundedList extends BasicListRecipeManager {
        private int calls;
        @Override protected MachineRecipe<IRecipeInput, Object> getRecipe(ItemStack input) {
            if (++calls > 64) throw new IllegalStateException("replacement loop exceeded 64 lookups");
            return super.getRecipe(input);
        }
    }
}
