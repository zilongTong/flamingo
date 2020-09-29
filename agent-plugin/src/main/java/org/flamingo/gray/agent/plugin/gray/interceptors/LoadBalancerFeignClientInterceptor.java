package org.flamingo.gray.agent.plugin.gray.interceptors;


import org.flamingo.gray.agent.plugin.gray.DependencyServices;
import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.RuleEngine;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 用于发起feign调用时，检验请求是否满足灰度规则，设置灰度上下文，添加灰度相关http 请求头
 */
public class LoadBalancerFeignClientInterceptor implements Interceptor {
    static Field headerField;
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            feign.Request request = (feign.Request) allArguments[0];
            String grayTag = GrayContextProxy.getGrayHead();
            if (AgentStringUtils.isEmpty(grayTag)) { //如果不存在灰度标志，则对request进行检查
                RuleEngine.checkRequest(request);
            }
            grayTag = GrayContextProxy.getGrayHead();
            if(AgentStringUtils.isNotEmpty(grayTag)){
                addGrayHeader(request, grayTag);
            }
            //将依赖添加到依赖列表
            URI asUri = URI.create(request.url());
            String clientName = asUri.getHost();
//            AgentLogger.getLogger().info("添加依赖服务" + clientName);
            DependencyServices.addDependencyService(clientName);
        } catch (Throwable t) {
            AgentLogger.getLogger().severe("FeignExecuteError: " + AgentLogger.getStackTraceString(t));
        } finally {
        }
        return zuper.call();
    }

    private void addGrayHeader(feign.Request request, String grayTag) {
        try {
            if (!AgentStringUtils.isEmpty(grayTag)) {
                Collection<String> arrayList = new ArrayList<>();
                arrayList.add(grayTag);
                Map<String, Collection<String>> headers = new HashMap<>(request.headers());
                headers.put(TagConst.HTTP_GRAY_HEADER_TAG, arrayList);
                if (headerField == null) {
                    headerField = feign.Request.class.getDeclaredField("headers");
                    headerField.setAccessible(true);
                }
                headerField.set(request, headers);
            }
        } catch (Exception ex) {
            AgentLogger.getLogger().severe("addSubEnvHearder: " + ex.getMessage()+ex);
        }
    }
}
