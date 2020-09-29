package org.flamingo.gray.agent.utils;

/**
 * Created by zhuyx on 2019/9/24.
 */
public class StrUtil {

    public static final String EMPTY = "";

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
}
