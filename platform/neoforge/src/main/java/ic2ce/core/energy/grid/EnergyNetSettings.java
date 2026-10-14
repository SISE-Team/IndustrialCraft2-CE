package ic2ce.core.energy.grid;

import ic2ce.neoforge.config.ProjectConfiguration;
import ic2ce.neoforge.config.NativeConfigValues;

public class EnergyNetSettings {
   public static final boolean logEnetApiAccesses = NativeConfigValues.getBool(ProjectConfiguration.get(), "debug/logEnetApiAccesses");
   public static final boolean logEnetApiAccessTraces = NativeConfigValues.getBool(ProjectConfiguration.get(), "debug/logEnetApiAccessTraces");
   public static boolean logGridUpdateIssues = NativeConfigValues.getBool(ProjectConfiguration.get(), "debug/logGridUpdateIssues");
   public static boolean logGridUpdatesVerbose = NativeConfigValues.getBool(ProjectConfiguration.get(), "debug/logGridUpdatesVerbose");
   public static boolean logGridCalculationIssues = NativeConfigValues.getBool(ProjectConfiguration.get(), "debug/logGridCalculationIssues");
   public static final boolean logGridUpdatePerformance = false;
   public static final boolean logGridCalculationPerformance = false;
   public static final boolean roundLossDown = NativeConfigValues.getBool(ProjectConfiguration.get(), "misc/roundEnetLoss");
   public static final int changesQueueDelay = 1;
   public static final double nonConductorResistance = 0.001;
   public static final int bfsThreshold = 2048;
}
