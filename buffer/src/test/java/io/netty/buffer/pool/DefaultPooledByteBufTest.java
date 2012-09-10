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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.nio.ByteOrder;

import org.junit.After;
import org.junit.Before;

import io.netty.buffer.AbstractChannelBufferTest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DefaultPooledByteBufTest extends AbstractChannelBufferTest {

    private ByteBufPool pool;
    private ByteBuf buffer;

    @Before
    public void init() {
        pool = createPool();
        super.init();
    }


    @After
    public void dispose() {
        super.dispose();
        pool = null;
    }

    @Override
    protected ByteBuf newBuffer(int capacity) {
        buffer = pool.acquire(capacity);
        assertSame(ByteOrder.BIG_ENDIAN, buffer.order());
        assertEquals(0, buffer.writerIndex());
        return buffer;
    }

    @Override
    protected ByteBuf[] components() {
        return new ByteBuf[] { buffer };

    }

    protected ByteBufPool createPool() {
        return new UnpooledByteBufPool();
    }

    private final class UnpooledByteBufPool implements ByteBufPool {

        @Override
        public PooledByteBuf acquire(int capacity) {
            return new DefaultPooledByteBuf(this, Unpooled.buffer(capacity));
        }
        
    }
}
