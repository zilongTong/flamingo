package org.flamingo.gray.agent.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuyx on 2019/8/2.
 */
public class InterceptConfig {

    private static List<InterceptConfig> configs = new ArrayList<>();

    public static void loadConfig(String file) {

    }

    public static List<InterceptConfig> getConfigs() {
        return configs;
    }

    private String className;
    private String methodName;
    private String interceptorClass;

    public InterceptConfig(){

    }

    public InterceptConfig(String className, String methodName, String interceptorClass) {
        this.className = className;
        this.methodName = methodName;
        this.interceptorClass = interceptorClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }
}
