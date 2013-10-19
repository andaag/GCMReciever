package no.codebox.gcmreciever.gcm;


import java.io.IOException;
import java.util.Map;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import no.codebox.gcmreciever.MainActivity;
import no.codebox.gcmreciever.R;
import no.codebox.gcmreciever.db.MessageAsyncHandler;
import no.codebox.gcmreciever.helpers.JsonParser;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getName();
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            MessageAsyncHandler messageAsyncHandler = new MessageAsyncHandler(getContentResolver());
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                String rawData = extras.getString("data");
                try {
                    Map data = (Map) JsonParser.parseBlock(rawData);
                    messageAsyncHandler.insertMessage(rawData, data.containsKey("expires") ? ((Number) data.get("expires")).longValue() : 0);
                    sendNotification("Received: " + extras.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    sendNotification("FAILED TO PARSE: " + extras.toString());
                }

                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
