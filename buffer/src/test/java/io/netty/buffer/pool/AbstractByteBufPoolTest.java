/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer.pool;


import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

import static junit.framework.Assert.*;

import org.junit.Test;

public abstract class AbstractByteBufPoolTest {

    @Test
    public void testSimple() {
        AbstractFixedByteBufPool pool = createPool(4, 10, ByteOrder.LITTLE_ENDIAN);
        assertEquals(10, pool.usableBuffers());

        ByteBuf buf = pool.acquire(2);
        checkBuf(buf, 4, ByteOrder.LITTLE_ENDIAN, true);
        assertEquals(1, buf.unsafe().references());
        buf.writeInt(1);
        assertEquals(9, pool.usableBuffers());

        ByteBuf buf2 = pool.acquire(2);
        checkBuf(buf2, 4, ByteOrder.LITTLE_ENDIAN, true);
        assertEquals(1, buf.unsafe().references());

        assertNotSame("Should not be the same", buf, buf2);
        
        assertEquals(8, pool.usableBuffers());
        buf.unsafe().release();
        assertEquals(9, pool.usableBuffers());
        buf2.unsafe().release();
        assertEquals(10, pool.usableBuffers());
    }

    @Test
    public void testAcquireBig() {
        AbstractFixedByteBufPool pool = createPool(4, 10, ByteOrder.LITTLE_ENDIAN);
        assertEquals(10, pool.usableBuffers());

        ByteBuf buf = pool.acquire(8);
        checkBuf(buf, 8, ByteOrder.LITTLE_ENDIAN, true);
        assertEquals(1, buf.unsafe().references());
        buf.writeInt(1);
        assertEquals(8, pool.usableBuffers());

        ByteBuf buf2 = pool.acquire(9);
        checkBuf(buf2, 9, ByteOrder.LITTLE_ENDIAN, true);
        assertEquals(1, buf.unsafe().references());

        assertNotSame("Should not be the same", buf, buf2);
        
        assertEquals(5, pool.usableBuffers());

        buf.unsafe().release();
        assertEquals(7, pool.usableBuffers());
        buf2.unsafe().release();
        assertEquals(10, pool.usableBuffers());
    }

    @Test
    public void testPoolExhausted() {
        AbstractFixedByteBufPool pool = createPool(4, 1, ByteOrder.LITTLE_ENDIAN);
        ByteBuf buf = pool.acquire(2);
        checkBuf(buf, 2, ByteOrder.LITTLE_ENDIAN, true);
        assertEquals(1, buf.unsafe().references());
        
        ByteBuf buf2 = pool.acquire(2);
        checkBuf(buf2, 2, ByteOrder.LITTLE_ENDIAN, false);
        assertEquals(1, buf.unsafe().references());
        assertNotSame("Should not be the same", buf, buf2);
    }

    private static void checkBuf(ByteBuf buf, int minSize, ByteOrder order, boolean pooled) {
        assertTrue(buf.capacity() + ">=" + minSize, buf.capacity() >= minSize);
        assertEquals(order, buf.order());
        assertEquals(pooled, buf.isPooled());
    }

    protected abstract AbstractFixedByteBufPool createPool(int bufferCapacity, int bufferCount, ByteOrder order);
}
