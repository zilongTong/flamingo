package org.flamingo.gray.agent.plugin.gray.interceptors;


import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;

import org.flamingo.gray.agent.plugin.gray.DependencyServices;
import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.RuleEngineForZuul;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandContext;

import java.lang.reflect.Method;

import java.util.concurrent.Callable;

/**
 *         用于网关调用时检查是否满足灰度规则并设置灰度上下文，传递请求头
 */
public class RibbonRoutingFilterInterceptor implements Interceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            RibbonCommandContext ribbonCommandContext = (RibbonCommandContext) allArguments[0];
            String grayTag = GrayContextProxy.getGrayHead();
            if (AgentStringUtils.isEmpty(grayTag)) { //如果不存在灰度标志，则对request进行检查
//                AgentLogger.getLogger().info("没有灰度标传递，检查是否满足灰度规则");
                boolean isMatch = RuleEngineForZuul.checkRequest(ribbonCommandContext);
                if(isMatch){
                    grayTag = GrayContextProxy.getGrayHead();
                }
            }
            if (AgentStringUtils.isNotEmpty(grayTag)) {
//                        AgentLogger.getLogger().info("请求增加灰度头");
                ribbonCommandContext.getHeaders().add(TagConst.HTTP_GRAY_HEADER_TAG, grayTag);
            }
            //将依赖添加到依赖列表
            DependencyServices.addDependencyService(ribbonCommandContext.getServiceId().toUpperCase());
        } catch (Throwable t) {
            AgentLogger.getLogger().severe("FeignExecuteError: " + AgentLogger.getStackTraceString(t));
        } finally {
        }
        return zuper.call();
    }
}
