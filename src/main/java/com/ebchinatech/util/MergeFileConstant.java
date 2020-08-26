package com.ebchinatech.util;

public class MergeFileConstant {

    final static String DRIVER_MEMORY = "driver-memory";
    final static String EXECUTOR_MEMORY = "executor-memory";
    final static String EXECUTOR_CORES = "executor-cores";
    final static String NUM_EXECUTORS = "num-executors";
    //spark.default.parallelism
    final static String PARALLELISM = "spark.default.parallelism";
    //spark.executor.memoryOverhead  executorMemoryOverhead
    final static String EXECUTOR_OVERHEAD = "spark.executor.memoryOverhead";
    //dynamicAllocation
    final static String DYNAMIC_ALLOCATION = "spark.dynamicAllocation.enabled";
    final static String SHUFFLE_SERVICE_ENABLE = "spark.shuffle.service.enabled";
    final static String MIN_EXECUTORS = "spark.dynamicAllocation.minExecutors";
    final static String MAX_EXECUTORS = "spark.dynamicAllocation.maxExecutors";

    final static String MERGE_SIZE = "spark.merge.size";
    final static String SHELL_PATH = "shell.path";

    final static String FILE_OUTPUT_PATH = "hdfs.file.outPutPath";
    final static String FILE_INPUT_PATH = "hdfs.file.intPutPath";


}
