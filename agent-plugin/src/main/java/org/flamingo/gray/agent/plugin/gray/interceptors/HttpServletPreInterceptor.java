package org.flamingo.gray.agent.plugin.gray.interceptors;


import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.RuleEngineForGateway;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.plugin.gray.rule.RuleCache;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 用于拦截http请求，实现灰度的标记传递，以及针对灰度网关规则，进行流量标记
 */
public class HttpServletPreInterceptor implements Interceptor {
    public Object intercept(Object obj,
                            Object[] allArguments,
                            Callable<?> zuper,
                            Method method) throws Throwable {
        Object ret;
        try {
            //获取http request中的灰度相关信息
            if (method.getModifiers() == 4) {
                String grayTag = ((HttpServletRequest) allArguments[0]).getHeader(TagConst.HTTP_GRAY_HEADER_TAG);
                try {
                    if (grayTag != null && grayTag.length() > 0) {
                        GrayContextProxy.setGrayHead(grayTag);
                    }
                } catch (Throwable t) {
                    AgentLogger.getLogger().severe("HttpServletPreInterceptor_setGrayHead : " + AgentLogger.getStackTraceString(t));
                }

                try { //检查本应用是否为网关，是否需要检查网关的流量标记规则
                    if (AgentStringUtils.isBlank(GrayContextProxy.getGrayHead()) && RuleCache.isGatewayApp()) {
                        RuleEngineForGateway.checkRequest((HttpServletRequest) allArguments[0]);
                    }
                } catch (Throwable t) {
                    AgentLogger.getLogger().severe("HttpServletPreInterceptor_checkGatewayCache: " + AgentLogger.getStackTraceString(t));

                }
            }
            ret = zuper.call();
        } catch (Throwable t) {
            throw t;
        } finally {
            if (method.getModifiers() == 4) {
                try {
                    GrayContextProxy.clearContext();
                } catch (Throwable t) {
                    AgentLogger.getLogger().severe("HttpServletPreInterceptor.intercept after: "  + AgentLogger.getStackTraceString(t));
                }
            }
        }
        return ret;
    }
}
