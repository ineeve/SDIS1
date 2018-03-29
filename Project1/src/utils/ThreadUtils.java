package utils;

import java.util.concurrent.ThreadLocalRandom;

public class ThreadUtils {

    public static void waitBetween(int low, int high) {
        int milliseconds = (int) Math.abs(ThreadLocalRandom.current().nextFloat() * (high - low) + low)  ;
        waitFixed(milliseconds);
    }
    public static void waitFixed(int milliseconds){
        try {
            java.lang.Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.out.println("Thread Interrupted");
            java.lang.Thread.currentThread().interrupt();
        }
    }
}
