package ic2ce.neoforge.handheld.menu;

import ic2ce.api.energy.*;
import ic2ce.api.energy.tile.IEnergyTile;
import ic2ce.api.network.ClientModifiable;
import ic2ce.neoforge.handheld.HandheldMenus;
import ic2ce.neoforge.handheld.MeterSession;
import ic2ce.neoforge.runtime.SideServices;

/**
 * Native energy observations retain fractional/high-voltage values and incremental means without
 * integer overflow.
 */
public class EnergyMeterMenu extends HandheldMenu<MeterSession> {
  private IEnergyTile uut;
  private double resultAvg, resultMin, resultMax;
  private int resultCount;
  private long sampledTick = Long.MIN_VALUE;
  private Mode measuredMode = Mode.EnergyIn;
  @ClientModifiable private Mode mode = Mode.EnergyIn;

  public EnergyMeterMenu(int id, MeterSession inventory) {
    super(HandheldMenus.METER, id, inventory);
  }

  @Override
  public void broadcastChanges() {
    if (!player.level().isClientSide() && uut != null) {
      if (mode == null) mode = Mode.EnergyIn;
      if (mode != measuredMode) {
        measuredMode = mode;
        resetLocal();
      }
      long tick = player.level().getGameTime();
      if (sampledTick != tick) {
        sampledTick = tick;
        var stats = EnergyNet.instance.getNodeStats(uut);
        if (stats == null) {
          base.closeGUI();
          return;
        }
        double value =
            switch (mode) {
              case EnergyIn -> stats.getEnergyIn();
              case EnergyOut -> stats.getEnergyOut();
              case EnergyGain -> stats.getEnergyIn() - stats.getEnergyOut();
              case Voltage -> stats.getVoltage();
            };
        sample(value);
      }
    }
    super.broadcastChanges();
  }

  public void sample(double value) {
    if (!Double.isFinite(value)) return;
    if (resultCount == 0) resultAvg = resultMin = resultMax = value;
    else {
      resultMin = Math.min(resultMin, value);
      resultMax = Math.max(resultMax, value);
      resultAvg += (value - resultAvg) / ((double) resultCount + 1);
    }
    if (resultCount < Integer.MAX_VALUE) resultCount++;
    syncResults();
  }

  public double getResultAvg() {
    return resultAvg;
  }

  public double getResultMin() {
    return resultMin;
  }

  public double getResultMax() {
    return resultMax;
  }

  public int getResultCount() {
    return resultCount;
  }

  public Mode getMode() {
    return mode == null ? Mode.EnergyIn : mode;
  }

  public void setMode(Mode value) {
    if (value == null) return;
    mode = value;
    resetLocal();
    if (player.level().isClientSide())
      SideServices.network(false).sendContainerField(this, "mode");
  }

  private void resetLocal() {
    resultCount = 0;
    resultAvg = resultMin = resultMax = 0;
    sampledTick = Long.MIN_VALUE;
  }

  private void syncResults() {
    if (!player.level().isClientSide()) {
      SideServices.network(true).sendContainerField(this, "resultAvg");
      SideServices.network(true).sendContainerField(this, "resultMin");
      SideServices.network(true).sendContainerField(this, "resultMax");
      SideServices.network(true).sendContainerField(this, "resultCount");
    }
  }

  public void reset() {
    if (player.level().isClientSide())
      SideServices.network(false).sendContainerEvent(this, "reset");
    else {
      resetLocal();
      syncResults();
    }
  }

  public void setUut(IEnergyTile tile) {
    if (uut == null) uut = tile;
  }

  @Override
  public void onContainerEvent(String event) {
    if (event.equals("reset")) reset();
    else super.onContainerEvent(event);
  }

  public enum Mode {
    EnergyIn,
    EnergyOut,
    EnergyGain,
    Voltage;
  }
}
