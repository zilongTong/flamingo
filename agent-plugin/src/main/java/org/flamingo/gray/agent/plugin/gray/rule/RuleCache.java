package org.flamingo.gray.agent.plugin.gray.rule;

import org.flamingo.gray.agent.plugin.gray.config.ComparatorType;
import org.flamingo.gray.agent.plugin.gray.rpc.GetRuleResponse;
import org.flamingo.gray.agent.plugin.gray.rpc.GrayRulePullJob;
import org.flamingo.gray.agent.pure.utils.AgentExecutor;
import org.flamingo.gray.agent.pure.utils.AppIdUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Created by zhuyx on 2019/8/26.
 */
public class RuleCache {
    private static GetRuleResponse currentGetRuleResponse;
    private static GetRuleResponse currentGatewayRules;
    private static List<String> gatewayList = null;
    private static boolean graySwitch = true;

    public static boolean isGraySwitch() {
        return graySwitch;
    }

    public static void setGraySwitch(boolean graySwitch) {
        RuleCache.graySwitch = graySwitch;
    }

    public static void setGatewayList(List<String> gateways) {
        if (gateways != null && gateways.contains(AppIdUtils.getAppId())) {
            GrayRulePullJob.fireJobNow();
        }
        gatewayList = gateways;
    }

    public static boolean isGatewayApp() {
        if (gatewayList == null) {
            AgentExecutor.execute(GrayRulePullJob.getGatewayListCall());
            return false;
        }
        return gatewayList.contains(AppIdUtils.getAppId());
    }

    public static boolean isZuul() {
        if (gatewayList == null) {
            return false;
        }
        return gatewayList.contains(AppIdUtils.getAppId());
    }

    public static void setCurrentGetRuleResponse(GetRuleResponse response) {
        generateRuleValueList(response);
        currentGetRuleResponse = response;
    }


    public static void setCurrentGatewayRules(GetRuleResponse response) {
        generateRuleValueList(response);
        currentGatewayRules = response;
    }

    private static void generateRuleValueList(GetRuleResponse response) {
        for (Map.Entry<String, List<GrayPlan>> ruleMapEntry : response.getGrayPlanMap().entrySet()) {
            if (ruleMapEntry.getValue() != null) {
                for (GrayPlan grayPlan : ruleMapEntry.getValue()) {
                    for (GrayPath grayPath : grayPlan.getGrayPathList()) {
                        for (GrayRule rule : grayPath.getGrayRuleList()) {
                            if (rule.getComparator().equals(ComparatorType.In)
                                    || rule.getComparator().equals(ComparatorType.NotIn)) {
                                rule.setParamValueList(Arrays.asList(rule.getParamValue().split(",")));
                            }
                            if (rule.getComparator().equals(ComparatorType.Range)
                                    || rule.getComparator().equals(ComparatorType.HashRange)
                                    || rule.getComparator().equals(ComparatorType.Random)) {
                                rule.setParamValueList(new ArrayList<>());
                                rule.setParamRangeList(new ArrayList<>());
                                String args1 = rule.getParamValue().split(",")[0];
                                String args2 = rule.getParamValue().split(",")[1];
                                rule.getParamRangeList().add(Double.parseDouble(args1));
                                rule.getParamRangeList().add(Double.parseDouble(args2));
                            }
                        }
                    }
                }
            }
        }

    }

    public static GetRuleResponse getCurrentGatewayRules() {
        return currentGatewayRules;
    }

    public static List<GrayPlan> getGrayPlans(String serviceName) {
        if (currentGetRuleResponse == null) {
            return new ArrayList<>();
        } else {
            List<GrayPlan> grayPlans = currentGetRuleResponse.getGrayPlanMap().get(serviceName);
            if (grayPlans == null || grayPlans.size() == 0) {
                return new ArrayList<>();
            } else {
                return grayPlans;
            }
        }
    }

    public static boolean isEmpty() {
        if (currentGetRuleResponse == null) {
            return true;
        } else {
            return currentGetRuleResponse.getGrayPlanMap().size() == 0;
        }
    }

    public static boolean isEmptyWithService(String serviceName) {
        if (currentGetRuleResponse == null) {
            return true;
        } else {
            List<GrayPlan> grayPlans = currentGetRuleResponse.getGrayPlanMap().get(serviceName);
            if (grayPlans == null || grayPlans.size() == 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean gatewayRuleIsEmpty() {
        return currentGatewayRules == null || currentGatewayRules.getGrayPlanMap() ==null || currentGatewayRules.getGrayPlanMap().size() == 0;
    }
}
