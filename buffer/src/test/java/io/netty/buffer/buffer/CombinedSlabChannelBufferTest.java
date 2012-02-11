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


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.netty.buffer.BigEndianHeapChannelBuffer;
import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.pool.SlabChannelBuffer;

public class CombinedSlabChannelBufferTest extends SlabChannelBufferTest{
    private SlabChannelBuffer buffer;
    
    @Override
    protected ChannelBuffer newBuffer(int capacity) {
        int bufferSize = 2;
        ChannelBuffer[] buffers = new ChannelBuffer[capacity / bufferSize];
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = new BigEndianHeapChannelBuffer(new byte[bufferSize]);
        }
        
        buffer = new SlabChannelBuffer(buffers);
        return buffer;
    }

    @Override
    protected ChannelBuffer[] components() {
        return new ChannelBuffer[] {buffer};
    }
    
    @Test
    public void testGetSlabs() {
        List<ChannelBuffer> buffers = buffer.getSlabs(0, buffer.capacity());
        assertEquals(buffer.capacity() / 2, buffers.size());
        for (ChannelBuffer buf: buffers) {
            assertEquals(2, buf.capacity());
        }
        
        buffers = buffer.getSlabs(0, buffer.capacity() -1);
        assertEquals(buffer.capacity() / 2, buffers.size());
        for (ChannelBuffer buf: buffers) {
            assertEquals(2, buf.capacity());
        }
        buffers = buffer.getSlabs(0, 1);
        assertEquals(1, buffers.size());
        assertEquals(2, buffers.get(0).capacity());
        
        buffers = buffer.getSlabs(1, 4);
        assertEquals(3, buffers.size());
        assertEquals(2, buffers.get(0).capacity());
    }
}
