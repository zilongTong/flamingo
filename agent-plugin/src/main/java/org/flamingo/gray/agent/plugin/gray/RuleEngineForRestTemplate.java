package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.plugin.gray.config.PathWildType;

import org.flamingo.gray.agent.plugin.gray.rpc.GrayRulePullJob;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPath;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPlan;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.plugin.gray.rule.RuleCache;
import org.flamingo.gray.agent.pure.utils.AgentExecutor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.springframework.http.client.ClientHttpRequest;
import java.net.URI;
import java.util.List;


public class RuleEngineForRestTemplate {
    public static void checkRequest(ClientHttpRequest request) {
        if (RuleCache.isEmpty()) {
            AgentLogger.getLogger().info("首次调用没有拉取灰度规则");
            return;
        }
        URI asUri = request.getURI();
        String clientName = asUri.getHost().toUpperCase();
        if (!DependencyServices.getDependencyGrayServices().contains(clientName)) {
            return;
        }
        if (RuleCache.isEmptyWithService(clientName)) {
            AgentExecutor.execute(GrayRulePullJob.getSinglePullJob());
            return;
        } else {
            List<GrayPlan> grayPlans = RuleCache.getGrayPlans(clientName);
            for (GrayPlan grayPlan : grayPlans) {
                String requestPath = asUri.getRawPath();
                GrayPath grayPath = grayPlan.getGrayPathMap().get(requestPath);
                if (grayPath != null) {
                    if (isMatch(request,grayPath)) {
                        GrayContextProxy.setGrayHead(grayPlan.getGrayVersion());
                        break;
                    }
                }
                for (GrayPath grayPath1 : grayPlan.getGrayPathList()) {
                    if (isMatch(request,grayPath1)) {
                        AgentLogger.getLogger().info("满足灰度规则，增加灰度标"+grayPlan.getGrayVersion());
                        GrayContextProxy.setGrayHead(grayPlan.getGrayVersion());
                        break;
                    }
                }
            }
        }
        return;
    }

    private static boolean isMatch(ClientHttpRequest request, GrayPath grayPath) {
        boolean isPathMatch = false;
        switch (grayPath.getWildMatchType()) {
            case PathWildType.ActualMatch:
                isPathMatch = request.getURI().getPath().equals(grayPath.getPath());
                break;
            case PathWildType.OneLevelWildMatchEnd:
                if (request.getURI().getPath().startsWith(grayPath.getMatchPath())) {
                    if (!request.getURI().getPath().replace(grayPath.getMatchPath(), "").contains("/")) {
                        isPathMatch = true;
                    }
                }
                break;
            case PathWildType.MutilLevelWildMatchEnd:
                if (request.getURI().getPath().startsWith(grayPath.getMatchPath())) {
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

    private static boolean checkPathRuleMatch(ClientHttpRequest request, GrayPath grayPath) {
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

    private static boolean checkRuleMatch(ClientHttpRequest request, GrayRule rule) {
        try {
            String value = RuleValueGetterForRestTemple.getRuleValue(request, rule);
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
