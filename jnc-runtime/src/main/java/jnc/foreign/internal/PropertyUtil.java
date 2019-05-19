package jnc.foreign.internal;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class PropertyUtil {

    static String getStringProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException ex) {
            return defaultValue;
        }
    }

}
