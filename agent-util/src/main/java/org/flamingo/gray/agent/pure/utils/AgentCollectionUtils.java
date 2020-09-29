package org.flamingo.gray.agent.pure.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by zhuyx on 2019/8/24.
 * copy from org.springframework.util.CollectionUtils
 */
public class AgentCollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
