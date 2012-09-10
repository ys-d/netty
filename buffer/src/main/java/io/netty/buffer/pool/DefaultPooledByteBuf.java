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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufIndexFinder;
import io.netty.buffer.SwappedByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

/**
 * Default {@link PooledByteBuf} implementation 
 *
 */
final class DefaultPooledByteBuf extends PooledByteBuf {

    private ByteBuf buf;
    private final ByteBufPool pool;
    private final SwappedByteBuf swappedBuf;

    DefaultPooledByteBuf(ByteBufPool pool, ByteBuf buf) {
        this.pool = pool;
        this.buf = buf;
        this.swappedBuf = new SwappedByteBuf(this);
    }

    @Override
    public int capacity0() {
        return buf.capacity();
    }

    @Override
    public void capacity0(int newCapacity) {
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
    public int maxCapacity0() {
        return buf.maxCapacity();
    }

    @Override
    public ByteOrder order0() {
        return buf.order();
    }

    @Override
    public ByteBuf swapped() {
        return swappedBuf;
    }

    @Override
    public int readerIndex0() {
        return buf.readerIndex();
    }

    @Override
    public void readerIndex0(int readerIndex) {
        buf.readerIndex(readerIndex);
    }

    @Override
    public int writerIndex0() {
        return buf.writerIndex();
    }

    @Override
    public void writerIndex0(int writerIndex) {
        buf.writerIndex(writerIndex);
    }

    @Override
    public void setIndex0(int readerIndex, int writerIndex) {
        buf.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes0() {
        return buf.readableBytes();
    }

    @Override
    public int writableBytes0() {
        return buf.writableBytes();
    }

    @Override
    public boolean readable0() {
        return buf.readable();
    }

    @Override
    public boolean writable0() {
        return buf.writable();
    }

    @Override
    public void clear0() {
        buf.clear();
    }

    @Override
    public void markReaderIndex0() {
        buf.markReaderIndex();
    }

    @Override
    public void resetReaderIndex0() {
        buf.resetReaderIndex();
    }

    @Override
    public void markWriterIndex0() {
        buf.markWriterIndex();
    }

    @Override
    public void resetWriterIndex0() {
        buf.resetWriterIndex();
    }

    @Override
    public void discardReadBytes0() {
        buf.discardReadBytes();
    }

    @Override
    public void ensureWritableBytes0(int minWritableBytes) {
        buf.ensureWritableBytes(minWritableBytes);
    }

    @Override
    public int ensureWritableBytes0(int minWritableBytes, boolean force) {
        return buf.ensureWritableBytes(minWritableBytes, force);
    }

    @Override
    public boolean getBoolean0(int index) {
        return buf.getBoolean(index);
    }

    @Override
    public byte getByte0(int index) {
        return buf.getByte(index);
    }

    @Override
    public short getUnsignedByte0(int index) {
        return buf.getUnsignedByte(index);
    }

    @Override
    public short getShort0(int index) {
        return buf.getShort(index);
    }

    @Override
    public int getUnsignedShort0(int index) {
        return buf.getUnsignedShort(index);
    }

    @Override
    public int getMedium0(int index) {
        return buf.getMedium(index);
    }

    @Override
    public int getUnsignedMedium0(int index) {
        return buf.getUnsignedMedium(index);
    }

    @Override
    public int getInt0(int index) {
        return buf.getInt(index);
    }

    @Override
    public long getUnsignedInt0(int index) {
        return buf.getUnsignedInt(index);
    }

    @Override
    public long getLong0(int index) {
        return buf.getLong(index);
    }

    @Override
    public char getChar0(int index) {
        return buf.getChar(index);
    }

    @Override
    public float getFloat0(int index) {
        return buf.getFloat(index);
    }

    @Override
    public double getDouble0(int index) {
        return buf.getDouble(index);
    }

    @Override
    public void getBytes0(int index, ByteBuf dst) {
        buf.getBytes(index, dst);
    }

    @Override
    public void getBytes0(int index, ByteBuf dst, int length) {
        buf.getBytes(index, dst, length);
    }

    @Override
    public void getBytes0(int index, ByteBuf dst, int dstIndex, int length) {
        buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public void getBytes0(int index, byte[] dst) {
        buf.getBytes(index, dst);
    }

    @Override
    public void getBytes0(int index, byte[] dst, int dstIndex, int length) {
        buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public void getBytes0(int index, ByteBuffer dst) {
        buf.getBytes(index, dst);
    }

    @Override
    public void getBytes0(int index, OutputStream out, int length) throws IOException {
        buf.getBytes(index, out, length);
    }

    @Override
    public int getBytes0(int index, GatheringByteChannel out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    @Override
    public void setBoolean0(int index, boolean value) {
        buf.setBoolean(index, value);
    }

    @Override
    public void setByte0(int index, int value) {
        buf.setByte(index, value);
    }

    @Override
    public void setShort0(int index, int value) {
        buf.setShort(index, value);
    }

    @Override
    public void setMedium0(int index, int value) {
        buf.setMedium(index, value);
    }

    @Override
    public void setInt0(int index, int value) {
        buf.setInt(index, value);
    }

    @Override
    public void setLong0(int index, long value) {
        buf.setLong(index, value);
    }

    @Override
    public void setChar0(int index, int value) {
        buf.setChar(index, value);
    }

    @Override
    public void setFloat0(int index, float value) {
        buf.setFloat(index, value);
    }

    @Override
    public void setDouble0(int index, double value) {
        buf.setDouble(index, value);
    }

    @Override
    public void setBytes0(int index, ByteBuf src) {
        buf.setBytes(index, src);
    }

    @Override
    public void setBytes0(int index, ByteBuf src, int length) {
        buf.setBytes(index, src, length);
    }

    @Override
    public void setBytes0(int index, ByteBuf src, int srcIndex, int length) {
        buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public void setBytes0(int index, byte[] src) {
        buf.setBytes(index, src);
    }

    @Override
    public void setBytes0(int index, byte[] src, int srcIndex, int length) {
        buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public void setBytes0(int index, ByteBuffer src) {
        buf.setBytes(index, src);
    }

    @Override
    public int setBytes0(int index, InputStream in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes0(int index, ScatteringByteChannel in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    @Override
    public void setZero0(int index, int length) {
        buf.setZero(index, length);
    }

    @Override
    public boolean readBoolean0() {
        return buf.readBoolean();
    }

    @Override
    public byte readByte0() {
        return buf.readByte();
    }

    @Override
    public short readUnsignedByte0() {
        return buf.readUnsignedByte();
    }

    @Override
    public short readShort0() {
        return buf.readShort();
    }

    @Override
    public int readUnsignedShort0() {
        return buf.readUnsignedShort();
    }

    @Override
    public int readMedium0() {
        return buf.readMedium();
    }

    @Override
    public int readUnsignedMedium0() {
        return buf.readUnsignedMedium();
    }

    @Override
    public int readInt0() {
        return buf.readInt();
    }

    @Override
    public long readUnsignedInt0() {
        return buf.readUnsignedInt();
    }

    @Override
    public long readLong0() {
        return buf.readLong();
    }

    @Override
    public char readChar0() {
        return buf.readChar();
    }

    @Override
    public float readFloat0() {
        return buf.readFloat();
    }

    @Override
    public double readDouble0() {
        return buf.readDouble();
    }

    @Override
    public ByteBuf readBytes0(int length) {
        return buf.readBytes(length);
    }

    @Override
    public ByteBuf readSlice0(int length) {
        return buf.readSlice(length);
    }

    @Override
    public void readBytes0(ByteBuf dst) {
        buf.readBytes(dst);
    }

    @Override
    public void readBytes0(ByteBuf dst, int length) {
        buf.readBytes(dst, length);
    }

    @Override
    public void readBytes0(ByteBuf dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
        
    }

    @Override
    public void readBytes0(byte[] dst) {
        buf.readBytes(dst);
        
    }

    @Override
    public void readBytes0(byte[] dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public void readBytes0(ByteBuffer dst) {
        buf.readBytes(dst);
    }

    @Override
    public void readBytes0(OutputStream out, int length) throws IOException {
        buf.readBytes(out, length);
    }

    @Override
    public int readBytes0(GatheringByteChannel out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    @Override
    public void skipBytes0(int length) {
        buf.skipBytes(length);
    }

    @Override
    public void writeBoolean0(boolean value) {
        buf.writeBoolean(value);
    }

    @Override
    public void writeByte0(int value) {
        buf.writeByte(value);
    }

    @Override
    public void writeShort0(int value) {
        buf.writeShort(value);
        
    }

    @Override
    public void writeMedium0(int value) {
        buf.writeMedium(value);
    }

    @Override
    public void writeInt0(int value) {
        buf.writeInt(value);
    }

    @Override
    public void writeLong0(long value) {
        buf.writeLong(value);
    }

    @Override
    public void writeChar0(int value) {
        buf.writeChar(value);
    }

    @Override
    public void writeFloat0(float value) {
        buf.writeFloat(value);
    }

    @Override
    public void writeDouble0(double value) {
        buf.writeDouble(value);
    }

    @Override
    public void writeBytes0(ByteBuf src) {
        buf.writeBytes(src);
    }

    @Override
    public void writeBytes0(ByteBuf src, int length) {
        buf.writeBytes(src, length);
    }

    @Override
    public void writeBytes0(ByteBuf src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes0(byte[] src) {
        buf.writeBytes(src);
    }

    @Override
    public void writeBytes0(byte[] src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes0(ByteBuffer src) {
        buf.writeBytes(src);
    }

    @Override
    public int writeBytes0(InputStream in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes0(ScatteringByteChannel in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    @Override
    public void writeZero0(int length) {
        buf.writeZero(length);
    }

    @Override
    public int indexOf0(int fromIndex, int toIndex, byte value) {
        return buf.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int indexOf0(int fromIndex, int toIndex, ByteBufIndexFinder indexFinder) {
        return buf.indexOf(fromIndex, toIndex, indexFinder);
    }

    @Override
    public int bytesBefore0(byte value) {
        return buf.bytesBefore(value);
    }

    @Override
    public int bytesBefore0(ByteBufIndexFinder indexFinder) {
        return buf.bytesBefore(indexFinder);
    }

    @Override
    public int bytesBefore0(int length, byte value) {
        return buf.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore0(int length, ByteBufIndexFinder indexFinder) {
        return buf.bytesBefore(length, indexFinder);
    }

    @Override
    public int bytesBefore0(int index, int length, byte value) {
        return buf.bytesBefore(index, length, value);
    }

    @Override
    public int bytesBefore0(int index, int length, ByteBufIndexFinder indexFinder) {
        return buf.bytesBefore(index, length, indexFinder);
    }

    @Override
    public ByteBuf copy0() {
        return buf.copy();
    }

    @Override
    public ByteBuf copy0(int index, int length) {
        return buf.copy(index, length);
    }

    @Override
    public ByteBuf slice0() {
        return buf.slice();
    }

    @Override
    public ByteBuf slice0(int index, int length) {
        return buf.slice(index, length);
    }

    @Override
    public ByteBuf duplicate0() {
        return buf.duplicate();
    }

    @Override
    public String toString0(Charset charset) {
        return buf.toString(charset);
    }

    @Override
    public String toString0(int index, int length, Charset charset) {
        return buf.toString(index, length, charset);
    }

    @Override
    protected int compareTo0(ByteBuf buffer) {
        return buf.compareTo(buffer);
    }

    @Override
    protected PooledUnsafe createUnsafe() {
        return new DefaulPooledUnsafe();
    }

    private final class DefaulPooledUnsafe extends PooledUnsafe {

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
            return buf.unsafe().references();
        }
    }
}
