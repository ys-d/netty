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
 * Utility class for {@link ChannelBuffer}
 * 
 *
 */
public class ChannelBufferUtil {


    /**
     * Convert the given value from long to byte depending on the given index
     * 
     * @param index
     * @param value
     * @param order
     * @return byte
     */
    public static byte longToByte(int index, long value, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 7:
                return (byte) (value >>> 56);
            case 6:
                return (byte) (value >>> 48);
            case 5:
                return (byte) (value >>> 40);
            case 4:
                return (byte) (value >>> 32);
            case 3:
                return (byte) (value >>> 24);
            case 2:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 56);
            case 1:
                return (byte) (value >>> 48);
            case 2:
                return (byte) (value >>> 40);
            case 3:
                return (byte) (value >>> 32);
            case 4:
                return (byte) (value >>> 24);
            case 5:
                return (byte) (value >>> 16);
            case 6:
                return (byte) (value >>> 8);
            case 7:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");

    }
    
    /**
     * Convert the given value from int to byte depending on the given index
     * 
     * @param index
     * @param value
     * @param order
     * @return byte
     */
    public static byte intToByte(int index, int value, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 3:
                return (byte) (value >>> 24);
            case 2:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 24);
            case 1:
                return (byte) (value >>> 16);
            case 2:
                return (byte) (value >>> 8);
            case 3:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");
       
    }
    
    /**
     * Convert the given value from medium to byte depending on the given index
     * 
     * @param index
     * @param value
     * @param order
     * @return byte
     */
    public static byte mediumToByte(int index, int value, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 2:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            switch (index) {
            case 0:
                return (byte) (value >>> 16);
            case 1:
                return (byte) (value >>> 8);
            case 2:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");
        
    }
    
    /**
     * Convert the given value from short to byte depending on the given index
     * 
     * @param index
     * @param value
     * @param order
     * @return byte
     */
    public static byte shortToByte(int index, int value, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            switch (index) {
            case 1:
                return (byte) (value >>> 8);
            case 0:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        } else if (order == ByteOrder.BIG_ENDIAN) {
            
            switch (index) {
            case 0:
                return (byte) (value >>> 8);
            case 1:
                return (byte) (value >>> 0);
            default:
                throw new IllegalArgumentException();
            }
        }
        throw new RuntimeException("Unsupported ByteOrder");

       
    }
    
    
    public static long getLong(byte[] array, ByteOrder order) {
        return getLong(0, array, order);
    }

        
    /**
     * Get a long out of the given array.
     * 
     * @param index
     *            the index to start in the array
     * @param array
     *            the array to build it from
     * @param order
     *            the {@link ByteOrder}
     * @return value
     */
    public static long getLong(int index, byte[] array, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return  ((long) array[index]     & 0xff) <<  0 |
                    ((long) array[index + 1] & 0xff) <<  8 |
                    ((long) array[index + 2] & 0xff) << 16 |
                    ((long) array[index + 3] & 0xff) << 24 |
                    ((long) array[index + 4] & 0xff) << 32 |
                    ((long) array[index + 5] & 0xff) << 40 |
                    ((long) array[index + 6] & 0xff) << 48 |
                    ((long) array[index + 7] & 0xff) << 56;
        } else if (order == ByteOrder.BIG_ENDIAN) {
            return  ((long) array[index]     & 0xff) << 56 |
                    ((long) array[index + 1] & 0xff) << 48 |
                    ((long) array[index + 2] & 0xff) << 40 |
                    ((long) array[index + 3] & 0xff) << 32 |
                    ((long) array[index + 4] & 0xff) << 24 |
                    ((long) array[index + 5] & 0xff) << 16 |
                    ((long) array[index + 6] & 0xff) <<  8 |
                    ((long) array[index + 7] & 0xff) <<  0;
        } else {
            throw new RuntimeException("Unsupported ByteOrder");
        }
    }
    public static int getInt(byte[] array, ByteOrder order) {
        return getInt(0, array, order);
    }
    
    
    /**
     * Get a int out of the given array.
     * 
     * @param index
     *            the index to start in the array
     * @param array
     *            the array to build it from
     * @param order
     *            the {@link ByteOrder}
     * @return value
     */
    public static int getInt(int index, byte[] array, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return  (array[index]     & 0xff) <<  0 |
                    (array[index + 1] & 0xff) <<  8 |
                    (array[index + 2] & 0xff) << 16 |
                    (array[index + 3] & 0xff) << 24;
        } else if (order == ByteOrder.BIG_ENDIAN) {
            return  (array[index]     & 0xff) << 24 |
                    (array[index + 1] & 0xff) << 16 |
                    (array[index + 2] & 0xff) <<  8 |
                    (array[index + 3] & 0xff) <<  0;
        } else {
            throw new RuntimeException("Unsupported ByteOrder");
        }
    }
    
    public static short getShort(byte[] array, ByteOrder order) {
        return getShort(0, array, order);
    }
    
    /**
     * Get a short out of the given array.
     * 
     * @param index
     *            the index to start in the array
     * @param array
     *            the array to build it from
     * @param order
     *            the {@link ByteOrder}
     * @return value
     */
    public static short getShort(int index, byte[] array, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return (short) (array[index] & 0xFF | array[index +1] << 8);
        } else if (order == ByteOrder.BIG_ENDIAN) {
            return (short) (array[index] << 8 | array[index + 1] & 0xFF);
        } else {
            throw new RuntimeException("Unsupported ByteOrder");
        }
    }
    
    public static int getUnsignedMedium(byte[] array, ByteOrder order) {
        return getUnsignedMedium(0, array, order);
    }
    
    
    /**
     * Get a unsigned medium out of the given array.
     * 
     * @param index
     *            the index to start in the array
     * @param array
     *            the array to build it from
     * @param order
     *            the {@link ByteOrder}
     * @return value
     */
    public static int getUnsignedMedium(int index, byte[] array, ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return  (array[index]     & 0xff) <<  0 |
                    (array[index + 1] & 0xff) <<  8 |
                    (array[index + 2] & 0xff) << 16;
        } else if (order == ByteOrder.BIG_ENDIAN) {
            return  (array[index]     & 0xff) << 16 |
                    (array[index + 1] & 0xff) <<  8 |
                    (array[index + 2] & 0xff) <<  0;
        } else {
            throw new RuntimeException("Unsupported ByteOrder");
        }
        
    }
    
}
