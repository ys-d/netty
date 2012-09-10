/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 0(the "License"); you may not use this file except in compliance
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
import io.netty.buffer.Unpooled;

/**
 * {@link ByteBuf} which is pooled
 *
 */
public abstract class PooledByteBuf implements ByteBuf {
    private int refCnt = 1;
    private final PooledUnsafe unsafe = createUnsafe();

    private void checkReleased() {
        if (refCnt == 0) {
            throw new IllegalStateException("PooledByteBuf was already released");
        }
    }

    /**
     * See {@link #capacity()}
     */
    protected abstract int capacity0();

    /**
     * See {@link #capacity(int)}
     */
    protected abstract void capacity0(int newCapacity);

    /**
     * See {@link #maxCapacity()}
     */
    protected abstract int maxCapacity0();

    /**
     * See {@link #order()}
     */
    protected abstract ByteOrder order0();

    /**
     * See {@link #order(ByteOrder)}
     */
    protected abstract ByteBuf swapped();

    /**
     * See {@link #readerIndex()}
     */
    protected abstract int readerIndex0();

    /**
     * See {@link #readerIndex(int)}
     */
    protected abstract void readerIndex0(int readerIndex);

    /**
     * See {@link #writerIndex()}
     */
    protected abstract int writerIndex0();

    /**
     * See {@link #writerIndex(int)}
     */
    protected abstract void writerIndex0(int writerIndex);

    /**
     * See {@link #setIndex(int, int)}
     */
    protected abstract void setIndex0(int readerIndex, int writerIndex);

    /**
     * See {@link #readableBytes()}
     */
    protected abstract int readableBytes0();

    /**
     * See {@link #writableBytes()}
     */
    protected abstract int writableBytes0();

    /**
     * See {@link #readable()}
     */
    protected abstract boolean readable0();

    /**
     * See {@link #writable()}
     */
    protected abstract boolean writable0();

    /**
     * See {@link #clear()}
     */
    protected abstract void clear0();

    /**
     * See {@link #markReaderIndex()}
     */
    protected abstract void markReaderIndex0();

    /**
     * See {@link #resetReaderIndex()}
     */
    protected abstract void resetReaderIndex0();

    /**
     * See {@link #markWriterIndex()}
     */
    protected abstract void markWriterIndex0();

    /**
     * See {@link #resetWriterIndex()}
     */
    protected abstract void resetWriterIndex0();

    /**
     * See {@link #discardReadBytes()}
     */
    protected abstract void discardReadBytes0();

    /**
     * See {@link #ensureWritableBytes(int)}
     */
    protected abstract void ensureWritableBytes0(int minWritableBytes);

    /**
     * See {@link #ensureWritableBytes(int, boolean)}
     */
    protected abstract int ensureWritableBytes0(int minWritableBytes, boolean force);

    /**
     * See {@link #getBoolean(int)}
     */
    protected abstract boolean getBoolean0(int index);

    /**
     * See {@link #getByte(int)}
     */
    protected abstract byte getByte0(int index);

    /**
     * See {@link #getUnsignedByte(int)}
     */
    protected abstract short getUnsignedByte0(int index);

    /**
     * See {@link #getShort(int)}
     */
    protected abstract short getShort0(int index);

    /**
     * See {@link #getUnsignedShort(int)
     */
    protected abstract int getUnsignedShort0(int index);

    /**
     * See {@link #getMedium(int)}
     */
    protected abstract int getMedium0(int index);

    /**
     * See {@link #getUnsignedMedium(int)}
     */
    protected abstract int getUnsignedMedium0(int index);

    /**
     * See {@link #getInt(int)}
     */
    protected abstract int getInt0(int index);

    /**
     * See {@link #getUnsignedInt(int)}
     */
    protected abstract long getUnsignedInt0(int index);

    /**
     * See {@link #getLong(int)}
     */
    protected abstract long getLong0(int index);

    /**
     * See {@link #getChar0(int)}
     */
    protected abstract char getChar0(int index);

    /**
     * See {@link #getFloat0(int)}
     */
    protected abstract float getFloat0(int index);

    /**
     * See {@link #getDouble0(int)}
     */
    protected abstract double getDouble0(int index);

    /**
     * See {@link #getBytes(int, ByteBuf)}
     */
    protected abstract void getBytes0(int index, ByteBuf dst);

    /**
     * See {@link #getBytes(int, ByteBuf, int)}
     */
    protected abstract void getBytes0(int index, ByteBuf dst, int length);

    /**
     * See {@link #getBytes(int, ByteBuf, int, int)}
     */
    protected abstract void getBytes0(int index, ByteBuf dst, int dstIndex, int length);

    /**
     * See {@link #getBytes(int, byte[])}
     */
    protected abstract void getBytes0(int index, byte[] dst);

    /**
     * See {@link #getBytes(int, byte[], int, int)}
     */
    protected abstract void getBytes0(int index, byte[] dst, int dstIndex, int length);

    /**
     * See {@link #getBytes(int, ByteBuffer)}
     */
    protected abstract void getBytes0(int index, ByteBuffer dst);

    /**
     * See {@link #getBytes(int, OutputStream, int)}
     */
    protected abstract void getBytes0(int index, OutputStream out, int length) throws IOException;

    /**
     * See {@link #getBytes(int, GatheringByteChannel, int)}
     */
    protected abstract int getBytes0(int index, GatheringByteChannel out, int length) throws IOException;

    /**
     * See {@link #setBoolean(int, boolean)}
     */
    protected abstract void setBoolean0(int index, boolean value);

    /**
     * See {@link #setByte(int, int)}
     */
    protected abstract void setByte0(int index, int value);

    /**
     * See {@link #setShort(int, int)}
     */
    protected abstract void setShort0(int index, int value);

    /**
     * See {@link #setMedium(int, int)}
     */
    protected abstract void setMedium0(int index, int value);

    /**
     * See {@link #setInt(int, int)}
     */
    protected abstract void setInt0(int index, int value);

    /**
     * See {@link #setLong(int, long)}
     */
    protected abstract void setLong0(int index, long value);

    /**
     * See {@link #setChar(int, int)}
     */
    protected abstract void setChar0(int index, int value);

    /**
     * See {@link #setFloat(int, float)}
     */
    protected abstract void setFloat0(int index, float value);

    /**
     * See {@link #setDouble(int, double)}
     */
    protected abstract void setDouble0(int index, double value);

    /**
     * See {@link #setBytes(int, ByteBuf)}
     */
    protected abstract void setBytes0(int index, ByteBuf src);

    /**
     * See {@link #setBytes(int, ByteBuf, int)}
     */
    protected abstract void setBytes0(int index, ByteBuf src, int length);

    /**
     * See {@link #setBytes(int, ByteBuf, int, int)}
     */
    protected abstract void setBytes0(int index, ByteBuf src, int srcIndex, int length);

    /**
     * See {@link #setBytes(int, byte[])}
     */
    protected abstract void setBytes0(int index, byte[] src);

    /**
     * See {@link #setBytes(int, byte[], int, int)}
     */
    protected abstract void setBytes0(int index, byte[] src, int srcIndex, int length);

    /**
     * See {@link #setBytes(int, ByteBuffer)}
     */
    protected abstract void setBytes0(int index, ByteBuffer src);

    /**
     * See {@link #setBytes(int, InputStream, int)}
     */
    protected abstract int setBytes0(int index, InputStream in, int length) throws IOException;

    /**
     * See {@link #setBytes(int, ScatteringByteChannel, int)}
     */
    protected abstract int setBytes0(int index, ScatteringByteChannel in, int length) throws IOException;

    /**
     * See {@link #setZero(int, int)}
     */
    protected abstract void setZero0(int index, int length);

    /**
     * See {@link #readBoolean()}
     */
    protected abstract boolean readBoolean0();

    /**
     * See {@link #readByte()}
     */
    protected abstract byte readByte0();

    /**
     * See {@link #readUnsignedByte()}
     */
    protected abstract short readUnsignedByte0();

    /**
     * See {@link #readShort()}
     */
    protected abstract short readShort0();

    /**
     * See {@link #readUnsignedShort()}
     */
    protected abstract int readUnsignedShort0();

    /**
     * See {@link #readMedium()}
     */
    protected abstract int readMedium0();

    /**
     * See {@link #readUnsignedMedium()}
     */
    protected abstract int readUnsignedMedium0();

    /**
     * See {@link #readInt()}
     */
    protected abstract int readInt0();

    /**
     * See {@link #readUnsignedInt()}
     */
    protected abstract long readUnsignedInt0();

    /**
     * See {@link #readLong0()}
     */
    protected abstract long readLong0();

    /**
     * See {@link #readChar()}
     */
    protected abstract char readChar0();

    /**
     * See {@link #readFloat()}
     */
    protected abstract float readFloat0();

    /**
     * See {@link #readDouble()}
     */
    protected abstract double readDouble0();

    /**
     * See {@link #readBytes()}
     */
    protected abstract ByteBuf readBytes0(int length);

    /**
     * See {@link #readSlice(int)}
     */
    protected abstract ByteBuf readSlice0(int length);

    /**
     * See {@link #readBytes(ByteBuf)}
     */
    protected abstract void readBytes0(ByteBuf dst);

    /**
     * See {@link #readBytes(ByteBuf, int)}
     */
    protected abstract void readBytes0(ByteBuf dst, int length);

    /**
     * See {@link #readBytes(ByteBuf, int, int)}
     */
    protected abstract void readBytes0(ByteBuf dst, int dstIndex, int length);

    /**
     * See {@link #readBytes(byte[])}
     */
    protected abstract void readBytes0(byte[] dst);

    /**
     * See {@link #readBytes(byte[], int, int)}
     */
    protected abstract void readBytes0(byte[] dst, int dstIndex, int length);

    /**
     * See {@link #readBytes(ByteBuffer)}
     */
    protected abstract void readBytes0(ByteBuffer dst);

    /**
     * See {@link #readBytes(OutputStream, int)}
     */
    protected abstract void readBytes0(OutputStream out, int length) throws IOException;

    /**
     * See {@link #readBytes(GatheringByteChannel, int)}
     */
    protected abstract int readBytes0(GatheringByteChannel out, int length) throws IOException;

    /**
     * See {@link #skipBytes(int)}
     */
    protected abstract void skipBytes0(int length);

    /**
     * See {@link #writeBoolean(boolean)}
     */
    protected abstract void writeBoolean0(boolean value);

    /**
     * See {@link #writeByte(int)}
     */
    protected abstract void writeByte0(int value);

    /**
     * See {@link #writeShort(int)}
     */
    protected abstract void writeShort0(int value);

    /**
     * See {@link #writeMedium(int)}
     */
    protected abstract void writeMedium0(int value);

    /**
     * See {@link #writeInt(int)}
     */
    protected abstract void writeInt0(int value);

    /**
     * See {@link #writeLong(long)}
     */
    protected abstract void writeLong0(long value);

    /**
     * See {@link #writeChar(int)}
     */
    protected abstract void writeChar0(int value);

    /**
     * See {@link #writeFloat(float)}
     */
    protected abstract void writeFloat0(float value);

    /**
     * See {@link #writeDouble(double)}
     */
    protected abstract void writeDouble0(double value);

    /**
     * See {@link #writeBytes(ByteBuf)}
     */
    protected abstract void writeBytes0(ByteBuf src);

    /**
     * See {@link #writeBytes(ByteBuf, int)}
     */
    protected abstract void writeBytes0(ByteBuf src, int length);

    /**
     * See {@link #writeBytes(ByteBuf, int, int)}
     */
    protected abstract void writeBytes0(ByteBuf src, int srcIndex, int length);

    /**
     * See {@link #writeBytes(byte[])}
     */
    protected abstract void writeBytes0(byte[] src);

    /**
     * See {@link #writeBytes(byte[], srcIndex, int)}
     */
    protected abstract void writeBytes0(byte[] src, int srcIndex, int length);

    /**
     * See {@link #writeBytes(ByteBuffer)}
     */
    protected abstract void writeBytes0(ByteBuffer src);

    /**
     * See {@link #writeBytes(InputStream, int)}
     */
    protected abstract int writeBytes0(InputStream in, int length) throws IOException;

    /**
     * See {@link #writeBytes(ScatteringByteChannel, int)}
     */
    protected abstract int writeBytes0(ScatteringByteChannel in, int length) throws IOException;

    /**
     * See {@link #writeZero(int)}
     */
    protected abstract void writeZero0(int length);

    /**
     * See {@link #indexOf(int, int, byte)}
     */
    protected abstract int indexOf0(int fromIndex, int toIndex, byte value);

    /**
     * See {@link #indexOf(int, int, ByteBufIndexFinder)}
     */
    protected abstract int indexOf0(int fromIndex, int toIndex, ByteBufIndexFinder indexFinder);

    /**
     * See {@link #bytesBefore(byte)}
     */
    protected abstract int bytesBefore0(byte value);

    /**
     * See {@link #bytesBefore(ByteBufIndexFinder)}
     */
    protected abstract int bytesBefore0(ByteBufIndexFinder indexFinder);

    /**
     * See {@link #bytesBefore(int, byte)}
     */
    protected abstract int bytesBefore0(int length, byte value);

    /**
     * See {@link #bytesBefore(int, ByteBufIndexFinder)}
     */
    protected abstract int bytesBefore0(int length, ByteBufIndexFinder indexFinder);

    /**
     * See {@link #bytesBefore(int, index, byte)}
     */
    protected abstract int bytesBefore0(int index, int length, byte value);

    /**
     * See {@link #bytesBefore(int, index, ByteBufIndexFinder)}
     */
    protected abstract int bytesBefore0(int index, int length, ByteBufIndexFinder indexFinder);

    /**
     * See {@link #copy()}
     */
    protected abstract ByteBuf copy0();

    /**
     * See {@link #copy(int, int)}
     */
    protected abstract ByteBuf copy0(int index, int length);

    /**
     * See {@link #slice()}
     */
    protected abstract ByteBuf slice0();

    /**
     * See {@link #slice(int, int)}
     */
    protected abstract ByteBuf slice0(int index, int length);

    /**
     * See {@link #duplicate()}
     */
    protected abstract ByteBuf duplicate0();

    /**
     * See {@link #toString(Charset)}
     */
    protected abstract String toString0(Charset charset);

    /**
     * See {@link #toString(int, int, Charset)}
     */
    protected abstract String toString0(int index, int length, Charset charset);

    /**
     * See {@link #compareTo(ByteBuf)}
     */
    protected abstract int compareTo0(ByteBuf buf);

    /**
     * Create the {@link PooledUnsafe} instance to use for {@link #unsafe()}
     */
    protected abstract PooledUnsafe createUnsafe();

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
        return capacity0();
    }

    @Override
    public final void capacity(int newCapacity) {
        checkReleased();
        capacity0();
    }

    @Override
    public final int maxCapacity() {
        checkReleased();
        return maxCapacity0();
    }

    @Override
    public final ByteOrder order() {
        checkReleased();
        return order0();
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
        return swapped();
    }

    @Override
    public final boolean isDirect() {
        checkReleased();
        return false;
    }

    @Override
    public final int readerIndex() {
        checkReleased();
        return readerIndex0();
    }

    @Override
    public final void readerIndex(int readerIndex) {
        checkReleased();
        readerIndex0(readerIndex);
    }

    @Override
    public final int writerIndex() {
        checkReleased();
        return writerIndex0();
    }

    @Override
    public final void writerIndex(int writerIndex) {
        checkReleased();
        writerIndex0(writerIndex);
    }

    @Override
    public final void setIndex(int readerIndex, int writerIndex) {
        checkReleased();
        setIndex0(readerIndex, writerIndex);
    }

    @Override
    public final int readableBytes() {
        checkReleased();
        return readableBytes0();
    }

    @Override
    public final int writableBytes() {
        checkReleased();
        return writableBytes0();
    }

    @Override
    public final boolean readable() {
        checkReleased();
        return readable0();
    }

    @Override
    public final boolean writable() {
        checkReleased();
        return writable0();
    }

    @Override
    public final void clear() {
        checkReleased();
        clear0();
    }

    @Override
    public final void markReaderIndex() {
        checkReleased();
        markReaderIndex0();
    }

    @Override
    public final void resetReaderIndex() {
        checkReleased();
        resetReaderIndex0();
    }

    @Override
    public final void markWriterIndex() {
        checkReleased();
        markWriterIndex0();
    }

    @Override
    public final void resetWriterIndex() {
        checkReleased();
        resetWriterIndex0();
    }

    @Override
    public final void discardReadBytes() {
        checkReleased();
        discardReadBytes0();
    }

    @Override
    public final void ensureWritableBytes(int minWritableBytes) {
        checkReleased();
        ensureWritableBytes0(minWritableBytes);
    }

    @Override
    public final int ensureWritableBytes(int minWritableBytes, boolean force) {
        checkReleased();
        return ensureWritableBytes0(minWritableBytes, force);
    }

    @Override
    public final boolean getBoolean(int index) {
        checkReleased();
        return getBoolean0(index);
    }

    @Override
    public final byte getByte(int index) {
        checkReleased();
        return getByte0(index);
    }

    @Override
    public final short getUnsignedByte(int index) {
        checkReleased();
        return getUnsignedByte0(index);
    }

    @Override
    public final short getShort(int index) {
        checkReleased();
        return getShort0(index);
    }

    @Override
    public final int getUnsignedShort(int index) {
        checkReleased();
        return getUnsignedShort0(index);
    }

    @Override
    public final int getMedium(int index) {
        checkReleased();
        return getMedium0(index);
    }

    @Override
    public final int getUnsignedMedium(int index) {
        checkReleased();
        return getUnsignedMedium0(index);
    }

    @Override
    public final int getInt(int index) {
        checkReleased();
        return getInt0(index);
    }

    @Override
    public final long getUnsignedInt(int index) {
        checkReleased();
        return getUnsignedInt0(index);
    }

    @Override
    public final long getLong(int index) {
        checkReleased();
        return getLong0(index);
    }

    @Override
    public final char getChar(int index) {
        checkReleased();
        return getChar0(index);
    }

    @Override
    public final float getFloat(int index) {
        checkReleased();
        return getFloat0(index);
    }

    @Override
    public final double getDouble(int index) {
        checkReleased();
        return getDouble0(index);
    }

    @Override
    public final void getBytes(int index, ByteBuf dst) {
        checkReleased();
        getBytes0(index, dst);
    }

    @Override
    public final void getBytes(int index, ByteBuf dst, int length) {
        checkReleased();
        getBytes0(index, dst, length);
    }

    @Override
    public final void getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        checkReleased();
        getBytes0(index, dst, dstIndex, length);
    }

    @Override
    public final void getBytes(int index, byte[] dst) {
        checkReleased();
        getBytes0(index, dst);
    }

    @Override
    public final void getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkReleased();
        getBytes0(index, dst, dstIndex, length);
    }

    @Override
    public final void getBytes(int index, ByteBuffer dst) {
        checkReleased();
        getBytes0(index, dst);
    }

    @Override
    public final void getBytes(int index, OutputStream out, int length) throws IOException {
        checkReleased();
        getBytes0(index, out, length);
    }

    @Override
    public final int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        checkReleased();
        return getBytes0(index, out, length);
    }

    @Override
    public final void setBoolean(int index, boolean value) {
        checkReleased();
        setBoolean0(index, value);
    }

    @Override
    public final void setByte(int index, int value) {
        checkReleased();
        setByte0(index, value);
    }

    @Override
    public final void setShort(int index, int value) {
        checkReleased();
        setShort0(index, value);
    }

    @Override
    public final void setMedium(int index, int value) {
        checkReleased();
        setMedium0(index, value);
    }

    @Override
    public final void setInt(int index, int value) {
        checkReleased();
        setInt0(index, value);
    }

    @Override
    public final void setLong(int index, long value) {
        checkReleased();
        setLong0(index, value);
    }

    @Override
    public final void setChar(int index, int value) {
        checkReleased();
        setChar0(index, value);
    }

    @Override
    public final void setFloat(int index, float value) {
        checkReleased();
        setFloat0(index, value);
    }

    @Override
    public final void setDouble(int index, double value) {
        checkReleased();
        setDouble0(index, value);
    }

    @Override
    public final void setBytes(int index, ByteBuf src) {
        checkReleased();
        setBytes0(index, src);
    }

    @Override
    public final void setBytes(int index, ByteBuf src, int length) {
        checkReleased();
        setBytes0(index, src, length);
    }

    @Override
    public final void setBytes(int index, ByteBuf src, int srcIndex, int length) {
        checkReleased();
        setBytes0(index, src, srcIndex, length);
    }

    @Override
    public final void setBytes(int index, byte[] src) {
        checkReleased();
        setBytes0(index, src);
    }

    @Override
    public final void setBytes(int index, byte[] src, int srcIndex, int length) {
        checkReleased();
        setBytes0(index, src, srcIndex, length);
    }

    @Override
    public final void setBytes(int index, ByteBuffer src) {
        checkReleased();
        setBytes0(index, src);
    }

    @Override
    public final int setBytes(int index, InputStream in, int length) throws IOException {
        checkReleased();
        return setBytes0(index, in, length);
    }

    @Override
    public final int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        checkReleased();
        return setBytes0(index, in, length);
    }

    @Override
    public final void setZero(int index, int length) {
        checkReleased();
        setZero0(index, length);
    }

    @Override
    public final boolean readBoolean() {
        checkReleased();
        return readBoolean0();
    }

    @Override
    public final byte readByte() {
        checkReleased();
        return readByte0();
    }

    @Override
    public final short readUnsignedByte() {
        checkReleased();
        return readUnsignedByte0();
    }

    @Override
    public final short readShort() {
        checkReleased();
        return readShort0();
    }

    @Override
    public final int readUnsignedShort() {
        checkReleased();
        return readUnsignedShort0();
    }

    @Override
    public final int readMedium() {
        checkReleased();
        return readMedium0();
    }

    @Override
    public final int readUnsignedMedium() {
        checkReleased();
        return readUnsignedMedium0();
    }

    @Override
    public final int readInt() {
        checkReleased();
        return readInt0();
    }

    @Override
    public final long readUnsignedInt() {
        checkReleased();
        return readUnsignedInt0();
    }

    @Override
    public final long readLong() {
        checkReleased();
        return readLong0();
    }

    @Override
    public final char readChar() {
        checkReleased();
        return readChar0();
    }

    @Override
    public final float readFloat() {
        checkReleased();
        return readFloat0();
    }

    @Override
    public final double readDouble() {
        checkReleased();
        return readDouble0();
    }

    @Override
    public final ByteBuf readBytes(int length) {
        checkReleased();
        return readBytes0(length);
    }

    @Override
    public final ByteBuf readSlice(int length) {
        checkReleased();
        ByteBuf slice = slice(readerIndex(), length);
        readerIndex0(readerIndex0() + length);
        return slice;
    }

    @Override
    public final void readBytes(ByteBuf dst) {
        checkReleased();
        readBytes0(dst);
    }

    @Override
    public final void readBytes(ByteBuf dst, int length) {
        checkReleased();
        readBytes0(dst, length);
    }

    @Override
    public final void readBytes(ByteBuf dst, int dstIndex, int length) {
        checkReleased();
        readBytes0(dst, dstIndex, length);
    }

    @Override
    public final void readBytes(byte[] dst) {
        checkReleased();
        readBytes0(dst);
    }

    @Override
    public final void readBytes(byte[] dst, int dstIndex, int length) {
        checkReleased();
        readBytes0(dst, dstIndex, length);
    }

    @Override
    public final void readBytes(ByteBuffer dst) {
        checkReleased();
        readBytes0(dst);
    }

    @Override
    public final void readBytes(OutputStream out, int length) throws IOException {
        checkReleased();
        readBytes0(out, length);
    }

    @Override
    public final int readBytes(GatheringByteChannel out, int length) throws IOException {
        checkReleased();
        return readBytes0(out, length);
    }

    @Override
    public final void skipBytes(int length) {
        checkReleased();
        skipBytes0(length);
    }

    @Override
    public final void writeBoolean(boolean value) {
        checkReleased();
        writeBoolean0(value);
    }

    @Override
    public final void writeByte(int value) {
        checkReleased();
        writeByte0(value);
    }

    @Override
    public final void writeShort(int value) {
        checkReleased();
        writeShort0(value);
    }

    @Override
    public final void writeMedium(int value) {
        checkReleased();
        writeMedium0(value);
    }

    @Override
    public final void writeInt(int value) {
        checkReleased();
        writeInt0(value);
    }

    @Override
    public final void writeLong(long value) {
        checkReleased();
        writeLong0(value);
    }

    @Override
    public final void writeChar(int value) {
        checkReleased();
        writeChar0(value);
    }

    @Override
    public final void writeFloat(float value) {
        checkReleased();
        writeFloat0(value);
    }

    @Override
    public final void writeDouble(double value) {
        checkReleased();
        writeDouble0(value);
    }

    @Override
    public final void writeBytes(ByteBuf src) {
        checkReleased();
        writeBytes0(src);
    }

    @Override
    public final void writeBytes(ByteBuf src, int length) {
        checkReleased();
        writeBytes0(src, length);
    }

    @Override
    public final void writeBytes(ByteBuf src, int srcIndex, int length) {
        checkReleased();
        writeBytes0(src, srcIndex, length);
    }

    @Override
    public final void writeBytes(byte[] src) {
        checkReleased();
        writeBytes0(src);
    }

    @Override
    public final void writeBytes(byte[] src, int srcIndex, int length) {
        checkReleased();
        writeBytes0(src, srcIndex, length);
    }

    @Override
    public final void writeBytes(ByteBuffer src) {
        checkReleased();
        writeBytes0(src);
    }

    @Override
    public final int writeBytes(InputStream in, int length) throws IOException {
        checkReleased();
        return writeBytes0(in, length);
    }

    @Override
    public final int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        checkReleased();
        return writeBytes0(in, length);
    }

    @Override
    public final void writeZero(int length) {
        checkReleased();
        writeZero0(length);
    }

    @Override
    public final int indexOf(int fromIndex, int toIndex, byte value) {
        checkReleased();
        return indexOf0(fromIndex, toIndex, value);
    }

    @Override
    public final int indexOf(int fromIndex, int toIndex, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return indexOf0(fromIndex, toIndex, indexFinder);
    }

    @Override
    public final int bytesBefore(byte value) {
        checkReleased();
        return bytesBefore0(value);
    }

    @Override
    public final int bytesBefore(ByteBufIndexFinder indexFinder) {
        checkReleased();
        return bytesBefore0(indexFinder);
    }

    @Override
    public final int bytesBefore(int length, byte value) {
        checkReleased();
        return bytesBefore0(length, value);
    }

    @Override
    public final int bytesBefore(int length, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return bytesBefore0(length, indexFinder);
    }

    @Override
    public final int bytesBefore(int index, int length, byte value) {
        checkReleased();
        return bytesBefore0(index, length, value);
    }

    @Override
    public final int bytesBefore(int index, int length, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return bytesBefore0(index, length, indexFinder);
    }

    @Override
    public final ByteBuf copy() {
        checkReleased();
        return copy0();
    }

    @Override
    public final ByteBuf copy(int index, int length) {
        checkReleased();
        return copy0(index, length);
    }

    @Override
    public final ByteBuf slice() {
        return slice(readerIndex(), readableBytes0());
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
        return toString0(charset);
    }

    @Override
    public final String toString(int index, int length, Charset charset) {
        checkReleased();
        return toString0(index, length, charset);
    }

    @Override
    public final int compareTo(ByteBuf buffer) {
        checkReleased();
        return compareTo0(buffer);
    }

    @Override
    public final Unsafe unsafe() {
        checkReleased();
        return unsafe;
    }

    @Override
    public final ChannelBufType type() {
        checkReleased();
        return ChannelBufType.BYTE;
    }

    @Override
    public int hashCode() {
        return ByteBufUtil.hashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ByteBuf) {
            return ByteBufUtil.equals(this, (ByteBuf) o);
        }
        return false;
    }

    public abstract class PooledUnsafe implements Unsafe {

        @Override
        public void acquire() {
            if (refCnt <= 0) {
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
                clear0();
            }
        }
    }

}
