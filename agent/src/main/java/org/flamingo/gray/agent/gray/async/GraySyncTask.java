package org.flamingo.gray.agent.gray.async;

import org.flamingo.gray.agent.gray.GrayContext;

import java.util.concurrent.Callable;

public class GraySyncTask implements Runnable, Callable {
    private String grayTag;
    private Runnable wrapRunnable;
    private Callable wrapCallable;

    public GraySyncTask(String gray, Object target){
        this.grayTag = gray;
        if(target instanceof Runnable){
            this.wrapRunnable = (Runnable)target;
        }else if(target instanceof Callable){
            this.wrapCallable = (Callable)target;
        }
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
    public Object call() throws Exception {
        try {
            GrayContext.setGrayHead(grayTag);
            if (wrapCallable != null) {
                return this.wrapCallable.call();
            } else {
                return null;
            }
        } finally {
            GrayContext.clearContext();
        }
    }
}
