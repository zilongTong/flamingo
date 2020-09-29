package org.flamingo.gray.agent.pure;

import org.flamingo.gray.agent.pure.utils.ReflectionUtils;


public class Stub {
    
    public static void main(String[] args) {
        ReflectionUtils.invokeStaticMethod("org.flamingo.gray.agent.pure.utils.CacheProxy", "getCache", "test");
    }
}
