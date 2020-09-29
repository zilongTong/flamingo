package org.flamingo.gray.agent.plugin.gray.config;

/**
 * Created by zhuyx on 2019/8/6.
 * 灰度规则命中相关的比较类型
 */
public class ComparatorType {
    public static final String HashRange = "hashRange";
    public static final String Range = "range";
    public static final String NotIn = "notIn";
    public static final String In = "in";
    public static final String Equals = "equals";
    public static final String StartWith = "startWith";
    public static final String EndWith = "endWith";
    public static final String Contains = "contains";//不支持
    public static final String Random = "random";
}
