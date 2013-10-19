package no.codebox.gcmreciever.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageSqliteDb extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gcmregister.db";
    private static final String CREATE_TABLE_TUTORIALS = "CREATE TABLE messages"
            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "timestamp INTEGER, title TEXT NOT NULL, message TEXT, icon INTEGER, expires INTEGER);";

    public MessageSqliteDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TUTORIALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("OnUpgrade not handled");
    }

    public long insert(ContentValues contentValues) {
        return getWritableDatabase().insert("messages", "null", contentValues);
    }
}
