package ic2ce.neoforge.diagnostics;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.Objects;
import org.apache.logging.log4j.Level;

/**
 * Incremental UTF-8 decoding preserves split characters, CRLF boundaries and bounded unterminated
 * lines without finalizers.
 */
class Utf8LogStream extends OutputStream {
  private static final int MAX_LINE = 65536;
  private final ProjectLog logger;
  private final DiagnosticArea category;
  private final Level level;
  private final CharsetDecoder decoder =
      StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE);
  private final ByteBuffer bytes = ByteBuffer.allocate(1024);
  private final CharBuffer characters = CharBuffer.allocate(1024);
  private final StringBuilder line = new StringBuilder();
  private boolean afterCarriageReturn, closed;

  Utf8LogStream(ProjectLog logger, DiagnosticArea category, Level level) {
    this.logger = Objects.requireNonNull(logger);
    this.category = Objects.requireNonNull(category);
    this.level = Objects.requireNonNull(level);
  }

  private void open() throws IOException {
    if (closed) throw new IOException("Closed IndustrialCraft2-CE log stream");
  }

  @Override
  public synchronized void write(int value) throws IOException {
    open();
    if (!bytes.hasRemaining()) decode(false);
    bytes.put((byte) value);
    decode(false);
  }

  @Override
  public synchronized void write(byte[] values, int offset, int length) throws IOException {
    Objects.checkFromIndexSize(offset, length, values.length);
    open();
    while (length > 0) {
      if (!bytes.hasRemaining()) decode(false);
      int count = Math.min(length, bytes.remaining());
      bytes.put(values, offset, count);
      offset += count;
      length -= count;
      decode(false);
    }
  }

  private void decode(boolean ending) throws IOException {
    bytes.flip();
    while (true) {
      var result = decoder.decode(bytes, characters, ending);
      emitCharacters();
      if (result.isError()) result.throwException();
      if (!result.isOverflow()) break;
    }
    bytes.compact();
    if (ending)
      while (true) {
        var result = decoder.flush(characters);
        emitCharacters();
        if (!result.isOverflow()) break;
      }
  }

  private void emitCharacters() {
    characters.flip();
    while (characters.hasRemaining()) {
      char value = characters.get();
      if (value == '\n' && afterCarriageReturn) {
        afterCarriageReturn = false;
        continue;
      }
      if (value == '\r' || value == '\n') {
        emitLine();
        afterCarriageReturn = value == '\r';
        continue;
      }
      afterCarriageReturn = false;
      line.append(value);
      if (line.length() >= MAX_LINE && !Character.isHighSurrogate(value)) emitLine();
    }
    characters.clear();
  }

  private void emitLine() {
    logger.log(category, level, line.toString());
    line.setLength(0);
  }

  @Override
  public synchronized void flush() throws IOException {
    open();
    decode(false);
  }

  @Override
  public synchronized void close() throws IOException {
    if (closed) return;
    try {
      decode(true);
      if (!line.isEmpty()) emitLine();
    } finally {
      closed = true;
    }
  }
}
