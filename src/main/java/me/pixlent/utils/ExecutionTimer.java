package me.pixlent.utils;

/**
 * A utility class to time how long something takes to execute
 * Initialize the object to begin, and run {@link #finished()}
 */
public class ExecutionTimer {
    private final long startTime = System.nanoTime();

    /**
     * Run method when your execution has finished
     *
     * @return The time used for execution in milliseconds (with decimals)
     */
    public double finished() {
        return (System.nanoTime() - startTime) / 1_000_000.0;
    }
}