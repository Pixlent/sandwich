package me.pixlent.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

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

    /* David Stafford's (http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html)
     * "Mix13" variant of the 64-bit finalizer in Austin Appleby's MurmurHash3 algorithm. */
    private static long staffordMix13(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public static long hashPath(Path path) {
        if (!Files.exists(path)) {
            return 0;
        }

        if (!Files.isDirectory(path)) {
            return hashFile(path);
        }

        try (Stream<@NotNull Path> files = Files.walk(path)) {
            return files
                    .filter(Files::isRegularFile)
                    .sorted()
                    .mapToLong(Hash::hashFile)
                    .reduce(0, Long::sum);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long hashFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024];

            int read;
            long hash = 0;

            while ((read = is.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    hash = hash ^ staffordMix13(buffer[i]);
                }
            }

            return hash;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
