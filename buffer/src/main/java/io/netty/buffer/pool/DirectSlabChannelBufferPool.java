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
import io.netty.buffer.ChannelBuffers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * {@link AbstractSlabChannelBufferPool} which contains {@link ChannelBuffer}'s that are located in the direct memory (off-heap)
 *
 */
public class DirectSlabChannelBufferPool extends AbstractSlabChannelBufferPool {
    
    public DirectSlabChannelBufferPool(int blockSize, int numBlocks, ByteOrder order) {
        super(blockSize, numBlocks, order);
    }

    @Override
    protected ChannelBuffer create(ByteOrder order, long size) {
        return ChannelBuffers.directBuffer(order, (int) size);
    }

    @Override
    public void releaseExternalResources() {
        for (ChannelBuffer buf: slabs) {
            if (buf.isDirect()) {
                try {
                    destroyDirectByteBuffer(buf.toByteBuffer());
                } catch (Exception e) {
                    LOGGER.error("Unable to destroy direct ChannelBuffer!", e);
                } 
            }
        }
        super.releaseExternalResources();
    }

    /**
     * DirectByteBuffers are garbage collected by using a phantom reference and
     * a reference queue. Every once a while, the JVM checks the reference queue
     * and cleans the DirectByteBuffers. However, as this doesn't happen
     * immediately after discarding all references to a DirectByteBuffer, it's
     * easy to OutOfMemoryError yourself using DirectByteBuffers. This function
     * explicitly calls the Cleaner method of a DirectByteBuffer.
     * 
     * @param toBeDestroyed
     *            The DirectByteBuffer that will be "cleaned". Utilizes
     *            reflection.
     * 
     */
    private static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {

        // this should never hit here, anyway just to be safe!
        if (!toBeDestroyed.isDirect()) {
            throw new IllegalArgumentException("Given ByteBuffer is not direct");
        }

        Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
        cleanerMethod.setAccessible(true);
        Object cleaner = cleanerMethod.invoke(toBeDestroyed);
        Method cleanMethod = cleaner.getClass().getMethod("clean");
        cleanMethod.setAccessible(true);
        cleanMethod.invoke(cleaner);

    }

}
