package no.codebox.gcmreciever.gcm;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import no.codebox.gcmreciever.helpers.MsgHandler;
import no.codebox.gcmreciever.model.GCMMsg;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getName();

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        MsgHandler msgHandler = new MsgHandler(this);
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                assert false;
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                assert false;
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                String raw = extras.getString("data");
                GCMMsg gcmMsg = new GCMMsg(raw);
                msgHandler.handleMessage(gcmMsg);

                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
