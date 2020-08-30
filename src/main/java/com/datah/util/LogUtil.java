package com.datah.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    public static Logger getLogger() {
        Logger  logger = LoggerFactory.getLogger("merge-dev");
        return logger;
    }
    public static Logger getLogger(String name) {
        Logger   logger = LoggerFactory.getLogger(name);
        return logger;
    }
    public static Logger getLogger(Class<?> clazz) {
        Logger  logger = LoggerFactory.getLogger(clazz);
        return logger;
    }
}
