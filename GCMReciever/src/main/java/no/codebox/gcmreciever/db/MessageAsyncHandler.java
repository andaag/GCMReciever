package no.codebox.gcmreciever.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;

public class MessageAsyncHandler extends AsyncQueryHandler {
    public MessageAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void insertMessage(String title) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", System.currentTimeMillis());
        contentValues.put("title", title);
        startInsert(0, null, MessageContentProvider.CONTENT_URI, contentValues);
    }
}
