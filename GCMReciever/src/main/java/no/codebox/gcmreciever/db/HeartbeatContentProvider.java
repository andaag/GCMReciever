package no.codebox.gcmreciever.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import no.codebox.gcmreciever.heartbeat.HeartbeatAlarmReciever;

public class HeartbeatContentProvider extends ContentProvider {
    private static final String AUTHORITY = "no.codebox.gcmreciever.db.HeartbeatContentProvider";
    private static final String HEARTBEAT_BASE_PATH = "heartbeats";

    private static final int HEARTBEATS = 100;
    private static final int HEARTBEAT_ID = 101;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + HEARTBEAT_BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, HEARTBEAT_BASE_PATH, HEARTBEATS);
        sURIMatcher.addURI(AUTHORITY, HEARTBEAT_BASE_PATH + "/#", HEARTBEAT_ID);
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
            case HEARTBEATS:
                Cursor cursor = db.getReadableDatabase().query("heartbeats", projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalStateException("Unknown id");
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        try {
            int uriType = sURIMatcher.match(uri);
            switch (uriType) {
                case HEARTBEATS:
                    long insertId = db.insert("heartbeats", contentValues);
                    if (insertId != 0) {
                        getContext().getContentResolver().notifyChange(uri, null);
                        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(insertId));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown id");
            }
            return null;
        } finally {
            HeartbeatAlarmReciever.updateHeartbeat(getContext());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int deleted = 0;
        switch (uriType) {
            case HEARTBEATS:
                deleted = db.getWritableDatabase().delete("heartbeats", selection, selectionArgs);
                break;
            case HEARTBEAT_ID:
                String id = uri.getLastPathSegment();
                deleted = db.getWritableDatabase().delete("heartbeats", "_id=?", new String[]{id});
                break;
            default:
                throw new IllegalStateException("Unknown id");
        }
        if (deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            HeartbeatAlarmReciever.updateHeartbeat(getContext());
        }
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int updated = db.getWritableDatabase().update("heartbeats", contentValues, selection, selectionArgs);
        if (updated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updated;
    }
}
