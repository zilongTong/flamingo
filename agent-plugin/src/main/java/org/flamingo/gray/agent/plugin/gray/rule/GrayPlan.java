package org.flamingo.gray.agent.plugin.gray.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhuyx on 2019/8/27.
 */
public class GrayPlan {
    private int planId;
    private String name;
    private String grayVersion;
    private String serviceName;
    private Map<String, GrayPath> grayPathMap = new ConcurrentHashMap<>();
    private List<GrayPath> grayPathList = new ArrayList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrayVersion() {
        return grayVersion;
    }

    public void setGrayVersion(String grayVersion) {
        this.grayVersion = grayVersion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, GrayPath> getGrayPathMap() {
        return grayPathMap;
    }

    public void setGrayPathMap(Map<String, GrayPath> grayPathMap) {
        this.grayPathMap = grayPathMap;
    }

    public List<GrayPath> getGrayPathList() {
        return grayPathList;
    }

    public void setGrayPathList(List<GrayPath> grayPathList) {
        this.grayPathList = grayPathList;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }
}
