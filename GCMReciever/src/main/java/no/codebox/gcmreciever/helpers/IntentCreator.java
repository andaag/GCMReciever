package no.codebox.gcmreciever.helpers;

import java.util.Map;

import android.content.Intent;
import android.net.Uri;

public class IntentCreator {
    public static Intent createIntent(Map data) {
        Intent intent = new Intent();
        intent.setType(data.containsKey("type") ? (String) data.get("type") : null);
        intent.setData(data.containsKey("data") ? Uri.parse((String) data.get("data")) : null);
        intent.setClassName((String) data.get("packagename"), (String) data.get("classname"));
        if (data.containsKey("extras")) {
            Map<String, Object> extras = (Map<String, Object>) data.get("extras");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value instanceof Number) {
                    intent.putExtra(key, (Number) value);
                } else if (value instanceof Boolean) {
                    intent.putExtra(key, (Boolean) value);
                } else {
                    intent.putExtra(key, (String) value);
                }

            }
        }
        return intent;
    }
}
