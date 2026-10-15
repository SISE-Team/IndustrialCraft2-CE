package ic2ce.core.event;

import ic2ce.neoforge.fluid.FluidHandler;
import ic2ce.core.network.*;
import ic2ce.neoforge.client.audio.ClientAudioService;
import ic2ce.neoforge.runtime.ProjectRuntime;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Actual fluid fog, native audio substitution and disconnect ownership replace obsolete model
 * registration callbacks.
 */
public final class EventHandlerClient {
  private EventHandlerClient() {}

  private static net.minecraft.world.level.material.Fluid projectFluid(BlockState state) {
    var fluid = FluidHandler.getWorldFluid(state);
    return fluid != null && BuiltInRegistries.FLUID.getKey(fluid).getNamespace().equals("ic2")
        ? fluid
        : null;
  }

  public static float onSetupFogDensity(BlockState state) {
    var fluid = projectFluid(state);
    return fluid == null
        ? -1
        : (float) Math.clamp(Math.abs((long) FluidHandler.getDensity(fluid)) / 10000D, 0, 2);
  }

  public static boolean hasProjectFluid(BlockState state) {
    return projectFluid(state) != null;
  }

  public static int onRenderFogColor(BlockState state) {
    var fluid = projectFluid(state);
    return fluid == null ? -1 : FluidHandler.getColor(fluid);
  }

  public static SoundInstance onSoundPlayed(SoundInstance sound) {
    return sound == null ? null : ClientAudioService.onSoundPlayed(sound);
  }

  public static void onDisconnect() {
    RpcHandler.onDisconnect();
    if (ProjectRuntime.soundManager instanceof ClientAudioService audio) audio.clear();
    if (ProjectRuntime.network.get(false) instanceof NetworkManagerClient network)
      network.resetIncomingFrames();
  }
}
