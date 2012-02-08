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

import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.pool.ChannelBufferPool;
import io.netty.buffer.pool.CouldNotAcquireException;
import static org.junit.Assert.*;

public abstract class AbstractChannelBufferPoolTest {

    protected abstract ChannelBufferPool createPool(int minCapacity);
    
    @Test
    public void testAcquire() throws CouldNotAcquireException {
        ChannelBufferPool pool = createPool(10);
        checkPool(pool, 10);
        
    }
    
    protected void checkPool(ChannelBufferPool pool, int bufferSize) throws CouldNotAcquireException {
        ChannelBuffer buf = pool.acquire(bufferSize);
        assertNotNull(buf);
        
        // check if the capacity of the buffer is at least 10
        assertTrue(buf.capacity() >= bufferSize);
        
        pool.release(buf);
        
        pool.releaseExternalResources();
    }
}
