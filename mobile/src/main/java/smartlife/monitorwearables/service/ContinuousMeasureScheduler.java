package smartlife.monitorwearables.service;

import android.os.Build;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import smartlife.monitorwearables.GBApplication;

public class ContinuousMeasureScheduler {
    private static ContinuousMeasureScheduler instance;

    //private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledThreadPoolExecutor  scheduledExecutorThreadPool;
    private static final int NUM_THREADS = 1;
    private ScheduledFuture future;

    private ContinuousMeasureScheduler(){
     //   scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorThreadPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(NUM_THREADS);
    }

    static {
        instance = new ContinuousMeasureScheduler();
    }

    public static ContinuousMeasureScheduler getInstance() {
        return instance;
    }

    public void init(int interval) {
        if (interval > 0) {
            if(scheduledExecutorThreadPool.getCompletedTaskCount() > 0){
                end();
            }
            future = scheduledExecutorThreadPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        GBApplication.deviceService().onHeartRateTest();
                        System.out.println("Thread: " + Thread.currentThread().getId());
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }, 0, interval, TimeUnit.SECONDS);
        } else {
            if(future != null) {
                boolean isCanceled = future.cancel(true);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // only for Lollipop and newer versions
                    scheduledExecutorThreadPool.setRemoveOnCancelPolicy(true);
                }
            }
        }
    }

    public void end() {
        if(future != null) {
            future.cancel(true);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // only for Lollipop and newer versions
                scheduledExecutorThreadPool.setRemoveOnCancelPolicy(true);
            }
        }
    }
}
