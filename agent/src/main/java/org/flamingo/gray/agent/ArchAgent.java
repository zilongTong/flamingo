package org.flamingo.gray.agent;

import org.flamingo.gray.agent.config.ConfigProperties;
import org.flamingo.gray.agent.config.InterceptConfig;
import org.flamingo.gray.agent.gray.async.transformer.ExecutorTransformer;
import org.flamingo.gray.agent.gray.async.transformer.ForkJoinTransformer;
import org.flamingo.gray.agent.gray.async.transformer.GrayTransformer;
import org.flamingo.gray.agent.gray.async.transformer.TimerTaskTransformer;
import org.flamingo.gray.agent.interceptor.InterceptorWrapper;
import org.flamingo.gray.agent.utils.AgentLogger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.List;


/**
 * javaagent核心类，通过ByteBuddy 和 Javaassist 向Instrument添加了一系列Transformer，以此达到修改和替换类的定义的目的
 */
public class ArchAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        loadConfig();
        try {
            AgentBuilder agentBuilder = new AgentBuilder.Default();

            for (InterceptConfig config : InterceptConfig.getConfigs()) {
                agentBuilder = agentBuilder.type(ElementMatchers.named(config.getClassName())).transform((builder, typeDescription, classLoader, module) -> {
                    builder = builder.method(ElementMatchers.named(config.getMethodName()))
                            .intercept(MethodDelegation.to(new InterceptorWrapper(config.getInterceptorClass())));
                    return builder;
                });
            }
            if(agentArgs == null || Boolean.valueOf(agentArgs)) {
                inst.addTransformer(new GrayTransformer(new ExecutorTransformer(), new TimerTaskTransformer(), new ForkJoinTransformer()), true);
            }
            agentBuilder.installOn(inst);

        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("install agent exception: " + AgentLogger.getStackTraceString(throwable));
        } finally {
            AgentLogger.getLogger().info("ArchAgent installOn finally !!!!!!");
        }
    }

    private static void loadConfig() {
        List<InterceptConfig> list = ConfigProperties.getInterceptConfig();
        if (list != null && list.size() > 0) {
            list.stream().forEach(item -> InterceptConfig.getConfigs().add(item));
        }
    }
}
