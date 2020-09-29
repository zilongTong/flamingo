package org.flamingo.gray.agent.plugin.gray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import org.flamingo.gray.agent.plugin.gray.config.ParamSource;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RuleValueGetterForZuul {
    public static String getRuleValue(RibbonCommandContext ribbonCommandContext, GrayRule grayRule) {
        String value = null;
        switch (grayRule.getParamType()) {
            case ParamSource.Cookie:
                value = getValueFromCookie(ribbonCommandContext, grayRule.getParamName());
                break;
            case ParamSource.Header:
                value = getValueFromHeader(ribbonCommandContext, grayRule.getParamName());
                break;
            case ParamSource.Param:
                value = getValueFromParam(ribbonCommandContext, grayRule.getParamName());
                break;
            case ParamSource.Body:
                value = getValueFromBody(ribbonCommandContext,grayRule.getParamName());
                break;
            case ParamSource.Random:
                value = String.valueOf(ThreadLocalRandom.current().nextInt(100));
            default:
                break;
        }
        return value;
    }

    private static String getValueFromParam(RibbonCommandContext ribbonCommandContext, String paramName) {
        return ribbonCommandContext.getParams().getFirst(paramName);
    }

    private static String getValueFromHeader(RibbonCommandContext ribbonCommandContext, String paramName) {
        return ribbonCommandContext.getHeaders().getFirst(paramName.toLowerCase());
    }

    private static String getValueFromCookie(RibbonCommandContext ribbonCommandContext, String cookieName) {
        Collection<String> values = ribbonCommandContext.getHeaders().get("cookie");
        if (values != null && values.size() > 0) {
            List<String> list = new ArrayList<>(values);
            if (list != null && list.size() != 0) {
                String cookies = list.get(0);
                for (String cookieRaw : cookies.split(";")) {
                    String[] cookiePair = cookieRaw.split("=");
                    if (cookiePair.length == 2) {
                        if (cookiePair[0].trim().equals(cookieName)) {
                            return cookiePair[1];
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return null;
    }

    static Field field;

    private static String getValueFromBody(RibbonCommandContext ribbonCommandContext, String paramName) {
        if("application/json".equals(ribbonCommandContext.getHeaders().getFirst("Content-Type"))) {
            try  {
                if(field == null) {
                    field = ServletInputStreamWrapper.class.getDeclaredField("data");
                    field.setAccessible(true);
                }
                byte[] bytes = (byte[]) field.get(ribbonCommandContext.getRequestEntity());
                String body = new String(bytes);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(body);
                String[] keys = paramName.split("\\.");
                for (String key : keys) {
                    if (jsonNode == null || jsonNode.isNull()) {
                        return null;
                    } else if (jsonNode.isArray()) {
                        jsonNode = jsonNode.elements().next().get(key);
                    } else{
                        jsonNode = jsonNode.get(key);
                    }
                }
                if (jsonNode.isNull())
                    return null;
                else
                    return jsonNode.toString();
                }catch (Exception e) {
                AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
            }
        }
        return null;
    }
}
