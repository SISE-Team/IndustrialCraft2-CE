package ic2ce.engine.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/** One ordered compressed message with bounded assembly, expansion and frame lifetime. */
public final class FramedCompression extends OutputStream {
  public record Message(int kind, byte[] bytes) {}

  private final int compressedLimit, expandedLimit;
  private final ByteArrayOutputStream compressed = new ByteArrayOutputStream(32768);
  private int kind = -1;
  private long started;

  public FramedCompression(int compressedLimit, int expandedLimit) {
    if (compressedLimit < 1 || expandedLimit < 1)
      throw new IllegalArgumentException("Invalid compressed message limits");
    this.compressedLimit = compressedLimit;
    this.expandedLimit = expandedLimit;
  }

  public void frame(int flags, long now) throws IOException {
    int next = flags >>> 2;
    if (next > 2 || (flags & ~255) != 0) {
      reset();
      throw new IOException("Unknown compressed message kind");
    }
    if ((flags & 1) != 0) {
      if (kind >= 0) {
        reset();
        throw new IOException("Compressed message restarted before completion");
      }
      kind = next;
      started = now;
      compressed.reset();
    } else if (kind < 0 || next != kind) {
      reset();
      throw new IOException("Unexpected compressed message continuation");
    }
    if (now - started > 10_000_000_000L || now < started) {
      reset();
      throw new IOException("Compressed message assembly timed out");
    }
  }

  @Override
  public void write(int value) throws IOException {
    check(1);
    compressed.write(value);
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    java.util.Objects.checkFromIndexSize(offset, length, bytes.length);
    check(length);
    compressed.write(bytes, offset, length);
  }

  private void check(int length) throws IOException {
    if (kind < 0 || length > compressedLimit - compressed.size()) {
      reset();
      throw new IOException("Compressed message size limit exceeded");
    }
  }

  public Message finish() throws IOException {
    if (kind < 0) throw new IOException("No compressed message is active");
    var inflater = new Inflater();
    int completedKind = kind;
    try {
      inflater.setInput(compressed.toByteArray());
      var expanded =
          new ByteArrayOutputStream(Math.min(expandedLimit, Math.max(128, compressed.size() * 2)));
      var block = new byte[8192];
      while (!inflater.finished()) {
        int amount = inflater.inflate(block);
        if (amount > expandedLimit - expanded.size())
          throw new IOException("Compressed message expansion limit exceeded");
        if (amount > 0) expanded.write(block, 0, amount);
        else if (!inflater.finished())
          throw new IOException(
              inflater.needsDictionary()
                  ? "Unsupported compression dictionary"
                  : "Truncated or stalled compressed message");
      }
      if (inflater.getRemaining() != 0) throw new IOException("Trailing compressed message bytes");
      return new Message(completedKind, expanded.toByteArray());
    } catch (DataFormatException error) {
      throw new IOException("Invalid compressed message", error);
    } finally {
      inflater.end();
      reset();
    }
  }

  public void reset() {
    kind = -1;
    started = 0;
    compressed.reset();
  }
}
