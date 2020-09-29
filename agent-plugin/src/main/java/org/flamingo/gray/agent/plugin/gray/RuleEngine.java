package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.plugin.gray.config.PathWildType;
import org.flamingo.gray.agent.plugin.gray.rpc.GrayRulePullJob;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPath;
import org.flamingo.gray.agent.plugin.gray.rule.GrayPlan;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.plugin.gray.rule.RuleCache;
import org.flamingo.gray.agent.pure.utils.AgentExecutor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import feign.Request;

import java.net.URI;
import java.util.List;

/**
 * Created by zhuyx on 2019/8/24.
 */
public class RuleEngine {

    public static boolean checkRequest(Request request) {
        if (RuleCache.isEmpty()) {
            //AgentLogger.getLogger().info("首次调用没有拉取灰度规则");
            return false;
        }
        URI asUri = URI.create(request.url());
        String clientName = asUri.getHost();
        if (!DependencyServices.getDependencyGrayServices().contains(clientName)) {
            return false;
        }
        if (RuleCache.isEmptyWithService(clientName)) {
            AgentExecutor.execute(GrayRulePullJob.getSinglePullJob());
            return false;
        } else {
            List<GrayPlan> grayPlans = RuleCache.getGrayPlans(clientName);
            for (GrayPlan grayPlan : grayPlans) {
                String requestPath = asUri.getRawPath();
                GrayPath grayPath = grayPlan.getGrayPathMap().get(requestPath);
                if (grayPath != null) {
                    if (isMatch(request, asUri, grayPath)) {
                        GrayContextProxy.setGrayHead(grayPlan.getGrayVersion());
                        return true;
                    }
                }
                for (GrayPath grayPath1 : grayPlan.getGrayPathList()) {
                    if (isMatch(request, asUri, grayPath1)) {
                        AgentLogger.getLogger().info("满足灰度规则，增加灰度标"+grayPlan.getGrayVersion());
                        GrayContextProxy.setGrayHead(grayPlan.getGrayVersion());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isMatch(Request request, URI asUri, GrayPath grayPath) {
        boolean isPathMatch = false;
        switch (grayPath.getWildMatchType()) {
            case PathWildType.ActualMatch:
                isPathMatch = asUri.getRawPath().equals(grayPath.getPath());
                break;
            case PathWildType.OneLevelWildMatchEnd:
                if (asUri.getRawPath().startsWith(grayPath.getMatchPath())) {
                    if (!asUri.getRawPath().replace(grayPath.getMatchPath(), "").contains("/")) {
                        isPathMatch = true;
                    }
                }
                break;
            case PathWildType.MutilLevelWildMatchEnd:
                if (asUri.getRawPath().startsWith(grayPath.getMatchPath())) {
                    isPathMatch = true;
                }
                break;
            case PathWildType.AllWildMatch:
                isPathMatch = true;
                break;
            default:
                break;
        }
        if (!isPathMatch) {
            return false;
        }
        return checkPathRuleMatch(request, asUri, grayPath);
    }

    private static boolean checkPathRuleMatch(Request request, URI asUri, GrayPath grayPath) {
        boolean isMatch = false;
        if (grayPath.getMatchType() == 1) {  //全部命中
            isMatch = true;
            for (GrayRule rule : grayPath.getGrayRuleList()) {

                if (!checkRuleMatch(request, asUri, rule)) {
                    isMatch = false;
                    break;
                }
            }
        } else if (grayPath.getMatchType() == 2) {  //命中一条
            for (GrayRule rule : grayPath.getGrayRuleList()) {
                if (checkRuleMatch(request, asUri, rule)) {
                    isMatch = true;
                    break;
                }
            }
        } else {
            isMatch = false;
        }
        return isMatch;
    }

    private static boolean checkRuleMatch(Request request, URI asUri, GrayRule rule) {
        try {
            String value = RuleValueGetter.getRuleValue(request, asUri, rule);
            if (value == null) {
                return false;
            } else {
                return ValueComparator.compare(value, rule);
            }

        } catch (Exception ex) {
            AgentLogger.getLogger().severe("checkRuleMatchError:" + ex.getMessage());
        }
        return false;
    }
}
