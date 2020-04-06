package com.speedphoenix.aestivius;

import android.location.Location;
import android.provider.BaseColumns;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "match")
public class Match implements Comparable<Match> {

    public final static List<Match> DUMMYLIST = new ArrayList<>();
    private static Random random = new Random();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addMatch(createDummyMatch(i));
        }
    }

    private static void addMatch(Match item) {
        DUMMYLIST.add(item);
    }

    private static Match createDummyMatch(int position) {

        int winner = position * 10 + random.nextInt(10);
        int loser = position * 10 + random.nextInt(10);
        int winscore = 10 + random.nextInt(5);
        int loserscore = random.nextInt(9);
        String denom = random.nextBoolean() ? "Mr. " : "Ms. ";
        return new Match(new Date(),
                "Quai de Grenelle",
                denom + String.valueOf(winner),
                denom + String.valueOf(loser),
                String.valueOf(winscore) + " : " + String.valueOf(loserscore));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private Date date;

    @NonNull
    private String location;

    @NonNull
    private String winner;

    @NonNull
    private String loser;

    @NonNull
    private String finalScore;

    public Match(Date date, String location, String winner, String loser, String finalScore) {
        this.date = date;
        this.location = location;
        this.winner = winner;
        this.loser = loser;
        this.finalScore = finalScore;
    }

    @Ignore
    public Match(Date date, String location, String winner, String loser, String finalScore, long id) {
        this(date, location, winner, loser, finalScore);
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public long getDateTimestamp() {
        return date.getTime();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDateFromTimestamp(long value) {
        this.date = new Date(value);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }

    public String getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(String finalScore) {
        this.finalScore = finalScore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(Match other) {
        long diff = this.getDateTimestamp() - other.getDateTimestamp();
        if (diff > 0)
            return 1;
        else if (diff < 0)
            return -1;
        else
            return 0;
    }

    /* Inner class that defines the table contents */
    public static class MatchEntry implements BaseColumns {
        public static final String TABLE_NAME = "match";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_WINNER = "winner";
        public static final String COLUMN_NAME_LOSER = "loser";
        public static final String COLUMN_NAME_SCORE = "score";
    }
}