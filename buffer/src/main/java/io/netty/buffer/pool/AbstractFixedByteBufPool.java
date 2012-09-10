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
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@link ByteBufPool} implementation which has a fixed number of {@link ByteBuf} to
 * use.
 *
 */
public abstract class AbstractFixedByteBufPool implements ByteBufPool {
    private final PooledByteBuf[] buffers;
    private final Queue<Integer> indexes = new ConcurrentLinkedQueue<Integer>();
    private final int maxCapacity;
    private final ByteOrder order;

    public AbstractFixedByteBufPool(int bufferCapacity, int bufferCount, ByteOrder order) {
        this.order = order;
        this.maxCapacity = bufferCapacity * bufferCount;
        this.buffers = new PooledByteBuf[bufferCount];
        for (int i = 0; i < bufferCount; i++) {
            buffers[i] = new FixedPooledByteBuf(i, createByteBuf(bufferCapacity).order(order));
            indexes.add(i);
        }
    }

    @Override
    public final ByteBuf acquire(int minCapacity) {
        if (minCapacity == 0) {
            return Unpooled.EMPTY_BUFFER;
        }
        int needed = (minCapacity / maxCapacity) + 1;
        int acquired = 0;
        int[] i = new int[needed];
        while (acquired < needed) {
            Integer a = indexes.poll();
            if (a == null) {
                for (int b = 0; b < i.length; b++) {
                    indexes.add(i[b]);
                }
                return createExtraByteBuf(minCapacity).order(order);
            } else {
                i[acquired++] = a;
            }
        }
        if (i.length == 1) {
            return buffers[i[0]];
        } else {
            PooledByteBuf[] bufs = new PooledByteBuf[i.length];
            for (int a = 0; a < i.length; a++) {
                bufs[a] = buffers[i[a]];
            }
            return Unpooled.wrappedBuffer(bufs);
        }
    }

    /**
     * Create a {@link ByteBuf} with the requested capacity.
     */
    protected abstract ByteBuf createByteBuf(int minCapacity);

    /**
     * Gets called when not enough {@link ByteBuf}'s were left to full-fill
     * the requested capacity.
     *
     * By default it delegate to {@link #createByteBuf(int)}. Sub-classes
     * may override this.
     */
    protected ByteBuf createExtraByteBuf(int minCapacity) {
        return createByteBuf(minCapacity);
    }

    private final class FixedPooledByteBuf extends DefaultPooledByteBuf {

        private final Integer index;

        FixedPooledByteBuf(Integer index, ByteBuf buf) {
            super(AbstractFixedByteBufPool.this, buf);
            this.index = index;
        }

        @Override
        protected void afterRelease() {
            indexes.add(index);
        }
    }
}
