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
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import io.netty.buffer.AbstractChannelBufferTest;
import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.buffer.pool.SlabChannelBuffer;

public class SlabChannelBufferTest extends AbstractChannelBufferTest{

    private ChannelBuffer buffer;
    
    @Override
    protected ChannelBuffer newBuffer(int capacity) {
        buffer = new SlabChannelBuffer(Arrays.asList(ChannelBuffers.buffer(capacity)));
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
    public void testEmptyList() {
        new SlabChannelBuffer(Collections.<ChannelBuffer>emptyList());
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void testDifferentByteOrder() {        
        new SlabChannelBuffer(Arrays.asList(ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN, 1), ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN,1)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDifferentCapacity() {        
        new SlabChannelBuffer(Arrays.asList(ChannelBuffers.buffer(1), ChannelBuffers.buffer(2)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMixingDirectAndHeap() {
        new SlabChannelBuffer(Arrays.asList(ChannelBuffers.buffer(1), ChannelBuffers.directBuffer(1)));
    }
}
