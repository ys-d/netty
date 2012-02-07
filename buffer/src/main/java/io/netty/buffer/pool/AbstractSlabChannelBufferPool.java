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
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBufferFactory;
import io.netty.buffer.ChannelBufferIndexFinder;
import io.netty.buffer.ChannelBuffers;
import io.netty.buffer.CompositeChannelBuffer;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

/**
 * This an abstract base class for {@link ChannelBufferPool} implementations that use Slab to manage the pooled {@link ChannelBuffer} instances.
 * 
 *
 */
public abstract class AbstractSlabChannelBufferPool implements ChannelBufferPool {

    private final LinkedBlockingQueue<ChannelBuffer> buffers;

    protected final ConcurrentLinkedQueue<ChannelBuffer> slabs;

    protected static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractSlabChannelBufferPool.class);
    
    private final int blockSize;
    private final int numBlocks;
    private final ByteOrder order;

    private final boolean blockWhenSaturate;

    /**
     *  
     * Calss {@link #AbstractSlabChannelBufferPool(int, int, ByteOrder, boolean)} with {@link ByteOrder#nativeOrder()} and <code>true</code> as last arguments
     */
    AbstractSlabChannelBufferPool(int blockSize, int numBlocks) {
        this(blockSize, numBlocks, ByteOrder.nativeOrder(), true);
    }

        
    /**
     * Construct a {@link ChannelBufferPool} implementation which use Slab to keep the memory fragmentation and GC to a minimum. 
     * 
     * @param blockSize
     * @param numBlocks
     * @param order
     * @param blockWhenSaturate
     */
    AbstractSlabChannelBufferPool(int blockSize, int numBlocks, ByteOrder order, boolean blockWhenSaturate) {
      buffers = new LinkedBlockingQueue<ChannelBuffer>();
      slabs = new ConcurrentLinkedQueue<ChannelBuffer>();
      if (blockSize <= 0) {
          throw new IllegalArgumentException("blockSize must be positiv");
      }
      if (numBlocks <= 0) {
          throw new IllegalArgumentException("numBlocks must be positiv");
      }
      this.blockSize = blockSize;
      this.numBlocks = numBlocks;
      this.order = order;
      this.blockWhenSaturate = blockWhenSaturate;

      int maxBlocksPerSlab = Integer.MAX_VALUE / blockSize;
      int maxSlabSize = maxBlocksPerSlab * blockSize;

      int numFullSlabs = numBlocks / maxBlocksPerSlab;
      int partialSlabSize = (numBlocks % maxBlocksPerSlab) * blockSize;
     
      // allocate the full slabs
      for (int i = 0; i < numFullSlabs; i++) {
        allocateAndSlice(maxSlabSize, blockSize);
      }

      // allocate the last partial slab if needed
      if (partialSlabSize > 0) {
        allocateAndSlice(partialSlabSize, blockSize);
      }
    }
    
    @Override
    public ChannelBuffer acquire(int capacity) {
        if (capacity < 0) {
            // a negative capacity makes no sense so throw an exception here
            throw new IllegalArgumentException("Capacity MUST be >= 0");
        } else if (capacity == 0) {
            return ChannelBuffers.EMPTY_BUFFER;
        } else {
            try {
                
                // calculate the number of channelbuffers that are needed to acquire a buffer of the requested capacity
                int blocks = Math.round(blockSize / capacity);
                ChannelBuffer buf;
                if (blocks == 1) {
                    // we only need one buffer so just get one of the queue
                    buf = nextBuffer(capacity);
                } else {
                    // more then one buffer is needed so get as many as we need and return a wrapped buffer
                    ChannelBuffer bufs[] = new ChannelBuffer[blocks];
                    for (int i = 0; i < blocks; i++) {
                        ChannelBuffer b = nextBuffer(capacity);
                        b.clear();
                        bufs[i] = b;
                    }
                    buf =  ChannelBuffers.wrappedBuffer(bufs);
                }
                
                
                return buf;
                
            } catch (InterruptedException e) {
                LOGGER.error("Interupted while acquire a ChannelBuffer", e);
                Thread.currentThread().interrupt();
                
                // maybe we should better return an EMTPY buffer here ?
                return ChannelBuffers.buffer((int) capacity);
            }
        }
    }

    private ChannelBuffer nextBuffer(int capacity) throws InterruptedException {
        if (blockWhenSaturate) {
            return buffers.take();
        } else {
            ChannelBuffer buf = buffers.poll();
            if (buf == null) {
                buf = nextBuffer(capacity);
            }
            return buf;
        }
    }
    @Override
    public void release(ChannelBuffer buf) {
        if (buf instanceof PooledChannelBuffer) {
            // the ChannelBuffer is of type PooledChannelBuffer so hopefully it was acquired by this pool. Every other check would be to "slow" here
            buffers.add(buf);
        } else if (buf instanceof CompositeChannelBuffer) {
            // We have a composite buffer here, so decompose it and try to release the contained buffers
            List<ChannelBuffer> buffers = ((CompositeChannelBuffer) buf).decompose(0, buf.capacity());
            for (ChannelBuffer cbuf: buffers) {
                
                // try release again for every buffer
                release(cbuf);
            }
        }
    }
    
   

    @Override
    public void releaseExternalResources() {
        buffers.clear();
        slabs.clear();
    }

    /**
     * Create a new {@link ChannelBuffer} with the given capacity and use it to slice it in many {@link ChannelBuffer}'s, which will
     * be used later when calling {@link #acquire(long)} 
     * 
     * @param capacity
     * @param sliceSize
     */
    private void allocateAndSlice(int capacity, int sliceSize) {
      ChannelBuffer newSlab = create(order, capacity);
      slabs.add(newSlab);
      for (int j = 0; j < newSlab.capacity(); j += sliceSize) {
        ChannelBuffer aSlice = newSlab.slice(j, j + sliceSize);
        buffers.add(new PooledChannelBuffer(aSlice));
      }
    }
    
    public int getBlockSize() {
      return this.blockSize;
    }

    public int getBlockCapacity() {
      return this.numBlocks;
    }

    /**
     * Return the amount of remaining {@link ChannelBuffer}'s which can get acquired via the {@link #acquire(long)} method
     * 
     * @return remaining the remaining {@link ChannelBuffer}'s
     */
    public int getRemaining() {
      return this.buffers.size();
    }

    /**
     * Create a new {@link ChannelBuffer}
     */
    protected abstract ChannelBuffer create(ByteOrder order, int capacity);

    
    private static final class PooledChannelBuffer implements ChannelBuffer{

        private final ChannelBuffer buf;

        public PooledChannelBuffer(ChannelBuffer buf) {
            this.buf = buf;
        }
        
        @Override
        public ChannelBufferFactory factory() {
            return buf.factory();
        }

        @Override
        public int capacity() {
            return buf.capacity();
        }

        @Override
        public ByteOrder order() {
            return buf.order();
        }

        @Override
        public boolean isDirect() {
            return buf.isDirect();
        }

        @Override
        public int readerIndex() {
            return buf.readerIndex();
        }

        @Override
        public void readerIndex(int readerIndex) {
            buf.readerIndex(readerIndex);
        }

        @Override
        public int writerIndex() {
            return buf.writerIndex();
        }

        @Override
        public void writerIndex(int writerIndex) {
            buf.writerIndex(writerIndex);
        }

        @Override
        public void setIndex(int readerIndex, int writerIndex) {
            buf.setIndex(readerIndex, writerIndex);
        }

        @Override
        public int readableBytes() {
            return buf.readableBytes();
        }

        @Override
        public int writableBytes() {
            return buf.writableBytes();
        }

        @Override
        public boolean readable() {
            return buf.readable();
        }

        @Override
        public boolean writable() {
            return buf.writable();
        }

        @Override
        public void clear() {
            buf.clear();
        }

        @Override
        public void markReaderIndex() {
            buf.markReaderIndex();
        }

        @Override
        public void resetReaderIndex() {
            buf.resetReaderIndex();
        }

        @Override
        public void markWriterIndex() {
            buf.markWriterIndex();
        }

        @Override
        public void resetWriterIndex() {
            buf.resetWriterIndex();
        }

        @Override
        public void discardReadBytes() {
            buf.discardReadBytes();
        }

        @Override
        public void ensureWritableBytes(int writableBytes) {
            buf.ensureWritableBytes(writableBytes);
        }

        @Override
        public boolean getBoolean(int index) {
            return buf.getBoolean(index);
        }

        @Override
        public byte getByte(int index) {
            return buf.getByte(index);
        }

        @Override
        public short getUnsignedByte(int index) {
            return buf.getUnsignedByte(index);
        }

        @Override
        public short getShort(int index) {
            return buf.getShort(index);
        }

        @Override
        public int getUnsignedShort(int index) {
            return buf.getUnsignedShort(index);
        }

        @Override
        public int getMedium(int index) {
            return buf.getMedium(index);
        }

        @Override
        public int getUnsignedMedium(int index) {
            return buf.getUnsignedMedium(index);
        }

        @Override
        public int getInt(int index) {
            return buf.getInt(index);
        }

        @Override
        public long getUnsignedInt(int index) {
            return buf.getUnsignedInt(index);
        }

        @Override
        public long getLong(int index) {
            return buf.getLong(index);
        }

        @Override
        public char getChar(int index) {
            return buf.getChar(index);
        }

        @Override
        public float getFloat(int index) {
            return buf.getFloat(index);
        }

        @Override
        public double getDouble(int index) {
            return buf.getDouble(index);
        }

        @Override
        public void getBytes(int index, ChannelBuffer dst) {
            buf.getBytes(index, dst);
        }

        @Override
        public void getBytes(int index, ChannelBuffer dst, int length) {
            buf.getBytes(index, dst, length);
        }

        @Override
        public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
            buf.getBytes(index, dst, dstIndex, length);
        }

        @Override
        public void getBytes(int index, byte[] dst) {
            buf.getBytes(index, dst);
            
        }

        @Override
        public void getBytes(int index, byte[] dst, int dstIndex, int length) {
            buf.getBytes(index, dst, dstIndex, length);
            
        }

        @Override
        public void getBytes(int index, ByteBuffer dst) {
            buf.getBytes(index, dst);
            
        }

        @Override
        public void getBytes(int index, OutputStream out, int length) throws IOException {
            buf.getBytes(index, out, length);
            
        }

        @Override
        public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
            return buf.getBytes(index, out, length);
        }

        @Override
        public void setBoolean(int index, boolean value) {
            buf.setBoolean(index, value);
            
        }

        @Override
        public void setByte(int index, int value) {
            buf.setByte(index, value);
            
        }

        @Override
        public void setShort(int index, int value) {
            buf.setShort(index, value);
            
        }

        @Override
        public void setMedium(int index, int value) {
            buf.setMedium(index, value);
            
        }

        @Override
        public void setInt(int index, int value) {
            buf.setInt(index, value);
            
        }

        @Override
        public void setLong(int index, long value) {
            buf.setLong(index, value);
            
        }

        @Override
        public void setChar(int index, int value) {
            buf.setChar(index, value);
            
        }

        @Override
        public void setFloat(int index, float value) {
            buf.setFloat(index, value);
            
        }

        @Override
        public void setDouble(int index, double value) {
            buf.setDouble(index, value);
            
        }

        @Override
        public void setBytes(int index, ChannelBuffer src) {
            buf.setBytes(index, src);
        }

        @Override
        public void setBytes(int index, ChannelBuffer src, int length) {
            buf.setBytes(index, src, length);
        }

        @Override
        public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
            buf.setBytes(index, src, srcIndex, length);
            
        }

        @Override
        public void setBytes(int index, byte[] src) {
            buf.setBytes(index, src);
            
        }

        @Override
        public void setBytes(int index, byte[] src, int srcIndex, int length) {
            buf.setBytes(index, src, srcIndex, length);
            
        }

        @Override
        public void setBytes(int index, ByteBuffer src) {
            buf.setBytes(index, src);
            
        }

        @Override
        public int setBytes(int index, InputStream in, int length) throws IOException {
            return buf.setBytes(index, in, length);
        }

        @Override
        public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
            return buf.setBytes(index, in, length);
        }

        @Override
        public void setZero(int index, int length) {
            buf.setZero(index, length);
            
        }

        @Override
        public boolean readBoolean() {
            return buf.readBoolean();
        }

        @Override
        public byte readByte() {
            return buf.readByte();

        }

        @Override
        public short readUnsignedByte() {
            return buf.readUnsignedByte();

        }

        @Override
        public short readShort() {
            return buf.readShort();

        }

        @Override
        public int readUnsignedShort() {
            return buf.readUnsignedShort();

        }

        @Override
        public int readMedium() {
            return buf.readMedium();

        }

        @Override
        public int readUnsignedMedium() {
            return buf.readUnsignedMedium();

        }

        @Override
        public int readInt() {
            return buf.readInt();

        }

        @Override
        public long readUnsignedInt() {
            return buf.readUnsignedInt();

        }

        @Override
        public long readLong() {
            return buf.readLong();

        }

        @Override
        public char readChar() {
            return buf.readChar();

        }

        @Override
        public float readFloat() {
            return buf.readFloat();

        }

        @Override
        public double readDouble() {
            return buf.readDouble();

        }

        @Override
        public ChannelBuffer readBytes(int length) {
            return buf.readBytes(length);

        }

        @Override
        public ChannelBuffer readSlice(int length) {
            return buf.readSlice(length);

        }

        @Override
        public void readBytes(ChannelBuffer dst) {
            buf.readBytes(dst);
            
        }

        @Override
        public void readBytes(ChannelBuffer dst, int length) {
            buf.readBytes(dst, length);
        }

        @Override
        public void readBytes(ChannelBuffer dst, int dstIndex, int length) {
            buf.readBytes(dst, dstIndex, length);
            
        }

        @Override
        public void readBytes(byte[] dst) {
            buf.readBytes(dst);
            
        }

        @Override
        public void readBytes(byte[] dst, int dstIndex, int length) {
            buf.readBytes(dst, dstIndex, length);
            
        }

        @Override
        public void readBytes(ByteBuffer dst) {
            buf.readBytes(dst);
            
        }

        @Override
        public void readBytes(OutputStream out, int length) throws IOException {
            buf.readBytes(out, length);
            
        }

        @Override
        public int readBytes(GatheringByteChannel out, int length) throws IOException {
            return buf.readBytes(out, length);

        }

        @Override
        public void skipBytes(int length) {
            buf.skipBytes(length);
            
        }

        @Override
        public void writeBoolean(boolean value) {
            buf.writeBoolean(value);
        }

        @Override
        public void writeByte(int value) {
            buf.writeByte(value);
            
        }

        @Override
        public void writeShort(int value) {
            buf.writeShort(value);
            
        }

        @Override
        public void writeMedium(int value) {
            buf.writeMedium(value);
            
        }

        @Override
        public void writeInt(int value) {
            buf.writeInt(value);
            
        }

        @Override
        public void writeLong(long value) {
            buf.writeLong(value);
            
        }

        @Override
        public void writeChar(int value) {
            buf.writeChar(value);
            
        }

        @Override
        public void writeFloat(float value) {
            buf.writeFloat(value);
        }

        @Override
        public void writeDouble(double value) {
            buf.writeDouble(value);
            
        }

        @Override
        public void writeBytes(ChannelBuffer src) {
            buf.writeBytes(src);
            
        }

        @Override
        public void writeBytes(ChannelBuffer src, int length) {
            buf.writeBytes(src, length);
            
        }

        @Override
        public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
            buf.writeBytes(src, srcIndex, length);
            
        }

        @Override
        public void writeBytes(byte[] src) {
            buf.writeBytes(src);
            
        }

        @Override
        public void writeBytes(byte[] src, int srcIndex, int length) {
            buf.writeBytes(src, srcIndex, length);
            
        }

        @Override
        public void writeBytes(ByteBuffer src) {
            buf.writeBytes(src);
            
        }

        @Override
        public int writeBytes(InputStream in, int length) throws IOException {
            return buf.writeBytes(in, length);
        }

        @Override
        public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
            return buf.writeBytes(in, length);
        }

        @Override
        public void writeZero(int length) {
            buf.writeZero(length);
            
        }

        @Override
        public int indexOf(int fromIndex, int toIndex, byte value) {
            return buf.indexOf(fromIndex, toIndex, value);
        }

        @Override
        public int indexOf(int fromIndex, int toIndex, ChannelBufferIndexFinder indexFinder) {
            return buf.indexOf(fromIndex, toIndex, indexFinder);
        }

        @Override
        public int bytesBefore(byte value) {
            return buf.bytesBefore(value);
        }

        @Override
        public int bytesBefore(ChannelBufferIndexFinder indexFinder) {
            return buf.bytesBefore(indexFinder);

        }

        @Override
        public int bytesBefore(int length, byte value) {
            return buf.bytesBefore(length, value);
        }

        @Override
        public int bytesBefore(int length, ChannelBufferIndexFinder indexFinder) {
            return buf.bytesBefore(length, indexFinder);
        }

        @Override
        public int bytesBefore(int index, int length, byte value) {
            return buf.bytesBefore(index, length, value);
        }

        @Override
        public int bytesBefore(int index, int length, ChannelBufferIndexFinder indexFinder) {
            return buf.bytesBefore(index, length, indexFinder);
        }

        @Override
        public ChannelBuffer copy() {
            return buf.copy();
        }

        @Override
        public ChannelBuffer copy(int index, int length) {
            return buf.copy(index, length);
        }

        @Override
        public ChannelBuffer slice() {
            return buf.slice();
        }

        @Override
        public ChannelBuffer slice(int index, int length) {
            return buf.slice(index, length);
        }

        @Override
        public ChannelBuffer duplicate() {
            return buf.duplicate();

        }

        @Override
        public ByteBuffer toByteBuffer() {
            return buf.toByteBuffer();

        }

        @Override
        public ByteBuffer toByteBuffer(int index, int length) {
            return buf.toByteBuffer(index, length);

        }

        @Override
        public ByteBuffer[] toByteBuffers() {
            return buf.toByteBuffers();
        }

        @Override
        public ByteBuffer[] toByteBuffers(int index, int length) {
            return buf.toByteBuffers();
        }

        @Override
        public boolean hasArray() {
            return buf.hasArray();
        }

        @Override
        public byte[] array() {
            return buf.array();
        }

        @Override
        public int arrayOffset() {
            return buf.arrayOffset();
        }

        @Override
        public String toString(Charset charset) {
            return buf.toString(charset);
        }

        @Override
        public String toString(int index, int length, Charset charset) {
            return buf.toString(index, length, charset);
        }

        @Override
        public int compareTo(ChannelBuffer buffer) {
            return buf.compareTo(buffer);
        }
    }
}
