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

import io.netty.buffer.Unpooled;

import org.junit.After;
import org.junit.Before;

public class DefaultPooledByteBufTest extends PooledByteBufTest {

    private UnpooledByteBufPool pool;

    @Before
    public void init() {
        pool = new UnpooledByteBufPool();
        super.init();
    }

    @After
    public void dispose() {
        super.dispose();
        pool = null;
    }

    private final class UnpooledByteBufPool implements ByteBufPool {

        @Override
        public PooledByteBuf acquire(int capacity) {
            PooledByteBuf buf =  new DefaultPooledByteBuf(this, Unpooled.buffer(capacity));
            buf.unsafe().acquire();
            return buf;
        }
        
    }

    @Override
    protected PooledByteBuf createBuf(int capacity) {
        return pool.acquire(capacity);
    }

}
