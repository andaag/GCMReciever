package no.codebox.gcmreciever.db;

import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GCMSqliteDb extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gcmregister.db";
    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE messages"
            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "timestamp INTEGER, `collapse-key` TEXT UNIQUE, json TEXT, expires INTEGER);";
    private static final String CREATE_TABLE_HEARTBEATS = "CREATE TABLE heartbeats"
            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "timestamp INTEGER, heartbeat TEXT UNIQUE NOT NULL, interval INTEGER NOT NULL, lastheartbeat INTEGER NOT NULL, lastseen INTEGER);";
    public GCMSqliteDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setLocale(Locale.ENGLISH);
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);
        db.execSQL(CREATE_TABLE_HEARTBEATS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("OnUpgrade not handled");
    }

    public long insert(String table, ContentValues contentValues) {
        return getWritableDatabase().insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
