package no.codebox.gcmreciever.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.res.Resources;
import android.graphics.Color;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.codebox.gcmreciever.helpers.HashReader;
import no.codebox.gcmreciever.helpers.JsonParser;

public class GCMMsg {
    private final Map<String, Object> raw;

    public static GCMMsg createHeartbeatMsg(String heartbeatKey) {
        Map<String, Object> raw = new HashMap<String, Object>();
        raw.put("title", "Heartbeat failure!");
        raw.put("message", "No heartbeat recieved from " + heartbeatKey);
        raw.put("notification", createNotification(true, true, 100));
        raw.put("icon", "alert");
        raw.put("icon-background", "red");
        return new GCMMsg(raw);
    }

    private static Map<String, Object> createNotification(boolean vibrate, boolean sound, int priority) {
        Map<String, Object> raw = new HashMap<String, Object>();
        raw.put("vibrate", vibrate);
        raw.put("sound", sound);
        raw.put("priority", priority);
        return raw;
    }

    private GCMMsg(Map<String, Object> raw) {
        this.raw = raw;
    }

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

    public boolean isHeartbeat() {
        return raw.containsKey("heartbeat");
    }

    public Number getHeartbeatInterval() {
        return HashReader.getNumber((Map) raw.get("heartbeat"), "interval", 0);
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
            return new Random().nextInt();
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

    public void prependMessage(String s) {
        if (getMessage() != null) {
            s = s + "\n" + getMessage();
        }
        raw.put("message", s);
    }

    public String getHeartbeatKey() {
        return HashReader.getString((Map) raw.get("heartbeat"), "key", null);
    }

    public Object getRaw(String key) {
        return raw.get(key);
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(raw);
    }

    @Override
    public String toString() {
        return "GCMMsg:" + getTitle();
    }

}