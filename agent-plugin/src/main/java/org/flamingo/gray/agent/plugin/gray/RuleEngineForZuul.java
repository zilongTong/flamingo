package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.plugin.gray.config.PathWildType;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPath;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPlan;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.plugin.gray.rule.RuleCache;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandContext;
import java.util.List;


public class RuleEngineForZuul {
    public static boolean checkRequest(RibbonCommandContext ribbonCommandContext) {
        if (RuleCache.isEmptyWithService(ribbonCommandContext.getServiceId().toUpperCase())) {
//            AgentLogger.getLogger().info("首次调用没有拉取网关灰度规则");
            return false;
        }
//        GetRuleResponse allRules = RuleCache.getCurrentGatewayRules();
        List<GrayPlan> allRules = RuleCache.getGrayPlans(ribbonCommandContext.getServiceId().toUpperCase());
//        for (Map.Entry<String, List<GrayPlan>> ruleMapEntry : allRules.getGrayPlanMap().entrySet()) {
            for (GrayPlan grayPlan : allRules) {
                for (GrayPath grayPath : grayPlan.getGrayPathList()) {
                    if (isMatch(ribbonCommandContext, grayPath)) {
                        AgentLogger.getLogger().info("满足灰度规则，增加灰度标"+grayPlan.getGrayVersion());
                        GrayContextProxy.setGrayHead(grayPlan.getGrayVersion());
                        return true;
                    }
                }
            }
//        }
        return false;
    }

    private static boolean isMatch(RibbonCommandContext ribbonCommandContext, GrayPath grayPath) {
        boolean isPathMatch = false;
        switch (grayPath.getWildMatchType()) {
            case PathWildType.ActualMatch:
                isPathMatch = ribbonCommandContext.getUri().equals(grayPath.getPath());
                break;
            case PathWildType.OneLevelWildMatchEnd:
                if (ribbonCommandContext.getUri().startsWith(grayPath.getMatchPath())) {
                    if (!ribbonCommandContext.getUri().replace(grayPath.getMatchPath(), "").contains("/")) {
                        isPathMatch = true;
                    }
                }
                break;
            case PathWildType.MutilLevelWildMatchEnd:
                if (ribbonCommandContext.getUri().startsWith(grayPath.getMatchPath())) {
                    isPathMatch = true;
                }
                break;
            default:
                break;
        }
        if (!isPathMatch) {
            return false;
        }

        return checkPathRuleMatch(ribbonCommandContext, grayPath);
    }

    private static boolean checkPathRuleMatch(RibbonCommandContext ribbonCommandContext, GrayPath grayPath) {
        boolean isMatch = false;
        if (grayPath.getMatchType() == 1) {  //全部命中
            isMatch = true;
            if (grayPath.getGrayRuleList() == null || grayPath.getGrayRuleList().size() == 0) {
                isMatch = false;
            } else {
                for (GrayRule rule : grayPath.getGrayRuleList()) {
                    if (!checkRuleMatch(ribbonCommandContext, rule)) {
                        isMatch = false;
                        break;
                    }
                }
            }
        } else if (grayPath.getMatchType() == 2) {  //命中一条
            for (GrayRule rule : grayPath.getGrayRuleList()) {
                if (checkRuleMatch(ribbonCommandContext, rule)) {
                    isMatch = true;
                    break;
                }
            }
        } else {
            isMatch = false;
        }
        return isMatch;
    }

    private static boolean checkRuleMatch(RibbonCommandContext ribbonCommandContext, GrayRule rule) {
        try {
            String value = RuleValueGetterForZuul.getRuleValue(ribbonCommandContext, rule);
            if (value == null) {
                return false;
            } else {
                return ValueComparator.compare(value, rule);
            }
        }catch (Exception ex){
            AgentLogger.getLogger().severe("checkRuleMatchError_gateway:" + ex.getMessage());
        }
        return false;
    }
}
