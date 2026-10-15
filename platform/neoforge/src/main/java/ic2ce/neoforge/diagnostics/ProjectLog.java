package ic2ce.neoforge.diagnostics;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.*;

/**
 * Category loggers are safely published; disabled messages avoid formatting and caller object
 * rendering.
 */
public class ProjectLog {
  public static final ProjectLog LOG = new ProjectLog(LogManager.getLogger("IndustrialCraft2-CE"));

  private final Logger parent;
  private final Map<DiagnosticArea, Logger> categories = new ConcurrentHashMap<>();

  public ProjectLog(Logger parent) {
    this.parent = Objects.requireNonNull(parent);
  }

  private Logger logger(DiagnosticArea category) {
    return categories.computeIfAbsent(
        Objects.requireNonNull(category),
        key -> LogManager.getLogger(parent.getName() + "." + key.name()));
  }

  public void log(DiagnosticArea category, Level level, String message, Object... arguments) {
    log(category, level, null, message, arguments);
  }

  public void log(
      DiagnosticArea category, Level level, Throwable error, String message, Object... arguments) {
    var output = logger(category);
    if (!output.isEnabled(level)) return;
    String rendered = message;
    if (arguments != null && arguments.length > 0)
      try {
        rendered = String.format(Locale.ROOT, message, arguments);
      } catch (IllegalFormatException failure) {
        rendered = message + " " + Arrays.toString(arguments);
        output.warn("IndustrialCraft2-CE log format rejected", failure);
      }
    output.log(level, rendered, error);
  }

  public void log(DiagnosticArea category, Level level, String message) {
    var output = logger(category);
    if (output.isEnabled(level)) output.log(level, message);
  }

  public void log(DiagnosticArea category, Level level, Throwable error, String message) {
    var output = logger(category);
    if (output.isEnabled(level)) output.log(level, message, error);
  }

  public PrintStream getPrintStream(DiagnosticArea category, Level level) {
    return new PrintStream(new Utf8LogStream(this, category, level), true, StandardCharsets.UTF_8);
  }

  public void error(DiagnosticArea category, String message, Object... arguments) {
    log(category, Level.ERROR, message, arguments);
  }

  public void error(DiagnosticArea category, Throwable error, String message, Object... arguments) {
    log(category, Level.ERROR, error, message, arguments);
  }

  public void error(DiagnosticArea category, String message) {
    log(category, Level.ERROR, message);
  }

  public void error(DiagnosticArea category, Throwable error, String message) {
    log(category, Level.ERROR, error, message);
  }

  public void warn(DiagnosticArea category, String message, Object... arguments) {
    log(category, Level.WARN, message, arguments);
  }

  public void warn(DiagnosticArea category, Throwable error, String message, Object... arguments) {
    log(category, Level.WARN, error, message, arguments);
  }

  public void warn(DiagnosticArea category, String message) {
    log(category, Level.WARN, message);
  }

  public void warn(DiagnosticArea category, Throwable error, String message) {
    log(category, Level.WARN, error, message);
  }

  public void info(DiagnosticArea category, String message, Object... arguments) {
    log(category, Level.INFO, message, arguments);
  }

  public void info(DiagnosticArea category, Throwable error, String message, Object... arguments) {
    log(category, Level.INFO, error, message, arguments);
  }

  public void info(DiagnosticArea category, String message) {
    log(category, Level.INFO, message);
  }

  public void info(DiagnosticArea category, Throwable error, String message) {
    log(category, Level.INFO, error, message);
  }

  public void debug(DiagnosticArea category, String message, Object... arguments) {
    log(category, Level.DEBUG, message, arguments);
  }

  public void debug(DiagnosticArea category, Throwable error, String message, Object... arguments) {
    log(category, Level.DEBUG, error, message, arguments);
  }

  public void debug(DiagnosticArea category, String message) {
    log(category, Level.DEBUG, message);
  }

  public void debug(DiagnosticArea category, Throwable error, String message) {
    log(category, Level.DEBUG, error, message);
  }

  public void trace(DiagnosticArea category, String message, Object... arguments) {
    log(category, Level.TRACE, message, arguments);
  }

  public void trace(DiagnosticArea category, Throwable error, String message, Object... arguments) {
    log(category, Level.TRACE, error, message, arguments);
  }

  public void trace(DiagnosticArea category, String message) {
    log(category, Level.TRACE, message);
  }

  public void trace(DiagnosticArea category, Throwable error, String message) {
    log(category, Level.TRACE, error, message);
  }
}
