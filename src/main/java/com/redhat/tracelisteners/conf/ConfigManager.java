package com.redhat.tracelisteners.conf;

public class ConfigManager {

    private static final String JNDI_USER = "jndiUser";
    private static final String JNDI_PASSWORD = "jndiPassword";

    private static String getConfigOrDefault(String varName, String defaultValue) {

        String envValue = System.getenv( varName );
        if (envValue == null) {
            return defaultValue;
        } else {
            return envValue;
        }

    }

    public static String getJndiUser() {
        return getConfigOrDefault(JNDI_USER, "");
    }

    public static String getJndiPassword() {
        return getConfigOrDefault(JNDI_PASSWORD, "");
    }

}
