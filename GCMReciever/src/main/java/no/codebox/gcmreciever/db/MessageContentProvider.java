package no.codebox.gcmreciever.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class MessageContentProvider extends ContentProvider {
    private static final String TAG = MessageContentProvider.class.getName();

    private static final String AUTHORITY = "no.codebox.gcmreciever.db.MessageContentProvider";
    private static final String MESSAGES_BASE_PATH = "messages";

    private static final int MESSAGES = 100;
    private static final int MESSAGE_ID = 101;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + MESSAGES_BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, MESSAGES_BASE_PATH, MESSAGES);
        sURIMatcher.addURI(AUTHORITY, MESSAGES_BASE_PATH + "/#", MESSAGE_ID);
    }

    private GCMSqliteDb db;

    @Override
    public boolean onCreate() {
        db = new GCMSqliteDb(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case MESSAGES:
                trimDb();
                Cursor cursor = db.getReadableDatabase().query("messages", projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalStateException("Unknown id");
        }
    }

    private void trimDb() {
        int deletedMessages = db.getWritableDatabase().delete("messages", "expires < ? and expires != 0", new String[]{Long.valueOf(System.currentTimeMillis()).toString()});
        //@todo : trim length here with deletedMessages += trim msg.
        if (deletedMessages != 0) {
            Log.i(TAG, "trimdb deleted " + deletedMessages + " messages");
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sURIMatcher.match(uri);
        trimDb();
        switch (uriType) {
            case MESSAGES:
                long insertId = db.insert("messages", contentValues);
                if (insertId != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(CONTENT_URI, String.valueOf(insertId));
                }
                break;
            default:
                throw new IllegalStateException("Unknown id");
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
