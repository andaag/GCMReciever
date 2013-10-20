package no.codebox.gcmreciever.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.res.Resources;
import android.graphics.Color;

import no.codebox.gcmreciever.helpers.HashReader;
import no.codebox.gcmreciever.helpers.JsonParser;

public class GCMMsg {
    private static final Random random = new Random();
    private final Map<String, Object> raw;

    public GCMMsg(String data) {
        Map<String, Object> raw;
        try {
            raw = JsonParser.parseBlock(data);
        } catch (Exception e) {
            raw = new HashMap<String, Object>();
            raw.put("title", "Failed to parse");
            raw.put("message", data + "\n\nException:" + e);
        }
        this.raw = raw;
    }

    public String getString(String key, String defaultValue) {
        return HashReader.getString(raw, key, defaultValue);
    }

    public Number getNumber(String key, Number defaultValue) {
        return HashReader.getNumber(raw, key, defaultValue);
    }

    public String getTitle() {
        return getString("title", "msg missing title!");
    }

    public String getMessage() {
        return getString("message", null);
    }

    public int getIcon() {
        String key = getString("icon", "info");
        if (key.equals("alert")) {
            return android.R.drawable.ic_dialog_alert;
        }
        return android.R.drawable.ic_dialog_info;
    }

    public int getIconBackground(Resources r) {
        String colorString = getString("icon-background", null);
        if (colorString != null) {
            try {
                int result = Color.parseColor(colorString);
                /*if (!colorString.startsWith("#") || colorString.length() == 9) {
                    //@todo : no alpha channel sent, lets add some
                }*/
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r.getColor(android.R.color.holo_orange_light);
    }

    public String getNotificationString(String key, String defaultValue) {
        return HashReader.getString((Map) raw.get("notification"), key, defaultValue);
    }

    public Number getNotificationNumber(String key, Number defaultValue) {
        return HashReader.getNumber((Map) raw.get("notification"), key, defaultValue);
    }

    public boolean getNotificationBoolean(String key, boolean defaultValue) {
        return HashReader.getBoolean((Map) raw.get("notification"), key, defaultValue);
    }


    public String getIntentString(String key, String defaultValue) {
        return HashReader.getString((Map) raw.get("intent"), key, defaultValue);
    }

    public Number getIntentNumber(String key, Number defaultValue) {
        return HashReader.getNumber((Map) raw.get("intent"), key, defaultValue);
    }

    public boolean getIntentBoolean(String key, boolean defaultValue) {
        return HashReader.getBoolean((Map) raw.get("intent"), key, defaultValue);
    }

    public int getNotificationKey() {
        String key = getNotificationString("notification-key", null);
        if (key == null) {
            return random.nextInt();
        }
        return key.hashCode();
    }

    public boolean hasIntent() {
        return raw.get("intent") != null && ((Map) raw.get("intent")).containsKey("packagename")
                && ((Map) raw.get("intent")).containsKey("classname");
    }

    public boolean containsKey(String key) {
        return raw.containsKey(key);
    }

    public Object getRaw(String key) {
        return raw.get(key);
    }
}