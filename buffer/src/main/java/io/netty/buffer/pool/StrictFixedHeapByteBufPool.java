/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 0(the "License"); you may not use this file except in compliance
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

/**
 * {@link FixedHeapByteBufPool} implementation which throws a {@link PoolExhaustedException} if
 * a {@link ByteBuf} was requested but not enough capacity is left in the {@link FixedHeapByteBufPool}.
 *
 */
public class StrictFixedHeapByteBufPool extends FixedHeapByteBufPool {

    /**
     * See {@link AbstractFixedByteBufPool#AbstractFixedByteBufPool(int, int, ByteOrder)}
     */
    public StrictFixedHeapByteBufPool(int bufferCapacity, int bufferCount, ByteOrder order) {
        super(bufferCapacity, bufferCount, order);
    }

    /**
     * Throws {@link PoolExhaustedException}
     */
    @Override
    protected ByteBuf createExtraByteBuf(int minCapacity) {
        throw new PoolExhaustedException();
    }
}
