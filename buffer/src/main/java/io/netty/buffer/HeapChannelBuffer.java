/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * A skeletal implementation for Java heap buffers.
 */
public abstract class HeapChannelBuffer extends AbstractChannelBuffer {

    /**
     * The underlying heap byte array that this buffer is wrapping.
     */
    protected final byte[] array;

    /**
     * Creates a new heap buffer with a newly allocated byte array.
     *
     * @param length the length of the new byte array
     */
    public HeapChannelBuffer(int length) {
        this(new byte[length], 0, 0);
    }

    /**
     * Creates a new heap buffer with an existing byte array.
     *
     * @param array the byte array to wrap
     */
    public HeapChannelBuffer(byte[] array) {
        this(array, 0, array.length);
    }

    /**
     * Creates a new heap buffer with an existing byte array.
     *
     * @param array        the byte array to wrap
     * @param readerIndex  the initial reader index of this buffer
     * @param writerIndex  the initial writer index of this buffer
     */
    protected HeapChannelBuffer(byte[] array, int readerIndex, int writerIndex) {
        if (array == null) {
            throw new NullPointerException("array");
        }
        this.array = array;
        setIndex(readerIndex, writerIndex);
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public int capacity() {
        return array.length;
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public byte[] array() {
        return array;
    }

    @Override
    public int arrayOffset() {
        return 0;
    }

    @Override
    public byte getByte(int index) {
        return array[index];
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        if (dst instanceof HeapChannelBuffer) {
            getBytes(index, ((HeapChannelBuffer) dst).array, dstIndex, length);
        } else {
            dst.setBytes(dstIndex, array, index, length);
        }
    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        System.arraycopy(array, index, dst, dstIndex, length);
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        dst.put(array, index, Math.min(capacity() - index, dst.remaining()));
    }

    @Override
    public void getBytes(int index, OutputStream out, int length)
            throws IOException {
        out.write(array, index, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        return out.write(ByteBuffer.wrap(array, index, length));
    }

    @Override
    public void setByte(int index, int value) {
        array[index] = (byte) value;
    }

    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        if (src instanceof HeapChannelBuffer) {
            setBytes(index, ((HeapChannelBuffer) src).array, srcIndex, length);
        } else {
            src.getBytes(srcIndex, array, index, length);
        }
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        System.arraycopy(src, srcIndex, array, index, length);
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        src.get(array, index, src.remaining());
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        int readBytes = 0;
        do {
            int localReadBytes = in.read(array, index, length);
            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            }
            readBytes += localReadBytes;
            index += localReadBytes;
            length -= localReadBytes;
        } while (length > 0);

        return readBytes;
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(array, index, length);
        int readBytes = 0;

        do {
            int localReadBytes;
            try {
                localReadBytes = in.read(buf);
            } catch (ClosedChannelException e) {
                localReadBytes = -1;
            }
            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            } else if (localReadBytes == 0) {
                break;
            }
            readBytes += localReadBytes;
        } while (readBytes < length);

        return readBytes;
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return ByteBuffer.wrap(array, index, length).order(order());
    }
    

    @Override
    public short getShort(int index) {
        return ChannelBufferUtil.getShort(index, array, order());
    }

    @Override
    public int getUnsignedMedium(int index) {
        return ChannelBufferUtil.getUnsignedMedium(index, array, order());

    }

    @Override
    public int getInt(int index) {
        return ChannelBufferUtil.getInt(index, array, order());
    }

    @Override
    public long getLong(int index) {
        return ChannelBufferUtil.getLong(index, array, order());

    }

    @Override
    public void setShort(int index, int value) {
        array[index]     = ChannelBufferUtil.shortToByte(0, value, order());
        array[index + 1] = ChannelBufferUtil.shortToByte(1, value, order());
    }

    @Override
    public void setMedium(int index, int   value) {
        array[index]     = ChannelBufferUtil.mediumToByte(0, value, order());
        array[index + 1] = ChannelBufferUtil.mediumToByte(1, value, order());
        array[index + 2] = ChannelBufferUtil.mediumToByte(2, value, order());
    }

    @Override
    public void setInt(int index, int   value) {
        array[index]     = ChannelBufferUtil.intToByte(0, value, order());
        array[index + 1] = ChannelBufferUtil.intToByte(1, value, order());
        array[index + 2] = ChannelBufferUtil.intToByte(2, value, order());
        array[index + 3] = ChannelBufferUtil.intToByte(3, value, order());
    }

    @Override
    public void setLong(int index, long  value) {
        array[index]     = ChannelBufferUtil.longToByte(0, value, order());
        array[index + 1] = ChannelBufferUtil.longToByte(1, value, order());
        array[index + 2] = ChannelBufferUtil.longToByte(2, value, order());
        array[index + 3] = ChannelBufferUtil.longToByte(3, value, order());
        array[index + 4] = ChannelBufferUtil.longToByte(4, value, order());
        array[index + 5] = ChannelBufferUtil.longToByte(5, value, order());
        array[index + 6] = ChannelBufferUtil.longToByte(6, value, order());
        array[index + 7] = ChannelBufferUtil.longToByte(7, value, order());
    }

}
