package com.speedphoenix.aestivius;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MatchFragment.OnListFragmentInteractionListener {

    public static final int LOCAL_MATCHES_COUNT = 5;
    public static final String DATABASE_NAME = "matches";
    //private static AppRoomDatabase db = null;
    private static MatchDBHelper dbHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (db == null)
            db = Room.databaseBuilder(getApplicationContext(), AppRoomDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        */

        if (dbHelper == null)
            dbHelper = new MatchDBHelper(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.createMatchButton:
                Intent intent = new Intent(this, CreateMatchActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Match match) {

    }

    public static MatchDBHelper getDbHelper() {
        return dbHelper;
    }

    /*
    public static AppRoomDatabase getDb() {
        return db;
    }

    public static MatchDao getDao() {
        return db.matchDao();
    }
    */
}
