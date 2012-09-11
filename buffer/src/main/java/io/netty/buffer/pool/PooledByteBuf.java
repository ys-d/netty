/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufIndexFinder;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.ChannelBufType;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.SlicedByteBuf;
import io.netty.buffer.SwappedByteBuf;
import io.netty.buffer.Unpooled;

/**
 * {@link ByteBuf} which is pooled.
 * <br/>
 * <br/>
 * <strong>Sub-classes must call {@link #initDone()} once they are done with
 * constructing the instance</strong>.
 *
 */
abstract class PooledByteBuf implements ByteBuf {
    private int refCnt;
    private boolean initDone;
    private final Unsafe unsafe = new PooledUnsafe();

    private void checkReleased() {
        if (initDone && refCnt == 0) {
            throw new IllegalStateException("PooledByteBuf was already released");
        }
    }

    private ByteBuf buf;
    private final ByteBufPool pool;
    private final SwappedByteBuf swappedBuf;

    PooledByteBuf(ByteBufPool pool, ByteBuf buf) {
        this.pool = pool;
        this.buf = buf;
        this.swappedBuf = new SwappedByteBuf(this);
        initDone = true;
    }

    /**
     * Returns <code>true</code>
     */
    @Override
    public final boolean isPooled() {
        checkReleased();
        return true;
    }

    @Override
    public final int capacity() {
        checkReleased();
        return buf.capacity();
    }

    @Override
    public final void capacity(int newCapacity) {
        checkReleased();
        if (newCapacity < 0 || newCapacity > maxCapacity()) {
            throw new IllegalArgumentException("newCapacity: " + newCapacity);
        }
        if (newCapacity == capacity()) {
            return;
        }
        ByteBuf newBuf = pool.acquire(newCapacity);
        int refCount = buf.unsafe().references();
        while (refCount-- > 1) {
            // loop over all the old reference and make sure the reference count
            // is updated for these
            newBuf.unsafe().acquire();
            buf.unsafe().release();
        }
        // remove last reference to wrapped buffer
        buf.unsafe().release();
        this.buf = newBuf;
    }

    @Override
    public final int maxCapacity() {
        checkReleased();
        return buf.maxCapacity();
    }

    @Override
    public final ByteOrder order() {
        checkReleased();
        return buf.order();
    }

    @Override
    public final ByteBuf order(ByteOrder endianness) {
        checkReleased();
        if (endianness == null) {
            throw new NullPointerException("endianness");
        }
        if (endianness == order()) {
            return this;
        }
        return swappedBuf;
    }

    @Override
    public final boolean isDirect() {
        checkReleased();
        return false;
    }

    @Override
    public final int readerIndex() {
        checkReleased();
        return buf.readerIndex();
    }

    @Override
    public final void readerIndex(int readerIndex) {
        checkReleased();
        buf.readerIndex(readerIndex);
    }

    @Override
    public final int writerIndex() {
        checkReleased();
        return buf.writerIndex();
    }

    @Override
    public final void writerIndex(int writerIndex) {
        checkReleased();
        buf.writerIndex(writerIndex);
    }

    @Override
    public final void setIndex(int readerIndex, int writerIndex) {
        checkReleased();
        buf.setIndex(readerIndex, writerIndex);
    }

    @Override
    public final int readableBytes() {
        checkReleased();
        return buf.readableBytes();
    }

    @Override
    public final int writableBytes() {
        checkReleased();
        return buf.writableBytes();
    }

    @Override
    public final boolean readable() {
        checkReleased();
        return buf.readable();
    }

    @Override
    public final boolean writable() {
        checkReleased();
        return buf.writable();
    }

    @Override
    public final void clear() {
        checkReleased();
        buf.clear();
    }

    @Override
    public final void markReaderIndex() {
        checkReleased();
        buf.markReaderIndex();
    }

    @Override
    public final void resetReaderIndex() {
        checkReleased();
        buf.resetReaderIndex();
    }

    @Override
    public final void markWriterIndex() {
        checkReleased();
        buf.markWriterIndex();
    }

    @Override
    public final void resetWriterIndex() {
        checkReleased();
        buf.resetWriterIndex();
    }

    @Override
    public final void discardReadBytes() {
        checkReleased();
        buf.discardReadBytes();
    }

    @Override
    public final void ensureWritableBytes(int minWritableBytes) {
        checkReleased();
        buf.ensureWritableBytes(minWritableBytes);
    }

    @Override
    public final int ensureWritableBytes(int minWritableBytes, boolean force) {
        checkReleased();
        return buf.ensureWritableBytes(minWritableBytes, force);
    }

    @Override
    public final boolean getBoolean(int index) {
        checkReleased();
        return buf.getBoolean(index);
    }

    @Override
    public final byte getByte(int index) {
        checkReleased();
        return buf.getByte(index);
    }

    @Override
    public final short getUnsignedByte(int index) {
        checkReleased();
        return buf.getUnsignedByte(index);
    }

    @Override
    public final short getShort(int index) {
        checkReleased();
        return buf.getShort(index);
    }

    @Override
    public final int getUnsignedShort(int index) {
        checkReleased();
        return buf.getUnsignedShort(index);
    }

    @Override
    public final int getMedium(int index) {
        checkReleased();
        return buf.getMedium(index);
    }

    @Override
    public final int getUnsignedMedium(int index) {
        checkReleased();
        return buf.getUnsignedMedium(index);
    }

    @Override
    public final int getInt(int index) {
        checkReleased();
        return buf.getInt(index);
    }

    @Override
    public final long getUnsignedInt(int index) {
        checkReleased();
        return buf.getUnsignedInt(index);
    }

    @Override
    public final long getLong(int index) {
        checkReleased();
        return buf.getLong(index);
    }

    @Override
    public final char getChar(int index) {
        checkReleased();
        return buf.getChar(index);
    }

    @Override
    public final float getFloat(int index) {
        checkReleased();
        return buf.getFloat(index);
    }

    @Override
    public final double getDouble(int index) {
        checkReleased();
        return buf.getDouble(index);
    }

    @Override
    public final void getBytes(int index, ByteBuf dst) {
        checkReleased();
        buf.getBytes(index, dst);
    }

    @Override
    public final void getBytes(int index, ByteBuf dst, int length) {
        checkReleased();
        buf.getBytes(index, dst, length);
    }

    @Override
    public final void getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        checkReleased();
        buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public final void getBytes(int index, byte[] dst) {
        checkReleased();
        buf.getBytes(index, dst);
    }

    @Override
    public final void getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkReleased();
        buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public final void getBytes(int index, ByteBuffer dst) {
        checkReleased();
        buf.getBytes(index, dst);
    }

    @Override
    public final void getBytes(int index, OutputStream out, int length) throws IOException {
        checkReleased();
        buf.getBytes(index, out, length);
    }

    @Override
    public final int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        checkReleased();
        return buf.getBytes(index, out, length);
    }

    @Override
    public final void setBoolean(int index, boolean value) {
        checkReleased();
        buf.setBoolean(index, value);
    }

    @Override
    public final void setByte(int index, int value) {
        checkReleased();
        buf.setByte(index, value);
    }

    @Override
    public final void setShort(int index, int value) {
        checkReleased();
        buf.setShort(index, value);
    }

    @Override
    public final void setMedium(int index, int value) {
        checkReleased();
        buf.setMedium(index, value);
    }

    @Override
    public final void setInt(int index, int value) {
        checkReleased();
        buf.setInt(index, value);
    }

    @Override
    public final void setLong(int index, long value) {
        checkReleased();
        buf.setLong(index, value);
    }

    @Override
    public final void setChar(int index, int value) {
        checkReleased();
        buf.setChar(index, value);
    }

    @Override
    public final void setFloat(int index, float value) {
        checkReleased();
        buf.setFloat(index, value);
    }

    @Override
    public final void setDouble(int index, double value) {
        checkReleased();
        buf.setDouble(index, value);
    }

    @Override
    public final void setBytes(int index, ByteBuf src) {
        checkReleased();
        buf.setBytes(index, src);
    }

    @Override
    public final void setBytes(int index, ByteBuf src, int length) {
        checkReleased();
        buf.setBytes(index, src, length);
    }

    @Override
    public final void setBytes(int index, ByteBuf src, int srcIndex, int length) {
        checkReleased();
        buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public final void setBytes(int index, byte[] src) {
        checkReleased();
        buf.setBytes(index, src);
    }

    @Override
    public final void setBytes(int index, byte[] src, int srcIndex, int length) {
        checkReleased();
        buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public final void setBytes(int index, ByteBuffer src) {
        checkReleased();
        buf.setBytes(index, src);
    }

    @Override
    public final int setBytes(int index, InputStream in, int length) throws IOException {
        checkReleased();
        return buf.setBytes(index, in, length);
    }

    @Override
    public final int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        checkReleased();
        return buf.setBytes(index, in, length);
    }

    @Override
    public final void setZero(int index, int length) {
        checkReleased();
        buf.setZero(index, length);
    }

    @Override
    public final boolean readBoolean() {
        checkReleased();
        return buf.readBoolean();
    }

    @Override
    public final byte readByte() {
        checkReleased();
        return buf.readByte();
    }

    @Override
    public final short readUnsignedByte() {
        checkReleased();
        return buf.readUnsignedByte();
    }

    @Override
    public final short readShort() {
        checkReleased();
        return buf.readShort();
    }

    @Override
    public final int readUnsignedShort() {
        checkReleased();
        return buf.readUnsignedShort();
    }

    @Override
    public final int readMedium() {
        checkReleased();
        return buf.readMedium();
    }

    @Override
    public final int readUnsignedMedium() {
        checkReleased();
        return buf.readUnsignedMedium();
    }

    @Override
    public final int readInt() {
        checkReleased();
        return buf.readInt();
    }

    @Override
    public final long readUnsignedInt() {
        checkReleased();
        return buf.readUnsignedInt();
    }

    @Override
    public final long readLong() {
        checkReleased();
        return buf.readLong();
    }

    @Override
    public final char readChar() {
        checkReleased();
        return buf.readChar();
    }

    @Override
    public final float readFloat() {
        checkReleased();
        return buf.readFloat();
    }

    @Override
    public final double readDouble() {
        checkReleased();
        return buf.readDouble();
    }

    @Override
    public final ByteBuf readBytes(int length) {
        checkReleased();
        return buf.readBytes(length);
    }

    @Override
    public final ByteBuf readSlice(int length) {
        checkReleased();
        ByteBuf slice = slice(readerIndex(), length);
        buf.readerIndex(readerIndex() + length);
        return slice;
    }

    @Override
    public final void readBytes(ByteBuf dst) {
        checkReleased();
        buf.readBytes(dst);
    }

    @Override
    public final void readBytes(ByteBuf dst, int length) {
        checkReleased();
        buf.readBytes(dst, length);
    }

    @Override
    public final void readBytes(ByteBuf dst, int dstIndex, int length) {
        checkReleased();
        buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public final void readBytes(byte[] dst) {
        checkReleased();
        buf.readBytes(dst);
    }

    @Override
    public final void readBytes(byte[] dst, int dstIndex, int length) {
        checkReleased();
        buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public final void readBytes(ByteBuffer dst) {
        checkReleased();
        buf.readBytes(dst);
    }

    @Override
    public final void readBytes(OutputStream out, int length) throws IOException {
        checkReleased();
        buf.readBytes(out, length);
    }

    @Override
    public final int readBytes(GatheringByteChannel out, int length) throws IOException {
        checkReleased();
        return buf.readBytes(out, length);
    }

    @Override
    public final void skipBytes(int length) {
        checkReleased();
        buf.skipBytes(length);
    }

    @Override
    public final void writeBoolean(boolean value) {
        checkReleased();
        buf.writeBoolean(value);
    }

    @Override
    public final void writeByte(int value) {
        checkReleased();
        buf.writeByte(value);
    }

    @Override
    public final void writeShort(int value) {
        checkReleased();
        buf.writeShort(value);
    }

    @Override
    public final void writeMedium(int value) {
        checkReleased();
        buf.writeMedium(value);
    }

    @Override
    public final void writeInt(int value) {
        checkReleased();
        buf.writeInt(value);
    }

    @Override
    public final void writeLong(long value) {
        checkReleased();
        buf.writeLong(value);
    }

    @Override
    public final void writeChar(int value) {
        checkReleased();
        buf.writeChar(value);
    }

    @Override
    public final void writeFloat(float value) {
        checkReleased();
        buf.writeFloat(value);
    }

    @Override
    public final void writeDouble(double value) {
        checkReleased();
        buf.writeDouble(value);
    }

    @Override
    public final void writeBytes(ByteBuf src) {
        checkReleased();
        buf.writeBytes(src);
    }

    @Override
    public final void writeBytes(ByteBuf src, int length) {
        checkReleased();
        buf.writeBytes(src, length);
    }

    @Override
    public final void writeBytes(ByteBuf src, int srcIndex, int length) {
        checkReleased();
        buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public final void writeBytes(byte[] src) {
        checkReleased();
        buf.writeBytes(src);
    }

    @Override
    public final void writeBytes(byte[] src, int srcIndex, int length) {
        checkReleased();
        buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public final void writeBytes(ByteBuffer src) {
        checkReleased();
        buf.writeBytes(src);
    }

    @Override
    public final int writeBytes(InputStream in, int length) throws IOException {
        checkReleased();
        return buf.writeBytes(in, length);
    }

    @Override
    public final int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        checkReleased();
        return buf.writeBytes(in, length);
    }

    @Override
    public final void writeZero(int length) {
        checkReleased();
        buf.writeZero(length);
    }

    @Override
    public final int indexOf(int fromIndex, int toIndex, byte value) {
        checkReleased();
        return buf.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public final int indexOf(int fromIndex, int toIndex, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return buf.indexOf(fromIndex, toIndex, indexFinder);
    }

    @Override
    public final int bytesBefore(byte value) {
        checkReleased();
        return buf.bytesBefore(value);
    }

    @Override
    public final int bytesBefore(ByteBufIndexFinder indexFinder) {
        checkReleased();
        return buf.bytesBefore(indexFinder);
    }

    @Override
    public final int bytesBefore(int length, byte value) {
        checkReleased();
        return buf.bytesBefore(length, value);
    }

    @Override
    public final int bytesBefore(int length, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return buf.bytesBefore(length, indexFinder);
    }

    @Override
    public final int bytesBefore(int index, int length, byte value) {
        checkReleased();
        return buf.bytesBefore(index, length, value);
    }

    @Override
    public final int bytesBefore(int index, int length, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return buf.bytesBefore(index, length, indexFinder);
    }

    @Override
    public final ByteBuf copy() {
        checkReleased();
        return buf.copy();
    }

    @Override
    public final ByteBuf copy(int index, int length) {
        checkReleased();
        return buf.copy(index, length);
    }

    @Override
    public final ByteBuf slice() {
        return slice(readerIndex(), readableBytes());
    }

    @Override
    public final ByteBuf slice(int index, int length) {
        checkReleased();
        if (length == 0) {
            return Unpooled.EMPTY_BUFFER;
        }

        return new SlicedByteBuf(this, index, length);
    }

    @Override
    public final ByteBuf duplicate() {
        checkReleased();
        return new DuplicatedByteBuf(this);
    }

    @Override
    public final boolean hasNioBuffer() {
        checkReleased();
        return false;
    }

    @Override
    public final ByteBuffer nioBuffer() {
        checkReleased();
        throw new UnsupportedOperationException();
    }

    @Override
    public final ByteBuffer nioBuffer(int index, int length) {
        checkReleased();
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean hasNioBuffers() {
        checkReleased();
        return false;
    }

    @Override
    public final ByteBuffer[] nioBuffers() {
        checkReleased();
        throw new UnsupportedOperationException();
    }

    @Override
    public final ByteBuffer[] nioBuffers(int offset, int length) {
        checkReleased();
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean hasArray() {
        checkReleased();
        return false;
    }

    @Override
    public final byte[] array() {
        checkReleased();
        throw new UnsupportedOperationException();
    }

    @Override
    public final int arrayOffset() {
        checkReleased();
        throw new UnsupportedOperationException();
    }

    @Override
    public final String toString(Charset charset) {
        checkReleased();
        return buf.toString(charset);
    }

    @Override
    public final String toString(int index, int length, Charset charset) {
        checkReleased();
        return buf.toString(index, length, charset);
    }

    @Override
    public final int compareTo(ByteBuf buffer) {
        checkReleased();
        return buf.compareTo(buffer);
    }

    @Override
    public final Unsafe unsafe() {
        return unsafe;
    }

    @Override
    public final ChannelBufType type() {
        checkReleased();
        return ChannelBufType.BYTE;
    }

    @Override
    public final int hashCode() {
        return ByteBufUtil.hashCode(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ByteBuf) {
            return ByteBufUtil.equals(this, (ByteBuf) o);
        }
        return false;
    }

    private final class PooledUnsafe implements Unsafe {
        @Override
        public void acquire() {
            if (refCnt < 0) {
                throw new IllegalStateException();
            }
            refCnt ++;
        }

        @Override
        public void release() {
            if (refCnt <= 0) {
                throw new IllegalStateException();
            }
            refCnt --;
            if (refCnt == 0) {
                buf.clear();
                afterRelease();
            }
        }

        @Override
        public ByteBuffer nioBuffer() {
            return buf.unsafe().nioBuffer();
        }

        @Override
        public ByteBuffer[] nioBuffers() {
            return buf.unsafe().nioBuffers();
        }

        @Override
        public ByteBuf newBuffer(int initialCapacity) {
            return buf.unsafe().newBuffer(initialCapacity);
        }

        @Override
        public void discardSomeReadBytes() {
            buf.unsafe().discardSomeReadBytes();
        }

        @Override
        public int references() {
            return refCnt;
        }
    }

    /**
     * Is called once this instance is completely released via {@link PooledUnsafe#release()}
     */
    protected abstract void afterRelease();

}
