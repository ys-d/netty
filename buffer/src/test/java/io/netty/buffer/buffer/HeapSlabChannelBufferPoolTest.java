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

import org.junit.Test;

import io.netty.buffer.pool.ChannelBufferPool;
import io.netty.buffer.pool.CouldNotAcquireException;
import io.netty.buffer.pool.HeapSlabChannelBufferPool;

public class HeapSlabChannelBufferPoolTest extends AbstractChannelBufferPoolTest{

    @Override
    protected final ChannelBufferPool createPool(int minCapacity) {
        return createSlabPool(minCapacity, 1);
    }
    
    protected ChannelBufferPool createSlabPool(int blockSize, int numBlocks) {
        return new HeapSlabChannelBufferPool(blockSize, numBlocks);
    }
    
    @Test
    public void testAcquireWithDifferentSlab() throws CouldNotAcquireException {
        ChannelBufferPool pool = createSlabPool(3,5);
        checkPool(pool, 10);
    }
}
