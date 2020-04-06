package com.speedphoenix.aestivius;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// Meant to use SQLite directly rather than Room
@Deprecated
@Database(entities = {Match.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppRoomDatabase extends RoomDatabase {
    public abstract MatchDao matchDao();
}
