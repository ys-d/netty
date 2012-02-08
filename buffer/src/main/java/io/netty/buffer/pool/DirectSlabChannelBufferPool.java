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

import java.nio.ByteOrder;

/**
 * {@link AbstractSlabChannelBufferPool} which contains {@link ChannelBuffer}'s that are located in the direct memory (off-heap)
 *
 */
public class DirectSlabChannelBufferPool extends AbstractSlabChannelBufferPool {
    
    /**
     * See {@link AbstractSlabChannelBufferPool#AbstractSlabChannelBufferPool(int, int, ByteOrder, boolean)}
     */
    public DirectSlabChannelBufferPool(int blockSize, int numBlocks, ByteOrder order, boolean blockWhenSaturate) {
        super(blockSize, numBlocks, order, blockWhenSaturate);
    }

    /**
     * See {@link AbstractSlabChannelBufferPool#AbstractSlabChannelBufferPool(int, int)}
     */
    public DirectSlabChannelBufferPool(int blockSize, int numBlocks) {
        super(blockSize, numBlocks);
    }

    @Override
    protected ChannelBuffer create(ByteOrder order, int size) {
        return ChannelBuffers.directBuffer(order, size);
    }

    /**
     * Make sure all direct {@link ChannelBuffer}'s get released 
     */
    @Override
    public void releaseExternalResources() {
        for (ChannelBuffer buf: slabs) {
            // check if the channelbuffer is direct and if so destroy it
            if (buf.isDirect()) {
                try {
                    DirectBufferUtil.destroy(buf);
                } catch (Exception e) {
                    LOGGER.error("Unable to destroy direct ChannelBuffer!", e);
                } 
            }
        }
        super.releaseExternalResources();
    }



}
