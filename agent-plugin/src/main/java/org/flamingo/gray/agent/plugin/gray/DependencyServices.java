package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.plugin.gray.rpc.GrayRulePullJob;
import org.flamingo.gray.agent.pure.utils.AgentLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuyx on 2019/8/26.
 */
public class DependencyServices {
    private static Set<String> dependencyServices = new HashSet<>();
    private static Set<String> dependencyGrayServices = new HashSet<>();

    static {
        // dependencyServices.add("SUBENV-DEMO-A");
        // dependencyGrayServices.add("SUBENV-DEMO-A");
    }

    public static Set<String> getDependencyServices() {
        return dependencyServices;
    }

    public static Set<String> getDependencyGrayServices() {
        return dependencyGrayServices;
    }

    //添加微服务自己依赖的服务列表
    public static void addDependencyService(String app) {
        if(app != null){
            app = app.toUpperCase();
        }
        if (!dependencyServices.contains(app)) {
            //如果是第一次添加，检查一下是否是灰度应用，如果是也加到灰度列表
            if (EurekaApplicationsHolder.checkApplicationIsGray(app)) {
                AgentLogger.getLogger().info("添加依赖服务" + app);
                dependencyGrayServices.add(app);
                GrayRulePullJob.fireJobNow();
            }
            dependencyServices.add(app);
        }
    }

    //添加微服务到灰度列表
    public static void addDependencyGrayService(String app) {
        if(app != null){
            app = app.toUpperCase();
        }
        if (!dependencyServices.contains(app)) {
            dependencyServices.add(app);
        }
        if (!dependencyGrayServices.contains(app)) {
            GrayRulePullJob.fireJobNow();
            dependencyGrayServices.add(app);
        }

    }

    public static void removeDependencyGrayService(String app) {
        if(app != null){
            app = app.toUpperCase();
        }
        if (dependencyGrayServices.contains(app)) {
            AgentLogger.getLogger().info("删除"+app+"从依赖灰度服务列表");
            dependencyGrayServices.remove(app);
        }
    }

    //添加微服务到灰度列表
    public static void addDependencyGrayServiceIfNeed(String app) {
        if(app != null){
            app = app.toUpperCase();
        }
        if (dependencyServices.contains(app)) {
            if (!dependencyGrayServices.contains(app)) {
                GrayRulePullJob.fireJobNow();
                AgentLogger.getLogger().info("增加"+app+"到依赖灰度服务列表");
                dependencyGrayServices.add(app);
            }
        }
    }

    public static boolean hasGrayService(String appName) {
        return dependencyGrayServices.contains(appName);
    }
}
