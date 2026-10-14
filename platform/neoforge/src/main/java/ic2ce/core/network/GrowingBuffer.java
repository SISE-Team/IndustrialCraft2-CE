package ic2ce.core.network;

import ic2ce.api.network.IGrowingBuffer;
import ic2ce.engine.network.WireBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Objects;

/**
 * Native Netty view adapter; primitive/string storage and decoding live in the independent engine.
 */
public class GrowingBuffer extends WireBuffer implements IGrowingBuffer {
  public GrowingBuffer() {
    super();
  }

  public GrowingBuffer(int capacity) {
    super(capacity);
  }

  public static GrowingBuffer wrap(byte[] bytes) {
    var result = new GrowingBuffer(0);
    result.adopt(Objects.requireNonNull(bytes), 0, bytes.length);
    return result;
  }

  public static GrowingBuffer wrap(ByteBuf source) {
    var result = new GrowingBuffer(0);
    int count = source.readableBytes();
    if (source.hasArray())
      result.adopt(source.array(), source.arrayOffset() + source.readerIndex(), count);
    else {
      var bytes = new byte[count];
      source.getBytes(source.readerIndex(), bytes);
      result.adopt(bytes, 0, count);
    }
    return result;
  }

  public void writeTo(GrowingBuffer target) {
    super.writeTo(target);
  }

  public void writeTo(ByteBuf target) {
    int count = available();
    target.writeBytes(array(), readPosition(), count);
    skipBytes(count);
  }

  public ByteBuf toByteBuf(boolean advance) {
    int count = available();
    if (count == 0) return Unpooled.EMPTY_BUFFER;
    var result = Unpooled.wrappedBuffer(array(), readPosition(), count);
    if (advance) skipBytes(count);
    return result;
  }

  public GrowingBuffer copy(int maximum) {
    return wrap(copyBytes(maximum));
  }
}
