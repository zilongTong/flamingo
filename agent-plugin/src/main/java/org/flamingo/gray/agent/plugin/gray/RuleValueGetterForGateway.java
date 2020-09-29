package org.flamingo.gray.agent.plugin.gray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flamingo.gray.agent.plugin.gray.config.ParamSource;
import org.flamingo.gray.agent.plugin.gray.rule.GrayRule;
import org.flamingo.gray.agent.pure.utils.AgentLogger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by zhuyx on 2019/8/27.
 */
public class RuleValueGetterForGateway {

    public static String getRuleValue(HttpServletRequest request, GrayRule grayRule) {
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
//                value = getValueFromBody(request,grayRule.getParamName());
                value = null;
                break;
                case ParamSource.Random:
                value = String.valueOf(ThreadLocalRandom.current().nextInt(100));
            default:
                break;
        }
        return value;
    }

    private static String getValueFromParam(HttpServletRequest request, String paramName) {
        return request.getParameter(paramName);
    }

    private static String getValueFromHeader(HttpServletRequest request, String paramName) {
        return request.getHeader(paramName.toLowerCase());
    }

    private static String getValueFromCookie(HttpServletRequest request, String paramName) {
        String cookieName = paramName;
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0)
            return null;

        String cookieValue = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().trim().equals(cookieName)) {
                cookieValue = cookie.getValue();
                break;
            }
        }
        return cookieValue;
    }

    //FIXME:破坏body
    private static String getValueFromBody(HttpServletRequest request, String paramName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = request.getReader()) {
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            if("application/json".equals(request.getHeader("Content-Type"))) {
                String body = sb.toString();
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
        } catch (Exception e) {
            AgentLogger.getLogger().info(AgentLogger.getStackTraceString(e));
        }
        return null;
    }
}
