package org.flamingo.gray.agent.plugin.gray.rpc;

import org.flamingo.gray.agent.plugin.gray.rule.GrayPlan;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhuyx on 2019/8/24.
 */
public class GetRuleResponse {
    private Map<String, List<GrayPlan>> grayPlanMap = new ConcurrentHashMap<>();

    public Map<String, List<GrayPlan>> getGrayPlanMap() {
        return grayPlanMap;
    }

    public void setGrayPlanMap(Map<String, List<GrayPlan>> grayPlanMap) {
        this.grayPlanMap = grayPlanMap;
    }

}
