package com.mflechl.mfchess;

import java.lang.reflect.Array;
import java.util.Arrays;


/**
 * Class with deepcopy helpers
 */

public class Copy {

    // Constructor
    public Copy() {
    }

    //works for int 2D arrays
    @SuppressWarnings("unused")
    public static int[][] deepCopyInt(int[][] original) {
        if (original == null) {
            return null;
        }

        final int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
            // For Java versions prior to Java 6 use the next:
            // System.arraycopy(original[i], 0, result[i], 0, original[i].length);
        }
        return result;
    }

    public static byte[][] deepCopyInt(byte[][] original) {
        if (original == null) {
            return null;
        }

        final byte[][] result = new byte[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
            // For Java versions prior to Java 6 use the next:
            // System.arraycopy(original[i], 0, result[i], 0, original[i].length);
        }
        return result;
    }

    //works for any 2D array, but is about 10x slower
    @SuppressWarnings("unused")
    public static <T> T deepArrayCopy(T array) {
        if (array == null)
            return null;

        Class<?> arrayType = array.getClass();
        if (!arrayType.isArray())
            throw new IllegalArgumentException(arrayType.toString());

        int length = Array.getLength(array);
        Class<?> componentType = arrayType.getComponentType();

        @SuppressWarnings("unchecked")
        T copy = (T) Array.newInstance(componentType, length);

        if (componentType.isArray()) {
            for (int i = 0; i < length; ++i)
                Array.set(copy, i, deepArrayCopy(Array.get(array, i)));
        } else {
            System.arraycopy(array, 0, copy, 0, length);
        }

        return copy;
    }


}
