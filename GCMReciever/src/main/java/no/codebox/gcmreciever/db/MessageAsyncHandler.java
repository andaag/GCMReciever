package no.codebox.gcmreciever.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.codebox.gcmreciever.model.GCMMsg;

public class MessageAsyncHandler extends AsyncQueryHandler {
    public MessageAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void insertMessage(GCMMsg msg) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", System.currentTimeMillis());
        contentValues.put("`collapse-key`", msg.getString("collapse-key", null));
        try {
            contentValues.put("json", msg.toJson());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        contentValues.put("expires", msg.getNumber("expires", 0).longValue());
        startInsert(0, null, MessageContentProvider.CONTENT_URI, contentValues);
    }

    public void clearOld() {
        //@todo : clear expired stuff (or messages > #100?)
    }
}
