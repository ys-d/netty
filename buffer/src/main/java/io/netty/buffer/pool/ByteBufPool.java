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

/**
 * A Pool which is responsible to pool {@link ByteBuf} instances. The implementation
 * must take care that a {@link ByteBuf} is only acquired once until it is relased again
 * by calling {@link ByteBuf.Unsafe#release()} till {@link ByteBuf.Unsafe#references()} is
 * <code>0</code>.
 *
 */
public interface ByteBufPool {

    /**
     * Return a {@link ByteBuf} with the given minCapacity.
     * Implementations may also choose to throws {@link PoolExhaustedException}
     * if nothing is left in the {@link ByteBufPool} to full-fill the request.
     */
    ByteBuf acquire(int minCapacity);

}
