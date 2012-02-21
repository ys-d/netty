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
import java.util.List;

import io.netty.buffer.AbstractChannelBuffer;
import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBufferFactory;
import io.netty.buffer.ChannelBufferUtil;
import io.netty.buffer.HeapChannelBufferFactory;

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
    private final ChannelBuffer[] buffers;
    private final int capacity;
    private static final int LONG_SIZE = 8;
    private static final int INT_SIZE = 4;
    private static final int MEDIUM_SIZE = 3;
    private static final int SHORT_SIZE = 2;
    private static final int BYTE_SIZE = 1;
    
    /**
     * Create a new instance of a {@link SlabChannelBuffer}. 
     * 
     * @param buffers
     */
    public SlabChannelBuffer(ChannelBuffer... buffers) {
        if (buffers == null) {
            throw new NullPointerException("buffers");
        } 
        if (buffers.length == 0) {
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
        this.capacity = capPerBuf * buffers.length;
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
        checkIndexInBounds(index, BYTE_SIZE);
        int [] indexAndOffset = getIndexAndOffset(index);
        return buffers[indexAndOffset[0]].getByte(indexAndOffset[1]);
    }

    @Override
    public short getShort(int index) {
        ChannelBuffer[] bufs = slabs(index, SHORT_SIZE);
        if (bufs.length == 1) {
            int [] indexAndOffset = getIndexAndOffset(index);
            return bufs[0].getShort(indexAndOffset[1]);
        } else {
            byte[] array = new byte[SHORT_SIZE];
            getBytes(index, array);
            return ChannelBufferUtil.getShort(array, order());

        }

    }

    @Override
    public int getUnsignedMedium(int index) {
        ChannelBuffer[] bufs = slabs(index, MEDIUM_SIZE);
        if (bufs.length == 1) {
            int [] indexAndOffset = getIndexAndOffset(index);
            return bufs[0].getUnsignedMedium(indexAndOffset[1]);
        } else {
            byte[] array = new byte[MEDIUM_SIZE];
            getBytes(index, array);
            return ChannelBufferUtil.getUnsignedMedium(array, order());
            
        }
    }

    @Override
    public int getInt(int index) {
        ChannelBuffer[] bufs = slabs(index, INT_SIZE);
        if (bufs.length == 1) {
            int [] indexAndOffset = getIndexAndOffset(index);
            return bufs[0].getInt(indexAndOffset[1]);
        } else {
            byte[] array = new byte[INT_SIZE];
            getBytes(index, array);

            return ChannelBufferUtil.getInt(array, order);
        }
    }

    @Override
    public long getLong(int index) {
        ChannelBuffer[] bufs = slabs(index, LONG_SIZE);
        if (bufs.length == 1) {
            int [] indexAndOffset = getIndexAndOffset(index);

            return bufs[0].getLong(indexAndOffset[1]);
        } else {
            byte[] array = new byte[LONG_SIZE];
            getBytes(index, array);
            
            return ChannelBufferUtil.getLong(array, order);
            
        }
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        
        int subLength = length(offset, length);
        int written = 0;
        
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, dst, dstIndex, subLength);

            offset = 0;
            dstIndex += subLength;
            written += subLength;
            subLength = left(written, length);
        }

    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        if (index < 0 || dstIndex < 0 || index + length > capacity() || dstIndex + length > dst.length) {
            throw new IndexOutOfBoundsException();
        }
        
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        
        int subLength = length(offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {

            buf.getBytes(offset, dst, dstIndex, subLength);

            offset = 0;
            dstIndex += subLength;
            written += subLength;
            subLength = left(written, length);
        }
        
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        
        ChannelBuffer[] subBufs = slabs(index, dst.remaining());
        int offset = getIndexAndOffset(index)[1];
      
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, dst);
            if (dst.remaining() == 0) {
                return;
            }
            offset = 0;
        }     

    }

    @Override
    public void getBytes(int index, OutputStream out, int length) throws IOException {
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        
        int subLength = length(offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.getBytes(offset, out, subLength);

            offset = 0;
            written += subLength;
            subLength = left(written, length);
        }

    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return out.write(toByteBuffer(index, length));
    }

    @Override
    public void setByte(int index, int value) {
        checkIndexInBounds(index, BYTE_SIZE);
        int [] indexAndOffset = getIndexAndOffset(index);
        buffers[indexAndOffset[0]].setByte(indexAndOffset[1], value);
    }

    @Override
    public void setShort(int index, int value) {
        int [] indexAndOffset = getIndexAndOffset(index);

        ChannelBuffer[] subBufs = slabs(index, SHORT_SIZE);
        if (subBufs.length == 1) {
            subBufs[0].setShort(indexAndOffset[1], value);
        } else {
            // Create a new byte array that hold all bytes and set it
            byte[] bytes = new byte[SHORT_SIZE];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = ChannelBufferUtil.shortToByte(i, value, order());
            }
            setBytes(index, bytes);
        }
    }

    @Override
    public void setMedium(int index, int value) {
        int [] indexAndOffset = getIndexAndOffset(index);
        
        ChannelBuffer[] subBufs = slabs(index, MEDIUM_SIZE);

        if (subBufs.length == 1) {
            subBufs[0].setMedium(indexAndOffset[1], value);
        } else {
            // Create a new byte array that hold all bytes and set it
            byte[] bytes = new byte[MEDIUM_SIZE];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = ChannelBufferUtil.mediumToByte(i, value, order());
            }
            setBytes(index, bytes);
        }
    }

    @Override
    public void setInt(int index, int value) {
        int [] indexAndOffset = getIndexAndOffset(index);

        ChannelBuffer[] subBufs = slabs(index, INT_SIZE);
        if (subBufs.length == 1) {
            subBufs[0].setInt(indexAndOffset[1], value);
        } else {
            // Create a new byte array that hold all bytes and set it
            byte[] bytes = new byte[INT_SIZE];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = ChannelBufferUtil.intToByte(i, value, order());
            }
            setBytes(index, bytes);
        }
    }

    @Override
    public void setLong(int index, long value) {
        int [] indexAndOffset = getIndexAndOffset(index);
        ChannelBuffer[] subBufs = slabs(index, LONG_SIZE);
        if (subBufs.length ==1) {
            subBufs[0].setLong(indexAndOffset[1], value);
        } else {
            // Create a new byte array that hold all bytes and set it
            byte[] bytes = new byte[LONG_SIZE];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = ChannelBufferUtil.longToByte(i, value, order());
            }
            setBytes(index, bytes);
        }
    }

    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        
        int subLength = length(offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.setBytes(offset, src, srcIndex, subLength);
            offset = 0;
            written += subLength;
            srcIndex += subLength;

            subLength = left(written, length);
            
            if (srcIndex >= src.capacity()) {
                break;
            }
        }
        
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        
        int subLength = length(offset, length);
        int written = 0;
        for (ChannelBuffer buf : subBufs) {
            buf.setBytes(offset, src, srcIndex, subLength);
            offset = 0;
            written += subLength;
            srcIndex += subLength;

            subLength = left(written, length);
            
            if (srcIndex >= src.length) {
                break;
            }
        }
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        int offset = getIndexAndOffset(index)[1];
        int remain = src.remaining();
        int limit = src.limit();
        ChannelBuffer[] subBufs = slabs(index, remain);
        
        // set a new limit for the ByteBuffer so we not get an IndexOutOfBounds exception if the remaining bytes are more then then what a single ChannelBuffer holds
        src.limit(Math.min(limit, src.position() + capPerBuf - offset));

        for (ChannelBuffer buf : subBufs) {
            buf.setBytes(offset, src);
            
            // update the limit for the next loop
            src.limit(Math.min(limit, src.position() + capPerBuf - offset));

            if (!src.hasRemaining()) {
                // nothing left to write to, so exit here
                break;
            }
            offset = 0;
        }
        // reset the limit to the old value
        src.limit(limit);
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        int subLength = length(offset, length);
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
            offset = 0;

        }
        
        
        return written;
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        int offset = getIndexAndOffset(index)[1];

        ChannelBuffer[] subBufs = slabs(index, length);
        int subLength = length(offset, length);
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
            offset = 0;
        }
        
        
        return written;
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        checkIndexInBounds(index, length);
       
        ChannelBuffer dst = factory().getBuffer(order(), length);
        getBytes(index, dst);
        
        return dst;
    }

    @Override
    public ChannelBuffer duplicate() {
        ChannelBuffer duplicate = new SlabChannelBuffer(this);
        duplicate.setIndex(readerIndex(), writerIndex());
        return duplicate;
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        ChannelBuffer[] slabs = slabs(index, length);
        if (slabs.length == 1) {
            return slabs[0].toByteBuffer(index, length);
        } else {
            ByteBuffer merged = ByteBuffer.allocate(length).order(order());

            int[] indexAndOffset = getIndexAndOffset(index);
            int offset = indexAndOffset[1];
            int written = 0;
            for (ChannelBuffer buf: slabs) {
                int remain = merged.remaining();
                buf.getBytes(offset, merged);
                offset = 0;
                written += remain - merged.remaining();
                if (written >= length) {
                    break;
                }
            }
            merged.flip();
            return merged;
        }
        
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
    private int[] getIndexAndOffset(int index) {
        if (index < capPerBuf) {
            return new int[] {0, index};
        } else {
            int off = index % capPerBuf;
            int listIndex = index / capPerBuf;

            return new int[] {listIndex, off};
        }

    }
    
    /**
     * Calculate the maximal length to use
     * 
     * @param offset
     * @param length
     * @return
     */
    private int length(int offset, int length) {
        return Math.min(capPerBuf - offset, length);
    }
    
    
    /**
     * Calculate the amount of what is left to write.
     *  
     * @param written
     * @param length
     * @return leftToWrite
     */
    private int left(int written, int length) {
        return Math.min(capPerBuf, length - written);
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
        return Arrays.asList(buffers);
    }
    
    /**
     * Return an array which contains all {@link ChannelBuffer}'s that are needed to be able to read the needed length from the given index.
     * 
     * This method also checks if the requested index and length are valid, and if not throws an {@link IndexOutOfBoundsException}
     * @param index
     * @param length
     * @return slabs
     */
    private ChannelBuffer[] slabs(int index, int length) {
        // check if the requested index and length are valid
        checkIndexInBounds(index, length);

        int startIndex = getIndexAndOffset(index)[0];
        
        // we need to add +1 to the index as subList is exclusive
        int endIndex =  getIndexAndOffset(index + length +1)[0]; 

        
        if (endIndex > buffers.length) {
            endIndex = buffers.length -1;
        }

        
        int arrayLength = endIndex - startIndex;
        if (arrayLength == 0) {
            return new ChannelBuffer[] {buffers[endIndex]};
        } else {
            if (arrayLength >= buffers.length) {
                // We need all buffers to full fill the requested slabs.
                // So we can just return the stored array and not need to create a new one
                return buffers;
            } else {
                // Create a new array which holds all needed ChannelBuffers
                ChannelBuffer[] slabs = new ChannelBuffer[arrayLength];
                for(int i = 0; i < slabs.length; startIndex++) {
                    slabs[i++] = buffers[startIndex];
                }
                return slabs;
            }
        }
        
      

    }
    
    /**
     * Return an immutable {@link List} that holds the requested {@link ChannelBuffer}'s
     * 
     * @param index
     * @param length
     * @return slabs
     */
    public List<ChannelBuffer> getSlabs(int index, int length) {
        return Arrays.asList(slabs(index, length));
    }


    @Override
    public ByteBuffer[] toByteBuffers(int index, int length) {        
        ChannelBuffer[] slabs = slabs(index, length);
        int[] indexAndOffset = getIndexAndOffset(index);
        int offset = indexAndOffset[1];
        if (slabs.length == 1) {
            return slabs[0].toByteBuffers(offset, length);
        } else {
            int len = length;
            ByteBuffer[] buffers = new ByteBuffer[slabs.length];
            for (int i = 0; i < buffers.length; i++) {
                int subLength = Math.min(len, capPerBuf);

                buffers[i] = slabs[i].toByteBuffer(offset, subLength);
                len -= subLength;
                offset = 0;
            }
            
            return buffers;
        }
        
    }
}
