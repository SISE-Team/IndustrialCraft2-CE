package ic2ce.engine.network;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Bounded big-endian packet storage with reusable write/read cursors and JDK modified UTF decoding.
 */
public class WireBuffer extends OutputStream implements DataInput, DataOutput {
  private static final byte[] EMPTY = new byte[0];
  private static final VarHandle SHORTS =
      MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
  private static final VarHandle INTS =
      MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
  private static final VarHandle LONGS =
      MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
  private byte[] storage;
  private int origin, position, otherPosition, marked = -1;

  public WireBuffer() {
    this(4096);
  }

  public WireBuffer(int capacity) {
    if (capacity < 0) throw new IllegalArgumentException("Negative buffer capacity");
    storage = capacity == 0 ? EMPTY : new byte[capacity];
  }

  protected final void adopt(byte[] bytes, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, bytes.length);
    storage = bytes;
    origin = position = offset;
    otherPosition = offset + length;
    marked = -1;
  }

  public static WireBuffer wrap(byte[] bytes) {
    var result = new WireBuffer(0);
    result.adopt(Objects.requireNonNull(bytes), 0, bytes.length);
    return result;
  }

  protected final byte[] array() {
    return storage;
  }

  protected final int readPosition() {
    return position;
  }

  public void clear() {
    position = otherPosition = origin;
    marked = -1;
  }

  public void mark() {
    marked = position;
  }

  public void reset() {
    if (marked < 0) throw new IllegalStateException("Buffer has no mark");
    position = marked;
  }

  public void flip() {
    int previous = position;
    position = otherPosition;
    otherPosition = previous;
  }

  public void rewind() {
    if (position > otherPosition) throw new IllegalStateException("Buffer is not readable");
    position = origin;
  }

  public int available() {
    return Math.max(0, otherPosition - position);
  }

  public boolean hasAvailable() {
    return position < otherPosition;
  }

  private void require(int count) {
    if (count < 0 || count > available()) throw new BufferUnderflowException();
  }

  private void reserve(int count) {
    if (count < 0 || count > Integer.MAX_VALUE - 8 - position)
      throw new IllegalArgumentException("Packet buffer size overflow");
    int needed = position + count;
    if (needed > storage.length)
      storage =
          Arrays.copyOf(
              storage,
              Math.max(
                  needed,
                  (int) Math.min(Integer.MAX_VALUE - 8L, Math.max(16L, storage.length * 2L))));
  }

  public void writeTo(WireBuffer target) {
    writeTo(target, available());
  }

  public void writeTo(WireBuffer target, int maximum) {
    Objects.requireNonNull(target);
    if (target == this) throw new IllegalArgumentException("Cannot transfer a buffer into itself");
    int count = Math.max(0, Math.min(maximum, available()));
    target.write(storage, position, count);
    position += count;
  }

  public byte[] readableCopy() {
    return available() == 0 ? EMPTY : Arrays.copyOfRange(storage, position, otherPosition);
  }

  public void writeTo(OutputStream target) throws IOException {
    int count = available();
    target.write(storage, position, count);
    position += count;
  }

  public byte[] copyBytes(int maximum) {
    int count = Math.max(0, Math.min(maximum, available()));
    if (count == 0) return EMPTY;
    var copy = Arrays.copyOfRange(storage, position, position + count);
    position += count;
    return copy;
  }

  @Override
  public void write(int value) {
    reserve(1);
    storage[position++] = (byte) value;
  }

  @Override
  public void write(byte[] bytes) {
    write(bytes, 0, bytes.length);
  }

  @Override
  public void write(byte[] bytes, int offset, int count) {
    Objects.checkFromIndexSize(offset, count, bytes.length);
    reserve(count);
    System.arraycopy(bytes, offset, storage, position, count);
    position += count;
  }

  @Override
  public void writeBoolean(boolean value) {
    write(value ? 1 : 0);
  }

  @Override
  public void writeByte(int value) {
    write(value);
  }

  @Override
  public void writeShort(int value) {
    reserve(2);
    SHORTS.set(storage, position, (short) value);
    position += 2;
  }

  @Override
  public void writeChar(int value) {
    writeShort(value);
  }

  @Override
  public void writeInt(int value) {
    reserve(4);
    INTS.set(storage, position, value);
    position += 4;
  }

  @Override
  public void writeLong(long value) {
    reserve(8);
    LONGS.set(storage, position, value);
    position += 8;
  }

  @Override
  public void writeFloat(float value) {
    writeInt(Float.floatToRawIntBits(value));
  }

  @Override
  public void writeDouble(double value) {
    writeLong(Double.doubleToRawLongBits(value));
  }

  public void writeVarInt(int value) {
    if (value < 0) throw new IllegalArgumentException("Negative variable integer");
    reserve(5);
    while (value >= 128) {
      storage[position++] = (byte) ((value & 127) | 128);
      value >>>= 7;
    }
    storage[position++] = (byte) value;
  }

  public int readVarInt() {
    int value = 0;
    for (int shift = 0; shift <= 28; shift += 7) {
      int next = readUnsignedByte();
      if (shift == 28 && (next & 0xF8) != 0)
        throw new IllegalArgumentException("Variable integer exceeds its positive 31-bit range");
      value |= (next & 127) << shift;
      if ((next & 128) == 0) return value;
    }
    throw new IllegalArgumentException("Variable integer is too long");
  }

  public void writeString(String value) {
    var encoded = value.getBytes(StandardCharsets.UTF_8);
    writeVarInt(encoded.length);
    write(encoded);
  }

  public String readString() {
    int count = readVarInt();
    require(count);
    var value = new String(storage, position, count, StandardCharsets.UTF_8);
    position += count;
    return value;
  }

  @Override
  public void writeBytes(String value) {
    for (int index = 0; index < value.length(); index++) write(value.charAt(index));
  }

  @Override
  public void writeChars(String value) {
    for (int index = 0; index < value.length(); index++) writeChar(value.charAt(index));
  }

  @Override
  public void writeUTF(String value) throws IOException {
    if (value.length() > 65535)
      throw new UTFDataFormatException("Modified UTF value exceeds 65535 bytes");
    long count = 0;
    for (int index = 0; index < value.length(); index++) {
      char c = value.charAt(index);
      count += c >= 1 && c < 128 ? 1 : c < 2048 ? 2 : 3;
    }
    if (count > 65535) throw new UTFDataFormatException("Modified UTF value exceeds 65535 bytes");
    reserve((int) count + 2);
    SHORTS.set(storage, position, (short) count);
    position += 2;
    for (int index = 0; index < value.length(); index++) {
      char c = value.charAt(index);
      if (c >= 1 && c < 128) storage[position++] = (byte) c;
      else if (c < 2048) {
        storage[position++] = (byte) (0xC0 | c >>> 6);
        storage[position++] = (byte) (0x80 | c & 0x3F);
      } else {
        storage[position++] = (byte) (0xE0 | c >>> 12);
        storage[position++] = (byte) (0x80 | c >>> 6 & 0x3F);
        storage[position++] = (byte) (0x80 | c & 0x3F);
      }
    }
  }

  @Override
  public String readUTF() throws IOException {
    return DataInputStream.readUTF(this);
  }

  @Override
  public void readFully(byte[] bytes) {
    readFully(bytes, 0, bytes.length);
  }

  @Override
  public void readFully(byte[] bytes, int offset, int count) {
    Objects.checkFromIndexSize(offset, count, bytes.length);
    require(count);
    System.arraycopy(storage, position, bytes, offset, count);
    position += count;
  }

  @Override
  public int skipBytes(int count) {
    int moved = (int) Math.clamp((long) count, (long) origin - position, available());
    position += moved;
    return moved;
  }

  @Override
  public boolean readBoolean() {
    return readByte() != 0;
  }

  @Override
  public byte readByte() {
    require(1);
    return storage[position++];
  }

  @Override
  public int readUnsignedByte() {
    return readByte() & 255;
  }

  @Override
  public short readShort() {
    require(2);
    short value = (short) SHORTS.get(storage, position);
    position += 2;
    return value;
  }

  @Override
  public int readUnsignedShort() {
    return readShort() & 65535;
  }

  @Override
  public char readChar() {
    return (char) readUnsignedShort();
  }

  @Override
  public int readInt() {
    require(4);
    int value = (int) INTS.get(storage, position);
    position += 4;
    return value;
  }

  @Override
  public long readLong() {
    require(8);
    long value = (long) LONGS.get(storage, position);
    position += 8;
    return value;
  }

  @Override
  public float readFloat() {
    return Float.intBitsToFloat(readInt());
  }

  @Override
  public double readDouble() {
    return Double.longBitsToDouble(readLong());
  }

  @Override
  public String readLine() {
    throw new UnsupportedOperationException("Packet buffers have no line encoding");
  }
}
