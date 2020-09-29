package org.flamingo.gray.agent.config;

import org.flamingo.gray.agent.classloader.AgentPackagePath;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 解析 agent.properties 文件，产生【被拦截类】，【被拦截方法】，【拦截方法】的list
 */
public class ConfigProperties {
    private static Properties configProperties = new Properties();
    private final static String filePath = "/agent.properties";
    private final static String key = "arch.agent.interceptors";

    static {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(AgentPackagePath.getPath() + filePath);
            configProperties.load(fileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<InterceptConfig> getInterceptConfig() {
        List<InterceptConfig> configs = new ArrayList<>();
        String props = configProperties.get(key).toString();
        String[] interceptors = props.split("/");
        for (int i = 0; i < interceptors.length; i++) {
            String interceptor = interceptors[i];
            String[] element = interceptor.split(",");
            configs.add(new InterceptConfig(element[0], element[1], element[2]));
        }
        return configs;
    }

    public static String getConfig(String configKey) {
        Object value = configProperties.get(configKey);
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

}
