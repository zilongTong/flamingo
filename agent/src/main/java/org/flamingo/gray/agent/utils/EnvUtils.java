package org.flamingo.gray.agent.utils;

/**
 * Created by zhuyx on 2019/8/2.
 */
public class EnvUtils {
    private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
    private static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";

    public static boolean isOSWindows() {
        String osName = System.getProperty("os.name");
        if (isEmpty(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
