package org.flamingo.gray.agent.gray;

public class GrayContext {
    private static final ThreadLocal<String> current_gray_Head = new ThreadLocal<>();
    private static final ThreadLocal<String> current_gray_trace_id = new ThreadLocal<>();

    public static final String getGrayHead() {
        return current_gray_Head.get();
    }

    public static final String getTraceId() {
        return current_gray_trace_id.get();
    }

    public static final void setGrayHead(String gray) {
        current_gray_Head.set(gray);
    }

    public static final void setTraceId(String traceId) {
        current_gray_trace_id.set(traceId);
    }

    public static void clearContext() {
        current_gray_Head.remove();
        current_gray_trace_id.remove();
    }
}
