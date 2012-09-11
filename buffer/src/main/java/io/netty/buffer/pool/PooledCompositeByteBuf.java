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
import java.nio.ReadOnlyBufferException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufIndexFinder;
import io.netty.buffer.ChannelBufType;
import io.netty.buffer.DefaultCompositeByteBuf;

final class PooledCompositeByteBuf extends DefaultCompositeByteBuf {

    public PooledCompositeByteBuf(PooledByteBuf[] buffers) {
        super(buffers.length);
        for (int i = 0; i < buffers.length; i++) {
            ByteBuf buf =  buffers[i];
            buf.writerIndex(buf.capacity());

            super.addComponent(i, buf);
            buf.writerIndex(0);

            // release the buf again after we added it as the add operation will
            // increase the reference count also.
            buf.unsafe().release();

        }
    }

    private void checkReleased() {
        if (refCnt == 0) {
            throw new IllegalStateException("PooledCompositeByteBuf was already released");
        }
    }

    @Override
    public void addComponent(ByteBuf buffer) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void addComponents(ByteBuf... buffers) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void addComponents(Iterable<ByteBuf> buffers) {
        throw new ReadOnlyBufferException();

    }

    @Override
    public void addComponent(int cIndex, ByteBuf buffer) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void addComponents(int cIndex, ByteBuf... buffers) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void addComponents(int cIndex, Iterable<ByteBuf> buffers) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void removeComponent(int cIndex) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void removeComponents(int cIndex, int numComponents) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Iterator<ByteBuf> iterator() {
        checkReleased();
        final Iterator<ByteBuf> it = super.iterator();
        return new Iterator<ByteBuf>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ByteBuf next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public List<ByteBuf> decompose(int offset, int length) {
        checkReleased();
        return super.decompose(offset, length);
    }

    @Override
    public boolean isDirect() {
        checkReleased();
        return super.isDirect();
    }

    @Override
    public boolean hasArray() {
        checkReleased();
        return super.hasArray();
    }

    @Override
    public byte[] array() {
        checkReleased();
        return super.array();
    }

    @Override
    public int arrayOffset() {
        checkReleased();
        return super.arrayOffset();
    }

    @Override
    public int capacity() {
        checkReleased();
        return super.capacity();
    }

    @Override
    public void capacity(int newCapacity) {
        checkReleased();
        super.capacity(newCapacity);
    }

    @Override
    public int numComponents() {
        checkReleased();
        return super.numComponents();
    }

    @Override
    public int maxNumComponents() {
        checkReleased();
        return super.maxNumComponents();
    }

    @Override
    public int toComponentIndex(int offset) {
        checkReleased();
        return super.toComponentIndex(offset);
    }

    @Override
    public int toByteIndex(int cIndex) {
        checkReleased();
        return super.toByteIndex(cIndex);
    }

    @Override
    public byte getByte(int index) {
        checkReleased();
        return super.getByte(index);
    }

    @Override
    public short getShort(int index) {
        checkReleased();
        return super.getShort(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        checkReleased();
        return super.getUnsignedMedium(index);
    }

    @Override
    public int getInt(int index) {
        checkReleased();
        return super.getInt(index);
    }

    @Override
    public long getLong(int index) {
        checkReleased();
        return super.getLong(index);
    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkReleased();
        super.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        checkReleased();
        super.getBytes(index, dst);
    }

    @Override
    public void getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        checkReleased();
        super.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        checkReleased();
        return super.getBytes(index, out, length);
    }

    @Override
    public void getBytes(int index, OutputStream out, int length) throws IOException {
        checkReleased();
        super.getBytes(index, out, length);
    }

    @Override
    public void setByte(int index, int value) {
        checkReleased();
        super.setByte(index, value);
    }

    @Override
    public void setShort(int index, int value) {
        checkReleased();
        super.setShort(index, value);
    }

    @Override
    public void setMedium(int index, int value) {
        checkReleased();
        super.setMedium(index, value);
    }

    @Override
    public void setInt(int index, int value) {
        checkReleased();
        super.setInt(index, value);
    }

    @Override
    public void setLong(int index, long value) {
        checkReleased();
        super.setLong(index, value);
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        checkReleased();
        super.setBytes(index, src, srcIndex, length);
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        checkReleased();
        super.setBytes(index, src);
    }

    @Override
    public void setBytes(int index, ByteBuf src, int srcIndex, int length) {
        checkReleased();
        super.setBytes(index, src, srcIndex, length);
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        checkReleased();
        return super.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        checkReleased();
        return super.setBytes(index, in, length);
    }

    @Override
    public ByteBuf copy(int index, int length) {
        checkReleased();
        return super.copy(index, length);
    }

    @Override
    public ByteBuf component(int cIndex) {
        checkReleased();
        return super.component(cIndex);
    }

    @Override
    public ByteBuf componentAtOffset(int offset) {
        checkReleased();
        return super.componentAtOffset(offset);
    }

    @Override
    public boolean hasNioBuffer() {
        checkReleased();
        return super.hasNioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        checkReleased();
        return super.nioBuffer(index, length);
    }

    @Override
    public boolean hasNioBuffers() {
        checkReleased();
        return super.hasNioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        checkReleased();
        return super.nioBuffers(index, length);
    }

    @Override
    public void consolidate() {
        checkReleased();
        super.consolidate();
    }

    @Override
    public void consolidate(int cIndex, int numComponents) {
        checkReleased();
        super.consolidate(cIndex, numComponents);
    }

    @Override
    public void discardReadComponents() {
        checkReleased();
        super.discardReadComponents();
    }

    @Override
    public void discardReadBytes() {
        checkReleased();
        super.discardReadBytes();
    }

    @Override
    public String toString() {
        checkReleased();
        return super.toString();
    }

    @Override
    public Unsafe unsafe() {
        checkReleased();
        return super.unsafe();
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        checkReleased();
        return super.nioBuffers();
    }

    @Override
    public ChannelBufType type() {
        checkReleased();
        return super.type();
    }

    @Override
    public int maxCapacity() {
        checkReleased();
        return super.maxCapacity();
    }

    @Override
    public int readerIndex() {
        checkReleased();
        return super.readerIndex();
    }

    @Override
    public void readerIndex(int readerIndex) {
        checkReleased();
        super.readerIndex(readerIndex);
    }

    @Override
    public int writerIndex() {
        checkReleased();
        return super.writerIndex();
    }

    @Override
    public void writerIndex(int writerIndex) {
        checkReleased();
        super.writerIndex(writerIndex);
    }

    @Override
    public void setIndex(int readerIndex, int writerIndex) {
        checkReleased();
        super.setIndex(readerIndex, writerIndex);
    }

    @Override
    public void clear() {
        checkReleased();
        super.clear();
    }

    @Override
    public boolean readable() {
        checkReleased();
        return super.readable();
    }

    @Override
    public boolean writable() {
        checkReleased();
        return super.writable();
    }

    @Override
    public int readableBytes() {
        checkReleased();
        return super.readableBytes();
    }

    @Override
    public int writableBytes() {
        checkReleased();
        return super.writableBytes();
    }

    @Override
    public void markReaderIndex() {
        checkReleased();
        super.markReaderIndex();
    }

    @Override
    public void resetReaderIndex() {
        checkReleased();
        super.resetReaderIndex();
    }

    @Override
    public void markWriterIndex() {
        checkReleased();
        super.markWriterIndex();
    }

    @Override
    public void resetWriterIndex() {
        checkReleased();
        super.resetWriterIndex();
    }

    @Override
    protected void adjustMarkers(int decrement) {
        checkReleased();
        super.adjustMarkers(decrement);
    }

    @Override
    public void ensureWritableBytes(int minWritableBytes) {
        checkReleased();
        super.ensureWritableBytes(minWritableBytes);
    }

    @Override
    public int ensureWritableBytes(int minWritableBytes, boolean force) {
        checkReleased();
        return super.ensureWritableBytes(minWritableBytes, force);
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        checkReleased();
        return super.order(endianness);
    }

    @Override
    public boolean getBoolean(int index) {
        checkReleased();
        return super.getBoolean(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        checkReleased();
        return super.getUnsignedByte(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        checkReleased();
        return super.getUnsignedShort(index);
    }

    @Override
    public int getMedium(int index) {
        checkReleased();
        return super.getMedium(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        checkReleased();
        return super.getUnsignedInt(index);
    }

    @Override
    public char getChar(int index) {
        checkReleased();
        return super.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        checkReleased();
        return super.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        checkReleased();
        return super.getDouble(index);
    }

    @Override
    public void getBytes(int index, byte[] dst) {
        checkReleased();
        super.getBytes(index, dst);
    }

    @Override
    public void getBytes(int index, ByteBuf dst) {
        checkReleased();
        super.getBytes(index, dst);
    }

    @Override
    public void getBytes(int index, ByteBuf dst, int length) {
        checkReleased();
        super.getBytes(index, dst, length);
    }

    @Override
    public void setBoolean(int index, boolean value) {
        checkReleased();
        super.setBoolean(index, value);
    }

    @Override
    public void setChar(int index, int value) {
        checkReleased();
        super.setChar(index, value);
    }

    @Override
    public void setFloat(int index, float value) {
        checkReleased();
        super.setFloat(index, value);
    }

    @Override
    public void setDouble(int index, double value) {
        checkReleased();
        super.setDouble(index, value);
    }

    @Override
    public void setBytes(int index, byte[] src) {
        checkReleased();
        super.setBytes(index, src);
    }

    @Override
    public void setBytes(int index, ByteBuf src) {
        checkReleased();
        super.setBytes(index, src);
    }

    @Override
    public void setBytes(int index, ByteBuf src, int length) {
        checkReleased();
        super.setBytes(index, src, length);
    }

    @Override
    public void setZero(int index, int length) {
        checkReleased();
        super.setZero(index, length);
    }

    @Override
    public byte readByte() {
        checkReleased();
        return super.readByte();
    }

    @Override
    public boolean readBoolean() {
        checkReleased();
        return super.readBoolean();
    }

    @Override
    public short readUnsignedByte() {
        checkReleased();
        return super.readUnsignedByte();
    }

    @Override
    public short readShort() {
        checkReleased();
        return super.readShort();
    }

    @Override
    public int readUnsignedShort() {
        checkReleased();
        return super.readUnsignedShort();
    }

    @Override
    public int readMedium() {
        checkReleased();
        return super.readMedium();
    }

    @Override
    public int readUnsignedMedium() {
        checkReleased();
        return super.readUnsignedMedium();
    }

    @Override
    public int readInt() {
        checkReleased();
        return super.readInt();
    }

    @Override
    public long readUnsignedInt() {
        checkReleased();
        return super.readUnsignedInt();
    }

    @Override
    public long readLong() {
        checkReleased();
        return super.readLong();
    }

    @Override
    public char readChar() {
        checkReleased();
        return super.readChar();
    }

    @Override
    public float readFloat() {
        checkReleased();
        return super.readFloat();
    }

    @Override
    public double readDouble() {
        checkReleased();
        return super.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        checkReleased();
        return super.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        checkReleased();
        return super.readSlice(length);
    }

    @Override
    public void readBytes(byte[] dst, int dstIndex, int length) {
        checkReleased();
        super.readBytes(dst, dstIndex, length);
    }

    @Override
    public void readBytes(byte[] dst) {
        checkReleased();
        super.readBytes(dst);
    }

    @Override
    public void readBytes(ByteBuf dst) {
        checkReleased();
        super.readBytes(dst);
    }

    @Override
    public void readBytes(ByteBuf dst, int length) {
        checkReleased();
        super.readBytes(dst, length);
    }

    @Override
    public void readBytes(ByteBuf dst, int dstIndex, int length) {
        checkReleased();
        super.readBytes(dst, dstIndex, length);
    }

    @Override
    public void readBytes(ByteBuffer dst) {
        checkReleased();
        super.readBytes(dst);
    }

    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        checkReleased();
        return super.readBytes(out, length);
    }

    @Override
    public void readBytes(OutputStream out, int length) throws IOException {
        checkReleased();
        super.readBytes(out, length);
    }

    @Override
    public void skipBytes(int length) {
        checkReleased();
        super.skipBytes(length);
    }

    @Override
    public void writeBoolean(boolean value) {
        checkReleased();
        super.writeBoolean(value);
    }

    @Override
    public void writeByte(int value) {
        checkReleased();
        super.writeByte(value);
    }

    @Override
    public void writeShort(int value) {
        checkReleased();
        super.writeShort(value);
    }

    @Override
    public void writeMedium(int value) {
        checkReleased();
        super.writeMedium(value);
    }

    @Override
    public void writeInt(int value) {
        checkReleased();
        super.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        checkReleased();
        super.writeLong(value);
    }

    @Override
    public void writeChar(int value) {
        checkReleased();
        super.writeChar(value);
    }

    @Override
    public void writeFloat(float value) {
        checkReleased();
        super.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        checkReleased();
        super.writeDouble(value);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        checkReleased();
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(byte[] src) {
        checkReleased();
        super.writeBytes(src);
    }

    @Override
    public void writeBytes(ByteBuf src) {
        checkReleased();
        super.writeBytes(src);
    }

    @Override
    public void writeBytes(ByteBuf src, int length) {
        checkReleased();
        super.writeBytes(src, length);
    }

    @Override
    public void writeBytes(ByteBuf src, int srcIndex, int length) {
        checkReleased();
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        checkReleased();
        super.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        checkReleased();
        return super.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        checkReleased();
        return super.writeBytes(in, length);
    }

    @Override
    public void writeZero(int length) {
        checkReleased();
        super.writeZero(length);
    }

    @Override
    public ByteBuf copy() {
        checkReleased();
        return super.copy();
    }

    @Override
    public ByteBuf duplicate() {
        checkReleased();
        return super.duplicate();
    }

    @Override
    public ByteBuf slice() {
        checkReleased();
        return super.slice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        checkReleased();
        return super.slice(index, length);
    }

    @Override
    public ByteBuffer nioBuffer() {
        checkReleased();
        return super.nioBuffer();
    }

    @Override
    public String toString(Charset charset) {
        checkReleased();
        return super.toString(charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        checkReleased();
        return super.toString(index, length, charset);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        checkReleased();
        return super.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return super.indexOf(fromIndex, toIndex, indexFinder);
    }

    @Override
    public int bytesBefore(byte value) {
        checkReleased();
        return super.bytesBefore(value);
    }

    @Override
    public int bytesBefore(ByteBufIndexFinder indexFinder) {
        checkReleased();
        return super.bytesBefore(indexFinder);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        checkReleased();
        return super.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int length, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return super.bytesBefore(length, indexFinder);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        checkReleased();
        return super.bytesBefore(index, length, value);
    }

    @Override
    public int bytesBefore(int index, int length, ByteBufIndexFinder indexFinder) {
        checkReleased();
        return super.bytesBefore(index, length, indexFinder);
    }

    @Override
    public int hashCode() {
        checkReleased();
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        checkReleased();
        return super.equals(o);
    }

    @Override
    public int compareTo(ByteBuf that) {
        checkReleased();
        return super.compareTo(that);
    }

    @Override
    protected void checkReadableBytes(int minimumReadableBytes) {
        checkReleased();
        super.checkReadableBytes(minimumReadableBytes);
    }

    /**
     * Returns <code>true</code>
     */
    @Override
    public boolean isPooled() {
        return true;
    }

}
