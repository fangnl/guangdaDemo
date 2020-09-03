package com.datah.main;

import com.datah.util.MergeException;
import com.datah.util.MergeFileConstant;
import com.datah.util.MergeUtil;
import com.datah.util.PropertiesUtil;

import java.util.MissingResourceException;

public class App {
    public static void main(String[] args) throws MergeException {
        //读取配置文件的路径
        MergeUtil.merge();
        //手动输入路径
        // MergeUtil.merge("/input", "/output");

    }
}
