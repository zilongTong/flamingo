package org.flamingo.gray.agent.plugin.gray.interceptors;


import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;
import org.apache.http.HttpMessage;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 *   用于发起httpClient调用时，添加灰度相关请求头
 */
public class HttpClientInterceptor implements Interceptor {

    /**
     * @param obj          apache HttpClient
     * @param allArguments method args
     * @param zuper        callable
     * @param method       reflect method
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            String grayTag = GrayContextProxy.getGrayHead();
            if (AgentStringUtils.isNotBlank(grayTag)) {
                if (allArguments != null && allArguments.length == 2) {
                    for (Object arg : allArguments) {
                        if (arg instanceof HttpMessage) {
                            //AgentLogger.getLogger().info("通过HttpClient调用，传递灰度标");
                            ((HttpMessage) arg).setHeader(TagConst.HTTP_GRAY_HEADER_TAG, grayTag);
                            break;
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("HttpClientInterceptor," + AgentLogger.getStackTraceString(throwable));
        } finally {

        }

        return zuper.call();
    }
}
