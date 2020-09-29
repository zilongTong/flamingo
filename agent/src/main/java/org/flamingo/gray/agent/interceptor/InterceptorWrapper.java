package org.flamingo.gray.agent.interceptor;

import org.flamingo.gray.agent.classloader.AgentClassLoader;
import org.flamingo.gray.agent.classloader.ClassLoaderUtils;
import org.flamingo.gray.agent.utils.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 拦截器包装器，当指定方法调用时获取指定类加载器加载拦截器，并调用拦截方法
 */
public class InterceptorWrapper {
    private String className;
    private Object classInterceptor = null;
    private Method interceptorMethod = null;

    public InterceptorWrapper(String className) {
        this.className = className;
    }

    @RuntimeType
    public Object intercept(@This Object obj,
                            @AllArguments Object[] allArguments,
                            @SuperCall Callable<?> zuper,
                            @Origin Method method) throws Throwable {
        initInterceptor();

        if (interceptorMethod == null) {
            interceptorMethod = findInterceptor();
        }

        if (interceptorMethod != null) {
            return interceptorMethod.invoke(classInterceptor, obj, allArguments, zuper, method);
        } else {
            return zuper.call();
        }
    }

    private void initInterceptor() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (classInterceptor == null) {
            try {
                AgentClassLoader agentClassLoader = ClassLoaderUtils.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                classInterceptor = agentClassLoader.loadClass(this.className).newInstance();
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("initInterceptor error " + AgentLogger.getStackTraceString(throwable));

            }
        }
    }

    public Method findInterceptor() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            initInterceptor();
            if (classInterceptor != null) {
                Method[] methods = classInterceptor.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equals("intercept")) {
                        return method;
                    }
                }
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("findInterceptor error " + AgentLogger.getStackTraceString(throwable));
        }
        return null;
    }
}
