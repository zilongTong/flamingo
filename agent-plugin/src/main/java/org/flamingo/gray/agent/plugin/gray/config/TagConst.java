package org.flamingo.gray.agent.plugin.gray.config;

/**
 * Created by zhuyx on 2019/8/28.
 * Eureke 灰度metaInfo中的key
 * 以及 http 请求中 灰度请求头的key
 */
public class TagConst {
    public static final String HTTP_GRAY_HEADER_TAG = "x-squirrel-gray-tag";
    public static final String META_DATA_SERVIVE_TAG = "service-gary-versions";
    public static final String META_DATA_INSTANCE_TAG = "gray-meta-tag";
}
