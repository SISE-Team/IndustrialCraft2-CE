package ic2.core.util;

import net.minecraft.util.profiling.ProfilerFiller;

/**
 * No-op replacement for the removed {@code ic2.core.util.NoopProfiler.INSTANCE} accessor.
 * IC2 only used the profiler for push/pop sections; dropping them has no
 * behavioral effect.
 */
public enum NoopProfiler implements ProfilerFiller {
    INSTANCE;

    @Override
    public void startTick() {}

    @Override
    public void endTick() {}

    @Override
    public void push(String name) {}

    @Override
    public void push(java.util.function.Supplier<String> nameSupplier) {}

    @Override
    public void pop() {}

    @Override
    public void popPush(String name) {}

    @Override
   public void popPush(java.util.function.Supplier<String> nameSupplier) {}

   @Override
   public void markForCharting(net.minecraft.util.profiling.metrics.MetricCategory category) {}

   @Override
   public void incrementCounter(java.util.function.Supplier<String> nameSupplier, int count) {}

   @Override
   public void incrementCounter(String name, int count) {}
}
