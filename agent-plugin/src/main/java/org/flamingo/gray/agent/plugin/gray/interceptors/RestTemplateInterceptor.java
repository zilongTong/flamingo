package org.flamingo.gray.agent.plugin.gray.interceptors;


import org.flamingo.gray.agent.plugin.gray.DependencyServices;
import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.RuleEngineForRestTemplate;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;
import org.springframework.http.client.AbstractClientHttpRequest;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 *         用于restTemplate调用时检查是否满足灰度规则并设置灰度上下文，传递请求头
 */
public class RestTemplateInterceptor implements Interceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            AbstractClientHttpRequest request = (AbstractClientHttpRequest) obj;
            String grayTag = GrayContextProxy.getGrayHead();
            if (AgentStringUtils.isEmpty(grayTag)) { //如果不存在灰度标志，则对request进行检查
//                AgentLogger.getLogger().info("没有灰度标传递，检查是否满足灰度规则");
                RuleEngineForRestTemplate.checkRequest(request);
            }
            grayTag = GrayContextProxy.getGrayHead();
            if (AgentStringUtils.isNotEmpty(grayTag)) {
                //AgentLogger.getLogger().info("请求增加灰度头");
                request.getHeaders().add(TagConst.HTTP_GRAY_HEADER_TAG, grayTag);
            }
            //AgentLogger.getLogger().info("添加依赖服务" + request.getURI().getHost());
            DependencyServices.addDependencyService(request.getURI().getHost().toUpperCase());
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("RestTemplateInterceptor," + AgentLogger.getStackTraceString(throwable));
        }
        return zuper.call();
    }
}
