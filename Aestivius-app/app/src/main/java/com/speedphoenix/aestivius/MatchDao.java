package com.speedphoenix.aestivius;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

// Meant to use SQLite directly rather than Room
@Deprecated
@Dao
public interface MatchDao {

    @Query("SELECT * FROM [match]")
    public Match[] getAll();

    @Insert
    public void insertMatch(Match match);

    @Delete
    public void deleteMatch(Match match);
}
