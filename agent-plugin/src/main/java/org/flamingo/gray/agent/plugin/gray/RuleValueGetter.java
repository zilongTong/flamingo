package org.flamingo.gray.agent.plugin.gray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flamingo.gray.agent.plugin.gray.config.ParamSource;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import feign.Request;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zhuyx on 2019/8/27.
 */
public class RuleValueGetter {

    public static String getRuleValue(Request request, URI asUri, GrayRule grayRule) {
        String value = null;
        switch (grayRule.getParamType()) {
            case ParamSource.Cookie:
                value = getValueFromCookie(request, grayRule.getParamName());
                break;
            case ParamSource.Header:
                value = getValueFromHeader(request, grayRule.getParamName());
                break;
            case ParamSource.Param:
                value = getValueFromParam(asUri, grayRule.getParamName());
                break;
            case ParamSource.Body:
                value = getValueFromBody(request,grayRule.getParamName());
                break;
            case ParamSource.Random:
                value = String.valueOf(ThreadLocalRandom.current().nextInt(100));
            default:
                break;
        }
        return value;
    }

    private static String getValueFromParam(URI asUri, String paramName) {
        if (asUri.getQuery() != null) {
            for (String paramRaw : asUri.getQuery().split("&")) {
                String[] paramPair = paramRaw.split("=");
                if (paramPair.length == 2) {
                    if (paramPair[0].equals(paramName)) {
                        return paramPair[1];
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    private static String getValueFromHeader(Request request, String headerName) {
        Collection<String> values = request.headers().get(headerName.toLowerCase());
        if (values != null && values.size() > 0) {
            List<String> list = new ArrayList<>(values);
            if (list != null && list.size() != 0) {
                return list.get(0);
            }
        }
        return null;
    }

    private static String getValueFromCookie(Request request, String cookieName) {
        Collection<String> values = request.headers().get("cookie");
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

    private static String getValueFromBody(Request request, String paramName) {
        try {
            if (request.body() != null) {
                List<String> contentType = (ArrayList) request.headers().get("Content-Type");
                if (contentType != null && contentType.size() == 1 && contentType.get(0).contains("application/json")) {
                    String body = new String(request.body());
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
                }
            }
        }catch (Exception e){
            AgentLogger.getLogger().severe("error");
        }
        return null;
    }

}
