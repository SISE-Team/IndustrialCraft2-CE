package ic2ce.neoforge.item.tool;

import ic2ce.api.energy.EnergyNet;
import ic2ce.api.item.*;
import ic2ce.api.reactor.IReactor;
import ic2ce.api.tile.IEnergyStorage;
import ic2ce.neoforge.energy.grid.EnergyNetGlobal;
import ic2ce.neoforge.item.PriorityUsableItem;
import ic2ce.neoforge.item.StackTag;
import ic2ce.neoforge.agriculture.CropPlot;
import ic2ce.neoforge.block.NativeBlockEntity;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.SignalInput;
import ic2ce.neoforge.item.energy.CreativeChargeSource;
import ic2ce.neoforge.power.BufferedGenerator;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.*;

/**
 * Independent bounded diagnostics, live energy reports and native ticking retain six distinct debug
 * protocols.
 */
public class DiagnosticTool extends Item
    implements PriorityUsableItem, ISpecialElectricItem, IBoxable {
  private enum Mode {
    InterfacesFields,
    InterfacesFieldsRetrace,
    TileData,
    EnergyNet,
    Accelerate,
    AccelerateX100;
  }

  private static final Mode[] MODES = Mode.values();
  private static final IElectricItemManager ENERGY = new CreativeChargeSource();
  private static final ClassValue<List<Field>> FIELDS =
      new ClassValue<>() {
        @Override
        protected List<Field> computeValue(Class<?> type) {
          var result = new ArrayList<Field>();
          for (var current = type; current != null; current = current.getSuperclass())
            for (var field : current.getDeclaredFields())
              if (!field.isSynthetic()
                  && !Modifier.isStatic(field.getModifiers())
                  && field.trySetAccessible()) result.add(field);
          return List.copyOf(result);
        }
      };

  public DiagnosticTool(Properties properties) {
    super(properties);
  }

  private static Mode mode(ItemStack stack) {
    var data = StackTag.get(stack);
    int index = data == null ? 0 : data.getIntOr("mode", 0);
    return index >= 0 && index < MODES.length ? MODES[index] : Mode.InterfacesFields;
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var owner = context.getPlayer();
    var world = context.getLevel();
    var pos = context.getClickedPos();
    if (owner == null || !stack.is(this)) return InteractionResult.PASS;
    if (world.isClientSide()) return InteractionResult.PASS;
    var selected = mode(stack);
    if (ProjectRuntime.keyboard.isModeSwitchKeyDown(owner)) {
      int next = (selected.ordinal() + 1) % MODES.length;
      StackTag.update(
          stack,
          data -> {
            data.putInt("mode", next);
            return data;
          });
      ProjectRuntime.sideProxy.messagePlayer(owner, "ic2.debug.mode", MODES[next].name());
      return InteractionResult.SUCCESS;
    }
    var output = new Report();
    var state = world.getBlockState(pos);
    var tile = world.getBlockEntity(pos);
    if (tile instanceof IDebuggable debug && debug.isDebuggable())
      output.chat(debug.getDebugText());
    else
      switch (selected) {
        case InterfacesFields, InterfacesFieldsRetrace -> {
          output.chat(
              "Block: " + state + " " + pos + " class=" + state.getBlock().getClass().getName());
          fields(state.getBlock(), output);
          if (tile != null) fields(tile, output);
        }
        case TileData -> {
          if (tile instanceof NativeBlockEntity device) {
            output.chat("Block: Active=" + device.getActive() + " Facing=" + device.getFacing());
            for (var component : device.getComponents()) {
              if (component instanceof ElectricalBuffer energy)
                output.chat("Energy: " + energy.getEnergy() + " / " + energy.getCapacity());
              else if (component instanceof SignalInput redstone)
                output.chat("Redstone: " + redstone.getRedstoneInput());
            }
          }
          if (tile instanceof BufferedGenerator generator)
            output.chat("BaseGen: Fuel=" + generator.fuel);
          if (tile instanceof IEnergyStorage storage)
            output.chat("EnergyStorage: Stored=" + storage.getStored());
          if (tile instanceof IReactor reactor)
            output.chat(
                "Reactor: Heat="
                    + reactor.getHeat()
                    + " MaxHeat="
                    + reactor.getMaxHeat()
                    + " Output="
                    + reactor.getReactorEnergyOutput());
          if (tile instanceof CropPlot crop)
            output.chat(
                "Crop: "
                    + (crop.getCrop() == null ? "none" : crop.getCrop().getId())
                    + " Age="
                    + crop.getCurrentAge()
                    + " Growth="
                    + crop.getStatGrowth()
                    + " Gain="
                    + crop.getStatGain()
                    + " Resistance="
                    + crop.getStatResistance()
                    + " Water="
                    + crop.getStorageWater()
                    + " Progress="
                    + crop.getGrowthPoints());
        }
        case EnergyNet -> {
          if (EnergyNet.instance instanceof EnergyNetGlobal network) {
            var console = new ByteArrayOutputStream();
            var chat = new ByteArrayOutputStream();
            try (var consoleOut = new PrintStream(console, false, StandardCharsets.UTF_8);
                var chatOut = new PrintStream(chat, false, StandardCharsets.UTF_8)) {
              network.dumpDebugInfo(world, pos, consoleOut, chatOut);
            }
            output.console(console.toString(StandardCharsets.UTF_8));
            output.chat(chat.toString(StandardCharsets.UTF_8));
          }
        }
        case Accelerate, AccelerateX100 -> {
          if (owner.mayBuild()
              && owner.mayUseItemAt(pos, context.getClickedFace(), stack)
              && world instanceof ServerLevel server)
            accelerate(server, pos, selected == Mode.Accelerate ? 1000 : 100000, output);
        }
      }
    output.send(owner);
    return InteractionResult.SUCCESS;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void accelerate(ServerLevel world, BlockPos pos, int count, Report report) {
    var state = world.getBlockState(pos);
    int performed = 0;
    for (; performed < count; performed++) {
      var current = world.getBlockState(pos);
      var tile = world.getBlockEntity(pos);
      if (tile != null && !tile.isRemoved()) {
        BlockEntityTicker ticker = current.getTicker(world, tile.getType());
        if (ticker == null) break;
        ticker.tick(world, pos, current, tile);
      } else if (current.isRandomlyTicking()) current.randomTick(world, pos, world.getRandom());
      else break;
    }
    report.chat(
        "Ran " + performed + " native ticks at " + pos + "; state=" + world.getBlockState(pos));
  }

  private static void fields(Object value, Report report) {
    report.console("Class: " + value.getClass().getName());
    for (var field : FIELDS.get(value.getClass())) {
      Object data;
      try {
        data = field.get(value);
      } catch (IllegalAccessException error) {
        data = "<inaccessible>";
      }
      report.console(
          field.getDeclaringClass().getSimpleName()
              + "."
              + field.getName()
              + " = "
              + limited(data));
    }
  }

  private static String limited(Object value) {
    if (value == null) return "null";
    var text = Objects.toString(value);
    return text.length() <= 256 ? text : text.substring(0, 256) + "...";
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player owner, LivingEntity target, InteractionHand hand) {
    if (owner.level().isClientSide() || mode(stack) != Mode.InterfacesFieldsRetrace)
      return InteractionResult.PASS;
    var report = new Report();
    report.chat("Entity: " + target.getType() + " " + target.getUUID() + " " + target.position());
    fields(target, report);
    report.send(owner);
    return InteractionResult.SUCCESS;
  }

  @Override
  public IElectricItemManager getManager(ItemStack stack) {
    return ENERGY;
  }

  @Override
  public boolean canBeStoredInToolbox(ItemStack stack) {
    return stack != null && !stack.isEmpty();
  }

  private static final class Report {
    private final StringBuilder chat = new StringBuilder(), console = new StringBuilder();

    void chat(String text) {
      append(chat, text);
    }

    void console(String text) {
      append(console, text);
    }

    private static void append(StringBuilder target, String text) {
      if (target.length() < 65536)
        target.append(text, 0, Math.min(text.length(), 65536 - target.length())).append('\n');
    }

    void send(Player owner) {
      if (owner instanceof ServerPlayer actor) {
        if (!console.isEmpty())
          ProjectRuntime.network.get(true).sendConsole(actor, console.toString());
        if (!chat.isEmpty()) ProjectRuntime.network.get(true).sendChat(actor, chat.toString());
      }
    }
  }
}
