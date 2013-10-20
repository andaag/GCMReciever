package no.codebox.gcmreciever.gcm;


import android.app.IntentService;
import android.app.Notification;
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
import no.codebox.gcmreciever.model.GCMMsg;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getName();

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
                assert false;
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                assert false;
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                String raw = extras.getString("data");
                GCMMsg gcmMsg = new GCMMsg(raw);
                messageAsyncHandler.insertMessage(gcmMsg, raw);
                sendNotification(gcmMsg);

                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(GCMMsg msg) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        int notificationDefaults = Notification.DEFAULT_ALL;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setAutoCancel(true)
                        .setTicker(msg.getTitle());

        if (msg.getMessage() != null) {
            builder.setContentText(msg.getMessage()).setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(msg.getMessage()));
        }

        boolean soundEnabled = (notificationDefaults & Notification.DEFAULT_SOUND) == Notification.DEFAULT_SOUND;
        if (msg.getNotificationBoolean("sound", true) != soundEnabled) {
            notificationDefaults |= Notification.DEFAULT_SOUND;
        }

        boolean vibrateEnabled = (notificationDefaults & Notification.DEFAULT_VIBRATE) == Notification.DEFAULT_VIBRATE;
        if (msg.getNotificationBoolean("vibrate", true) != vibrateEnabled) {
            notificationDefaults |= Notification.DEFAULT_VIBRATE;
        }

        int progress = msg.getNotificationNumber("progress", -1).intValue();
        if (progress != -1) {
            builder.setProgress(100, progress, progress == 0);
        }

        int priority = msg.getNotificationNumber("priority", Integer.MIN_VALUE).intValue();
        if (priority != Integer.MIN_VALUE) {
            builder.setPriority(priority);
        }

        builder.setDefaults(notificationDefaults);
        builder.setContentIntent(contentIntent);
        notificationManager.notify(msg.getNotificationKey(), builder.build());
    }
}
