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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Utility class which helps to deal with direct allocated {@link ChannelBuffer}'s and {@link ByteBuffer}'s
 * 
 * This is copied from the elasticsearch source.
 *
 */
public class DirectBufferUtil {

    public static final boolean CLEAN_SUPPORTED;
    private static final Method directBufferCleaner;
    private static final Method directBufferCleanerClean;

    static {
        Method directBufferCleanerX = null;
        Method directBufferCleanerCleanX = null;
        boolean supported;
        try {
            directBufferCleanerX = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner");
            directBufferCleanerX.setAccessible(true);
            directBufferCleanerCleanX = Class.forName("sun.misc.Cleaner").getMethod("clean");
            directBufferCleanerCleanX.setAccessible(true);
            supported = true;
        } catch (Exception e) {
            supported = false;
        }
        CLEAN_SUPPORTED = supported;
        directBufferCleaner = directBufferCleanerX;
        directBufferCleanerClean = directBufferCleanerCleanX;
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
     *            The {@link ByteBuffer} that will be "cleaned". Utilizes
     *            reflection.
     * 
     */
    public static void destroy(ByteBuffer toBeDestroyed) {
        if (CLEAN_SUPPORTED && toBeDestroyed.isDirect()) {
            try {
                Object cleaner = directBufferCleaner.invoke(toBeDestroyed);
                directBufferCleanerClean.invoke(cleaner);
            } catch (Exception e) {
                // silently ignore exception
            }
        }

    }
    

    /**
     * See {@link #destroyDirectByteBuffer(ByteBuffer)}
     * 
     * @param toBeDestroyed
     *            The {@link ChannelBuffer} which was created by {@link ChannelBuffers#directBuffer(int)} and should be destroyed. Utilizes
     *            reflection.
     * 
     */
    public static void destroy(ChannelBuffer toBeDestroyed) {
        destroy(toBeDestroyed.toByteBuffer());
    }
}
