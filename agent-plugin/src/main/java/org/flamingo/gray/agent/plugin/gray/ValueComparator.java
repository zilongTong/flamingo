package org.flamingo.gray.agent.plugin.gray;

import org.flamingo.gray.agent.plugin.gray.config.ComparatorType;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.pure.utils.AgentLogger;

/**
 * Created by zhuyx on 2019/8/27.
 */
public class ValueComparator {
    public static boolean compare(String value, GrayRule rule) {
        boolean isMatch = false;
        if (value != null) {
            switch (rule.getComparator()) {
                case ComparatorType.Equals:
                    if (value.equals(rule.getParamValue())) {
                        isMatch = true;
                    }
                    break;
                case ComparatorType.EndWith:
                    if (value.endsWith(rule.getParamValue())) {
                        isMatch = true;
                    }
                    break;
                case ComparatorType.StartWith:
                    if (value.startsWith(rule.getParamValue())) {
                        isMatch = true;
                    }
                    break;
                case ComparatorType.In:
                    if (rule.getParamValueList().contains(value)) {
                        isMatch = true;
                    }
                    break;
                case ComparatorType.NotIn:
                    if (!rule.getParamValueList().contains(value)) {
                        isMatch = true;
                    }
                    break;
                case ComparatorType.Range:
                    if (rule.getParamRangeList().size() == 2) {
//                        int maxLength = rule.getParamRangeList().get(1).toString().length();
//                        if (value.length() > maxLength) {
//                            value = value.substring(value.length() - maxLength, value.length());
//                        }
                        double longValue;
                        try {
                            longValue = Double.parseDouble(value);
                        } catch (Exception ex) {
                            AgentLogger.getLogger().severe("range parse long exception");
                            break;
                        }
                        if (longValue >= rule.getParamRangeList().get(0) && longValue <= rule.getParamRangeList().get(1)) {
                            isMatch = true;
                            break;
                        }
                    }
                    break;
                case ComparatorType.HashRange:
                    String hash = Integer.toString(value.hashCode());
                    if (rule.getParamRangeList().size() == 2) {

//                        int maxLength = rule.getParamRangeList().get(1).toString().length();
//                        if (hash.length() > maxLength) {
//                            hash = hash.substring(hash.length() - maxLength, hash.length());
//                        }
                        long hashLong;
                        try {
                            hashLong = Long.parseLong(hash);
                        } catch (Exception ex) {
                            AgentLogger.getLogger().severe("HashRange parse long exception");
                            break;
                        }
                        if (hashLong >= rule.getParamRangeList().get(0) && hashLong <= rule.getParamRangeList().get(1)) {
                            isMatch = true;
                            break;
                        }
                    }
                    break;
                case ComparatorType.Random:
                    if (rule.getParamRangeList().size() == 2) {
                        int realValue = Integer.parseInt(value);
                        if (realValue >= rule.getParamRangeList().get(0) && realValue <= rule.getParamRangeList().get(1)) {
                            isMatch = true;
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return isMatch;
    }


}
