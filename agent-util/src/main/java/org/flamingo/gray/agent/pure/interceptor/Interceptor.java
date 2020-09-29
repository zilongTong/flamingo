package org.flamingo.gray.agent.pure.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Created by zhuyx on 2019/7/31.
 */
public interface Interceptor {
    Object intercept(Object obj,
                     Object[] allArguments,
                     Callable<?> zuper,
                     Method method) throws Throwable;
}
