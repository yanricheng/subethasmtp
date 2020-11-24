package org.subethamail.smtp.internal.util;

import com.github.davidmoten.guavamini.Preconditions;

/**
 *
 * @author diego.salvi
 */
public class ArrayUtils {

    public static final boolean equals(byte[] a1, int from1, int to1, byte[] a2, int from2, int to2) {
        checkRange(a1.length, from1, to1);
        checkRange(a2.length, from2, to2);

        int len = to1 - from1;
        if (to2 - from2 != len) {
            return false;
        }

        while (from1 < to1) {
            if (a1[from1++] != a2[from2++]) {
                return false;
            }
        }

        return true;
    }

    private static void checkRange(int size, int from, int to) {
        Preconditions.checkArgument(from >= 0, "Invalid negative from index");
        Preconditions.checkArgument(to >= 0, "Invalid negative to index");
        Preconditions.checkArgument(to <= size, "Invaid to index too big");
    }

}
