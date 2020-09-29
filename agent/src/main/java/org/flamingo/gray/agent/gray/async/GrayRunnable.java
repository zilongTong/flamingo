package org.flamingo.gray.agent.gray.async;


import org.flamingo.gray.agent.gray.GrayContext;

/**
 * Created by zhuyx on 2019/9/21.
 */
public class GrayRunnable implements Runnable {
    private String grayTag;
    private Runnable wrapRunnable;

    public GrayRunnable(String grayTag, Runnable runnable) {
//        if(runnable == null){
//            AgentLogger.getLogger().info("runnable is null");
//        }
        this.grayTag = grayTag;
        this.wrapRunnable = runnable;
    }

    @Override
    public void run() {
        try {
            GrayContext.setGrayHead(grayTag);
            if(wrapRunnable != null) {
                this.wrapRunnable.run();
            }
        } finally {
            GrayContext.clearContext();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof GrayRunnable) {
            GrayRunnable that = (GrayRunnable) o;
            return wrapRunnable.equals(that.wrapRunnable);
        } else if (o instanceof Runnable) {
            Runnable that = (Runnable) o;
            return wrapRunnable.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return wrapRunnable.hashCode();
    }
}
