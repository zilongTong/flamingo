package org.flamingo.gray.agent.gray.async;


import org.flamingo.gray.agent.gray.GrayContext;

import java.util.concurrent.Callable;

/**
 * Created by zhuyx on 2019/9/21.
 */
public class GrayCallable implements Callable {
    private String grayTag;
    private Callable wrapCallable;

    public GrayCallable(String grayTag, Callable wrapCallable) {
        this.grayTag = grayTag;
        this.wrapCallable = wrapCallable;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof GrayCallable) {
            GrayCallable that = (GrayCallable) o;
            return wrapCallable.equals(that.wrapCallable);
        } else if (o instanceof Callable) {
            Callable that = (Callable) o;
            return wrapCallable.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return wrapCallable.hashCode();
    }
}
