package org.flamingo.gray.agent.plugin.gray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flamingo.gray.agent.plugin.gray.config.ParamSource;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.pure.utils.AgentLogger;
import org.springframework.http.client.ClientHttpRequest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RuleValueGetterForRestTemple {
    public static String getRuleValue(ClientHttpRequest request, GrayRule grayRule) {
        String value = null;
        switch (grayRule.getParamType()) {
            case ParamSource.Cookie:
                value = getValueFromCookie(request, grayRule.getParamName());
                break;
            case ParamSource.Header:
                value = getValueFromHeader(request, grayRule.getParamName());
                break;
            case ParamSource.Param:
                value = getValueFromParam(request, grayRule.getParamName());
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

    private static String getValueFromParam(ClientHttpRequest request, String paramName) {
        if (request.getURI().getQuery() != null) {
            for (String paramRaw : request.getURI().getQuery().split("&")) {
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

    private static String getValueFromHeader(ClientHttpRequest request, String headerName) {
        Collection<String> values = request.getHeaders().get(headerName.toLowerCase());
        if (values != null && values.size() > 0) {
            List<String> list = new ArrayList<>(values);
            if (list != null && list.size() != 0) {
                return list.get(0);
            }
        }
        return null;
    }

    private static String getValueFromCookie(ClientHttpRequest request, String cookieName) {
        Collection<String> values = request.getHeaders().get("cookie");
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

    private static String getValueFromBody(ClientHttpRequest request, String paramName) {
        try (ByteArrayOutputStream bos = (ByteArrayOutputStream) request.getBody()){
            if (bos != null && bos.toByteArray() != null) {
                List<String> contentType = (ArrayList) request.getHeaders().get("Content-Type");
                if (contentType != null && contentType.get(0).contains("application/json")) {
                    String body = new String(bos.toByteArray());
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
            AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
        return null;
    }
}
