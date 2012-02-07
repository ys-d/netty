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

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;

/**
 * {@link AbstractSlabChannelBufferPool} which contains {@link ChannelBuffer}'s which are located in the heap
 * 
 *
 */
public class HeapSlabChannelBufferPool extends AbstractSlabChannelBufferPool {

    /**
     * See {@link AbstractSlabChannelBufferPool#AbstractSlabChannelBufferPool(int, int, ByteOrder, boolean)}
     */
    public HeapSlabChannelBufferPool(int blockSize, int numBlocks, ByteOrder order, boolean blockWhenSaturate) {
        super(blockSize, numBlocks, order, blockWhenSaturate);
    }

    /**
     * See {@link AbstractSlabChannelBufferPool#AbstractSlabChannelBufferPool(int, int)}
     */
    public HeapSlabChannelBufferPool(int blockSize, int numBlocks) {
        super(blockSize, numBlocks);
    }

    @Override
    protected ChannelBuffer create(ByteOrder order, int capacity) {
        return ChannelBuffers.buffer(order, capacity);
    }

}
