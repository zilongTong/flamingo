package org.flamingo.gray.agent.gray.async;

import org.flamingo.gray.agent.gray.GrayContext;

import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * Created by zhuyx on 2019/9/23.
 */
public class RunnableWrapper {

    public static Runnable wrapRunnable(Runnable runnable) {
//        AgentLogger.getLogger().info("GrayRunnable.wrapRunnable");
        if (runnable instanceof GrayRunnable) {
            return runnable;
        } else {
            return new GrayRunnable(GrayContext.getGrayHead(), runnable);
        }
    }

    public static Callable wrapCallable(Callable callable) {
//        AgentLogger.getLogger().info("GrayRunnable.wrapCallable");
        if (callable instanceof GrayCallable) {
            return callable;
        } else {
            return new GrayCallable(GrayContext.getGrayHead(), callable);
        }
    }

    public static TimerTask wrapTimerTask(TimerTask timerTask) {
        if (timerTask instanceof GrayTimerTask) {
            return timerTask;
        } else {
            return new GrayTimerTask(GrayContext.getGrayHead(), timerTask);
        }
    }

    public static GraySyncTask wrapAsync(Object target) {
        return new GraySyncTask(GrayContext.getGrayHead(), target);
    }
}
