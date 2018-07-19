package jnc.foreign.internal;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class PropertyUtil {

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        try {
            String value = System.getProperty(key);
            if (value != null) {
                return Boolean.parseBoolean(value);
            }
        } catch (SecurityException ignored) {
        }
        return defaultValue;
    }

    public static Integer getIntProperty(String key, Integer defaultValue) {
        try {
            return Integer.getInteger(key, defaultValue);
        } catch (SecurityException ex) {
            return defaultValue;
        }
    }

    static String getStringProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException ex) {
            return defaultValue;
        }
    }

}
