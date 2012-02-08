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
package io.netty.buffer.pool;

import io.netty.buffer.ChannelBuffer;
import io.netty.util.ExternalResourceReleasable;

/**
 * A {@link ChannelBufferPool} buffers {@link ChannelBuffer} to save the overhead of creation of these. Its up to its implementation how to handle the creation. Some may prefer to create 
 * {@link ChannelBuffer}'s on demand, other may create a fixed amount and reuse them.
 * 
 * Its important that users of the {@link ChannelBufferPool} take care of call {@link #release(ChannelBuffer)} for every {@link ChannelBuffer} that was retrieved via {@link #acquire(int)}.
 * Otherwise the system <strong>deadlock</strong> because it has no {@link ChannelBuffer}'s left which are usable
 * 
 *
 */
public interface ChannelBufferPool extends ExternalResourceReleasable {

    /**
     * Acquire a {@link ChannelBuffer} which has at least the specified capacity. It's possible that the returned ChannelBuffer might be bigger,
     * so callers of this method should be prepared for it. In anyway the returned {@link ChannelBuffer} must have it's limit set to the specified
     * capacity.
     * 
     * 
     * @param capacity the capacity for the {@link ChannelBuffer}. This must be > 0
     * @return buffer the acquired {@link ChannelBuffer}
     * @throws IllegalArgumentException if given capacity < 0
     */
    ChannelBuffer acquire(int capacity) throws CouldNotAcquireException;
    
    /**
     * Release the {@link ChannelBuffer} and so make it possible to acquire it again
     * 
     * TODO: Think about moving the release method to {@link ChannelBuffer}
     * 
     * @param buf the {@link ChannelBuffer} to release
     */
    void release(ChannelBuffer buf);
}
