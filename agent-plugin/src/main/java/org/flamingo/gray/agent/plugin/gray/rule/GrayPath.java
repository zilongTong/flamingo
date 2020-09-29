package org.flamingo.gray.agent.plugin.gray.rule;

import java.util.ArrayList;
import java.util.List;

public class GrayPath {
    private List<GrayRule> grayRuleList = new ArrayList<>();

    private Integer id;
    private Integer planId;
    private String path;
    private int wildMatchType;
    private String matchPath;
    private int pathType;
    private Integer matchType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public Integer getMatchType() {
        return matchType;
    }

    public void setMatchType(Integer matchType) {
        this.matchType = matchType;
    }

    public List<GrayRule> getGrayRuleList() {
        return grayRuleList;
    }

    public void setGrayRuleList(List<GrayRule> grayRuleList) {
        this.grayRuleList = grayRuleList;
    }

    public int getPathType() {
        return pathType;
    }

    public void setPathType(int pathType) {
        this.pathType = pathType;
    }

    public int getWildMatchType() {
        return wildMatchType;
    }

    public void setWildMatchType(int wildMatchType) {
        this.wildMatchType = wildMatchType;
    }

    public String getMatchPath() {
        return matchPath;
    }

    public void setMatchPath(String matchPath) {
        this.matchPath = matchPath;
    }
}