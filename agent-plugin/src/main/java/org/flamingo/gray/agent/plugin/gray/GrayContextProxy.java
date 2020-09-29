package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.pure.utils.AgentLogger;
import java.lang.reflect.Method;

public class GrayContextProxy {

    private static Method getGrayMethod;
    private static Method setGrayMethod;
    private static Method clearGrayMethod;

    private static final String CrossClassLoaderCacheClassName = "org.flamingo.gray.agent.gray.GrayContext";

    public static final String getGrayHead() {
        String gray = "";
        try {
            if (getGrayMethod == null){
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(CrossClassLoaderCacheClassName);
                getGrayMethod = clazz.getMethod("getGrayHead");
            }
            gray = (String) getGrayMethod.invoke(null);
        }catch (Exception e){
            AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
        return gray;
    }


    public static final void setGrayHead(String gray) {
        try {
            if (setGrayMethod == null){
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(CrossClassLoaderCacheClassName);
                setGrayMethod = clazz.getMethod("setGrayHead",String.class);
            }
//            AgentLogger.getLogger().log(Level.INFO,"RibbonLoadBalancerClientInterceptor show gray " + gray + ", thread id: " + Thread.currentThread().getId());
            setGrayMethod.invoke(null,gray);
        }catch (Exception e){
            AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
    }

    public static final void setTraceId(String traceId) {
    }


    public static void clearContext() {
        try {
            if (clearGrayMethod == null){
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(CrossClassLoaderCacheClassName);
                clearGrayMethod = clazz.getMethod("clearContext");
            }
            clearGrayMethod.invoke(null);
        }catch (Exception e){
            AgentLogger.getLogger().severe(AgentLogger.getStackTraceString(e));
        }
    }
}
