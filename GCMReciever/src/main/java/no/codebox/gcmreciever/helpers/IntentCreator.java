package no.codebox.gcmreciever.helpers;

import java.util.Map;

import android.content.Intent;
import android.net.Uri;

import no.codebox.gcmreciever.model.GCMMsg;

public class IntentCreator {
    public static Intent createIntent(GCMMsg msg) {
        Intent intent = new Intent();
        intent.setType(msg.getIntentString("type", null));
        intent.setData(msg.getIntentString("data", null) != null ? Uri.parse(msg.getIntentString("data", null)) : null);
        intent.setClassName(msg.getIntentString("packagename", null), msg.getIntentString("classname", null));
        if (msg.containsKey("extras")) {
            Map<String, Object> extras = (Map<String, Object>) msg.getRaw("extras");
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
