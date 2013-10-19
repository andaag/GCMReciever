package no.codebox.gcmreciever.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;

public class MessageAsyncHandler extends AsyncQueryHandler {
    public MessageAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void insertMessage(String data, long expires) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", System.currentTimeMillis());
        contentValues.put("json", data);
        contentValues.put("expires", expires);
        startInsert(0, null, MessageContentProvider.CONTENT_URI, contentValues);
    }

    public void clearOld() {
        //@todo : clear expired stuff (or messages > #100?)
    }
}
