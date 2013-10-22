package no.codebox.gcmreciever.helpers;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import no.codebox.gcmreciever.MainActivity;
import no.codebox.gcmreciever.R;
import no.codebox.gcmreciever.db.HeartbeatAsyncHandler;
import no.codebox.gcmreciever.db.MessageAsyncHandler;
import no.codebox.gcmreciever.model.GCMMsg;

public class MsgHandler {
    private static final String TAG = MsgHandler.class.getName();

    private MessageAsyncHandler messageAsyncHandler = null;
    private HeartbeatAsyncHandler heartbeatAsyncHandler = null;
    private final Context context;

    public MsgHandler(Context context) {
        this.context = context;
    }

    public void handleMessage(GCMMsg msg) {
        Log.d(TAG, "handleMessage " + msg);
        if (messageAsyncHandler == null) {
            messageAsyncHandler = new MessageAsyncHandler(context.getContentResolver());
        }
        //@todo : if expired then dont show.
        if (msg.isHeartbeat()) {
            handleHeartbeat(msg);
        }
        messageAsyncHandler.insertMessage(msg);
        if (msg.containsKey("notification")) {
            //@todo : expire notification as well when neccesary!
            sendNotification(msg);
        }
    }

    private void handleHeartbeat(GCMMsg msg) {
        if (heartbeatAsyncHandler == null) {
            heartbeatAsyncHandler = new HeartbeatAsyncHandler(context.getContentResolver());
        }
        int interval = msg.getHeartbeatInterval().intValue();
        if (interval == 0) {
            msg.prependMessage("Heartbeat disabled for key " + msg.getHeartbeatKey());
            heartbeatAsyncHandler.deleteHeartbeat(msg);
        } else {
            msg.prependMessage("Heartbeat enabled with key " + msg.getHeartbeatKey());
            heartbeatAsyncHandler.insertHeartbeat(msg);
        }

    }

    private void sendNotification(GCMMsg msg) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        int notificationDefaults = Notification.DEFAULT_ALL;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(msg.getTitle())
                        .setAutoCancel(true)
                        .setTicker(msg.getTitle());

        if (msg.getMessage() != null) {
            builder.setContentText(msg.getMessage()).setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(msg.getMessage()));
        }

        boolean soundEnabled = (notificationDefaults & Notification.DEFAULT_SOUND) == Notification.DEFAULT_SOUND;
        if (msg.getNotificationBoolean("noSound", true) != soundEnabled) {
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
