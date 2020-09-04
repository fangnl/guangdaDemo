package com.datah.util;

import com.datah.merge.MergeFilePath;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class MergeUtil {
    static Logger logger = LogUtil.getLogger();
    //查找文件
//    private static final String fsDefault = PropertiesUtil.getValue("fs.defaultFS.value");
    private static Configuration configuration;
    private static FileSystem fileSystem;


    public static FileSystem getFilSystem() {
        configuration = new Configuration(true);
//        configuration.set("fs.defaultFS", fsDefault);
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        try {
            fileSystem = FileSystem.get(configuration);
            logger.info("{}连接hdfs系统", fileSystem.getUri());
        } catch (IOException e) {
            logger.error("连接文件系统出现异常{}", e.getMessage());
            try {
                fileSystem.close();
            } catch (IOException ex) {
                logger.error("关闭文件系统连接失败");
                ex.printStackTrace();
            }
            e.printStackTrace();
        }

        return fileSystem;
    }


    public static void close() throws IOException {
        fileSystem.close();
        logger.info("退出连接hdfs");
    }


    public static void merge(String paths, String outPath) {
        System.setProperty("HADOOP_USER_NAME", "root");
        logger.info("输入路径:{},输出路径:{}", paths, outPath);
        MergeFilePath mergeFilePath = new MergeFilePath();
        //合并文件
        try {
            mergeFilePath.merge(paths, outPath, 0);
        } catch (Exception e) {
            logger.error("文件{}合并失败{}", paths, e.getMessage());
            e.printStackTrace();
        }
    }

    //自动加载配置文件夹的配置路径
    public static void merge() throws MergeException {
        System.setProperty("HADOOP_USER_NAME", "root");
        String intPutPath = PropertiesUtil.getValue(MergeFileConstant.FILE_INPUT_PATH);
        String outPutPath = PropertiesUtil.getValue(MergeFileConstant.FILE_OUTPUT_PATH);
        logger.info("输入路径:{},输出路径:{}", intPutPath, outPutPath);

        if (StringUtils.isEmpty(intPutPath) || StringUtils.isEmpty(outPutPath)) {
            logger.error("输入路径或者输出路径为空");
            throw new MergeException("路径错误");
        }

        String[] inPutPaths = intPutPath.split(",");
        String[] outPutPaths = outPutPath.split(",");
        if (inPutPaths.length != outPutPaths.length) {
            logger.error("输入路径于输出路径不匹配");
            throw new MergeException("路径错误");
        }
        //校验输入路径的重复性
        Set<String> outPutPatSet = new HashSet<>();
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        for (int i = 0; i < inPutPaths.length; i++) {
            outPutPatSet.add(outPutPaths[i].trim());
            map.put(inPutPaths[i].trim(), outPutPaths[i].trim());
        }

        if (inPutPaths.length != map.size() || outPutPatSet.size() != map.size()) {
            logger.error("输入路径或者输出路径相同");
            throw new MergeException("路径错误");
        }

        AtomicInteger index = new AtomicInteger(inPutPaths.length);

        ThreadPoolExecutor threadPool = ThreadPoolUtil.getExecutor();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            threadPool.execute(() -> {
                try {
                    //System.out.println(key + "->" + map.get(key));
                    MergeFilePath mergeFilePath = new MergeFilePath();
                    //合并文件
                    int i = index.decrementAndGet();
                    //  System.out.println(i);
                    mergeFilePath.merge(key, map.get(key), i);
                    logger.info("线程{}执行完毕{}", Thread.currentThread().getName(), key);
                } catch (Exception e) {
                    logger.error("线程{}合并{}失败,{}", Thread.currentThread().getName(), key, e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        //关闭线程池
       threadPool.shutdown();
       logger.info("关闭线程池");

    }

}
