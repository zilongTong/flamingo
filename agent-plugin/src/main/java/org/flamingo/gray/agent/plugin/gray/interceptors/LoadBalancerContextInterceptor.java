package org.flamingo.gray.agent.plugin.gray.interceptors;

import com.netflix.loadbalancer.*;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.flamingo.gray.agent.plugin.gray.EurekaApplicationsHolder;
import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * 根据灰度上下文进行路由选择
 */
public class LoadBalancerContextInterceptor implements Interceptor {

    static Method getPredicate;

    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        if(!EurekaApplicationsHolder.checkApplicationIsGray(((LoadBalancerContext)obj).getClientName().toUpperCase())){
            return zuper.call();
        }
        Object ret = null;
        try {
            String key = null;
            if (allArguments[1] != null) {
                key = allArguments[1].toString();
            }
            Server server = chooseServer(key, obj, zuper);
            if (server != null) {
                //AgentLogger.getLogger().log(Level.INFO, "choose server : " + server.getHost());
                ret = server;
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("RibbonInterceptor.chooseSubEnvServer" + AgentLogger.getStackTraceString(throwable));
        } finally {
        }
        return ret;
    }

    private Server chooseServer(Object key, Object obj, Callable<?> zuper) throws Exception {
        List<Server> serverList = new ArrayList<>();

        // 获取所有的服务列表
        try {
            ILoadBalancer iLoadBalancer = ((LoadBalancerContext)obj).getLoadBalancer();
            if (getPredicate == null) {
                getPredicate = ((ZoneAwareLoadBalancer)iLoadBalancer).getRule().getClass().getDeclaredMethod("getPredicate", null);
            }
            AbstractServerPredicate predicate = (AbstractServerPredicate) getPredicate.invoke(((ZoneAwareLoadBalancer)iLoadBalancer).getRule());
            serverList = predicate.getEligibleServers(iLoadBalancer.getAllServers(), key);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            AgentLogger.getLogger().log(Level.SEVERE, AgentLogger.getStackTraceString(e), e);
        }

        if (serverList == null || serverList.size() == 0) {
            return null;
        }

//        Exception e = new Exception();
//        for (StackTraceElement stackTraceElement:e.getStackTrace()){
//            AgentLogger.getLogger().info(stackTraceElement.toString());
//        }

        //以下为灰度路由
        String grayHead = GrayContextProxy.getGrayHead();
        List<Server> staticServers = new ArrayList<>();

        Set<String> serviceTags = new HashSet<>();
        List<Server> commonServers = new ArrayList<>();
        List<Server> grayServers = new ArrayList<>();

        for (Server server : serverList) {
            if (server instanceof DiscoveryEnabledServer) {
                Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();
                String serverGrayMetaTag = metadata.get(TagConst.META_DATA_INSTANCE_TAG);
                if (AgentStringUtils.isNotBlank(serverGrayMetaTag)) {
                    serviceTags.add(serverGrayMetaTag);
                    if (serverGrayMetaTag.equals(grayHead)) {
                        grayServers.add(server);
                    }
                } else {
                    commonServers.add(server);
                }

                String serviceTagStr = metadata.get(TagConst.META_DATA_SERVIVE_TAG);
                if (AgentStringUtils.isNotBlank(serviceTagStr)) {
                    serviceTags.addAll(Arrays.asList(serviceTagStr.split(",")));
                }
            } else {
                staticServers.add(server);
            }
        }

        //如果是静态路由，优先选择
        if (staticServers.size() > 0) {
            Random random = new Random();
            return staticServers.get(ThreadLocalRandom.current().nextInt(staticServers.size()));
        }

        //如果是存在灰度标识，且该服务也存在这个灰度版本， 走灰度路由
        if (AgentStringUtils.isNotBlank(grayHead) && serviceTags.contains(grayHead)) {
            if (grayServers.size() > 0) {
                //AgentLogger.getLogger().log(Level.INFO, "choose gray server");
                return grayServers.get(ThreadLocalRandom.current().nextInt(grayServers.size()));
            } else {
                AgentLogger.getLogger().log(Level.INFO, "choose gray server no server ");
                return null;
            }
        } else {//否则走正常路由
            if (commonServers.size() > 0) {
                //AgentLogger.getLogger().log(Level.INFO, "choose common server");
                return commonServers.get(ThreadLocalRandom.current().nextInt(commonServers.size()));
            } else {
                AgentLogger.getLogger().log(Level.INFO, "choose common server no server ");
                return null;
            }
        }
    }
}
