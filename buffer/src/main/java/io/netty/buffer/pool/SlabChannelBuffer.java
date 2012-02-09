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
package io.netty.buffer.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.AbstractChannelBuffer;
import io.netty.buffer.BigEndianHeapChannelBuffer;
import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBufferFactory;
import io.netty.buffer.ChannelBuffers;
import io.netty.buffer.HeapChannelBufferFactory;
import io.netty.buffer.LittleEndianHeapChannelBuffer;
import io.netty.buffer.SlicedChannelBuffer;
import io.netty.buffer.TruncatedChannelBuffer;

/**
 * A special {@link AbstractChannelBuffer} which provide optimized access to Slab based {@link ChannelBuffer}'s. 
 * 
 * Be aware that when you use this class you must make sure that all {@link ChannelBuffer}'s that are used in the constructor full-fill the following things:
 * <br>
 * 
 *     - Same capacity
 *     <br>
 *     - Same {@link ByteOrder}
 *     <br>
 *     - Same type, which means all must either return <code>false</code> or <code>true</code> when calling {@link #isDirect()}
 *     <br>
 *       TODO: Check if this limitation can be lifted
 * 
 * 
 *
 */
public class SlabChannelBuffer extends AbstractChannelBuffer{

    private final ByteOrder order;
    private final int capPerBuf;
    private final List<ChannelBuffer> buffers;
    private final int capacity;

    /**
     * Create a new instance of a {@link SlabChannelBuffer}. 
     * 
     * @param buffers
     */
    public SlabChannelBuffer(List<ChannelBuffer> buffers) {
        if (buffers == null) {
            throw new NullPointerException("buffers");
        } 
        if (buffers.isEmpty()) {
            throw new IllegalArgumentException("At least one ChannelBuffer must be given");
        }
        
        boolean first = true;
        int lastCap = -1;
        ByteOrder lastOrder = null;
        boolean lastDirect = false;
        
        for (ChannelBuffer buf: buffers) {
            int cap = buf.capacity();
            if (!first) {
                if (lastCap != cap) {
                    throw new IllegalArgumentException("Capacity of given ChannelBuffers must be equal");
                }
            } else {
                lastCap = cap;
            }
            
            ByteOrder order = buf.order();
            if (!first) {
                if (lastOrder != order) {
                    throw new IllegalArgumentException("ByteOrder of given ChannelBuffers must be same");
                }
            } else {
                lastOrder = order;
            }
            
            boolean direct = buf.isDirect();
            if (!first) {
                if (lastDirect != direct) {
                    throw new IllegalArgumentException("All given ChannelBuffers must be of type direct or not");
                }
            } else {
                lastDirect = direct;
            }
            
            if (first) {
                first = false;
            }
            
        }
        this.capPerBuf = lastCap;
        this.buffers = buffers;
        this.order = lastOrder;
        
        // pre-calculate the capacity
        this.capacity = capPerBuf * buffers.size();
    }
    
    @Override
    public ChannelBufferFactory factory() {
        // TODO: Should we maybe better check if we should return a direct or an heap one here ?
        return HeapChannelBufferFactory.getInstance(order());
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public ByteOrder order() {
        return order;
    }

    /**
     * Returns <code>false</code>
     */
    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public byte getByte(int index) {
        checkIndexInBounds(index, 0);
        int listIndex = getListIndex(index);
        int offset = getOffset(index);
        return buffers.get(listIndex).getByte(offset);
    }

    @Override
    public short getShort(int index) {
        checkIndexInBounds(index, 0);
        int listIndex = getListIndex(index);
        int offset = getOffset(index);
        return buffers.get(listIndex).getShort(offset);
    }

    @Override
    public int getUnsignedMedium(int index) {
        checkIndexInBounds(index, 0);
        int listIndex = getListIndex(index);
        int offset = getOffset(index);
        return buffers.get(listIndex).getUnsignedMedium(offset);
    }

    @Override
    public int getInt(int index) {
        checkIndexInBounds(index, 0);
        int listIndex = getListIndex(index);
        int offset = getOffset(index);
        return buffers.get(listIndex).getInt(offset);
    }

    @Override
    public long getLong(int index) {
        checkIndexInBounds(index, 0);
        int listIndex = getListIndex(index);
        int offset = getOffset(index);
        return buffers.get(listIndex).getLong(offset);
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, length);
        
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, dst, dstIndex, subLength);

            offset += subLength;
            dstIndex += subLength;
            written += subLength;
            subLength = Math.min(capPerBuf, length - written);
        }

    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, length);
        
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, dst, dstIndex, subLength);

            offset += subLength;
            dstIndex += subLength;
            written += subLength;
            subLength = Math.min(capPerBuf, length - written);
        }
        
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        int offset = getOffset(index);
        int length = Math.min(capacity() - index, dst.remaining());
        
        List<ChannelBuffer> subBufs = getSlabs(index, length);
        
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, dst);
            
            // check if we have remaining space in the destination ByteBuffer. If not exist here
            if (!dst.hasRemaining()) {
                return;
            }

            offset += subLength;
            written += subLength;
            subLength = Math.min(capPerBuf, length - written);
        }        
    }

    @Override
    public void getBytes(int index, OutputStream out, int length) throws IOException {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, length);
        
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, out, subLength);

            offset += subLength;
            written += subLength;
            subLength = Math.min(capPerBuf, length - written);
        }

    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return out.write(toByteBuffer(index, length));
    }

    @Override
    public void setByte(int index, int value) {
        checkIndexInBounds(index, 0);
        int listIndex = getListIndex(index);
        int offset = getOffset(index);
        buffers.get(listIndex).setByte(offset, value);
    }

    @Override
    public void setShort(int index, int value) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 2);
        if (subBufs.size() ==1) {
            subBufs.get(0).setShort(offset, value);
        } else {
            int pos = 0;
            for (ChannelBuffer buf: subBufs) {
                int writable = buf.writableBytes();
                for (int i = 0; i < writable; i++) {
                    buf.setByte(offset++, shortToByte(pos++, value));
                    if (pos == 2) {
                        return;
                    }
                }
                
            }
        }
    }

    @Override
    public void setMedium(int index, int value) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 3);
        if (subBufs.size() ==1) {
            subBufs.get(0).setMedium(offset, value);
        } else {
            int pos = 0;
            for (ChannelBuffer buf: subBufs) {
                int writable = buf.writableBytes();
                for (int i = 0; i < writable; i++) {
                    buf.setByte(offset++, mediumToByte(pos++, value));
                    if (pos == 3) {
                        return;
                    }
                }
                
            }
        }
    }

    @Override
    public void setInt(int index, int value) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 4);
        if (subBufs.size() ==1) {
            subBufs.get(0).setInt(offset, value);
        } else {
            int pos = 0;
            for (ChannelBuffer buf: subBufs) {
                int writable = buf.writableBytes();
                for (int i = 0; i < writable; i++) {
                    buf.setByte(offset++, intToByte(pos++, value));
                    if (pos == 4) {
                        return;
                    }
                }
                
            }
        }
    }

    @Override
    public void setLong(int index, long value) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 8);
        if (subBufs.size() ==1) {
            subBufs.get(0).setLong(offset, value);
        } else {
            int pos = 0;
            for (ChannelBuffer buf: subBufs) {
                int writable = buf.writableBytes();
                for (int i = 0; i < writable; i++) {
                    buf.setByte(offset++, longToByte(pos++, value));
                    if (pos == 7) {
                        return;
                    }
                }
                
            }
        }
    }

    private byte longToByte(int index, long value) {
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 56);
            case 1:
                return (byte) (value >>> 48);
            case 2:
                return (byte) (value >>> 40);
            case 3:
                return (byte) (value >>> 32);
            case 4:
                return (byte) (value >>> 24);
            case 5:
                return (byte) (value >>> 16);
            case 6:
                return (byte) (value >>> 8);
            case 7:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 7:
                return (byte) (value >>> 56);
            case 6:
                return (byte) (value >>> 48);
            case 5:
                return (byte) (value >>> 40);
            case 4:
                return (byte) (value >>> 32);
            case 3:
                return (byte) (value >>> 24);
            case 2:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");

    }
    
    private  byte intToByte(int index, int value) {
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 24);
            case 1:
                return (byte) (value >>> 16);
            case 2:
                return (byte) (value >>> 8);
            case 3:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 3:
                return (byte) (value >>> 24);
            case 2:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");
       
    }
    
    private byte mediumToByte(int index, int value) {
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 2:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 2:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");
        
    }
    
    private byte shortToByte(int index, int value) {
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 8);
            case 1:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");

       
    }
    
    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, length);
        
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.setBytes(offset, src, srcIndex, subLength);
            offset += subLength;
            written += subLength;
            subLength = Math.min(capPerBuf, length - written);
            srcIndex += written;
        }
        
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, length);
        
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.setBytes(offset, src, srcIndex, subLength);
            offset += subLength;
            written += subLength;
            subLength = Math.min(capPerBuf, length - written);
            srcIndex += written;
        }
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 0);
        
        int remain = src.remaining();
        for (ChannelBuffer buf : subBufs) {
            buf.setBytes(offset, src);
            if (!src.hasRemaining()) {
                return;
            }
            offset += (remain - src.remaining());
            remain = src.remaining();
        }
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 0);
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        
        for (ChannelBuffer buf : subBufs) {
            int i = buf.setBytes(offset, in, subLength);
            if (i == -1) {
                if (written == 0) {
                    return -1;
                } else {
                    written += i;
                    return written;
                }
            } else {
                written += i;
            }
        }
        
        
        return written;
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        int offset = getOffset(index);

        List<ChannelBuffer> subBufs = getSlabs(index, 0);
        int subLength = Math.min(capPerBuf - offset, length);
        int written = 0;
        
        for (ChannelBuffer buf : subBufs) {
            int i = buf.setBytes(offset, in, subLength);
            if (i == -1) {
                if (written == 0) {
                    return -1;
                } else {
                    written += i;
                    return written;
                }
            } else {
                written += i;
            }
        }
        
        
        return written;
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        checkIndexInBounds(index, length);
        
        
        byte[] copiedArray = new byte[length];
        getBytes(index, copiedArray);
        
        // TODO: Should we use Channels static methods here ?
        if (order() == ByteOrder.BIG_ENDIAN) {
            return new BigEndianHeapChannelBuffer(copiedArray);
        } else if (order() == ByteOrder.LITTLE_ENDIAN) {
            return new LittleEndianHeapChannelBuffer(copiedArray);
        } else {
            // This should never happen!
            throw new RuntimeException("Unkown ByteOrder");
        }
    }

    
    @Override
    public ChannelBuffer slice(int index, int length) {
        if (index == 0) {
            if (length == 0) {
                return ChannelBuffers.EMPTY_BUFFER;
            }
            if (length == capacity) {
                ChannelBuffer slice = duplicate();
                slice.setIndex(0, length);
                return slice;
            } else {
                // TODO: Should we use Channels static methods here ?
                return new TruncatedChannelBuffer(this, length);
            }
        } else {
            if (length == 0) {
                return ChannelBuffers.EMPTY_BUFFER;
            }
            // TODO: Should we use Channels static methods here ?
            return new SlicedChannelBuffer(this, index, length);
        }
    }


    @Override
    public ChannelBuffer duplicate() {
        ChannelBuffer duplicate = new SlabChannelBuffer(Arrays.asList((ChannelBuffer)this));
        duplicate.setIndex(readerIndex(), writerIndex());
        return duplicate;
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        if (buffers.size() == 1) {
            return buffers.get(0).toByteBuffer(index, length);
        }

        ByteBuffer[] buffers = toByteBuffers(index, length);
        ByteBuffer merged = ByteBuffer.allocate(length).order(order());
        for (ByteBuffer b: buffers) {
            merged.put(b);
        }
        merged.flip();
        return merged;
    }

    /**
     * Returns <code>false</code>
     */
    @Override
    public boolean hasArray() {
        return false;
    }

    /**
     * Throws {@link UnsupportedOperationException}
     */
    @Override
    public byte[] array() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws {@link UnsupportedOperationException}
     */
    @Override
    public int arrayOffset() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the index under which you can obtain the Slab {@link ChannelBuffer} that holds the given index
     * 
     * @param index
     * @return listIndex
     */
    private int getListIndex(int index) {
        int listIndex = (int) Math.floor((double)index / capPerBuf);
        if (listIndex == 1) {
            // return 0 if the listIndex was 1 here. This is needed because when we get 1 its
            // the last element in the first ChannelBuffer
            return 0;
        } else {
            return listIndex;
        }
    }
    
    /**
     * Return the offset which must be used in the Slab block
     * 
     * @param index
     * @return offset
     */
    private int getOffset(int index) {
        int off = index % capPerBuf;
        if (off == 0) {
            return index;
        }

        return off;
    }
    
    /**
     * Check if the given arguments are valid for the {@link ChannelBuffer}. If not throw an {@link IndexOutOfBoundsException}
     * 
     * @param index
     * @param length
     */
    private void checkIndexInBounds(int index, int length) {
        if (index < 0 || length < 0 || index + length > capacity) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Returns a immutable {@link List} which holds all slabs which are part of this {@link ChannelBuffer}
     * 
     * @return slabs
     */
    public List<ChannelBuffer> getSlabs() {
        return Collections.unmodifiableList(buffers);
    }
    
    /**
     * Return a {@link List} which contains all {@link ChannelBuffer}'s that are needed to be able to read the needed length from the given index.
     * 
     * This method also checks if the requested index and length are valid, and if not throws an {@link IndexOutOfBoundsException}
     * @param index
     * @param length
     * @return slabs
     */
    private List<ChannelBuffer> getSlabs(int index, int length) {
        // check if the requested index and length are valid
        checkIndexInBounds(index, length);

        int startIndex = getListIndex(index);
        // we need to add +1 to the index as subList is exclusive
        int endIndex = getListIndex(index + length) +1; 

        List<ChannelBuffer> subBufs = buffers.subList(startIndex, endIndex);
        return subBufs;
    }
}
