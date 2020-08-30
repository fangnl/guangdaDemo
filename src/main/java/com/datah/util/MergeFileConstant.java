package com.datah.util;

public class MergeFileConstant {

    final static String DRIVER_MEMORY = "driver-memory";
    final static String EXECUTOR_MEMORY = "executor-memory";
    final static String EXECUTOR_CORES = "executor-cores";
    // 文件合并后的大小
    final public static String SPLIT_MAXSIZE = "spark.hadoop.mapreduce.input.fileinputformat.split.maxsize";
    //手动调整资源
    final static String NUM_EXECUTORS = "num-executors";
    //spark.default.parallelism
    final static String PARALLELISM = "spark.default.parallelism";
    //spark.executor.memoryOverhead  executor堆外内存
    final static String EXECUTOR_OVERHEAD = "spark.executor.memoryOverhead";
    //dynamicAllocation 自动调整资源
    final static String DYNAMIC_SWITCH = "dynamicAllocation";
    final static String DYNAMIC_ALLOCATION = "spark.dynamicAllocation.enabled";
    final static String SHUFFLE_SERVICE_ENABLE = "spark.shuffle.service.enabled";
    final static String MIN_EXECUTORS = "spark.dynamicAllocation.minExecutors";
    final static String MAX_EXECUTORS = "spark.dynamicAllocation.maxExecutors";
    //spark.scheduler.mode 资源调度模式
    final static String SCHEDULER_MODEL = "spark.scheduler.mode";
    //配置文件自定义配置选项
    final static String SPARK_SUBMIT_COMMAND = "shell.spark.submit.command";
    final static String MAIN_CLASS = "shell.spark.mainClass";
    final static String JAR_LOCATION = "shell.spark.jar.location";
    final static String MERGE_SIZE = "spark.merge.size";
    final static String SHELL_PATH = "shell.path";
    final static String FILE_OUTPUT_PATH = "hdfs.file.outPutPath";
    final static String FILE_INPUT_PATH = "hdfs.file.intPutPath";


}
