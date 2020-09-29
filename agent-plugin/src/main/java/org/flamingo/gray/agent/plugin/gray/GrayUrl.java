package org.flamingo.gray.agent.plugin.gray;


import org.flamingo.gray.agent.pure.utils.AgentStringUtils;
import org.flamingo.gray.agent.pure.utils.ConfigPropertiesProxy;
import org.flamingo.gray.agent.pure.utils.EnvUtils;

/**
 * Created by zhuyx on 2019/7/16.
 */
public class GrayUrl {
    
    public static String testServer = "http://test-gray-service";
    
    public static String fatServer = "http://fat-gray-service";
    
    public static String uatServer = "http://uat-gray-service";
    
    public static String prdServer = "http://pro-gray-service";
    
    
    public static String getGrayMetaUrl() {
        String server = getGrayServiceAddress();
        return String.format("%s/service/get-meta", server);
    }
    
    public static String getGrayRuleUrl() {
        String server = getGrayServiceAddress();
        return String.format("%s/service/get-rule", server);
    }
    
    public static String getGatewayListUrl() {
        String server = getGrayServiceAddress();
        return String.format("%s/service/get-gateway-list", server);
    }
    
    public static String getGatewayRuleUrl() {
        String server = getGrayServiceAddress();
        return String.format("%s/service/get-gateway-rule", server);
    }
    
    private static String getGrayServiceAddress() {
        String server = prdServer;
        if (AgentStringUtils.isNotBlank(ConfigPropertiesProxy.getGrayServerAddress())) {
            server = ConfigPropertiesProxy.getGrayServerAddress();
        } else {
            String env = EnvUtils.getEnvType();
            if (env.equals("uat")) {
                server = uatServer;
            } else if (env.equals("fat")) {
                server = fatServer;
            } else if (env.equals("test")) {
                server = testServer;
            }
        }
        return server;
    }
    
    public static String getSwitchUrl() {
        String server = getGrayServiceAddress();
        return String.format("%s/service/get-switch", server);
    }
}
