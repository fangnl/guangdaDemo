package com.ebchinatech.util;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    public static ThreadPoolExecutor threadPool;
    static Logger logger = LogUtil.getLogger();
    static boolean flag = true;

    public static ThreadPoolExecutor getExecutor() {
        threadPool = new ThreadPoolExecutor(5, 5, 3, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        logger.info("初始化线程池");
        return threadPool;
    }

    //关闭线程池
    public synchronized static void close() {
        if (flag) {
            threadPool.shutdown();
            try {
                //10分钟
                if (!threadPool.awaitTermination(10, TimeUnit.MILLISECONDS))
                    threadPool.shutdownNow();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("关闭线程池");
            flag = false;
        }
    }
}
