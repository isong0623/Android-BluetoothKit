package com.dreaming.bluetooth.framework.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class ThreadUtils {
    private final static class PoolInstanceHolder{
        private static ExecutorService instance = null;
        private synchronized static ExecutorService getInstance(){
            if(instance==null){
                synchronized (PoolInstanceHolder.class){
                    if(instance == null){
                        instance = new ThreadPoolExecutor(
                                Math.max(1,Runtime.getRuntime().availableProcessors()*2),
                                Integer.MAX_VALUE,  //线程池最大量
                                1L,  //线程空闲最大时间 空闲则丢弃任务
                                TimeUnit.HOURS,
                                new LinkedBlockingQueue()
                        );
                    }
                }
            }

            return instance;
        }
    }
    private static ExecutorService getPoolInstance() { return PoolInstanceHolder.getInstance(); }

    public static void start(Runnable task){
        if(task!=null) getPoolInstance().execute(task);
    }
}
