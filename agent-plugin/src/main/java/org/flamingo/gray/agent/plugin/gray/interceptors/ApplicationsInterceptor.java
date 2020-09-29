package org.flamingo.gray.agent.plugin.gray.interceptors;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import org.flamingo.gray.agent.plugin.gray.DependencyServices;
import org.flamingo.gray.agent.plugin.gray.EurekaApplicationsHolder;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;


/**
 * 用于 拦截 Eureka Client 服务发现时候，记录那些是灰度serviceList
 */
public class ApplicationsInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            //eureka 服务发生变更触发
            if (allArguments.length == 1) {
                Application application = (Application) allArguments[0];
                //以下为将灰度服务记录到 灰度服务列表，以及将非灰度服务从列表移除
                boolean isGray = false;
                for (InstanceInfo instanceInfo : application.getInstances()) {
                    if (AgentStringUtils.isNotBlank(instanceInfo.getMetadata().get(TagConst.META_DATA_INSTANCE_TAG))
                            || AgentStringUtils
                            .isNotBlank(instanceInfo.getMetadata().get(TagConst.META_DATA_SERVIVE_TAG))) {
                        isGray = true;
                        break;
                    }
                }
                
                if (isGray) {
                    //更新灰度列表
                    //                    AgentLogger.getLogger().info("增加"+application.getName()+"到所有灰度服务列表");
                    EurekaApplicationsHolder.addGrayApp(application.getName());
                    //更新灰度服务依赖列表， 这里需要测试以下类加载器的情况
                    DependencyServices.addDependencyGrayServiceIfNeed(application.getName());
                } else {
                    //更新灰度列表
                    //                    AgentLogger.getLogger().info("删除"+application.getName()+"从所有灰度服务列表");
                    EurekaApplicationsHolder.removeGrayApp(application.getName());
                    //更新灰度服务依赖列表
                    DependencyServices.removeDependencyGrayService(application.getName());
                }
                
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("EurekaAddApplicationError: " + AgentLogger.getStackTraceString(throwable));
        }
        
        return zuper.call();
    }
}
