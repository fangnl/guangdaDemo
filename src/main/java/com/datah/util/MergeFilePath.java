package com.datah.util;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

public class MergeFilePath {
    //driver的内存 --driver-memory
    private String driverMemory;
    //每个executor 内存 --executor-memory
    private String executorMemory;
    //每个executor 核数 --executor-cores
    private String executorCores;
    //一共几个executor  --num-executors
    private int numExecutors=3;
    //并行度的设置 spark.default.parallelism
    private String parallelism;
    // spark.executor.memoryOverhead 对executor堆内存的设置
    private String executorMemoryOverhead;
    //spark.dynamicAllocation.enabled 是否自动调整资源
    private String dynamicAllocation;
    //spark.shuffle.service.enabled 开启自动调整shuffle必须被开启
    private String shuffleServiceEnabled;
    //spark.dynamicAllocation.minExecutors
    private String minExecutors;
    //spark.dynamicAllocation.maxExecutors
    private String maxExecutors;


    public Logger logger = LogUtil.getLogger();
    //小文件的路径
    private List<String> filePath = new ArrayList<>();
    //文件的长度
    private long fileLength = 0;

    private final String sparkMergeSize = PropertiesUtil.getValue(MergeFileConstant.MERGE_SIZE);

    FileSystem fileSystem = MergeUtil.getFilSystem();

    //查找小文件
    private List<String> getPatternFile(String path) {
        List<String> listPath = new ArrayList<>();
        try {
            FileStatus[] statuses = fileSystem.globStatus(new Path(path));
            if (statuses != null && statuses.length > 0) {
                for (FileStatus status : statuses) {
                    listPath = getFile(status.getPath().toString());
                }
            }
        } catch (IOException e) {
            logger.error("输入的文件路径出错{}", e.getMessage());
        }
        logger.info("{}路径下的文件有{}，总大小为{}", path, listPath, fileLength);
        return listPath;
    }

    //查找所有文件的路径并且计算文件的大小
    private List<String> getFile(String path) throws IOException {
        FileStatus[] listStatus = fileSystem.listStatus(new Path(path));
        if (listStatus == null || listStatus.length < 0)
            return null;

        for (FileStatus status : listStatus) {
            if (status.isFile()) {
                if (status.getLen() > 0)
                    filePath.add(status.getPath().toString());
                fileLength += status.getLen();
            } else {
                getFile(status.getPath().toString());
            }
        }
        return filePath;
    }

    //获取文件的路径提供给spark进行合并
    private String getAllFilePathForSpark(List<String> paths) throws IOException {
        StringBuilder path = new StringBuilder();
        for (String pa : paths) {
            path.append(pa).append(",");
        }

        return path.substring(0, path.length() - 1);
    }


    public void merge(String paths, String outPath, int index) throws Exception {

        //得到路径
        List<String> pathFiles = getPatternFile(paths);

        if (fileLength <= 0 || pathFiles.size() <= 0) {
            logger.error("{}路径下无小文件或者是空文件", paths);
            return;
        }

        //判断大小
        if (fileLength < Integer.parseInt(sparkMergeSize)) {
            outPath = outPath + "/part-m-00000";
            FSDataOutputStream outputStream = fileSystem.create(new Path(outPath), true);
            for (int i = 0; i < pathFiles.size(); i++) {
                FSDataInputStream inputStream = fileSystem.open(new Path(pathFiles.get(i)));
                IOUtils.copyBytes(inputStream, outputStream, 1024);
                inputStream.close();
            }
            outputStream.close();
            logger.info("{}中的文件本合并到{}", paths, outPath);
        } else {
            String allFilePathForSpark = getAllFilePathForSpark(pathFiles);
            logger.info("使用spark合并{}路径下的文件,小文件的路径{}", paths, allFilePathForSpark);
            //执行spark合并（远程写shell脚本并且执行脚本）sparjar已经上传成功
            creatAndExecuteShell(allFilePathForSpark, outPath, index);
        }
    }

    private void creatAndExecuteShell(String paths, String outPath, int index) throws Exception {

        String shellPath = PropertiesUtil.getValue(MergeFileConstant.SHELL_PATH);
        //根据文件的元数据调整spark资源的情况
        StringBuffer command = new StringBuffer();
        command.append("source /etc/profile" + "\n");
        command.append("cd /opt/spark-2.3.1/bin/" + "\n");
        command.append(PropertiesUtil.getValue(MergeFileConstant.SPARK_SUBMIT_COMMAND)).append(" ");
        command.append(PropertiesUtil.getValue(MergeFileConstant.MAIN_CLASS)).append(" ");
        command.append("--" + MergeFileConstant.DRIVER_MEMORY + " ").append(PropertiesUtil.getValue(MergeFileConstant.DRIVER_MEMORY)).append(" ");
        command.append("--" + MergeFileConstant.EXECUTOR_MEMORY + " ").append(PropertiesUtil.getValue(MergeFileConstant.EXECUTOR_MEMORY)).append(" ");
        command.append("--" + MergeFileConstant.EXECUTOR_CORES + " ").append(PropertiesUtil.getValue(MergeFileConstant.EXECUTOR_CORES)).append(" ");
        // command.append("--conf " + MergeFileConstant.SCHEDULER_MODEL + "=").append("FAIR").append(" "); //yarn集群配置并行执行之后这个不用配置
        //根据数据大小选择executor的数量 每1G一个executor
       numExecutors = (int) Math.max((fileLength / Math.pow(1024,3)), 3);

        command.append("--" + MergeFileConstant.NUM_EXECUTORS + " ").append(numExecutors).append(" ");
        //动态资源分配
        command.append("--conf " + MergeFileConstant.DYNAMIC_ALLOCATION + "=").append("true").append(" ");
        command.append("--conf " + MergeFileConstant.SHUFFLE_SERVICE_ENABLE + "=").append("true").append(" ");
        try {
            String maxExecutors = PropertiesUtil.getValue(MergeFileConstant.MAX_EXECUTORS);
            command.append("--conf " + MergeFileConstant.MAX_EXECUTORS + "=").append(maxExecutors).append(" ");
        } catch (MissingResourceException e) {
            logger.info(e.getMessage());
        }

//        command.append("--conf " + MergeFileConstant.MIN_EXECUTORS + "=").append(PropertiesUtil.getValue(MergeFileConstant.MIN_EXECUTORS)).append(" ");

        try {
            String splitMaxSize = PropertiesUtil.getValue(MergeFileConstant.SPLIT_MAXSIZE);
            command.append("--conf " + MergeFileConstant.SPLIT_MAXSIZE + "=").append(splitMaxSize).append(" ");
        } catch (MissingResourceException e) {
            logger.info(e.getMessage());
        }
        command.append("--conf " + MergeFileConstant.PARALLELISM + "=").append(PropertiesUtil.getValue(MergeFileConstant.PARALLELISM)).append(" ");
        command.append(PropertiesUtil.getValue(MergeFileConstant.JAR_LOCATION)).append(" ").append(paths).append(" ").append(outPath);
        logger.info("连接Linux调用工具类");
        SSH2Util ssh2Util = new SSH2Util(PropertiesUtil.getValue("linux.host"), PropertiesUtil.getValue("linux.userName"), PropertiesUtil.getValue("linux.password"), 22);
        //上传脚本文件  shellpath.sh加上index值
        StringBuilder stringBuffer = new StringBuilder(shellPath);
        shellPath = stringBuffer.insert(shellPath.indexOf(".sh"), index).toString();
        logger.info("shell脚本被写到路径->{}", shellPath);
        //向Linux写入脚本
        ssh2Util.putFile(command.toString(), shellPath);
        logger.info("创建{}脚本成功,脚本命令->{}", shellPath, command);
        //执行脚本
        ssh2Util.runCommand("sh " + shellPath);
        logger.info("执行脚本->{}成功", shellPath);
        //关闭连接
        ssh2Util.close();
        logger.info("关闭Linux连接");
    }


}
