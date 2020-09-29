package org.flamingo.gray.agent.plugin.gray.rpc;

import org.flamingo.gray.agent.plugin.gray.DependencyServices;
import org.flamingo.gray.agent.plugin.gray.rule.RuleCache;
import org.flamingo.gray.agent.pure.utils.AgentExecutor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyx on 2019/8/26.
 */
public class GrayRulePullJob implements Runnable {
    private static GrayRulePullJob singlePullJob;
    private static long lastPullTime = 0;
    private static volatile boolean isRunning = false;
    private static volatile boolean isStarted = false;

    static {
        singlePullJob = new GrayRulePullJob();
    }

    private GrayRulePullJob() {

    }

    @Override
    public void run() {
        try {
            if (isRunning) {
                return;
            }
            isRunning = true;
            if (RuleCache.isGatewayApp()) {
                RuleCache.setCurrentGatewayRules(RemoteService.getGrayRuleForGateway());
            }
            if (DependencyServices.getDependencyGrayServices().size() == 0) {
                return;
            }
            if (System.currentTimeMillis() - lastPullTime < 5000) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    AgentLogger.getLogger().warning("pullGrayRuleJobStart Thread.sleep InterruptedException");
                }
            }
            if (System.currentTimeMillis() - lastPullTime < 5000) {
                return;
            }

//            AgentLogger.getLogger().info("pullGrayRuleJobStart, service:" + DependencyServices.getDependencyGrayServices().toString());
            //TODO RemoteService.getGrayRule need to get gray rule
            GetRuleResponse getRuleResponse = RemoteService.getGrayRule();
            RuleCache.setCurrentGetRuleResponse(getRuleResponse);
//            AgentLogger.getLogger().info("pullGrayRuleJobEnd");

        } catch (Throwable throwable) {
            AgentLogger.getLogger().info("pullGrayRuleJobError:" + AgentLogger.getStackTraceString(throwable));
        } finally {
            isRunning = false;
            lastPullTime = System.currentTimeMillis();
        }
    }

    public static GrayRulePullJob getSinglePullJob() {
        return singlePullJob;
    }

    public static void startJob() {
        if (!isStarted) {
            synchronized (GrayRulePullJob.class) {
                if (!isStarted) {
                    AgentExecutor.scheduleWithFixedDelay(GrayRulePullJob.getSinglePullJob(), 3, 20, TimeUnit.SECONDS);
                    isStarted = true;
                }
            }
        }
    }

    public static void fireJobNow() {
        startJob();
        AgentExecutor.execute(singlePullJob);
    }

    private static volatile boolean getGatewayListIsRunning = false;
    private static Runnable getGatewayList = () -> {
        try {
            if (getGatewayListIsRunning) {
                return;
            }
            getGatewayListIsRunning = true;
            RuleCache.setGatewayList(RemoteService.getGatewayAppIds());
        } catch (Exception ex) {
            AgentLogger.getLogger().warning("getGatewayList Call, " + AgentLogger.getStackTraceString(ex));
            getGatewayListIsRunning = false;
        }
    };

    public static Runnable getGatewayListCall() {
        return getGatewayList;
    }
}
