package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.plugin.gray.config.PathWildType;
import org.flamingo.gray.agent.plugin.gray.rpc.GetRuleResponse;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPath;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPlan;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.plugin.gray.rule.RuleCache;
import org.flamingo.gray.agent.pure.utils.AgentLogger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyx on 2019/8/24.
 */
public class RuleEngineForGateway {
    public static void checkRequest(HttpServletRequest request) {
        if (RuleCache.gatewayRuleIsEmpty()) {
            return;
        }
        GetRuleResponse allRules = RuleCache.getCurrentGatewayRules();
        for (Map.Entry<String, List<GrayPlan>> ruleMapEntry : allRules.getGrayPlanMap().entrySet()) {
            if(ruleMapEntry.getValue() == null)
                return;
            for (GrayPlan grayPlan : ruleMapEntry.getValue()) {
                for (GrayPath grayPath : grayPlan.getGrayPathList()) {
                    if (grayPath.getPathType() == 1 && isMatch(request, grayPath)) {
                        GrayContextProxy.setGrayHead(grayPlan.getGrayVersion());
                        return;
                    }
                }
            }
        }
        return;
    }

    private static boolean isMatch(HttpServletRequest request, GrayPath grayPath) {
        boolean isPathMatch = false;
        switch (grayPath.getWildMatchType()) {
            case PathWildType.ActualMatch:
                isPathMatch = request.getRequestURI().equals(grayPath.getPath());
                break;
            case PathWildType.OneLevelWildMatchEnd:
                if (request.getRequestURI().startsWith(grayPath.getMatchPath())) {
                    if (!request.getRequestURI().replace(grayPath.getMatchPath(), "").contains("/")) {
                        isPathMatch = true;
                    }
                }
                break;
            case PathWildType.MutilLevelWildMatchEnd:
                if (request.getRequestURI().startsWith(grayPath.getMatchPath())) {
                    isPathMatch = true;
                }
                break;
            default:
                break;
        }
        if (!isPathMatch) {
            return false;
        }

        return checkPathRuleMatch(request, grayPath);
    }

    private static boolean checkPathRuleMatch(HttpServletRequest request, GrayPath grayPath) {
        boolean isMatch = false;
        if (grayPath.getMatchType() == 1) {  //全部命中
            isMatch = true;
            if (grayPath.getGrayRuleList() == null || grayPath.getGrayRuleList().size() == 0) {
                isMatch = false;
            } else {
                for (GrayRule rule : grayPath.getGrayRuleList()) {
                    if (!checkRuleMatch(request, rule)) {
                        isMatch = false;
                        break;
                    }
                }
            }
        } else if (grayPath.getMatchType() == 2) {  //命中一条
            for (GrayRule rule : grayPath.getGrayRuleList()) {
                if (checkRuleMatch(request, rule)) {
                    isMatch = true;
                    break;
                }
            }
        } else {
            isMatch = false;
        }
        return isMatch;
    }

    private static boolean checkRuleMatch(HttpServletRequest request, GrayRule rule) {
        try {
            String value = RuleValueGetterForGateway.getRuleValue(request, rule);
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
