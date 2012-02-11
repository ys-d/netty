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

import java.nio.ByteOrder;
import java.util.List;

import org.junit.Test;

import io.netty.buffer.AbstractChannelBufferTest;
import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.buffer.pool.SlabChannelBuffer;

public class SlabChannelBufferTest extends AbstractChannelBufferTest{

    private SlabChannelBuffer buffer;
    
    @Override
    protected ChannelBuffer newBuffer(int capacity) {
        buffer = new SlabChannelBuffer(ChannelBuffers.buffer(capacity));
        return buffer;
    }

    @Override
    protected ChannelBuffer[] components() {
        return new ChannelBuffer[] {buffer};
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        new SlabChannelBuffer(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyArray() {
        new SlabChannelBuffer(new ChannelBuffer[0]);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void testDifferentByteOrder() {        
        new SlabChannelBuffer(ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN, 1), ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN,1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDifferentCapacity() {        
        new SlabChannelBuffer(ChannelBuffers.buffer(1), ChannelBuffers.buffer(2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMixingDirectAndHeap() {
        new SlabChannelBuffer(ChannelBuffers.buffer(1), ChannelBuffers.directBuffer(1));
    }
    
    @Test
    public void testGetSlabs() {
        List<ChannelBuffer> buffers = buffer.getSlabs(0, buffer.capacity());
        assertEquals(buffers.size(), 1);
        assertEquals(buffers.get(0).capacity(), buffer.capacity());
    }
}
