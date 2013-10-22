package no.codebox.gcmreciever.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;

import no.codebox.gcmreciever.model.GCMMsg;

public class HeartbeatAsyncHandler extends AsyncQueryHandler {
    public HeartbeatAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void deleteHeartbeat(GCMMsg msg) {
        startDelete(0, null, HeartbeatContentProvider.CONTENT_URI, "heartbeat=?", new String[]{msg.getHeartbeatKey()});
    }

    public void insertHeartbeat(GCMMsg msg) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", System.currentTimeMillis());
        contentValues.put("heartbeat", msg.getHeartbeatKey());
        contentValues.put("interval", msg.getHeartbeatInterval().intValue());
        contentValues.put("lastheartbeat", System.currentTimeMillis());
        startInsert(0, null, HeartbeatContentProvider.CONTENT_URI, contentValues);
    }

    public void updateLastseen() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("lastseen", System.currentTimeMillis());
        startUpdate(0, null, HeartbeatContentProvider.CONTENT_URI, contentValues, null, null);
    }
}
