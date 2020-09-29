package org.flamingo.gray.agent.plugin.gray.rpc;

import java.util.List;

/**
 * Created by zhuyx on 2019/8/26.
 */
public class GetMetaResponse {
    private int code;
    private int planId;
    private String grayMetaTag;
    private List<String> serviceGrayVersions;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getGrayMetaTag() {
        return grayMetaTag;
    }

    public void setGrayMetaTag(String grayMetaTag) {
        this.grayMetaTag = grayMetaTag;
    }

    public List<String> getServiceGrayVersions() {
        return serviceGrayVersions;
    }

    public void setServiceGrayVersions(List<String> serviceGrayVersions) {
        this.serviceGrayVersions = serviceGrayVersions;
    }

    public String getServiceTag(){
        if(serviceGrayVersions == null || serviceGrayVersions.size() ==0){
            return null;
        }else{
            return String.join(",",serviceGrayVersions);
        }
    }
}
