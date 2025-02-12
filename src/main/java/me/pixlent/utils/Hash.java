package me.pixlent.utils;

import java.util.Arrays;
import java.util.Objects;

public class Hash {

    public static long hash(Object... objects) {
        return Arrays.stream(objects)
                .mapToInt(Objects::hashCode)
                .map(Hash::mix)
                .reduce(0, Integer::sum);
    }

    private static int mix(int x) {
        x = ((x >>> 16) ^ x) * 0x45d9f3b;
        x = ((x >>> 16) ^ x) * 0x45d9f3b;
        x = (x >>> 16) ^ x;
        return x;
    }
}
