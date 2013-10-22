package no.codebox.gcmreciever.heartbeat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import no.codebox.gcmreciever.db.HeartbeatContentProvider;
import no.codebox.gcmreciever.helpers.MsgHandler;
import no.codebox.gcmreciever.model.GCMMsg;

public class HeartbeatAlarmReciever extends BroadcastReceiver {
    private static final String TAG = HeartbeatAlarmReciever.class.getName();

    //@todo : rerun this on boot.
    public static void updateHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Cursor cursor = context.getContentResolver().query(HeartbeatContentProvider.CONTENT_URI, new String[]{"lastheartbeat", "interval", "lastseen", "heartbeat"}, null, null, "lastheartbeat + interval");
        try {
            while (cursor.moveToNext()) {
                long lastheartbeat = cursor.getLong(0);
                long interval = cursor.getLong(1);
                long lastseen = cursor.getLong(2);
                long nextAlarm = lastheartbeat + (interval * 1000);
                if (lastseen > nextAlarm) {
                    continue;
                }
                String heartbeat = cursor.getString(3);

                Intent intent = new Intent(context, HeartbeatAlarmReciever.class);
                intent.putExtra("heartbeat", heartbeat);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                alarmManager.cancel(pendingIntent);

                Log.d(TAG, "Setting alarm in " + nextAlarm + " (current time " + System.currentTimeMillis() + ")");
                alarmManager.set(AlarmManager.RTC, nextAlarm, pendingIntent);
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MsgHandler msgHandler = new MsgHandler(context);
        String heartbeat = intent.getStringExtra("heartbeat");
        Log.d(TAG, "onReceive " + heartbeat);
        GCMMsg msg = GCMMsg.createHeartbeatMsg(heartbeat);
        msgHandler.handleMessage(msg);
    }
}
