package no.codebox.gcmreciever.helpers;

import java.util.Map;

public class HashReader {
    public static String getString(Map raw, String key, String defaultValue) {
        if (raw != null) {
            Object data = raw.get(key);
            if (data != null) {
                return data.toString();
            }
        }
        return defaultValue;
    }

    public static Number getNumber(Map raw, String key, Number defaultValue) {
        if (raw != null) {
            Object data = raw.get(key);
            if (data != null && data instanceof Number) {
                return (Number) data;
            }
        }
        return defaultValue;
    }

    public static boolean getBoolean(Map raw, String key, boolean defaultValue) {
        if (raw != null) {
            Object data = raw.get(key);
            if (data != null && data instanceof Boolean) {
                return (Boolean) data;
            }
        }
        return defaultValue;
    }
}
