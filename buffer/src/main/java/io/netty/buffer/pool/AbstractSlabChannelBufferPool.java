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

import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.buffer.CompositeChannelBuffer;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

public abstract class AbstractSlabChannelBufferPool implements ChannelBufferPool {

    private final LinkedBlockingQueue<ChannelBuffer> buffers;

    protected final ConcurrentLinkedQueue<ChannelBuffer> slabs;

    protected static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractSlabChannelBufferPool.class);
    
    private final int blockSize;
    private final int numBlocks;
    private final ByteOrder order;
    
    AbstractSlabChannelBufferPool(int blockSize, int numBlocks, ByteOrder order) {
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
    public ChannelBuffer acquire(long capacity) {
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
                    buf = buffers.take();
                } else {
                    // more then one buffer is needed so get as many as we need and return a wrapped buffer
                    ChannelBuffer bufs[] = new ChannelBuffer[blocks];
                    for (int i = 0; i < blocks; i++) {
                        ChannelBuffer b = buffers.take();
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
    private void allocateAndSlice(long capacity, int sliceSize) {
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
    protected abstract ChannelBuffer create(ByteOrder order, long capacity);

}
