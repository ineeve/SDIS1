package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

    private static ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);

    public static void waitBetween(int low, int high) {
        int milliseconds = (int) (ThreadLocalRandom.current().nextFloat() * (high - low) + low) ;
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

    public static void scheduleBetween(int low, int high, Runnable task){
        int milliseconds = (int) (ThreadLocalRandom.current().nextFloat() * (high - low) + low) ;
        scheduleFixed(milliseconds, task);
    }

    public static void scheduleFixed(int milliseconds, Runnable task){
        pool.schedule(task,milliseconds,TimeUnit.MILLISECONDS);
    }

}
