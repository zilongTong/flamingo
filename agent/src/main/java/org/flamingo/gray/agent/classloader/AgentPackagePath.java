package org.flamingo.gray.agent.classloader;

import org.flamingo.gray.agent.utils.AgentLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 定位 arch-agent.jar,并返回其父path
 */
public class AgentPackagePath {

    private static File AGENT_PACKAGE_PATH;

    public static File getPath() {
        if (AGENT_PACKAGE_PATH == null) {
            AGENT_PACKAGE_PATH = findPath();
        }
        return AGENT_PACKAGE_PATH;
    }

    public static boolean isPathFound() {
        return AGENT_PACKAGE_PATH != null;
    }

    private static File findPath() {
        String classResourcePath = AgentPackagePath.class.getName().replaceAll("\\.", "/") + ".class";

        URL resource = ClassLoader.getSystemClassLoader().getResource(classResourcePath);
        if (resource != null) {
            String urlString = resource.toString();

//            AgentLogger.getLogger().info("The beacon class location is " + urlString);

            int insidePathIndex = urlString.indexOf('!');
            boolean isInJar = insidePathIndex > -1;

            if (isInJar) {
                urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
                File agentJarFile = null;
                try {
                    agentJarFile = new File(new URL(urlString).toURI());
                } catch (MalformedURLException e) {
                    AgentLogger.getLogger().severe("Can not locate agent jar file by url:" + urlString + " ex:" + AgentLogger.getStackTraceString(e));
                } catch (URISyntaxException e) {
                    AgentLogger.getLogger().severe("Can not locate agent jar file by url:" + urlString + " ex:" + AgentLogger.getStackTraceString(e));
                }
                if (agentJarFile.exists()) {
                    return agentJarFile.getParentFile();
                }
            } else {
                int prefixLength = "file:".length();
                String classLocation = urlString.substring(prefixLength, urlString.length() - classResourcePath.length());
                return new File(classLocation);
            }
        }

        AgentLogger.getLogger().severe("Can not locate agent jar file.");
        throw new RuntimeException("Can not locate agent jar file.");
    }
}
