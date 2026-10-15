package ic2ce.neoforge.runtime;

import ic2ce.neoforge.network.NetworkManager;
import ic2ce.core.proxy.EnvProxy;
import ic2ce.core.proxy.SideProxy;
import ic2ce.core.proxy.SideProxyServer;
import ic2ce.core.util.Keyboard;
import ic2ce.engine.async.PriorityExecutor;
import ic2ce.core.util.SideGateway;
import ic2ce.neoforge.EnvProxyNeoForge;
import ic2ce.neoforge.audio.AudioService;
import ic2ce.neoforge.diagnostics.ProjectLog;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;

/** Shared runtime bindings for the sole supported NeoForge platform. */
public final class ProjectRuntime {
  public static final String MODID = "ic2",
      RESOURCE_DOMAIN = "ic2",
      ICON_STACK_NAME = "ic2:tab_icon";
  public static final String VERSION = version();
  public static final EnvProxy envProxy = new EnvProxyNeoForge();
  public static final SideProxy sideProxy =
      envProxy.isClientEnv()
          ? instantiate("ic2ce.core.proxy.SideProxyClient", SideProxy.class)
          : new SideProxyServer();
  public static final ProjectLog log = new ProjectLog(LogManager.getLogger("IndustrialCraft2-CE"));
  public static final SideGateway<NetworkManager> network =
      new SideGateway<>(
          NetworkManager::new,
          () -> instantiate("ic2ce.neoforge.network.NetworkManagerClient", NetworkManager.class));
  public static final Keyboard keyboard = sideProxy.getKeyboard();
  public static final AudioService soundManager = sideProxy.getSoundManager();
  public static final RandomSource random = RandomSource.create();
  public static final PriorityExecutor threadPool =
      new PriorityExecutor(Math.max(2, Runtime.getRuntime().availableProcessors()));
  public static ProgressionService achievements;
  public static boolean initialized, suddenlyHoes, seasonal;
  public static final int setBlockNotify = 1, setBlockUpdate = 2, setBlockNoUpdateFromClient = 4;
  public static final Identifier tabIc2General = tab("general", CreativeCategory.GENERAL);
  public static final Identifier tabIc2GeneratorsAndWiring =
      tab("generators_and_wiring", CreativeCategory.GENERATORS_AND_WIRING);
  public static final Identifier tabIc2Reactor = tab("reactor", CreativeCategory.REACTOR);
  public static final Identifier tabIc2Machines = tab("machines", CreativeCategory.MACHINES);
  public static final Identifier tabIc2ToolsAndUtilities =
      tab("tools_and_utilities", CreativeCategory.TOOLS_AND_UTILITIES);
  public static final Identifier tabIc2Combat = tab("combat", CreativeCategory.COMBAT);
  public static final Identifier tabIc2Farming = tab("farming", CreativeCategory.FARMING);
  public static final Identifier tabIc2Materials = tab("materials", CreativeCategory.MATERIALS);

  private ProjectRuntime() {}

  private static String version() {
    ModList mods = ModList.get();
    return mods == null
        ? "development"
        : mods.getModContainerById(MODID)
            .map(mod -> mod.getModInfo().getVersion().toString())
            .orElse("development");
  }

  private static Identifier tab(String name, CreativeCategory type) {
    return envProxy.createItemGroup(getIdentifier(name), new CategoryIcon(type));
  }

  public static Identifier getIdentifier(String name) {
    return Identifier.fromNamespaceAndPath(RESOURCE_DOMAIN, name);
  }

  public static int getSeaLevel(Level world) {
    return world.getSeaLevel();
  }

  public static int getWorldMinHeight(Level world) {
    return world.getMinY();
  }

  public static int getWorldMaxHeight(Level world) {
    return world.getMinY() + world.getHeight();
  }

  private static <T> T instantiate(String name, Class<T> contract) {
    try {
      return Class.forName(name).asSubclass(contract).getConstructor().newInstance();
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(
          "Cannot initialize IndustrialCraft2-CE side service " + name, error);
    }
  }
}
