package com.datah.util;

public class MergeFileConstant {

    final static String DRIVER_MEMORY = "driver-memory";
    final static String EXECUTOR_MEMORY = "executor-memory";
    final static String EXECUTOR_CORES = "executor-cores";
    final static String NUM_EXECUTORS = "num-executors";
    // 文件合并后的大小
    final public static String SPLIT_MAXSIZE = "spark.hadoop.mapreduce.input.fileinputformat.split.maxsize";
    //配置文件自定义配置选项
    final static String SPARK_SUBMIT_COMMAND = "shell.spark.submit.command";
    //spark工具百main方法
    final static String MAIN_CLASS = "shell.spark.mainClass";
    //spark工具包的位置
    final static String JAR_LOCATION = "shell.spark.jar.location";
    //合并后的文件大小
    final static String MERGE_SIZE = "spark.merge.size";
    //shell文件的位置
    final static String SHELL_PATH = "shell.path";
    final static String FILE_OUTPUT_PATH = "hdfs.file.outPutPath";
    final static String FILE_INPUT_PATH = "hdfs.file.intPutPath";


}
