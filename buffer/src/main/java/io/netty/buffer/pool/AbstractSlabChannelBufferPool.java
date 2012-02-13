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

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be positiv");
        }
        if (numBlocks <= 0) {
            throw new IllegalArgumentException("numBlocks must be positiv");
        }

        buffers = new LinkedBlockingQueue<ChannelBuffer>();
        slabs = new ConcurrentLinkedQueue<ChannelBuffer>();

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
    public ChannelBuffer acquire(int capacity) throws CouldNotAcquireException {
        if (capacity < 0) {
            // a negative capacity makes no sense so throw an exception here
            throw new IllegalArgumentException("Capacity MUST be >= 0");
        } else if (capacity == 0) {
            return ChannelBuffers.EMPTY_BUFFER;
        } else {
            try {
                // cast to double here as we need it later for the ceil
                double amount = (double)capacity / blockSize;
                
                // calculate the number of channelbuffers that are needed to acquire a buffer of the requested capacity
                int blocks = (int) Math.ceil(amount);
                
                ChannelBuffer buf;
                if (numBlocks < blocks) {
                    throw new CouldNotAcquireException("Requested capacity is bigger then the max offered by this ChannelPool");
                } else if (blocks == 1) {
                    // we only need one buffer so just get one of the queue
                    buf = new SlabChannelBuffer(nextBuffer(capacity));
                } else {
                    // more then one buffer is needed so get as many as we need and return a wrapped buffer
                    ChannelBuffer bufs[] = new ChannelBuffer[blocks];
                    
                    for (int i = 0; i < bufs.length; i++) {
                        ChannelBuffer b = nextBuffer(capacity);
                        
                        // we need to set the writerIndex to something bigger as the reader index as otherwise wrapping will not work
                        b.writerIndex(b.capacity());
                        bufs[i] = (b);
                    }

                    buf = new SlabChannelBuffer(bufs);
                    buf.writerIndex(0);
                }

                
                return buf;
                
            } catch (InterruptedException e) {
                LOGGER.error("Interupted while acquire a ChannelBuffer", e);
                Thread.currentThread().interrupt();
                
                throw new CouldNotAcquireException("Error while try to acquire ChannelBuffer", e);

            }
        }
    }

    private ChannelBuffer nextBuffer(int capacity) throws InterruptedException {
        if (blockWhenSaturate) {
            return buffers.take();
        } else {
            ChannelBuffer buf = buffers.poll();
            if (buf == null) {
                buf = create(order, capacity);
            }
            return buf;
        }
    }
    @Override
    public void release(ChannelBuffer buf) {
        if (buf instanceof SlabChannelBuffer) {
            buf.clear();

            // the ChannelBuffer is of type PooledChannelBuffer so hopefully it was acquired by this pool. Every other check would be to "slow" here
            buffers.addAll(((SlabChannelBuffer) buf).getSlabs());
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
            ChannelBuffer aSlice = newSlab.slice(j, sliceSize);
            buffers.add(aSlice);
        }
    }

    /**
     * Return the size (in bytes) of each block that is used by this {@link ChannelBufferPool} implementation to build up the Slab algorithm
     * 
     * @return blockSize
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Return the number of blocks that are offered via this {@link ChannelBufferPool} implementation
     * 
     * @return blockCapacity 
     */
    public int getBlockCapacity() {
        return numBlocks;
    }

    /**
     * Return the allocated memory of this {@link ChannelBufferPool} (in bytes)
     * 
     * @return allocatedMemory
     */
    public int getAllocatedMemory() {
        return blockSize * numBlocks;
    }
    
    /**
     * Return the amount of remaining {@link ChannelBuffer}'s which can get acquired via the {@link #acquire(long)} method
     * 
     * @return remaining the remaining {@link ChannelBuffer}'s
     */
    public int getRemaining() {
        return buffers.size();
    }

    /**
     * Create a new {@link ChannelBuffer}
     */
    protected abstract ChannelBuffer create(ByteOrder order, int capacity);

}
