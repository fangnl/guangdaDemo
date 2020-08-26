package com.ebchinatech.util;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    public static ThreadPoolExecutor threadPool;
    static Logger logger = LogUtil.getLogger();

    public static ThreadPoolExecutor getExecutor() {
        threadPool = new ThreadPoolExecutor(5, 5, 3, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        logger.info("初始化线程池");
        return threadPool;
    }


    public static void close() {
        threadPool.shutdown();
        try {
            //5分钟
            if (!threadPool.awaitTermination(5, TimeUnit.MINUTES))
                threadPool.shutdownNow();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("关闭线程池");
    }


}
