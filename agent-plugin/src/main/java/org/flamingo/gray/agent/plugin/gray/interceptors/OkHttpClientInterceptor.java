package org.flamingo.gray.agent.plugin.gray.interceptors;


import org.flamingo.gray.agent.plugin.gray.GrayContextProxy;
import org.flamingo.gray.agent.plugin.gray.config.TagConst;
import org.flamingo.gray.agent.pure.interceptor.Interceptor;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.flamingo.gray.agent.pure.utils.AgentStringUtils;
import okhttp3.Headers;
import okhttp3.Request;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 用于okhttp调用服务时传递 http 灰度请求头
 */
public class OkHttpClientInterceptor implements Interceptor {
    static Field namesAndValuesField;

    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            String grayTag = GrayContextProxy.getGrayHead();

            if (allArguments != null && AgentStringUtils.isNotBlank(grayTag)) {
                Request request = (Request) allArguments[0];
                Headers headers = request.headers();
                if (namesAndValuesField == null) {
                    namesAndValuesField = headers.getClass().getDeclaredField("namesAndValues");
                    namesAndValuesField.setAccessible(true);
                }
                String[] namesAndValues = (String[]) namesAndValuesField.get(headers);
                String[] newNamesAndValues;
                newNamesAndValues = new String[namesAndValues.length + 2];
                System.arraycopy(namesAndValues, 0, newNamesAndValues, 0, namesAndValues.length);
                //AgentLogger.getLogger().info("通过OKHttp调用，传递灰度标");
                newNamesAndValues[namesAndValues.length] = TagConst.HTTP_GRAY_HEADER_TAG;
                newNamesAndValues[namesAndValues.length + 1] = grayTag;
                namesAndValuesField.set(headers, newNamesAndValues);
            }
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("OkHttpClientInterceptor," + AgentLogger.getStackTraceString(throwable));
        } finally {
//            SubEnvContext.clearContext();
        }
        return zuper.call();
    }
}
