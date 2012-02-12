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
package io.netty.buffer;

import java.nio.ByteOrder;


/**
 * A little-endian Java heap buffer.  It is recommended to use {@link ChannelBuffers#buffer(ByteOrder, int)}
 * and {@link ChannelBuffers#wrappedBuffer(ByteOrder, byte[])} instead of
 * calling the constructor explicitly.
 */
public class LittleEndianHeapChannelBuffer extends HeapChannelBuffer {

    /**
     * Creates a new little-endian heap buffer with a newly allocated byte array.
     *
     * @param length the length of the new byte array
     */
    public LittleEndianHeapChannelBuffer(int length) {
        super(length);
    }

    /**
     * Creates a new little-endian heap buffer with an existing byte array.
     *
     * @param array the byte array to wrap
     */
    public LittleEndianHeapChannelBuffer(byte[] array) {
        super(array);
    }

    private LittleEndianHeapChannelBuffer(byte[] array, int readerIndex, int writerIndex) {
        super(array, readerIndex, writerIndex);
    }

    @Override
    public ChannelBufferFactory factory() {
        return HeapChannelBufferFactory.getInstance(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public ChannelBuffer duplicate() {
        return new LittleEndianHeapChannelBuffer(array, readerIndex(), writerIndex());
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        if (index < 0 || length < 0 || index + length > array.length) {
            throw new IndexOutOfBoundsException();
        }

        byte[] copiedArray = new byte[length];
        System.arraycopy(array, index, copiedArray, 0, length);
        return new LittleEndianHeapChannelBuffer(copiedArray);
    }
    
}
