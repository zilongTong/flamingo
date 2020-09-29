package org.flamingo.gray.agent.gray.async;

import org.flamingo.gray.agent.gray.GrayContext;

import java.util.TimerTask;

public class GrayTimerTask extends TimerTask {

    private String grayTag;
    private TimerTask timerTask;

    public GrayTimerTask(String grayTag, TimerTask timerTask) {
        this.grayTag = grayTag;
        this.timerTask = timerTask;
    }

    @Override
    public void run() {
        try {
            GrayContext.setGrayHead(grayTag);
            if(timerTask != null) {
                this.timerTask.run();
            }
        } finally {
            GrayContext.clearContext();
        }

    }
}
