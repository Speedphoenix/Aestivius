package com.speedphoenix.aestivius;

import com.speedphoenix.aestivius.Match.MatchEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;


public class MatchDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "matchdb";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + Match.MatchEntry.TABLE_NAME + " (" +
                    MatchEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MatchEntry.COLUMN_NAME_LOSER + " TEXT," +
                    MatchEntry.COLUMN_NAME_WINNER + " TEXT," +
                    MatchEntry.COLUMN_NAME_DATE + " INTEGER," +
                    MatchEntry.COLUMN_NAME_LOCATION + " TEXT," +
                    MatchEntry.COLUMN_NAME_SCORE + " TEXT" +
                    ")";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Match.MatchEntry.TABLE_NAME;


    public MatchDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertMatch(Match what) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MatchEntry.COLUMN_NAME_LOSER, what.getLoser());
        values.put(MatchEntry.COLUMN_NAME_WINNER, what.getWinner());
        values.put(MatchEntry.COLUMN_NAME_LOCATION, what.getLocation());
        values.put(MatchEntry.COLUMN_NAME_DATE, what.getDateTimestamp());
        values.put(MatchEntry.COLUMN_NAME_SCORE, what.getFinalScore());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(MatchEntry.TABLE_NAME, null, values);
        what.setId(newRowId);
        return newRowId;
    }

    public List<Match> getAllMatches() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MatchEntry.TABLE_NAME, null, null, null, null, null, null);

        List<Match> rep = new ArrayList<Match>();
        while(cursor.moveToNext()) {
            rep.add(new Match(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(MatchEntry.COLUMN_NAME_DATE))),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatchEntry.COLUMN_NAME_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatchEntry.COLUMN_NAME_WINNER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatchEntry.COLUMN_NAME_LOSER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MatchEntry.COLUMN_NAME_SCORE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(MatchEntry._ID))));
        }
        cursor.close();

        return rep;
    }

    public boolean deleteMatch(long whatId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = MatchEntry._ID + "=" + whatId;
        // Specify arguments in placeholder order.
        return db.delete(MatchEntry.TABLE_NAME, selection, null) > 0;
    }
}
