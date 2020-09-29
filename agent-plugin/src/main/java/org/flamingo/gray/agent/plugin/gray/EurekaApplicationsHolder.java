package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.pure.utils.CacheProxy;

/**
 * Created by zhuyx on 2019/8/28.
 */
public class EurekaApplicationsHolder {
    //private static final String CacheKey = "EurekaApplicationsHolder";
    public static final String GrayAppsCacheKey = "EurekaGrayApplications";

    public static void addGrayApp(String serviceName) {
        if(serviceName != null){
            serviceName = serviceName.toUpperCase();
        }
        CacheProxy.addCacheSet(EurekaApplicationsHolder.GrayAppsCacheKey, serviceName);
    }

    public static void removeGrayApp(String serviceName) {
        if(serviceName != null){
            serviceName = serviceName.toUpperCase();
        }
        CacheProxy.removeCacheSet(EurekaApplicationsHolder.GrayAppsCacheKey, serviceName);
    }

    public static boolean checkApplicationIsGray(String app) {
        if(app != null){
            app = app.toUpperCase();
        }
        return CacheProxy.isSetContains(GrayAppsCacheKey, app);
    }
}
