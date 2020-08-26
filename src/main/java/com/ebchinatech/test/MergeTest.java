package com.ebchinatech.test;

import com.ebchinatech.util.MergeException;
import com.ebchinatech.util.MergeUtil;

public class MergeTest {
    public static void main(String[] args) throws MergeException {
        //读取配置文件的路径
        MergeUtil.merge();
        //手动输入路径
        // MergeUtil.merge("/input","/output");

    }
}
