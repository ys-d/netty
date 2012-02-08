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
package io.netty.buffer.buffer;

import java.nio.ByteOrder;

import org.junit.Test;

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.pool.AbstractSlabChannelBufferPool;
import io.netty.buffer.pool.ChannelBufferPool;
import io.netty.buffer.pool.CouldNotAcquireException;
import io.netty.buffer.pool.HeapSlabChannelBufferPool;

import static org.junit.Assert.*;


public class HeapSlabChannelBufferPoolTest extends AbstractChannelBufferPoolTest{

    @Override
    protected final ChannelBufferPool createPool(int minCapacity) {
        return createSlabPool(minCapacity, 1, ByteOrder.nativeOrder(), true);
    }
    
    protected AbstractSlabChannelBufferPool createSlabPool(int blockSize, int numBlocks,ByteOrder order, boolean blockWhenSaturate) {
        return new HeapSlabChannelBufferPool(blockSize, numBlocks, order, blockWhenSaturate);
    }
    
    @Test
    public void testAcquireWithDifferentSlab() throws CouldNotAcquireException {
        ChannelBufferPool pool = createSlabPool(3,5, ByteOrder.nativeOrder(), true);
        checkPool(pool, 10);
    }
    
    @Test
    public void testStats() throws CouldNotAcquireException {
        AbstractSlabChannelBufferPool pool = createSlabPool(3,5, ByteOrder.nativeOrder(), true);
        assertEquals(3*5, pool.getAllocatedMemory());
        assertEquals(5, pool.getRemaining());
        assertEquals(5, pool.getBlockCapacity());
        assertEquals(3, pool.getBlockSize());
        
        ChannelBuffer buf = pool.acquire(2);
        assertEquals(4, pool.getRemaining());
        pool.release(buf);
        assertEquals(5, pool.getRemaining());

        buf = pool.acquire(4);
        assertEquals(3, pool.getRemaining());
        pool.release(buf);
        
        // TODO: Fix the code that let the test fail
        assertEquals(5, pool.getRemaining());

        pool.releaseExternalResources();
    }
    
    @Test
    public void testNotBlock() throws CouldNotAcquireException {
        AbstractSlabChannelBufferPool pool = createSlabPool(1,1, ByteOrder.nativeOrder(), false);
        pool.acquire(1);
        pool.acquire(1);
        
        pool.releaseExternalResources();

    }
}
