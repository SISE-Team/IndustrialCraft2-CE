package ic2ce.engine;

import ic2ce.engine.config.Config;
import ic2ce.engine.async.PendingResult;
import ic2ce.engine.energy.*;
import ic2ce.engine.fluid.FluidAmounts;
import ic2ce.engine.inventory.*;
import ic2ce.engine.machine.*;
import ic2ce.engine.network.FramedCompression;
import ic2ce.engine.network.WireBuffer;
import ic2ce.engine.recipe.OrderedRecipeIndex;
import ic2ce.engine.recipe.WeightedChoice;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Independent semantic oracles and adversarial state transitions, without Minecraft. */
public final class EngineContractTest {
  private static int checks;
  private static final List<String> failures = new ArrayList<>();

  private static void check(boolean result, String label) {
    checks++;
    if (!result) failures.add(label);
  }

  private record Query(int item, int component) {}

  private record Rule(int id, int item, int component, boolean direct, boolean dynamic) {}

  private static void windProfiles() {
    for (int height : new int[] {1, 16, 64, 128, 256, 320, 384, 1024})
      for (int sea : new int[] {0, 1, 32, 63, 128, 512}) {
        var curve = ic2ce.engine.weather.WindProfile.fromWorld(height, sea);
        double base = Math.max(0, sea) < height ? Math.max(0, sea) : height * .5,
            peak = (base + height) * .5,
            upper = height * 1.125;
        double[][] matrix = {
          {peak, peak * peak, peak * peak * peak, 1},
          {upper, upper * upper, upper * upper * upper, 0},
          {1, 2 * peak, 3 * peak * peak, 0}
        };
        for (int column = 0; column < 3; column++) {
          int pivot = column;
          for (int row = column + 1; row < 3; row++)
            if (Math.abs(matrix[row][column]) > Math.abs(matrix[pivot][column])) pivot = row;
          var exchange = matrix[column];
          matrix[column] = matrix[pivot];
          matrix[pivot] = exchange;
          double divisor = matrix[column][column];
          for (int entry = column; entry < 4; entry++) matrix[column][entry] /= divisor;
          for (int row = 0; row < 3; row++)
            if (row != column) {
              double scale = matrix[row][column];
              for (int entry = column; entry < 4; entry++)
                matrix[row][entry] -= scale * matrix[column][entry];
            }
        }
        check(
            Math.abs(curve.at(peak) - 1) < 1e-10 && curve.at(0) == 0 && curve.at(upper) < 1e-10,
            "closed-form wind retains independent peak, ground and upper-zero boundary");
        for (int index = -10; index <= 110; index++) {
          double altitude = height * index / 100.0,
              expected =
                  Math.max(
                      0,
                      matrix[0][3] * altitude
                          + matrix[1][3] * altitude * altitude
                          + matrix[2][3] * altitude * altitude * altitude);
          check(
              Math.abs(curve.at(altitude) - expected) < 1e-8,
              "closed-form wind matches independent pivoted linear-system oracle across native"
                  + " dimension heights");
        }
      }
    var curve = ic2ce.engine.weather.WindProfile.fromWorld(320, 63);
    check(
        curve.at(Double.NaN) == 0 && curve.at(Double.POSITIVE_INFINITY) == 0,
        "malformed wind altitude cannot publish a nonfinite machine power value");
  }

  private static void audioCallbacks() {
    var callbacks = new ic2ce.engine.audio.CompletionCallbacks();
    var events = new ArrayList<Integer>();
    callbacks.add(() -> events.add(1));
    callbacks.add(() -> events.add(2));
    callbacks.fire();
    check(
        events.equals(List.of(1, 2)),
        "audio completion listeners invoke in insertion order without recursive self-capture");
    callbacks.replace(
        () -> {
          events.add(3);
          callbacks.clear();
          callbacks.add(() -> events.add(5));
        });
    callbacks.add(() -> events.add(4));
    callbacks.fire();
    check(
        events.equals(List.of(1, 2, 3, 4)),
        "reentrant completion mutation cannot replace the current immutable invocation snapshot");
    callbacks.fire();
    check(events.getLast() == 5, "reentrant replacement belongs to the next playback completion");
    callbacks.replace(null);
    callbacks.fire();
    check(events.size() == 5, "cleared native finish callback cannot fire stale listeners");
    callbacks.add(
        () -> {
          throw new IllegalArgumentException("first");
        });
    callbacks.add(() -> events.add(6));
    callbacks.add(
        () -> {
          throw new IllegalStateException("second");
        });
    boolean aggregated = false;
    try {
      callbacks.fire();
    } catch (IllegalArgumentException expected) {
      aggregated = expected.getSuppressed().length == 1;
    }
    check(
        aggregated && events.getLast() == 6,
        "one failing finish callback does not silently skip subsequent callbacks");
  }

  public static void main(String[] args) throws Exception {
    windProfiles();
    audioCallbacks();
    recipes();
    materialCosts();
    materialCompilation();
    weighted();
    concurrency();
    pendingResults();
    processing();
    amplification();
    thermal();
    boiler();
    induction();
    experience();
    wireBuffers();
    compression();
    upgrades();
    configuration();
    routing();
    sinkRoutes();
    components();
    energyBuffer();
    storageSignals();
    tickBudgets();
    reactorHeat();
    reactorCooling();
    reactorCore();
    unitConversions();
    insertion();
    durability();
    fluids();
    fractionalFluids();
    packets();
    boundedSearch();
    fairFluidShares();
    miningLayers();
    cropArithmetic();
    cropBreeding();
    itemChargeConservation();
    craftingAssignments();
    deferredValues();
    scaffoldStructures();
    menuFillLevels();
    priorityWorkers();
    templateTexts();
    siFormatting();
    nuclearProfiles();
    insertionOrderedRecipes();
    explosionMasks();
    Path output = Path.of(args[0]);
    Files.createDirectories(output.getParent());
    Files.writeString(
        output,
        "{\"complete\":"
            + failures.isEmpty()
            + ",\"checks\":"
            + checks
            + ",\"failures\":"
            + failures.size()
            + ",\"runtime\":\""
            + Runtime.version()
            + "\"}\n");
    System.out.println("Engine contracts: " + checks + " checks, " + failures.size() + " failures");
    if (!failures.isEmpty())
      throw new AssertionError(failures.subList(0, Math.min(20, failures.size())));
  }

  private static void explosionMasks() {
    var random = new Random(2026101419);
    for (int sample = 0; sample < 1000; sample++) {
      int width = 1 + random.nextInt(40),
          height = 1 + random.nextInt(8),
          depth = 1 + random.nextInt(6);
      var mask = new ic2ce.engine.explosion.DamageMask(-20, -64, -30, width, height, depth);
      var expected = new HashMap<String, Boolean>();
      for (int step = 0; step < 100; step++) {
        int x = -21 + random.nextInt(width + 2),
            y = -65 + random.nextInt(height + 2),
            z = -31 + random.nextInt(depth + 2);
        boolean vapor = random.nextBoolean();
        mask.mark(x, y, z, vapor);
        if (x >= -20
            && x < -20 + width
            && y >= -64
            && y < -64 + height
            && z >= -30
            && z < -30 + depth) expected.merge(x + ":" + y + ":" + z, vapor, (a, b) -> a || b);
      }
      var actual = new HashMap<String, Boolean>();
      mask.forEach((x, y, z, vapor) -> actual.put(x + ":" + y + ":" + z, vapor));
      check(
          actual.equals(expected),
          "packed native damage flags match independent sparse-coordinate ownership and"
              + " irreversible vaporization dominance");
    }
    var mask = new ic2ce.engine.explosion.DamageMask(0, 0, 0, 32, 1, 1);
    mask.mark(31, 0, 0, true);
    var found = new ArrayList<Integer>();
    mask.forEach(
        (x, y, z, vapor) -> {
          if (vapor) found.add(x);
        });
    check(
        found.equals(List.of(31)) && mask.allocatedBytes() == 8,
        "damage mask uses all 64 bits, including the signed upper pair, for 32 actual cell flags");
  }

  private static void insertionOrderedRecipes() {
    var index =
        new OrderedRecipeIndex<Integer, String>(
            4, value -> value, (row, value) -> true, (row, value) -> true, false);
    index.add("dynamic-first", null);
    index.add("exact-later", new int[] {2});
    check(
        index.find(2, null).equals("dynamic-first"),
        "native canning candidates preserve original insertion precedence across dynamic and"
            + " certified exact rules");
    index.removeIf(row -> row.equals("dynamic-first"));
    check(
        index.find(2, null).equals("exact-later"),
        "removal publishes rebuilt ordered buckets without stale cached winners");
  }

  private static void nuclearProfiles() {
    var fuels = ic2ce.engine.explosion.NuclearCharge.Payload.values();
    for (var fuel : fuels)
      for (int detonators = 1; detonators <= 64; detonators++) {
        float previous = 0;
        for (int amount = 0; amount <= 64; amount++) {
          var profile = ic2ce.engine.explosion.NuclearCharge.evaluate(detonators, fuel, amount, 35);
          check(
              profile.armed()
                  && Float.isFinite(profile.power())
                  && profile.power() >= previous
                  && profile.power() <= 35
                  && profile.radiationRange() >= 0,
              "all legal nuclear inventories obey capped finite monotone physical payload"
                  + " admission");
          previous = profile.power();
          var restricted =
              ic2ce.engine.explosion.NuclearCharge.evaluate(detonators, fuel, amount, 4);
          check(
              restricted.power() == Math.min(4, profile.power())
                  && restricted.radiationRange() == profile.radiationRange(),
              "protection power cap cannot change the actual isotope radiation quantity");
        }
      }
    for (var fuel : fuels)
      check(
          !ic2ce.engine.explosion.NuclearCharge.evaluate(0, fuel, 64, 35).armed(),
          "every nuclear payload needs an actual detonator");
    var small = ic2ce.engine.explosion.NuclearCharge.Payload.SMALL_URANIUM_235;
    check(
        ic2ce.engine.explosion.NuclearCharge.evaluate(63, small, 64, 10000).power()
                == ic2ce.engine.explosion.NuclearCharge.evaluate(63, small, 1, 10000).power()
            && ic2ce.engine.explosion.NuclearCharge.evaluate(64, small, 64, 10000).power()
                > ic2ce.engine.explosion.NuclearCharge.evaluate(64, small, 1, 10000).power(),
        "small fissile payload boost begins at its exact 64-detonator threshold");
    check(
        !ic2ce.engine.explosion.NuclearCharge.evaluate(64, small, 64, Float.NaN).armed()
            && !ic2ce.engine.explosion.NuclearCharge.evaluate(
                    64, small, 64, Float.POSITIVE_INFINITY)
                .armed(),
        "invalid native nuclear limits fail admission without making a malformed entity");
  }

  private static void siFormatting() {
    Object[][] examples = {
      {0.0, 4, "0 "},
      {Double.NaN, 4, "NaN "},
      {Double.NEGATIVE_INFINITY, 4, "-∞ "},
      {1.0, 4, "1 "},
      {999.0, 6, "999 "},
      {1000.0, 6, "1 k"},
      {12345.0, 4, "12.35 k"},
      {999_950.0, 4, "1 M"},
      {40_000_000.0, 6, "40 M"},
      {-2_048.0, 3, "-2.05 k"},
      {0.0015, 4, "1.5 m"},
      {1e30, 4, "1 E30"}
    };
    for (var example : examples)
      check(
          ic2ce.engine.text.SiFormat.format((double) example[0], (int) example[1])
              .equals(example[2]),
          "SI presentation rounds exact decimals and carries into the next prefix");
    for (int digits : new int[] {0, 19}) {
      boolean rejected = false;
      try {
        ic2ce.engine.text.SiFormat.format(1, digits);
      } catch (IllegalArgumentException expected) {
        rejected = true;
      }
      check(rejected, "SI presentation rejects significant digits outside [1,18]");
    }
  }

  private static void templateTexts() {
    var variables = Map.of("name", "Machine", "n", "7", "selected", "name");
    var context =
        new ic2ce.engine.text.TemplateText.Context() {
          public String variable(String name) {
            return variables.get(name);
          }

          public String translate(String key, List<String> args) {
            return args.isEmpty()
                ? "[" + key + "]"
                : "[" + key + ":" + String.join("|", args) + "]";
          }
        };
    String[][] examples = {
      {"", ""},
      {"plain", "plain"},
      {"%name%", "Machine"},
      {"Prefix %name% %n%", "Prefix Machine 7"},
      {"100%%", "100%"},
      {"{machine.name}", "[machine.name]"},
      {"{energy,%n%,{unit.eu}}", "[energy:7|[unit.eu]]"},
      {"%{selected}%", null},
      {"\\{literal\\}", "{literal}"},
      {"{%n%}", "[7]"}
    };
    for (var pair : examples)
      check(
          Objects.equals(
              ic2ce.engine.text.TemplateText.compile(pair[0]).evaluate(context), pair[1]),
          "independent template fixture preserves escaped literals, variables and nested"
              + " translation");
    for (String malformed : List.of("%name", "{name", "escape\\")) {
      boolean rejected = false;
      try {
        ic2ce.engine.text.TemplateText.compile(malformed);
      } catch (IllegalArgumentException expected) {
        rejected = true;
      }
      check(rejected, "unfinished native template rejects deterministically");
    }
    boolean deep = false;
    try {
      ic2ce.engine.text.TemplateText.compile("{".repeat(66) + "key" + "}".repeat(66));
    } catch (IllegalArgumentException expected) {
      deep = true;
    }
    check(deep, "deep template fails before recursive stack exhaustion");
    var random = new Random(2026101411);
    for (int sample = 0; sample < 10000; sample++) {
      String original = "文字" + random.nextInt() + "{%}\\";
      String escaped =
          original.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("%", "%%");
      check(
          ic2ce.engine.text.TemplateText.compile(escaped).evaluate(context).equals(original),
          "independent random escaped Unicode strings preserve every character");
    }
  }

  private static void priorityWorkers() throws Exception {
    var worker = new ic2ce.engine.async.PriorityExecutor(1);
    var entered = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var order = Collections.synchronizedList(new ArrayList<String>());
    var firstEntered = entered;
    worker.execute(
        () -> {
          firstEntered.countDown();
          try {
            release.await();
          } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
          }
        });
    check(entered.await(5, TimeUnit.SECONDS), "priority worker native platform task started");
    class Job implements Callable<String>, ic2ce.engine.async.PriorityExecutor.CustomPriority {
      final String name;
      final ic2ce.engine.async.PriorityExecutor.Priority priority;

      Job(String name, ic2ce.engine.async.PriorityExecutor.Priority priority) {
        this.name = name;
        this.priority = priority;
      }

      public String call() {
        order.add(name);
        return name;
      }

      public ic2ce.engine.async.PriorityExecutor.Priority getPriority() {
        return priority;
      }
    }
    var jobs =
        List.<Callable<String>>of(
            new Job("low1", ic2ce.engine.async.PriorityExecutor.Priority.Low),
            new Job("default", ic2ce.engine.async.PriorityExecutor.Priority.Default),
            new Job("high1", ic2ce.engine.async.PriorityExecutor.Priority.High),
            new Job("high2", ic2ce.engine.async.PriorityExecutor.Priority.High),
            new Job("low2", ic2ce.engine.async.PriorityExecutor.Priority.Low));
    var futures = worker.submitAll(jobs);
    release.countDown();
    for (var task : futures)
      check(
          task.get(5, TimeUnit.SECONDS) != null,
          "submitted priority FutureTask completes its actual callable");
    worker.shutdown();
    check(
        worker.awaitTermination(5, TimeUnit.SECONDS)
            && order.equals(List.of("high1", "high2", "default", "low1", "low2")),
        "submitted priorities survive future wrapping and preserve FIFO in each lane");
    boolean rejected = false;
    try {
      worker.executeAll(List.of(() -> {}));
    } catch (RejectedExecutionException expected) {
      rejected = true;
    }
    check(rejected, "shutdown executor rejects every later batch through native admission");
    var removal = new ic2ce.engine.async.PriorityExecutor(1);
    entered = new CountDownLatch(1);
    var gate = new CountDownLatch(1);
    var start = entered;
    removal.execute(
        () -> {
          start.countDown();
          try {
            gate.await();
          } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
          }
        });
    check(start.await(5, TimeUnit.SECONDS), "removal worker has a real occupied CPU lane");
    var pending = removal.submit(() -> 17);
    var queue = removal.getQueue();
    var iterator = queue.iterator();
    check(iterator.hasNext(), "queue snapshot owns the pending task");
    iterator.next();
    iterator.remove();
    check(
        queue.isEmpty(), "detached queue iterator removes the actual pending cancellation target");
    pending.cancel(false);
    gate.countDown();
    removal.shutdown();
    check(
        removal.awaitTermination(5, TimeUnit.SECONDS),
        "removed pending task cannot prevent native executor shutdown");
  }

  private static void menuFillLevels() {
    var random = new Random(2026101407);
    for (int example = 0; example < 20000; example++) {
      int size = random.nextInt(10), offered = random.nextInt(129);
      int[] counts = new int[size], limits = new int[size];
      for (int index = 0; index < size; index++) {
        counts[index] = random.nextInt(81);
        limits[index] = random.nextInt(65);
      }
      int[] expected = counts.clone();
      for (int item = 0; item < offered; item++) {
        int minimum = Integer.MAX_VALUE, chosen = -1;
        for (int index = 0; index < size; index++)
          if (expected[index] < limits[index] && expected[index] < minimum) {
            chosen = index;
            minimum = expected[index];
          }
        if (chosen < 0) break;
        expected[chosen]++;
      }
      check(
          Arrays.equals(FillLevels.plan(counts, limits, offered), expected),
          "added-only menu fill agrees with independent physical single-item allocation, including"
              + " overfull cells");
    }
    check(
        Arrays.equals(
            FillLevels.plan(
                new int[] {Integer.MAX_VALUE - 1, 0},
                new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE},
                Integer.MAX_VALUE),
            new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE - 1}),
        "native menu fill uses bounded long arithmetic and retains existing quantities");
  }

  private static void scaffoldStructures() {
    var random = new Random(2026101405);
    int width = 4, height = 6, size = width * height;
    for (int example = 0; example < 3000; example++) {
      int[] reach = new int[size], above = new int[size], offsets = new int[size + 1];
      boolean[] ground = new boolean[size];
      var edges = new ArrayList<Integer>();
      for (int y = 0; y < height; y++)
        for (int x = 0; x < width; x++) {
          int node = x + y * width;
          reach[node] = new int[] {2, 5, 12}[random.nextInt(3)];
          above[node] = y + 1 < height ? node + width : -1;
          ground[node] = y == 0 && random.nextInt(4) == 0;
          offsets[node] = edges.size();
          if (x > 0) edges.add(node - 1);
          if (x + 1 < width) edges.add(node + 1);
        }
      offsets[size] = edges.size();
      int[] lateral = edges.stream().mapToInt(Integer::intValue).toArray();
      int[] expected = new int[size];
      Arrays.fill(expected, -1);
      for (int root = 0; root < size; root++)
        if (ground[root]) {
          int score = 0;
          for (int node = root; node >= 0; node = above[node]) {
            score = Math.max(reach[node], score - 1);
            expected[node] = Math.max(expected[node], score);
          }
        }
      boolean changed = true;
      while (changed) {
        changed = false;
        for (int node = 0; node < size; node++)
          if (expected[node] > 0) {
            int score = expected[node] - 1;
            int up = above[node];
            if (up >= 0 && expected[up] < score) {
              expected[up] = score;
              changed = true;
            }
            for (int edge = offsets[node]; edge < offsets[node + 1]; edge++) {
              int neighbor = lateral[edge];
              if (expected[neighbor] < score) {
                expected[neighbor] = score;
                changed = true;
              }
            }
          }
      }
      int[] actual =
          ic2ce.engine.world.ScaffoldSupport.solve(reach, above, offsets, lateral, ground);
      for (int node = 0; node < size; node++)
        check(
            actual[node] == expected[node],
            "physical scaffold frontier agrees with independent repeated relaxation and vertical"
                + " pillar oracle");
    }
  }

  private static boolean assignmentOracle(int[] candidates, int size) {
    boolean[] reachable = new boolean[1 << size];
    reachable[0] = true;
    for (int ingredient = 0; ingredient < size; ingredient++) {
      boolean[] next = new boolean[1 << size];
      for (int occupied = 0; occupied < reachable.length; occupied++)
        if (reachable[occupied])
          for (int slot = 0; slot < size; slot++)
            if ((occupied & (1 << slot)) == 0 && (candidates[ingredient] & (1 << slot)) != 0)
              next[occupied | (1 << slot)] = true;
      reachable = next;
    }
    return reachable[reachable.length - 1];
  }

  private static void craftingAssignments() {
    for (int size = 0; size <= 4; size++) {
      int choices = 1 << size, patterns = 1 << (size * size);
      for (int pattern = 0; pattern < patterns; pattern++) {
        int[] candidates = new int[size];
        int encoded = pattern;
        for (int index = 0; index < size; index++) {
          candidates[index] = encoded & (choices - 1);
          encoded >>>= size;
        }
        check(
            ic2ce.engine.recipe.IngredientAssignment.matches(candidates, size)
                == assignmentOracle(candidates, size),
            "complete ambiguous small-grid assignment agrees with independent subset enumeration");
      }
    }
    var random = new Random(2026101403);
    for (int size = 5; size <= 9; size++)
      for (int sample = 0; sample < 5000; sample++) {
        int[] candidates = new int[size];
        for (int index = 0; index < size; index++) candidates[index] = random.nextInt(1 << size);
        check(
            ic2ce.engine.recipe.IngredientAssignment.matches(candidates, size)
                == assignmentOracle(candidates, size),
            "larger native crafting assignment preserves actual one-to-one physical slots");
      }
  }

  private static void deferredValues() throws Exception {
    var resolutions = new AtomicInteger();
    var deferred =
        new ic2ce.engine.recipe.DeferredValue<>(
            () -> {
              resolutions.incrementAndGet();
              return new Object();
            });
    try (var executor = Executors.newFixedThreadPool(4)) {
      var tasks = new ArrayList<Future<Object>>();
      for (int index = 0; index < 16; index++) tasks.add(executor.submit(deferred::get));
      Object expected = tasks.getFirst().get(5, TimeUnit.SECONDS);
      for (var task : tasks)
        check(
            task.get(5, TimeUnit.SECONDS) == expected,
            "deferred record publishes one value to concurrent readers");
    }
    check(resolutions.get() == 1, "deferred supplier invoked exactly once");
    var nullCalls = new AtomicInteger();
    var nullable =
        new ic2ce.engine.recipe.DeferredValue<>(
            () -> {
              nullCalls.incrementAndGet();
              return null;
            });
    check(
        nullable.get() == null && nullable.get() == null && nullCalls.get() == 1,
        "resolved null does not retrigger supplier");
    var attempts = new AtomicInteger();
    var retry =
        new ic2ce.engine.recipe.DeferredValue<>(
            () -> {
              if (attempts.getAndIncrement() == 0)
                throw new IllegalArgumentException("first attempt");
              return 17;
            });
    boolean failed = false;
    try {
      retry.get();
    } catch (IllegalArgumentException expected) {
      failed = true;
    }
    check(
        failed && retry.get() == 17 && attempts.get() == 2,
        "failed deferred computation remains retryable without partial publication");
    var loop = new ic2ce.engine.recipe.DeferredValue<?>[1];
    loop[0] = new ic2ce.engine.recipe.DeferredValue<>(() -> loop[0].get());
    boolean recursive = false;
    try {
      loop[0].get();
    } catch (IllegalStateException expected) {
      recursive = true;
    }
    check(recursive, "recursive deferred graph rejects without infinite recursion");
  }

  private static void itemChargeConservation() {
    var random = new Random(2026101402);
    for (int example = 0; example < 10000; example++) {
      double capacity = (random.nextInt(100000) + 1) / 4.0, stored = 0, input = 0, output = 0;
      for (int step = 0; step < 16; step++) {
        double requested = random.nextInt(200000) / 4.0, rate = random.nextInt(5000) / 4.0;
        if (random.nextBoolean()) {
          double received = ChargeMath.charge(stored, capacity, requested, rate, false);
          input += received;
          stored += received;
        } else {
          double released = ChargeMath.discharge(stored, capacity, requested, rate, false);
          output += released;
          stored -= released;
        }
        check(
            stored >= 0 && stored <= capacity && input - output == stored,
            "physical charge sequence conserves independent input/output accounting");
      }
      double remaining = ChargeMath.discharge(stored, capacity, Double.POSITIVE_INFINITY, 0, true);
      output += remaining;
      stored -= remaining;
      check(
          stored == 0
              && input == output
              && ChargeMath.discharge(stored, capacity, 1, 1, false) == 0,
          "complete final drain accounts for every accepted input and cannot repeat a paid output");
    }
    for (double invalid :
        new double[] {Double.NaN, Double.NEGATIVE_INFINITY, -1, -Double.MAX_VALUE})
      check(
          ChargeMath.charge(50, 100, invalid, 10, false) == 0
              && ChargeMath.discharge(50, 100, invalid, 10, false) == 0,
          "invalid requests cannot reverse either side of the physical ledger");
    check(
        ChargeMath.stored(Double.POSITIVE_INFINITY, 100) == 0
            && ChargeMath.stored(Double.NaN, 100) == 0
            && ChargeMath.stored(-1, 100) == 0,
        "invalid saved charge never creates infinite or negative usable energy");
    check(
        ChargeMath.displayDamage(100, 100, 32) == 0
            && ChargeMath.displayDamage(0, 100, 32) == 31
            && ChargeMath.displayDamage(1, 0, 0) == 0,
        "native durability display has finite full/empty/zero-capacity boundaries");
  }

  private static void cropBreeding() {
    var random = new Random(2026101501);
    String[] names = {"Red", "red", "Food", "Wheat", "Flower", null};
    for (int sample = 0; sample < 10000; sample++) {
      var candidate = breedingSpecies(random, names);
      var parent = breedingSpecies(random, names);
      long expected = 0;
      for (int i = 0; i < Math.min(candidate.properties().length, parent.properties().length); i++)
        expected += 2 - Math.abs((long) candidate.properties()[i] - parent.properties()[i]);
      if (candidate.attributes() != null && parent.attributes() != null)
        for (var left : candidate.attributes())
          for (var right : parent.attributes())
            if (left != null && right != null && left.equalsIgnoreCase(right)) expected += 5;
      long difference = (long) candidate.tier() - parent.tier();
      if (difference > 1) expected -= difference * 2;
      else if (difference < -3) expected += difference;
      check(
          ic2ce.engine.agriculture.CropBreeding.ratio(candidate, parent, false)
              == Math.max(0, expected),
          "crossing weight sums property closeness, shared attributes and tier distance " + sample);
    }
    var any = breedingSpecies(random, names);
    check(
        ic2ce.engine.agriculture.CropBreeding.ratio(any, any, true) == 500,
        "a parent's own species keeps the fixed crossing weight");
    check(
        ic2ce.engine.agriculture.CropBreeding.ratio(
                new ic2ce.engine.agriculture.CropBreeding.Species(new int[] {0}, null, 20),
                new ic2ce.engine.agriculture.CropBreeding.Species(new int[] {5}, null, 0),
                false)
            == 0,
        "strongly unfavourable crossings clamp to zero weight");
    long bound = (1L << 40) + 3;
    long[] accepted = {42L};
    var index = new java.util.concurrent.atomic.AtomicInteger();
    check(
        ic2ce.engine.agriculture.CropBreeding.ticket(bound, () -> accepted[index.getAndIncrement()])
                == 21
            && index.get() == 1,
        "large crossing tickets reduce one unbiased 63-bit draw");
    // Long.MAX_VALUE lies in the biased tail above the last whole multiple of the bound.
    long[] tail = {-1L, Long.MIN_VALUE};
    index.set(0);
    check(
        ic2ce.engine.agriculture.CropBreeding.ticket(bound, () -> tail[index.getAndIncrement()])
                == (1L << 62) % bound
            && index.get() == 2,
        "large crossing tickets reject draws from the biased tail");
    for (int sample = 0; sample < 1000; sample++) {
      long limit = 1 + (random.nextLong() >>> 2);
      long value = ic2ce.engine.agriculture.CropBreeding.ticket(limit, random::nextLong);
      check(value >= 0 && value < limit, "crossing tickets stay within their bound " + sample);
    }
    var coins = new java.util.concurrent.atomic.AtomicInteger();
    check(
        ic2ce.engine.agriculture.CropBreeding.weedGrowth(31, 3, () -> coins.incrementAndGet() > 0)
                == 31
            && coins.get() == 0,
        "a maximal weed growth trait draws no coin");
    check(
        ic2ce.engine.agriculture.CropBreeding.weedGrowth(4, 9, () -> true) == 10
            && ic2ce.engine.agriculture.CropBreeding.weedGrowth(4, 9, () -> false) == 9,
        "overtaking weeds keep the stronger growth and may raise it once");
  }

  private static ic2ce.engine.agriculture.CropBreeding.Species breedingSpecies(
      Random random, String[] names) {
    var properties = new int[random.nextInt(6)];
    for (int i = 0; i < properties.length; i++) properties[i] = random.nextInt(11) - 2;
    String[] attributes = null;
    if (random.nextInt(5) != 0) {
      attributes = new String[random.nextInt(4)];
      for (int i = 0; i < attributes.length; i++) attributes[i] = names[random.nextInt(names.length)];
    }
    return new ic2ce.engine.agriculture.CropBreeding.Species(
        properties, attributes, random.nextInt(20) - 4);
  }

  private static void cropArithmetic() {
    var random = new Random(2026101401);
    for (int sample = 0; sample < 10000; sample++) {
      int count = 1 + random.nextInt(4);
      var parents = new ic2ce.engine.agriculture.CropMath.Genome[count];
      var sums = new int[3];
      for (int index = 0; index < count; index++) {
        int growth = random.nextInt(32), gain = random.nextInt(32), resistance = random.nextInt(32);
        parents[index] = new ic2ce.engine.agriculture.CropMath.Genome(growth, gain, resistance);
        sums[0] += growth;
        sums[1] += gain;
        sums[2] += resistance;
      }
      var averaged = ic2ce.engine.agriculture.CropMath.inherit(parents, 0, 0, 0);
      check(
          averaged.growth() == sums[0] / count
              && averaged.gain() == sums[1] / count
              && averaged.resistance() == sums[2] / count,
          "phenotype averages preserve independent parent sums without narrow integer wrap "
              + sample);
      var mutated = ic2ce.engine.agriculture.CropMath.inherit(parents, count, -count, count);
      check(
          mutated.growth() >= averaged.growth()
              && mutated.gain() <= averaged.gain()
              && mutated.resistance() >= averaged.resistance()
              && mutated.growth() <= 31
              && mutated.gain() >= 0
              && mutated.resistance() <= 31,
          "bounded genetic mutation stays directional and in the physical trait range " + sample);
    }
    var plain = new ic2ce.engine.agriculture.CropMath.Genome(0, 0, 0);
    check(
        ic2ce.engine.agriculture.CropMath.growth(plain, 1, 0, 0, 0)
            .equals(new ic2ce.engine.agriculture.CropMath.Growth(3, false)),
        "minimum-quality crop receives its three base points");
    check(
        ic2ce.engine.agriculture.CropMath.growth(plain, 1, 20, 0, 0).points() == 6,
        "one hundred extra quality percent doubles base growth");
    check(
        ic2ce.engine.agriculture.CropMath.growth(plain, 8, 0, 0, 1).dies(),
        "severe deficit exposes an unresistant crop to death");
    var resistant = new ic2ce.engine.agriculture.CropMath.Genome(0, 0, 31);
    check(
        !ic2ce.engine.agriculture.CropMath.growth(
                resistant, Integer.MAX_VALUE, Integer.MIN_VALUE, 6, 31)
            .dies(),
        "maximum legal resistance survives the deficit draw without arithmetic overflow");
    check(
        ic2ce.engine.agriculture.CropMath.growth(
                    new ic2ce.engine.agriculture.CropMath.Genome(31, 31, 31),
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    6,
                    0)
                .points()
            == Integer.MAX_VALUE,
        "extreme positive addon environment weights saturate positive growth");
    check(
        ic2ce.engine.agriculture.CropMath.accumulate(32000, 1000) == 33000
            && ic2ce.engine.agriculture.CropMath.accumulate(Integer.MAX_VALUE, 1)
                == Integer.MAX_VALUE,
        "native progress can cross the old short boundary and saturates without wrapping");
  }

  private static void storageSignals() {
    check(
        !StorageSignalPolicy.signal(1, 799, 1000, 10)
            && StorageSignalPolicy.signal(1, 800, 1000, 10),
        "near-full bank signal includes its exact twenty-packet boundary");
    check(
        !StorageSignalPolicy.signal(2, 10, 1000, 10)
            && StorageSignalPolicy.signal(2, 11, 1000, 10)
            && StorageSignalPolicy.signal(2, 989, 1000, 10)
            && !StorageSignalPolicy.signal(2, 990, 1000, 10),
        "partial-charge bank signal excludes both endpoint packet thresholds");
    check(
        StorageSignalPolicy.signal(3, 989, 1000, 10)
            && !StorageSignalPolicy.signal(3, 990, 1000, 10),
        "not-full bank signal keeps its strict upper boundary");
    check(
        StorageSignalPolicy.signal(4, 9, 1000, 10) && !StorageSignalPolicy.signal(4, 10, 1000, 10),
        "empty bank signal keeps its strict one-packet boundary");
    check(
        !StorageSignalPolicy.discharge(5, true, 1000, 1000, 10)
            && StorageSignalPolicy.discharge(5, false, 0, 1000, 10),
        "inhibit mode follows the actual external signal");
    check(
        !StorageSignalPolicy.discharge(6, true, 800, 1000, 10)
            && StorageSignalPolicy.discharge(6, true, 801, 1000, 10),
        "overflow mode discharges only above its strict near-full threshold");
  }

  private static void miningLayers() {
    for (int radius : new int[] {0, 1, 2, 8, 16, 32}) {
      long ordinal = 0;
      for (int depth = 0; depth < 3; depth++)
        for (int z = -radius; z <= radius; z++)
          for (int x = -radius; x <= radius; x++) {
            var cell = LayerSweep.cell(ordinal, radius);
            check(
                cell.x() == x
                    && cell.z() == z
                    && cell.depth() == depth
                    && LayerSweep.index(x, depth, z, radius) == ordinal,
                "mining layers follow independent nested-loop spatial order without duplicates "
                    + radius
                    + "/"
                    + ordinal);
            ordinal++;
          }
    }
    try {
      LayerSweep.index(33, 0, 0, 32);
      check(false, "out-of-range cursor rejected");
    } catch (IllegalArgumentException expected) {
      check(true, "out-of-range cursor rejected");
    }
    try {
      LayerSweep.cell(Long.MAX_VALUE, 0);
      check(false, "overflowing mining depth rejected");
    } catch (IllegalArgumentException expected) {
      check(true, "overflowing mining depth rejected");
    }
  }

  private static void boundedSearch() {
    var search = new ic2ce.engine.graph.BoundedSearch(256);
    var random = new Random(2026101321);
    for (int sample = 0; sample < 2000; sample++) {
      int nodes = 1 + random.nextInt(180), target = random.nextInt(nodes);
      boolean[] blocked = new boolean[nodes];
      int[][] graph = new int[nodes][];
      for (int node = 0; node < nodes; node++) {
        blocked[node] = node != 0 && random.nextInt(5) == 0;
        graph[node] = new int[random.nextInt(7)];
        for (int edge = 0; edge < graph[node].length; edge++)
          graph[node][edge] = random.nextInt(nodes);
      }
      var expected = new ArrayDeque<Integer>();
      boolean[] seen = new boolean[nodes];
      expected.add(0);
      seen[0] = true;
      boolean found = false;
      while (!expected.isEmpty()) {
        int node = expected.removeFirst();
        if (blocked[node]) continue;
        if (node == target) {
          found = true;
          break;
        }
        for (int adjacent : graph[node])
          if (!seen[adjacent]) {
            seen[adjacent] = true;
            expected.addLast(adjacent);
          }
      }
      var result =
          search.find(
              0,
              node -> blocked[(int) node] ? -1 : node == target ? 1 : 0,
              (node, output) -> {
                var edges = graph[(int) node];
                for (int edge = 0; edge < edges.length; edge++) output[edge] = edges[edge];
                return edges.length;
              });
      check(
          result.isPresent() == found && (result.isEmpty() || result.getAsLong() == target),
          "bounded cyclic region matches independent collection traversal " + sample);
    }
    check(
        search.find(Long.MIN_VALUE, node -> 1, (node, output) -> 0).orElseThrow() == Long.MIN_VALUE,
        "all long identifiers remain valid targets");
    var small = new ic2ce.engine.graph.BoundedSearch(8);
    var visits = new AtomicInteger();
    check(
        small
                .find(
                    0,
                    node -> {
                      visits.incrementAndGet();
                      return 0;
                    },
                    (node, output) -> {
                      for (int i = 0; i < 6; i++) output[i] = node * 6 + i + 1;
                      return 6;
                    })
                .isEmpty()
            && visits.get() <= 49,
        "an unbounded source graph performs at most one plus six times the configured region size"
            + " classifications");
  }

  private static void fairFluidShares() {
    var random = new Random(2026101322);
    var allocations = new int[6];
    for (int sample = 0; sample < 10000; sample++) {
      int[] demands = new int[6];
      for (int index = 0; index < demands.length; index++) demands[index] = random.nextInt(301);
      int budget = random.nextInt(2001);
      ic2ce.engine.fluid.FairAllocation.plan(budget, demands, allocations, sample);
      int total = 0, offered = 0;
      for (int index = 0; index < demands.length; index++) {
        total += allocations[index];
        offered += demands[index];
        check(
            allocations[index] >= 0 && allocations[index] <= demands[index],
            "fluid allocation honors each actual receiver capacity " + sample);
      }
      check(
          total == Math.min(budget, offered),
          "fluid plan conserves every offered integer millibucket " + sample);
      for (int a = 0; a < 6; a++)
        for (int b = 0; b < 6; b++)
          if (allocations[a] < demands[a])
            check(
                allocations[b] <= allocations[a] + 1,
                "unsaturated receivers receive max-min fair shares " + sample);
    }
    int[] demand = {1, 1, 1, 1, 1, 1}, received = new int[6];
    for (int tick = 0; tick < 600; tick++) {
      ic2ce.engine.fluid.FairAllocation.plan(1, demand, allocations, tick);
      for (int index = 0; index < 6; index++) received[index] += allocations[index];
    }
    for (int count : received)
      check(count == 100, "single-millibucket remainders rotate without outlet starvation");
    demand = new int[] {0, 1, 0, 1, 0, 0};
    Arrays.fill(received, 0);
    for (int tick = 0; tick < 600; tick++) {
      ic2ce.engine.fluid.FairAllocation.plan(1, demand, allocations, tick);
      for (int index = 0; index < 6; index++) received[index] += allocations[index];
    }
    check(
        received[1] == 300 && received[3] == 300,
        "absent outlets never bias sparse single-millibucket rotation");
  }

  private static void amplification() {
    var budget = new AmplificationBudget();
    budget.restore(1);
    double spent = 0;
    for (int packet = 0; packet < 100; packet++) spent += budget.consume(0.1);
    check(
        Math.abs(spent - 1) < 1e-12 && budget.available() == 0,
        "fractional incoming packets cannot amplify forever without spending one credit");
    var random = new Random(2026101319);
    double supplied = 0;
    spent = 0;
    for (int n = 0; n < 50000; n++) {
      if (random.nextBoolean()) {
        int amount = 1 + random.nextInt(1000);
        budget.add(amount);
        supplied += amount;
      }
      double incoming = random.nextInt(30001) / 16.0, taken = budget.consume(incoming);
      spent += taken;
      check(
          taken >= 0 && taken <= incoming && Math.abs(supplied - spent - budget.available()) < 1e-7,
          "amplifier exact credit conservation across fractional packets " + n);
    }
    budget.restore(Double.NaN);
    check(
        budget.available() == 0 && budget.consume(-1) == 0,
        "invalid amplifier persistence or requests are inert");
  }

  private static void boiler() {
    check(
        BoilerThermodynamics.target(0) == 100
            && BoilerThermodynamics.target(220) == 374
            && BoilerThermodynamics.huPerMb(220) == 200,
        "boiler pressure independently defines boiling threshold and latent HU cost");
    double temperature = 0;
    for (int tick = 0; tick < 2000; tick++) {
      var step = BoilerThermodynamics.advance(temperature, 100, 0, 1);
      temperature = step.temperature();
      check(step.boiledMb() == 0, "warm boiler cannot produce unpaid steam " + tick);
    }
    var boiling = BoilerThermodynamics.advance(temperature, 100, 0, 1);
    check(
        boiling.boiledMb() == 1
            && boiling.consumedMb() == 1
            && Math.abs(boiling.temperature() - 100) < 1e-7,
        "100 HU boils exactly one millibucket after warmup");
    var highPressure = BoilerThermodynamics.advance(374, 1200, 220, 1000);
    check(
        highPressure.boiledMb() == 6
            && highPressure.consumedMb() == 6
            && highPressure.temperature() == 374,
        "220-bar pressure consumes 200 HU for each of six steam batches");
    var random = new Random(2026101318);
    for (int n = 0; n < 50000; n++) {
      double before = random.nextInt(100000) / 200.0;
      int supplied = random.nextInt(1201),
          pressure = random.nextInt(301),
          feed = random.nextInt(1001);
      var step = BoilerThermodynamics.advance(before, supplied, pressure, feed);
      double removed =
          supplied
              - (step.temperature() - before) * 2000
              - step.boiledMb() * BoilerThermodynamics.huPerMb(pressure);
      check(
          step.boiledMb() >= 0
              && step.boiledMb() <= step.consumedMb()
              && step.consumedMb() <= feed
              && Double.isFinite(step.temperature())
              && removed >= -1e-6
              && removed <= 4000.000001,
          "bounded boiler water and energy balance including at most two degrees of thermostat"
              + " quenching "
              + n);
    }
  }

  private static void reactorCore() {
    var random = new Random(2026101502);
    for (int sample = 0; sample < 20000; sample++) {
      int heat = random.nextInt(), amount = random.nextInt();
      check(
          ic2ce.engine.reactor.ReactorCore.addHeat(heat, amount)
              == (int) Math.clamp((long) Math.max(0, heat) + amount, 0, Integer.MAX_VALUE),
          "reactor hull heat saturates within zero and the int range " + sample);
      int width = random.nextInt(400) - 10, maxHeat = random.nextInt(20001) - 1;
      int gauge = ic2ce.engine.reactor.ReactorCore.heatGauge(width, Math.abs(heat), maxHeat);
      check(
          gauge >= 0 && (maxHeat > 0 || gauge == 0),
          "reactor heat gauge is nonnegative and empty without capacity " + sample);
    }
    check(
        ic2ce.engine.reactor.ReactorCore.heatGauge(100, 8500, 10000) == 100
            && ic2ce.engine.reactor.ReactorCore.heatGauge(100, 4250, 10000) == 50,
        "the heat gauge reaches full scale at eighty-five percent of maximum heat");
    check(
        ic2ce.engine.reactor.ReactorCore.addOutput(-3, Float.NaN) == -3
            && ic2ce.engine.reactor.ReactorCore.addOutput(-3, 2) == 2
            && ic2ce.engine.reactor.ReactorCore.addOutput(Float.MAX_VALUE, Float.MAX_VALUE)
                == Float.MAX_VALUE,
        "reactor output ignores nonfinite pulses and saturates finite ones from zero");
    check(
        ic2ce.engine.reactor.ReactorCore.electricalOutput(3, 1) == 15
            && ic2ce.engine.reactor.ReactorCore.electricalOutput(3, Double.NaN) == 0
            && ic2ce.engine.reactor.ReactorCore.electricalOutput(Float.NaN, 2) == 0,
        "electrical reactor output is five EU per output point times the configured factor");
    check(
        ic2ce.engine.reactor.ReactorCore.addEmittedHeat(Integer.MAX_VALUE - 1, 5) == Integer.MAX_VALUE
            && ic2ce.engine.reactor.ReactorCore.addEmittedHeat(7, -3) == 7,
        "pending coolant heat only grows and saturates");
  }

  private static void reactorCooling() {
    var random = new Random(2026101317);
    for (int trial = 0; trial < 100; trial++) {
      var ledger = new ic2ce.engine.reactor.ReactorCooling();
      double factor = (1 + random.nextInt(400)) / 8.0, generated = 0, converted = 0, returned = 0;
      for (int step = 0; step < 500; step++) {
        int heat = random.nextInt(501),
            cost = 1 + random.nextInt(200),
            available = random.nextInt(101);
        var result = ledger.exchange(heat, factor, cost, available);
        generated += heat * factor;
        converted += (long) result.millibuckets() * cost;
        returned += result.returnedHullHeat() * factor;
        double held = ledger.pendingHu() + ledger.hullFraction() * factor;
        check(
            result.millibuckets() >= 0
                && result.millibuckets() <= available
                && Math.abs(generated - converted - returned - held) < 1e-7,
            "fractional reactor heat conservation under coolant/space shortages "
                + trial
                + "/"
                + step);
        if (step % 37 == 0) ledger.restore(ledger.pendingHu(), ledger.hullFraction());
      }
    }
    var ledger = new ic2ce.engine.reactor.ReactorCooling();
    check(
        ledger.exchange(1, 2.5, 10, 1).millibuckets() == 0 && ledger.pendingHu() == 2.5,
        "partial coolant heat is carried without heating the hull twice");
    check(
        ledger.exchange(0, 2.5, 10, 0).returnedHullHeat() == 1 && ledger.pendingHu() == 0,
        "a blocked coolant outlet returns previously reserved heat once");
    check(
        ledger.exchange(0, 2.5, 10, 0).returnedHullHeat() == 0,
        "a second blocked cycle cannot duplicate returned heat");
  }

  private static void reactorHeat() {
    var random = new Random(2026101316);
    for (int n = 0; n < 30000; n++) {
      int fromCapacity = 1 + random.nextInt(10000), toCapacity = 1 + random.nextInt(10000);
      int from = random.nextInt(fromCapacity + 1),
          to = random.nextInt(toCapacity + 1),
          rate = random.nextInt(1001);
      int moved =
          ic2ce.engine.reactor.HeatBalance.transfer(from, fromCapacity, to, toCapacity, rate);
      long difference = (long) from * toCapacity - (long) to * fromCapacity;
      long after = (long) (from - moved) * toCapacity - (long) (to + moved) * fromCapacity;
      check(
          Math.abs(moved) <= rate
              && from - moved >= 0
              && from - moved <= fromCapacity
              && to + moved >= 0
              && to + moved <= toCapacity
              && from + to == from - moved + to + moved
              && Math.abs(after) <= Math.abs(difference)
              && (difference == 0 || after == 0 || Long.signum(after) == Long.signum(difference)),
          "capacity-weighted reactor exchange conservation/equilibrium " + n);
    }
    check(
        ic2ce.engine.reactor.HeatBalance.triangular(Integer.MAX_VALUE) == Integer.MAX_VALUE,
        "pulse triangular arithmetic saturates instead of overflowing");
    check(
        ic2ce.engine.reactor.HeatBalance.transfer(10, 0, 10, 100, 10) == 0,
        "zero reactor capacity does not create a nonfinite exchange");
  }

  private static void tickBudgets() {
    var budget = new TickBudget();
    var random = new Random(2026101315);
    for (int tick = 0; tick < 10000; tick++) {
      int produced = random.nextInt(10001);
      budget.reset(produced);
      int spent = 0;
      for (int request = 0; request < 10; request++) {
        int wanted = random.nextInt(2001) - 1;
        boolean simulate = random.nextBoolean();
        int expected = Math.min(Math.max(0, wanted), produced - spent);
        int actual = budget.draw(wanted, simulate);
        if (!simulate) spent += expected;
        check(
            actual == expected && budget.available() + spent == produced,
            "per-tick source quantity oracle " + tick + "/" + request);
      }
    }
  }

  private static void unitConversions() {
    var ledger = new UnitConversionLedger();
    var random = new Random(2026101314);
    long units = 0;
    double used = 0;
    for (int n = 0; n < 50000; n++) {
      double demand = random.nextInt(51) / 10.0;
      int required = ledger.requiredUnits(demand, 0.5);
      ledger.commit(demand, 0.5, required);
      units += required;
      used += demand;
      check(
          Math.abs(units * 0.5 - used - ledger.credit()) < 1e-7
              && ledger.credit() >= 0
              && ledger.credit() < 0.5000000001,
          "indivisible conversion units conserve energy across packets " + n);
      if (n % 1001 == 0) ledger.restore(ledger.credit());
    }
    ledger.restore(0);
    int consumed = 0;
    for (int n = 0; n < 50000; n++) {
      int required = ledger.requiredUnits(0.1, 0.5);
      ledger.commit(0.1, 0.5, required);
      consumed += required;
    }
    check(consumed == 10000, "five decimal packets consume one indivisible heat unit");
    check(
        ledger.requiredUnits(Double.NaN, 0.5) == -1 && ledger.requiredUnits(1, 0) == -1,
        "invalid conversion requests and ratios are rejected");
    ledger.restore(0);
    check(
        ledger.requiredUnits(Double.MIN_VALUE, Double.MAX_VALUE) == 1,
        "subnormal positive request does not create energy for free");
  }

  private record CostRule(
      double overhead, int[][] inputs, int[][] counts, int[] outputs, int[] outputCounts) {}

  /** A quantity-carrying material whose equality, like the platform's, ignores the quantity. */
  private record Material(String name, int size) {
    @Override
    public boolean equals(Object other) {
      return other instanceof Material material && material.name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

  private static void materialCompilation() {
    var compiler =
        new ic2ce.engine.recipe.MaterialCostCompiler<Material>(
            Material::size, material -> new Material(material.name(), 1));
    var random = new Random(2026101504);
    for (int trial = 0; trial < 300; trial++) {
      var seeds = new LinkedHashMap<Material, Double>();
      for (int s = 0; s < 1 + random.nextInt(4); s++)
        seeds.put(new Material("m" + random.nextInt(10), 1), (double) random.nextInt(50));
      var rules = new ArrayList<ic2ce.engine.recipe.MaterialCostCompiler.Transformation<Material>>();
      for (int r = 0; r < random.nextInt(12); r++) {
        var inputs = new ArrayList<List<Material>>();
        for (int g = 0; g < random.nextInt(3); g++) {
          var alternatives = new ArrayList<Material>();
          for (int a = 0; a < 1 + random.nextInt(3); a++)
            alternatives.add(new Material("m" + random.nextInt(14), 1 + random.nextInt(4)));
          inputs.add(alternatives);
        }
        rules.add(
            new ic2ce.engine.recipe.MaterialCostCompiler.Transformation<>(
                random.nextInt(8),
                inputs,
                List.of(new Material("m" + random.nextInt(14), 1 + random.nextInt(3)))));
      }
      var compiled = compiler.compile(rules, seeds);
      var order = new LinkedHashMap<String, Integer>();
      for (var seed : seeds.keySet()) order.putIfAbsent(seed.name(), order.size());
      for (var rule : rules) {
        for (var group : rule.inputs()) for (var input : group) order.putIfAbsent(input.name(), order.size());
        for (var output : rule.outputs()) order.putIfAbsent(output.name(), order.size());
      }
      boolean ordered = compiled.materials().size() == order.size();
      int index = 0;
      for (var name : order.keySet())
        ordered &=
            compiled.materials().get(index).name().equals(name)
                && compiled.materials().get(index++).size() == 1;
      boolean seeded = true;
      for (int i = 0; i < compiled.seeds().length; i++) {
        var seed = seeds.get(compiled.materials().get(i));
        seeded &= compiled.seeds()[i] == (seed == null ? Double.POSITIVE_INFINITY : seed);
      }
      var solved = compiled.graph().solve(compiled.seeds());
      boolean bounded = true;
      for (int i = 0; i < solved.length; i++) bounded &= solved[i] <= compiled.seeds()[i];
      check(
          ordered && seeded && bounded,
          "material compilation keys unit materials in first-appearance order with seeded costs "
              + trial);
    }
  }

  private static void materialCosts() {
    var random = new Random(2026101313);
    for (int trial = 0; trial < 200; trial++) {
      int nodes = 40;
      var graph = new ic2ce.engine.recipe.MaterialCostGraph(nodes);
      var rules = new ArrayList<CostRule>();
      var expected = new double[nodes];
      Arrays.fill(expected, Double.POSITIVE_INFINITY);
      for (int n = 0; n < 8; n++) expected[n] = random.nextInt(200);
      for (int r = 0; r < 100; r++) {
        int groups = random.nextInt(4);
        var inputs = new int[groups][];
        var counts = new int[groups][];
        for (int g = 0; g < groups; g++) {
          int alternatives = 1 + random.nextInt(3);
          inputs[g] = new int[alternatives];
          counts[g] = new int[alternatives];
          for (int a = 0; a < alternatives; a++) {
            inputs[g][a] = random.nextInt(nodes);
            counts[g][a] = 1 + random.nextInt(4);
          }
        }
        var rule =
            new CostRule(
                random.nextInt(10),
                inputs,
                counts,
                new int[] {random.nextInt(nodes)},
                new int[] {1 + random.nextInt(4)});
        graph.add(rule.overhead, inputs, counts, rule.outputs, rule.outputCounts);
        rules.add(rule);
      }
      var actual = graph.solve(expected);
      for (int pass = 0; pass < 10000; pass++) {
        boolean changed = false;
        for (var rule : rules) {
          double total = rule.overhead;
          for (int g = 0; g < rule.inputs.length; g++) {
            double best = Double.POSITIVE_INFINITY;
            for (int a = 0; a < rule.inputs[g].length; a++)
              best = Math.min(best, expected[rule.inputs[g][a]] * rule.counts[g][a]);
            total += best;
          }
          double next = total / rule.outputCounts[0];
          int node = rule.outputs[0];
          if (Double.isFinite(next) && next + 1e-9 < expected[node]) {
            expected[node] = next;
            changed = true;
          }
        }
        if (!changed) break;
      }
      for (int n = 0; n < nodes; n++)
        check(
            actual[n] == expected[n] || Math.abs(actual[n] - expected[n]) < 1e-7,
            "material hypergraph full-scan oracle " + trial + "/" + n);
    }
    int depth = 30000;
    var chain = new ic2ce.engine.recipe.MaterialCostGraph(depth);
    for (int n = depth - 1; n > 0; n--)
      chain.add(1, new int[][] {{n - 1}}, new int[][] {{1}}, new int[] {n}, new int[] {1});
    var seeds = new double[depth];
    Arrays.fill(seeds, Double.POSITIVE_INFINITY);
    seeds[0] = 1;
    check(
        chain.solve(seeds)[depth - 1] == depth,
        "thirty-thousand-node dependency chain avoids call-stack recursion");
    var cycle = new ic2ce.engine.recipe.MaterialCostGraph(2);
    cycle.add(0, new int[][] {{0}}, new int[][] {{1}}, new int[] {1}, new int[] {1});
    cycle.add(0, new int[][] {{1}}, new int[][] {{1}}, new int[] {0}, new int[] {1});
    check(
        cycle.solve(new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY})[0]
            == Double.POSITIVE_INFINITY,
        "unseeded cycle cannot invent material values");
  }

  private static void fractionalFluids() {
    var ledger = new ic2ce.engine.fluid.FractionalFluidLedger();
    check(
        ledger.requiredMillibuckets(Double.NaN) == -1
            && ledger.requiredMillibuckets(Double.POSITIVE_INFINITY) == -1
            && ledger.requiredMillibuckets(-1) == -1
            && ledger.requiredMillibuckets(Double.MAX_VALUE) == -1,
        "invalid fractional costs cannot create fluid");
    ledger.restore(Double.NaN);
    check(ledger.creditBuckets() == 0, "corrupt saved fluid credit is rejected");
    var random = new Random(2026101312);
    long drained = 0;
    double consumed = 0;
    for (int n = 0; n < 50000; n++) {
      double amount = random.nextInt(10001) / 10000000.0;
      int required = ledger.requiredMillibuckets(amount);
      ledger.commit(amount, required);
      drained += required;
      consumed += amount;
      check(
          Math.abs(drained / 1000.0 - consumed - ledger.creditBuckets()) < 1e-9
              && ledger.creditBuckets() >= 0
              && ledger.creditBuckets() < 0.00100000001,
          "fractional fluid conservation over mixed costs " + n);
      if (n % 997 == 0) ledger.restore(ledger.creditBuckets());
    }
    ledger.restore(0);
    int decimalDrained = 0;
    for (int n = 0; n < 100000; n++) {
      int charge = ledger.requiredMillibuckets(0.0001);
      decimalDrained += charge;
      ledger.commit(0.0001, charge);
    }
    check(
        decimalDrained == 10000,
        "one hundred thousand decimal tenths do not accumulate millibucket rounding charges");
    ledger.restore(0);
    check(
        ledger.requiredMillibuckets(1e-100) == 1,
        "tiny positive fluid costs are not rounded down to zero");
    boolean rejected = false;
    try {
      ledger.commit(1, 0);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(rejected, "fractional transaction cannot commit without its required drain");
  }

  private static void thermal() {
    check(
        ThermalRamp.advance(5000, 5000, 5000, 1, true) == 5000,
        "powered heater cannot exceed maximum heat");
    check(ThermalRamp.ratio(0, 0) == 0, "zero thermal target has a finite gauge");
    var random = new Random(2026101307);
    for (int count = 0; count < 10000; count++) {
      int max = random.nextInt(10001),
          heat = random.nextInt(15001) - 1000,
          target = random.nextInt(15002) - 1,
          cooling = random.nextInt(10);
      boolean powered = random.nextBoolean();
      int expected = Math.max(0, Math.min(max, heat));
      if (powered && target >= 0) {
        int goal = Math.max(0, Math.min(max, target));
        expected = Math.min(expected, goal);
        if (expected < goal) expected++;
      } else for (int tick = 0; tick < cooling && expected > 0; tick++) expected--;
      check(
          ThermalRamp.advance(heat, target, max, cooling, powered) == expected,
          "thermal heating/cooling oracle " + count);
    }
  }

  private static void induction() {
    var cycle = new InductionCycle();
    cycle.restore(1000, 20);
    check(
        cycle.advance(true, false, 15) == 1 && cycle.progress() == 20,
        "heater cannot create processing progress without its remaining energy bill");
    var random = new Random(2026101308);
    for (int count = 0; count < 20000; count++) {
      int heat = random.nextInt(10001),
          progress = random.nextInt(4000),
          energy = random.nextInt(30);
      boolean recipe = random.nextBoolean(), hold = random.nextBoolean();
      cycle.restore(heat, progress);
      int expectedHeat = heat, expectedBill = 0, expectedProgress = progress;
      if ((recipe || hold) && energy > 0) {
        expectedBill++;
        if (expectedHeat < 10000) expectedHeat++;
      } else for (int cooling = 0; cooling < 4 && expectedHeat > 0; cooling++) expectedHeat--;
      if (!recipe) expectedProgress = 0;
      else if (energy - expectedBill >= 15) {
        expectedBill += 15;
        expectedProgress += expectedHeat / 30;
      }
      int actualBill = cycle.advance(recipe, hold, energy);
      check(
          actualBill == expectedBill
              && actualBill <= energy
              && cycle.heat() == expectedHeat
              && cycle.progress() == expectedProgress,
          "induction thermal/progress/energy oracle " + count);
    }
  }

  private static void experience() {
    check(
        ExperienceAccount.normalize(Double.POSITIVE_INFINITY) == 0
            && ExperienceAccount.normalize(Double.NaN) == 0,
        "nonfinite saved experience is rejected");
    check(
        ExperienceAccount.claimable(Double.MAX_VALUE, 10000) == 10000,
        "one experience collection has a bounded payout");
    var random = new Random(2026101309);
    for (int count = 0; count < 20000; count++) {
      int units = random.nextInt(4000000);
      double fraction = random.nextInt(4) / 4.0;
      double balance = units + fraction;
      int reward = random.nextInt(200);
      int expectedPaid = Math.min(units + reward, 10000);
      double earned = ExperienceAccount.earn(balance, reward);
      int paid = ExperienceAccount.claimable(earned, 10000);
      check(
          paid == expectedPaid
              && ExperienceAccount.afterClaim(earned, paid)
                  == units + reward - expectedPaid + fraction,
          "experience integer/fraction conservation oracle " + count);
    }
  }

  private static void wireBuffers() throws Exception {
    var random = new Random(2026101310);
    for (int count = 0; count < 10000; count++) {
      var bytes = new ByteArrayOutputStream();
      var oracle = new DataOutputStream(bytes);
      var actual = new WireBuffer(random.nextInt(9));
      int integer = random.nextInt();
      long wide = random.nextLong();
      short small = (short) random.nextInt();
      char character = (char) random.nextInt(65536);
      double decimal = random.nextDouble() * 1e120;
      float single = random.nextFloat() * 1e20F;
      oracle.writeBoolean(true);
      actual.writeBoolean(true);
      oracle.writeInt(integer);
      actual.writeInt(integer);
      oracle.writeLong(wide);
      actual.writeLong(wide);
      oracle.writeShort(small);
      actual.writeShort(small);
      oracle.writeChar(character);
      actual.writeChar(character);
      oracle.writeDouble(decimal);
      actual.writeDouble(decimal);
      oracle.writeFloat(single);
      actual.writeFloat(single);
      var text =
          new String(
              new char[] {'\0', character, (char) random.nextInt(65536), '中', '文', 'é', '漢', '字'});
      oracle.writeUTF(text);
      actual.writeUTF(text);
      actual.flip();
      actual.mark();
      check(
          Arrays.equals(bytes.toByteArray(), actual.copyBytes(Integer.MAX_VALUE)),
          "JDK primitive/modified UTF wire oracle " + count);
      actual.reset();
      check(
          actual.readBoolean()
              && actual.readInt() == integer
              && actual.readLong() == wide
              && actual.readShort() == small
              && actual.readChar() == character
              && actual.readDouble() == decimal
              && actual.readFloat() == single
              && actual.readUTF().equals(text)
              && actual.available() == 0,
          "JDK primitive/modified UTF round trip " + count);
      int positive = random.nextInt(Integer.MAX_VALUE);
      actual.clear();
      actual.writeVarInt(positive);
      actual.writeString(text);
      actual.flip();
      check(
          actual.readVarInt() == positive
              && actual
                  .readString()
                  .equals(
                      new String(
                          text.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                          java.nio.charset.StandardCharsets.UTF_8)),
          "variable integer and UTF8 round trip " + count);
    }
    var invalid =
        WireBuffer.wrap(
            new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0});
    boolean rejected = false;
    try {
      invalid.readVarInt();
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(rejected, "oversized variable integer cannot wrap its shift count");
    var declared = new WireBuffer();
    declared.writeVarInt(Integer.MAX_VALUE);
    declared.flip();
    rejected = false;
    try {
      declared.readString();
    } catch (java.nio.BufferUnderflowException expected) {
      rejected = true;
    }
    check(rejected, "string length is checked before allocating storage");
    var maximum = new WireBuffer();
    maximum.writeUTF("x".repeat(65535));
    maximum.flip();
    check(maximum.readUTF().length() == 65535, "maximum legal modified UTF byte count");
    rejected = false;
    try {
      maximum.clear();
      maximum.writeUTF("\0".repeat(32768));
    } catch (UTFDataFormatException expected) {
      rejected = true;
    }
    check(rejected, "modified UTF enforces its byte limit");
    class Frame extends WireBuffer {
      Frame(byte[] data) {
        super(0);
        adopt(data, 3, 2);
      }
    }
    var frame = new Frame(new byte[] {10, 20, 30, 40, 50});
    check(
        frame.readByte() == 40 && frame.skipBytes(-10) == -1 && frame.readByte() == 40,
        "borrowed frame cannot rewind before its origin");
    frame.rewind();
    check(
        frame.readByte() == 40 && frame.readByte() == 50 && !frame.hasAvailable(),
        "rewind retains borrowed-frame bounds");
    var from = WireBuffer.wrap(new byte[] {1, 2, 3});
    var to = new WireBuffer(0);
    from.writeTo(to);
    to.flip();
    check(
        from.available() == 0 && Arrays.equals(to.copyBytes(3), new byte[] {1, 2, 3}),
        "buffer transfer conserves bytes and advances its source");
  }

  private static void compression() throws Exception {
    var random = new Random(2026101311);
    var assembler = new FramedCompression(1 << 20, 1 << 20);
    for (int count = 0; count < 256; count++) {
      var original = new byte[random.nextInt(65537)];
      random.nextBytes(original);
      var compressed = new ByteArrayOutputStream();
      try (var stream = new java.util.zip.DeflaterOutputStream(compressed)) {
        stream.write(original);
      }
      var bytes = compressed.toByteArray();
      int offset = 0, kind = count % 3;
      while (offset < bytes.length) {
        int size = Math.min(bytes.length - offset, 1 + random.nextInt(4096));
        assembler.frame(
            (kind << 2) | (offset == 0 ? 1 : 0) | (offset + size == bytes.length ? 2 : 0), count);
        assembler.write(bytes, offset, size);
        offset += size;
      }
      var result = assembler.finish();
      check(
          result.kind() == kind && Arrays.equals(result.bytes(), original),
          "JDK compression/fragment oracle " + count);
    }
    boolean rejected = false;
    try {
      assembler.frame(2, 0);
    } catch (IOException expected) {
      rejected = true;
    }
    check(rejected, "continuation without a first compressed frame is rejected");
    assembler.frame(1, 0);
    rejected = false;
    try {
      assembler.frame(5, 1);
    } catch (IOException expected) {
      rejected = true;
    }
    check(rejected, "interleaved compressed messages cannot overwrite incomplete state");
    var compressed = new ByteArrayOutputStream();
    try (var stream = new java.util.zip.DeflaterOutputStream(compressed)) {
      stream.write(new byte[8192]);
    }
    var tiny = new FramedCompression(4096, 64);
    tiny.frame(3, 0);
    tiny.write(compressed.toByteArray());
    rejected = false;
    try {
      tiny.finish();
    } catch (IOException expected) {
      rejected = true;
    }
    check(rejected, "expansion is bounded independently of compressed input size");
    assembler.frame(3, 0);
    assembler.write(new byte[] {1, 2, 3});
    rejected = false;
    try {
      assembler.finish();
    } catch (IOException expected) {
      rejected = true;
    }
    check(rejected, "malformed compression resets its frame state");
    assembler.frame(1, 0);
    rejected = false;
    try {
      assembler.frame(0, 10_000_000_001L);
    } catch (IOException expected) {
      rejected = true;
    }
    check(rejected, "incomplete compressed messages have a finite lifetime");
  }

  private static void recipes() {
    var activeTag = new AtomicInteger(3);
    var rules = new ArrayList<Rule>();
    var index =
        new OrderedRecipeIndex<Query, Rule>(
            2,
            Query::item,
            (r, q) -> r.component() >= 0 || r.dynamic() || r.item() == q.item(),
            (r, q) ->
                (r.dynamic() ? activeTag.get() : r.item()) == q.item()
                    && (r.component() < 0 || r.component() == q.component()));
    var random = new Random(20261013);
    Object epoch = new Object();
    for (int step = 0; step < 500; step++) {
      if (step % 5 == 0) {
        int id = random.nextInt(step + 1);
        rules.removeIf(r -> r.id() == id);
        index.removeIf(r -> r.id() == id);
      }
      Rule rule =
          new Rule(
              step,
              random.nextInt(128),
              random.nextBoolean() ? -1 : random.nextInt(4),
              random.nextBoolean(),
              false);
      rules.add(rule);
      index.add(rule, rule.direct() ? new int[] {rule.item()} : null);
      if (step % 17 == 0) {
        activeTag.set(random.nextInt(128));
        epoch = new Object();
      }
      for (int q = 0; q < 32; q++) {
        Query input = new Query(random.nextInt(128), random.nextInt(4));
        Rule expected = null;
        for (boolean direct : new boolean[] {true, false}) {
          for (Rule candidate : rules)
            if (candidate.direct() == direct
                && candidate.item() == input.item()
                && (candidate.component() < 0 || candidate.component() == input.component())) {
              expected = candidate;
              break;
            }
          if (expected != null) break;
        }
        check(
            Objects.equals(expected, index.find(input, epoch)),
            "ordered linear oracle step=" + step);
      }
    }
    var tagged =
        new OrderedRecipeIndex<Query, Rule>(
            1,
            Query::item,
            (r, q) -> activeTag.get() == q.item(),
            (r, q) -> activeTag.get() == q.item());
    Rule rule = new Rule(0, 0, -1, false, true);
    tagged.add(rule, null);
    activeTag.set(7);
    epoch = new Object();
    check(tagged.find(new Query(7, 0), epoch) == rule, "positive tag bucket");
    check(tagged.find(new Query(8, 0), epoch) == null, "negative tag bucket");
    activeTag.set(8);
    epoch = new Object();
    check(tagged.find(new Query(7, 0), epoch) == null, "positive invalidation");
    check(tagged.find(new Query(8, 0), epoch) == rule, "negative invalidation");
    int[] direct = {255};
    var copiedKeys =
        new OrderedRecipeIndex<Query, Rule>(
            1, Query::item, (r, q) -> false, (r, q) -> q.item() == 255);
    copiedKeys.add(rule, direct);
    direct[0] = 4;
    check(copiedKeys.find(new Query(255, 0), epoch) == rule, "direct key defensively copied");
  }

  private static void weighted() {
    var random = new Random(819);
    for (int trial = 0; trial < 100; trial++) {
      var table = new WeightedChoice();
      var weights = new ArrayList<Integer>();
      for (int i = 0; i < 30; i++) {
        int weight = random.nextInt(15);
        table.add(weight);
        weights.add(weight);
      }
      for (long ticket = 0; ticket < table.totalWeight(); ticket++) {
        long cumulative = 0;
        int expected = -1;
        for (int i = 0; i < weights.size(); i++) {
          cumulative += weights.get(i);
          if (ticket < cumulative) {
            expected = i;
            break;
          }
        }
        check(
            table.select(ticket) == expected,
            "weighted selection matches exact linear mass distribution");
      }
      table.clear();
      check(table.size() == 0 && table.totalWeight() == 0, "weighted table clear resets mass");
    }
    var large = new WeightedChoice();
    large.add(Integer.MAX_VALUE);
    large.add(Integer.MAX_VALUE);
    check(
        large.totalWeight() == 2L * Integer.MAX_VALUE && large.select(Integer.MAX_VALUE) == 1,
        "weighted sums cannot overflow the legacy integer limit");
    boolean negative = false;
    try {
      large.add(-1);
    } catch (IllegalArgumentException error) {
      negative = true;
    }
    check(negative && large.size() == 2, "invalid weight does not partially mutate the table");
  }

  private static void concurrency() throws Exception {
    var index =
        new OrderedRecipeIndex<Query, Integer>(
            128, Query::item, (r, q) -> r == q.item(), (r, q) -> r == q.item());
    for (int i = 0; i < 32; i++) index.add(i, null);
    Object epoch = new Object();
    var pool = Executors.newFixedThreadPool(8);
    var gate = new CyclicBarrier(8);
    try {
      var jobs = new ArrayList<Future<Boolean>>();
      for (int worker = 0; worker < 8; worker++) {
        int offset = worker;
        jobs.add(
            pool.submit(
                () -> {
                  gate.await();
                  for (int n = 0; n < 10000; n++) {
                    int item = (n + offset) & 127;
                    if (!Objects.equals(
                        item < 32 ? item : null, index.find(new Query(item, 0), epoch)))
                      return false;
                  }
                  return true;
                }));
      }
      for (var job : jobs)
        check(job.get(30, TimeUnit.SECONDS), "concurrent cold/warm candidate reads");
    } finally {
      pool.shutdownNow();
    }
  }

  private static void pendingResults() throws Exception {
    var first = new PendingResult<Integer>();
    check(
        first.complete(7) && !first.complete(8) && first.get() == 7,
        "first future completion wins");
    var cancelled = new PendingResult<Integer>();
    cancelled.cancel(false);
    boolean rejected = false;
    try {
      cancelled.get();
    } catch (CancellationException error) {
      rejected = true;
    }
    check(
        rejected && !cancelled.complete(9) && cancelled.isCancelled(),
        "cancelled future cannot publish a later result");
    var waiting = new PendingResult<Integer>();
    boolean timeout = false;
    try {
      waiting.get(0, TimeUnit.NANOSECONDS);
    } catch (TimeoutException error) {
      timeout = true;
    }
    check(timeout && !waiting.isDone(), "timed future read does not complete the operation");
    waiting.complete(3);
    check(
        waiting.get(-1, TimeUnit.NANOSECONDS) == 3,
        "negative timeout retains indefinite-wait compatibility");
  }

  private static void processing() {
    var random = new Random(41);
    for (int length : new int[] {1, 2, 3, 20, 300, 10000}) {
      var cycle = new ProcessingCycle(0, false);
      int ticks = 0, completions = 0;
      for (int n = 0; n < 10000; n++) {
        boolean runnable = random.nextBoolean();
        var transition = cycle.advance(true, runnable, length);
        if (runnable) ticks++;
        if (transition == ProcessingCycle.Transition.COMPLETE) completions++;
        check(
            cycle.progress() == ticks % length && completions == ticks / length,
            "processing retains paused work");
      }
      cycle.advance(false, false, length);
      check(cycle.progress() == 0 && !cycle.active(), "missing recipe resets work");
      cycle.restore(length / 2, true);
      cycle.rescale(length, length * 2);
      check(cycle.progress() == length / 2 * 2, "upgrade rescales progress");
    }
  }

  private static void upgrades() {
    for (int count = 0; count <= 256; count++) {
      double time = Math.pow(.7, count), energy = Math.pow(1.6, count);
      for (int length : new int[] {0, 1, 20, 300, 400, 1000}) {
        var rates = UpgradeMath.rates(length, 32, 10000, 1, 0, time, 0, energy, 10000, 1, 2);
        double scaled = length * 64.0 * time;
        int expectedOps =
            length == 0 ? 64 : (int) Math.min(Math.ceil(64.0 / scaled), 2.147483647E9);
        int expectedTicks =
            length == 0 ? 1 : Math.max(1, (int) Math.round(scaled * expectedOps / 64));
        check(
            rates.operations() == expectedOps && rates.ticks() == expectedTicks,
            "upgrade arithmetic boundary");
        check(
            rates.capacity() >= 0 && rates.demand() >= 0 && rates.tier() == 3,
            "upgrade saturation");
      }
    }
  }

  private static void configuration() throws Exception {
    Config config = new Config("Contract");
    config.set("wide/\u3000option", 17);
    check(config.get("wide/option").getInt() == 17, "leading Unicode path whitespace retained");
    check(config.get("wide/\u2003option").getInt() == 17, "equivalent Unicode path whitespace");
    for (String key : new String[] {"misc/flag", "\"quoted/path\"/flag", "escaped\\/part/flag"}) {
      config.set(key, 3);
      check(config.get(key).getInt() == 3, "config nested path");
      config.get(key).value = "4";
      check(config.get(key).getInt() == 4, "direct numeric mutation invalidates cache");
      config.remove(key);
      check(config.get(key) == null, "config removal");
      config.set(key, 5);
      check(config.get(key).getInt() == 5, "config reinsertion");
    }
    String input =
        "\ufeff; root\n"
            + ";---\n"
            + "; section\n"
            + "[first / second]\n"
            + "; label\n"
            + "foo = 1,234.5\n"
            + "bar = some\\=value\n"
            + "continued = first\\\n"
            + " second\n"
            + "quoted = \"a=b\"\n";
    config = new Config("INI");
    config.load(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    check(config.get("first/second/foo").getDouble() == 1234.5, "US number grouping");
    check(config.get("first/second/bar").getString().equals("some=value"), "escaped separator");
    check(
        config.get("first/second/continued").getString().equals("first second"), "continued value");
    check(config.get("first/second/quoted").getString().equals("\"a=b\""), "quoted separator");
    config.addSub("hidden", "").setSaveWithParent(false);
    var output = new ByteArrayOutputStream();
    config.save(output);
    Config restored = new Config("Restored");
    restored.load(new ByteArrayInputStream(output.toByteArray()));
    check(restored.get("first/second/foo").getDouble() == 1234.5, "saved values reload");
    check(restored.getSub("hidden") == null, "hidden section excluded");
    for (String bad : new String[] {"[unclosed", "a=b=c", "=value", "a=unfinished\\"}) {
      boolean rejected = false;
      try {
        new Config("Bad").load(new ByteArrayInputStream(bad.getBytes(StandardCharsets.UTF_8)));
      } catch (Config.ParseException error) {
        rejected = true;
      }
      check(rejected, "invalid INI rejected: " + bad);
    }
  }

  private static void sinkRoutes() {
    var random = new Random(2026101503);
    for (int graph = 0; graph < 300; graph++) {
      int count = 4 + random.nextInt(12);
      var roles = new WeightedTopology.Role[count];
      for (int i = 0; i < count; i++)
        roles[i] = WeightedTopology.Role.values()[random.nextInt(WeightedTopology.Role.values().length)];
      roles[0] = WeightedTopology.Role.SOURCE;
      var adjacency = new ArrayList<List<int[]>>();
      for (int i = 0; i < count; i++) adjacency.add(new ArrayList<>());
      int size = 0;
      for (int a = 0; a < count; a++)
        for (int b = a + 1; b < count; b++)
          if (random.nextInt(3) == 0) {
            int loss = random.nextInt(6);
            adjacency.get(a).add(new int[] {b, loss});
            adjacency.get(b).add(new int[] {a, loss});
            size += 2;
          }
      int[] offsets = new int[count + 1], targets = new int[size];
      double[] losses = new double[size];
      int cursor = 0;
      for (int a = 0; a < count; a++) {
        offsets[a] = cursor;
        for (var edge : adjacency.get(a)) {
          targets[cursor] = edge[0];
          losses[cursor++] = edge[1] * .5;
        }
      }
      offsets[count] = cursor;
      var workspace = new WeightedTopology(roles, offsets, targets, losses).workspace();
      var owners = new Object[count];
      for (int i = 0; i < count; i++) owners[i] = "owner" + random.nextInt(4);
      workspace.solve(0);
      var sinks = new ArrayList<Integer>();
      for (int i = 0; i < count; i++)
        if (roles[i] == WeightedTopology.Role.SINK && Double.isFinite(workspace.lossTo(i)))
          sinks.add(i);
      sinks.sort(Comparator.comparingDouble(workspace::lossTo).thenComparingInt(Integer::intValue));
      var unique = new LinkedHashMap<Object, Integer>();
      for (int sink : sinks) {
        var existing = unique.get(owners[sink]);
        if (existing != null && workspace.lossTo(existing) <= workspace.lossTo(sink)) continue;
        unique.put(owners[sink], sink);
      }
      var expected = new ArrayList<>(unique.values());
      var selected = ic2ce.engine.energy.SinkRoutes.select(roles, workspace, owners);
      boolean same = selected.size() == expected.size();
      for (int i = 0; same && i < selected.size(); i++)
        same =
            selected.get(i).sink() == expected.get(i)
                && selected.get(i).loss() == workspace.lossTo(expected.get(i));
      check(
          same,
          "sink routes keep the cheapest reachable route per owner in loss/index order " + graph);
    }
  }

  private static void routing() {
    var random = new Random(817);
    for (int graph = 0; graph < 200; graph++) {
      int count = 12;
      var roles = new WeightedTopology.Role[count];
      for (int i = 0; i < count; i++)
        roles[i] =
            i < 3
                ? WeightedTopology.Role.SOURCE
                : i < 8 ? WeightedTopology.Role.CONDUCTOR : WeightedTopology.Role.SINK;
      double[][] edges = new double[count][count];
      for (double[] row : edges) Arrays.fill(row, Double.POSITIVE_INFINITY);
      int size = 0;
      for (int a = 0; a < count; a++)
        for (int b = a + 1; b < count; b++)
          if (random.nextInt(4) == 0) {
            edges[a][b] = edges[b][a] = random.nextInt(17) * .25;
            size += 2;
          }
      int[] offsets = new int[count + 1], targets = new int[size];
      double[] losses = new double[size];
      int cursor = 0;
      for (int a = 0; a < count; a++) {
        offsets[a] = cursor;
        for (int b = 0; b < count; b++)
          if (Double.isFinite(edges[a][b])) {
            targets[cursor] = b;
            losses[cursor++] = edges[a][b];
          }
      }
      offsets[count] = cursor;
      var topology = new WeightedTopology(roles, offsets, targets, losses);
      Arrays.fill(losses, 1e9); // Caller mutation cannot change the compiled graph.
      var workspace = topology.workspace();
      for (int source = 0; source < 3; source++) {
        var oracle = new double[count];
        Arrays.fill(oracle, Double.POSITIVE_INFINITY);
        oracle[source] = 0;
        for (int iteration = 0; iteration < count; iteration++) {
          for (int a = 0; a < count; a++) {
            if (roles[a] != WeightedTopology.Role.CONDUCTOR && a != source) continue;
            for (int b = 0; b < count; b++)
              if (roles[b] != WeightedTopology.Role.SOURCE)
                oracle[b] = Math.min(oracle[b], oracle[a] + edges[a][b]);
          }
        }
        workspace.solve(source);
        for (int target = 0; target < count; target++) {
          check(workspace.lossTo(target) == oracle[target], "Bellman-Ford routing oracle");
          int[] route = workspace.routeTo(target);
          if (!Double.isFinite(oracle[target])) {
            check(route.length == 0, "unreachable route");
            continue;
          }
          double sum = 0;
          for (int i = 1; i < route.length; i++) sum += edges[route[i - 1]][route[i]];
          check(
              route[0] == source && route[route.length - 1] == target && sum == oracle[target],
              "route loss and endpoints");
          for (int i = 1; i < route.length - 1; i++)
            check(roles[route[i]] == WeightedTopology.Role.CONDUCTOR, "terminals cannot conduct");
        }
      }
    }
  }

  private static void components() {
    var random = new Random(109);
    for (int trial = 0; trial < 200; trial++) {
      int size = 24;
      boolean[] wires = new boolean[size];
      boolean[][] edges = new boolean[size][size];
      int[] offsets = new int[size + 1];
      for (int i = 0; i < size; i++) wires[i] = random.nextBoolean();
      for (int a = 0; a < size; a++)
        for (int b = a + 1; b < size; b++)
          if (random.nextInt(7) == 0) edges[a][b] = edges[b][a] = true;
      for (int a = 0; a < size; a++) {
        offsets[a + 1] = offsets[a];
        for (boolean edge : edges[a]) if (edge) offsets[a + 1]++;
      }
      int[] neighbors = new int[offsets[size]];
      for (int a = 0; a < size; a++) {
        int cursor = offsets[a];
        for (int b = 0; b < size; b++) if (edges[a][b]) neighbors[cursor++] = b;
      }
      var actual = TerminalComponents.partition(wires, offsets, neighbors);
      boolean[] visited = new boolean[size];
      var expected = new HashSet<Set<Integer>>();
      for (int start = 0; start < size; start++) {
        if (!wires[start] || visited[start]) continue;
        var island = new HashSet<Integer>();
        var queue = new ArrayDeque<Integer>();
        queue.add(start);
        visited[start] = true;
        island.add(start);
        while (!queue.isEmpty()) {
          int a = queue.remove();
          for (int b = 0; b < size; b++)
            if (edges[a][b]) {
              island.add(b);
              if (wires[b] && !visited[b]) {
                visited[b] = true;
                queue.add(b);
              }
            }
        }
        expected.add(island);
      }
      for (int a = 0; a < size; a++)
        for (int b = a + 1; b < size; b++)
          if (!wires[a] && !wires[b] && edges[a][b]) expected.add(Set.of(a, b));
      for (int node = 0; node < size; node++) {
        int candidate = node;
        if (expected.stream().noneMatch(s -> s.contains(candidate))) expected.add(Set.of(node));
      }
      var sets = new HashSet<Set<Integer>>();
      for (int[] component : actual) {
        var set = new HashSet<Integer>();
        for (int node : component) set.add(node);
        check(set.size() == component.length, "component has no repeated endpoint");
        sets.add(set);
      }
      check(sets.equals(expected), "conductor-only breadth-first component oracle");
      for (int node = 0; node < size; node++)
        if (wires[node]) {
          int candidate = node;
          check(
              actual.stream().filter(c -> Arrays.stream(c).anyMatch(n -> n == candidate)).count()
                  == 1,
              "conductor belongs to exactly one component");
        }
    }
    var bridge =
        TerminalComponents.partition(
            new boolean[] {true, false, true}, new int[] {0, 1, 3, 4}, new int[] {1, 0, 2, 1});
    check(
        bridge.size() == 2
            && Arrays.equals(bridge.get(0), new int[] {0, 1})
            && Arrays.equals(bridge.get(1), new int[] {1, 2}),
        "terminal shared by two islands cannot bridge their conductors");
    check(
        TerminalComponents.partition(new boolean[0], new int[] {0}, new int[0]).isEmpty(),
        "empty component graph");
  }

  private static void energyBuffer() {
    var random = new Random(311);
    var buffer = new EnergyBuffer(1000);
    double oracle = 0;
    for (int tick = 0; tick < 50000; tick++) {
      if (tick % 31 == 0) {
        buffer.setCapacity(random.nextInt(2001));
        check(buffer.stored() == oracle, "capacity changes retain work");
      }
      int amount = random.nextInt(2001);
      switch (random.nextInt(4)) {
        case 0 -> {
          double accepted = Math.min(amount, Math.max(0, buffer.capacity() - oracle));
          check(buffer.add(amount) == accepted, "bounded energy insertion");
          oracle += accepted;
        }
        case 1 -> {
          double accepted = Math.min(amount, oracle);
          check(
              buffer.withdraw(amount, true) == accepted && buffer.stored() == oracle,
              "energy simulation has no side effects");
          check(buffer.withdraw(amount, false) == accepted, "energy withdrawal bound");
          oracle -= accepted;
        }
        case 2 -> {
          boolean accepted = oracle >= amount;
          check(buffer.spendExact(amount) == accepted, "exact spending is atomic");
          if (accepted) oracle -= amount;
        }
        case 3 -> {
          buffer.forceDelta(amount);
          oracle += amount;
        }
      }
      check(
          buffer.stored() == oracle
              && buffer.stored() >= 0
              && buffer.free() == Math.max(0, buffer.capacity() - oracle),
          "energy conservation and capacity demand");
    }
    buffer.restoreStored(100);
    buffer.setCapacity(0);
    check(
        buffer.add(20) == 0 && buffer.stored() == 100,
        "overcapacity addition cannot erase stored work");
    check(
        buffer.add(-120) == -100 && buffer.stored() == 0, "signed energy removal cannot underflow");
    buffer.restoreStored(Double.NaN);
    check(buffer.stored() == 0, "invalid saved quantity cannot poison the ledger");
  }

  private record InventoryStack(int item, int amount, int maximum) {}

  private static void insertion() {
    var random = new Random(114);
    for (int trial = 0; trial < 3000; trial++) {
      InventoryStack[] slots = new InventoryStack[9];
      for (int i = 0; i < slots.length; i++) {
        int maximum = new int[] {1, 16, 64}[random.nextInt(3)];
        slots[i] = new InventoryStack(random.nextInt(4), random.nextInt(maximum + 1), maximum);
      }
      var original = slots.clone();
      var workspace =
          new InsertionWorkspace<>(
              9,
              i -> slots[i],
              InventoryStack::amount,
              InventoryStack::maximum,
              (a, b) -> a.item() == b.item() && a.maximum() == b.maximum());
      var inputs = new ArrayList<InventoryStack>();
      for (int i = 0; i < 4; i++)
        inputs.add(
            new InventoryStack(
                random.nextInt(4), random.nextInt(128), new int[] {1, 16, 64}[random.nextInt(3)]));
      int limit = 1 + random.nextInt(64), remaining = 0;
      var expected = slots.clone();
      for (InventoryStack input : inputs) {
        int amount = input.amount();
        var candidates = new ArrayList<Integer>();
        for (int i = 0; i < expected.length; i++)
          if (expected[i].amount() > 0
              && expected[i].item() == input.item()
              && expected[i].maximum() == input.maximum()) candidates.add(i);
        for (int i = 0; i < expected.length; i++) if (expected[i].amount() == 0) candidates.add(i);
        for (int slot : candidates) {
          InventoryStack current = expected[slot];
          int accepted =
              Math.min(amount, Math.max(0, Math.min(limit, input.maximum()) - current.amount()));
          if (accepted > 0)
            expected[slot] =
                new InventoryStack(input.item(), current.amount() + accepted, input.maximum());
          amount -= accepted;
        }
        remaining += amount;
      }
      check(
          workspace.prepare(inputs, limit) == remaining,
          "output remainder matches independent candidate-list oracle");
      check(Arrays.equals(slots, original), "insertion simulation cannot modify caller state");
      int before = Arrays.stream(original).mapToInt(InventoryStack::amount).sum();
      int after = 0;
      for (int i = 0; i < slots.length; i++) {
        check(
            workspace.count(i) == expected[i].amount(),
            "output planner preserves per-slot quantity");
        if (workspace.count(i) > 0)
          check(
              workspace.kind(i).item() == expected[i].item()
                  && workspace.kind(i).maximum() == expected[i].maximum(),
              "output planner preserves item/components");
        after += workspace.count(i);
      }
      check(
          after - before + remaining == inputs.stream().mapToInt(InventoryStack::amount).sum(),
          "output quantity conservation");
    }
  }

  private static void durability() {
    for (int maximum = 1; maximum <= 64; maximum++)
      for (int count = 1; count <= 12; count++)
        for (int damage = 0; damage <= maximum; damage++) {
          int oracle = 0;
          for (int item = 0; item < count; item++) oracle += maximum - (item == 0 ? damage : 0);
          check(
              DurabilityBudget.available(count, damage, maximum) == oracle,
              "nominal wear matches explicit item-by-item capacity");
        }
    check(DurabilityBudget.available(4, 0, 0) == 0, "zero durability cannot enter a wear loop");
    check(
        DurabilityBudget.available(Integer.MAX_VALUE, 0, Integer.MAX_VALUE) == Integer.MAX_VALUE,
        "wear budget saturates without integer overflow");
  }

  private static void fluids() {
    var random = new Random(116);
    for (int i = 0; i < 20000; i++) {
      int capacity = random.nextInt(Integer.MAX_VALUE),
          stored = random.nextInt(Integer.MAX_VALUE),
          request = random.nextInt(Integer.MAX_VALUE);
      long free = Math.max(0L, (long) capacity - stored);
      check(
          FluidAmounts.fill(capacity, stored, request) == Math.min(free, request),
          "fluid fill matches wide integer conservation oracle");
      check(
          FluidAmounts.drain(stored, request) == Math.min((long) stored, request),
          "fluid drain matches wide integer conservation oracle");
    }
  }

  private static void packets() {
    var random = new Random(918);
    for (int i = 0; i < 10000; i++) {
      double offer = random.nextInt(8193),
          loss = random.nextInt(300) * .25,
          demand = random.nextInt(8193);
      double amount = PacketTransfer.offeredToSink(offer, loss, demand);
      check(
          amount <= demand && amount <= Math.max(0, offer - loss) && amount >= 0,
          "packet bounded by demand and loss");
      if (amount == 0) continue;
      double rejected = random.nextBoolean() ? amount : random.nextInt((int) amount + 1);
      var transfer = PacketTransfer.account(amount, rejected, loss, demand);
      check(
          transfer.accepted() >= 0 && transfer.accepted() <= amount && transfer.spent() <= offer,
          "packet conservation bounds");
      check(
          transfer.spent() == 0 || transfer.spent() == transfer.accepted() + loss,
          "loss charged only to accepted transfer");
    }
  }
}
