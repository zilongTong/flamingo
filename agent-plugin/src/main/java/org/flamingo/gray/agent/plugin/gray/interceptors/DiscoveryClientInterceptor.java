package org.flamingo.gray.agent.plugin.gray.interceptors;

import com.netflix.appinfo.InstanceInfo;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.plugin.gray.rpc.GetMetaResponse;
import org.flamingo.gray.agent.plugin.gray.rpc.RemoteService;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;
import org.flamingo.gray.agent.pure.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 用于注册到eureka时，添加灰度meta
 */
public class DiscoveryClientInterceptor implements Interceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {

        try {
            //从服务器获取实例的meta信息
            GetMetaResponse metaResponse = RemoteService.getGrayMetaInfo();
            if (metaResponse != null && metaResponse.getCode() != 0) {
                InstanceInfo instanceInfo = ReflectionUtils.getFieldValue(obj, "instanceInfo", InstanceInfo.class);
                if (instanceInfo != null) {
                    //如果由meta信息，则添加meta tag
                    if (AgentStringUtils.isNotBlank(metaResponse.getGrayMetaTag())) {
                        AgentLogger.getLogger().info("eureka元数据增加子环境标：" + metaResponse.getGrayMetaTag());
                        instanceInfo.getMetadata().put(TagConst.META_DATA_INSTANCE_TAG, metaResponse.getGrayMetaTag());
                    } else { // 没有的话，需要移除
                        AgentLogger.getLogger().info("eureka元数据移除子环境标：" + metaResponse.getGrayMetaTag());
                        instanceInfo.getMetadata().remove(TagConst.META_DATA_INSTANCE_TAG);
                    }

                    //注册服务维度的灰度信息
                    if (AgentStringUtils.isNotBlank(metaResponse.getServiceTag())) {
                        AgentLogger.getLogger().info("eureka元数据增加灰度标：" + metaResponse.getGrayMetaTag());
                        instanceInfo.getMetadata().put(TagConst.META_DATA_SERVIVE_TAG, metaResponse.getServiceTag());
                    } else { //移除服务维度的信息
                        AgentLogger.getLogger().info("eureka元数据移除灰度标：" + metaResponse.getGrayMetaTag());
                        instanceInfo.getMetadata().remove(TagConst.META_DATA_SERVIVE_TAG);
                    }
                }
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("doMetaTagError: " + AgentLogger.getStackTraceString(throwable));
        }

        return zuper.call();
    }
}
