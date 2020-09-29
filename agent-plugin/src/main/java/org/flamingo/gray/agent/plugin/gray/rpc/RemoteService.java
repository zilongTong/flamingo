package org.flamingo.gray.agent.plugin.gray.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flamingo.gray.agent.plugin.gray.DependencyServices;
import org.flamingo.gray.agent.plugin.gray.GrayUrl;
import org.flamingo.gray.agent.plugin.gray.config.ComparatorType;
import org.flamingo.gray.agent.plugin.gray.config.ParamSource;
import org.flamingo.gray.agent.plugin.gray.config.PathWildType;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPath;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPlan;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AppIdUtils;
import org.flamingo.gray.agent.pure.utils.HttpTinyClient;
import org.flamingo.gray.agent.pure.utils.IPUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhuyx on 2019/8/28.
 */
public class RemoteService {
    private static final String AppIdParamName = "appId";
    private static final String AddressParamName = "address";
    private static final int MaxRetryCount = 3;
    private static final String ServiceName = "services";

    public static GetMetaResponse getGrayMetaInfo() {
        String url = GrayUrl.getGrayMetaUrl();
        String appId = AppIdUtils.getAppId();
        List<String> addressList = IPUtils.getIpAddressList();

        GetMetaResponse result = new GetMetaResponse();
        result.setCode(0);
        for (String address : addressList) {
            List<String> params = new ArrayList<>();

            params.add(AppIdParamName);
            params.add(appId);
            params.add(AddressParamName);
            params.add(address);
            try {
                HttpTinyClient.HttpResult httpResult = HttpTinyClient.httpGet(url, params, 2000);
                if (httpResult.code == 200) {
                    AgentLogger.getLogger().info("get meta success");
                    GetMetaResponse getMetaResponse = new ObjectMapper().readValue(httpResult.content, GetMetaResponse.class);
                    if (getMetaResponse.getCode() > result.getCode()) {
                        result = getMetaResponse;
                    }
                }
            } catch (IOException e) {
                AgentLogger.getLogger().severe("get meta error: " + AgentLogger.getStackTraceString(e));
                continue;
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("get meta error: " + AgentLogger.getStackTraceString(throwable));
            } finally {

            }
        }
        return result;
    }

    public static GetRuleResponse getGrayRule() {
        String url = GrayUrl.getGrayRuleUrl();
        String appId = AppIdUtils.getAppId();
        List<String> addressList = IPUtils.getIpAddressList();
        String address = addressList.get(0);

        List<String> params = new ArrayList<>();
        for (String service : DependencyServices.getDependencyGrayServices()) {
            params.add(ServiceName);
            params.add(service);
        }

        params.add(AppIdParamName);
        params.add(appId);
        params.add(AddressParamName);
        params.add(address);

        try {
            HttpTinyClient.HttpResult httpResult = HttpTinyClient.httpGet(url, params, 2000);
            if (httpResult.code == 200) {
//                AgentLogger.getLogger().info("get gray rule success");
                return new ObjectMapper().readValue(httpResult.content, GetRuleResponse.class);
            }
        } catch (IOException e) {
            AgentLogger.getLogger().severe("get gray rule error: " + AgentLogger.getStackTraceString(e));
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("get gray rule error: " + AgentLogger.getStackTraceString(throwable));
        }
        return null;
    }

    public static GetMetaResponse getMockMetaForTest() {
        GetMetaResponse getMetaResponse = new GetMetaResponse();
        getMetaResponse.setCode(2);
        getMetaResponse.setGrayMetaTag("gray-tag-v1");
        getMetaResponse.setServiceGrayVersions(Arrays.asList("gray-tag-v1,gray-tag-v2".split(",")));
        return getMetaResponse;
    }

    public static GetRuleResponse getMockRuleResponse() {
        //for http servlet
        GetRuleResponse getRuleResponse = new GetRuleResponse();
        GrayPlan grayPlan = new GrayPlan();
        GrayPath grayPath = new GrayPath();
        GrayRule grayRule = new GrayRule();

        grayRule.setComparator(ComparatorType.StartWith);
        grayRule.setParamType(ParamSource.Param);
        grayRule.setParamName("gray");
        grayRule.setParamValue("test1");

        grayPath.setMatchType(1);
        grayPath.getGrayRuleList().add(grayRule);
        grayPath.setWildMatchType(PathWildType.ActualMatch);
        grayPath.setPathType(1);
        grayPath.setPath("/backend-host-info");
        grayPath.setMatchPath("/backend-host-info");

        grayPlan.setPlanId(5);
        grayPlan.setName("testPlan");
        grayPlan.setGrayVersion("gray-tag-v1");
        grayPlan.getGrayPathList().add(grayPath);
        grayPlan.getGrayPathMap().put(grayPath.getPath(), grayPath);

        getRuleResponse.getGrayPlanMap().put("ALL_GATEWAY_RULE", new ArrayList<>(Arrays.asList(grayPlan)));

        return getRuleResponse;
    }

    public static List<String> getGatewayAppIds() {
        int retry = 0;
        while (retry < MaxRetryCount) {
            try {
                HttpTinyClient.HttpResult httpResult = HttpTinyClient.httpGet(GrayUrl.getGatewayListUrl(), new ArrayList<>(), 2000);
                if (httpResult.code == 200) {
                    AgentLogger.getLogger().info("get gateway list success");
                    ObjectMapper mapper = new ObjectMapper();
                    return  mapper.readValue(httpResult.content, new TypeReference<List<String>>(){});
                }
            } catch (IOException e) {
                AgentLogger.getLogger().severe("get gateway list error: " + AgentLogger.getStackTraceString(e));
                continue;
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("get gateway list error: " + AgentLogger.getStackTraceString(throwable));
            } finally {
                retry++;
            }
        }
        return new ArrayList<>();
    }

    public static GetRuleResponse getGrayRuleForGateway() {
        String url = GrayUrl.getGatewayRuleUrl();
        try {
            HttpTinyClient.HttpResult httpResult = HttpTinyClient.httpGet(url, new ArrayList<>(), 2000);
            if (httpResult.code == 200) {
//                AgentLogger.getLogger().info("get gateway rule success");
                return new ObjectMapper().readValue(httpResult.content, GetRuleResponse.class);
            }
        } catch (IOException e) {
            AgentLogger.getLogger().severe("get gateway rule error: " + AgentLogger.getStackTraceString(e));
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("get gateway rule error: " + AgentLogger.getStackTraceString(throwable));
        }
        return null;
    }

    public static boolean getSwitch(){
        String url = GrayUrl.getSwitchUrl();
        try {
            HttpTinyClient.HttpResult httpResult = HttpTinyClient.httpGet(url, new ArrayList<>(), 2000);
            if (httpResult.code == 200) {
                return "on".equals(httpResult.content);
            }
        } catch (IOException e) {
            AgentLogger.getLogger().severe("get switch error: " + AgentLogger.getStackTraceString(e));
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("get switch error: " + AgentLogger.getStackTraceString(throwable));
        }
        return false;
    }

}
