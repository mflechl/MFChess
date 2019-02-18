package com.mflechl.mfchess;

import java.util.*;


/**
 * Class with deepcopy helpers
 */

public class DeepCopy {

    // Constructor
    public DeepCopy() {
    }

    public static int[][] deepCopyInt(int[][] original) { //TODO: Template
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

    /* <T> T[][] deepCopy(T[][] matrix) {
		return java.util.Arrays.stream(matrix).map(el -> el.clone()).toArray($ -> matrix.clone());
    } */


}
