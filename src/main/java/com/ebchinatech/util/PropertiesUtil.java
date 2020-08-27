package com.ebchinatech.util;
import java.util.ResourceBundle;

public class PropertiesUtil {
    private static ResourceBundle pro = ResourceBundle.getBundle("application-dev");
    public static String  getValue(String key) {
       return pro.getString(key);
    }
}
