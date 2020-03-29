package com.speedphoenix.aestivius;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.intellij.lang.annotations.JdkConstants;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CreateMatchActivity extends AppCompatActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 40;

    TextView loserView;
    TextView winnerView;
    TextView dateView;
    TextView locationView;
    TextView scoreView;
    String location;
    Date date;
    Location locatedlocation;
    Handler handler;
    LocationManager locationManager;
    String provider;
    Criteria criteria;

    public boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_match);
        loserView = (TextView) findViewById(R.id.loserinput);
        winnerView = (TextView) findViewById(R.id.winnerinput);
        dateView = (TextView) findViewById(R.id.date);
        locationView = (TextView) findViewById(R.id.location);
        scoreView = (TextView) findViewById(R.id.scoreinput);
        date = new Date();
        dateView.setText(date.toString());
        location = "14 rue de la Boustifaille";
        locationView.setText(location);
        handler = new Handler();


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            locatedlocation = locationManager.getLastKnownLocation(provider);

            // Initialize the location fields
            if (location != null) {
                useLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            boolean nowHavePermission = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    nowHavePermission = true;
            }
            if (nowHavePermission) {
                if (provider == null)
                    provider = locationManager.getBestProvider(criteria, false);
                if (provider == null)
                    return;
                locatedlocation = locationManager.getLastKnownLocation(provider);

                // Initialize the location fields
                if (location != null) {
                    useLocation();
                }
            }
        }
    }

    private void useLocation() {
        Geocoder geocoder = new Geocoder(this);
        if (Geocoder.isPresent()) {
            try {
                List<Address> addresses = geocoder.getFromLocation(locatedlocation.getLatitude(), locatedlocation.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    location = addresses.get(0).getAddressLine(0);
                } else
                    location = "" + locatedlocation.getLatitude() + " : " + locatedlocation.getLongitude();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // location = locatedlocation.toString();
            location = "" + locatedlocation.getLatitude() + " : " + locatedlocation.getLongitude();
        }
        locationView.setText(location);
    }


    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.validatebutton:
                String loser = loserView.getText().toString();
                String winner = winnerView.getText().toString();
                String score = scoreView.getText().toString();
                location = locationView.getText().toString();

                if (loser.isEmpty() || winner.isEmpty() || score.isEmpty() || location.isEmpty()) {
                    break;
                }

                final Match newMatch = new Match(date, location, winner, loser, score);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        MatchDao dao = MainActivity.getDao();
                        Match[] matches = dao.getAll();
                        // We don't want to have more than 5 matches saved locally
                        if (matches.length >= MainActivity.LOCAL_MATCHES_COUNT) {
                            Match inter = matches[0];
                            for (int i = 1; i < matches.length; i++) {
                                if (matches[i].getDate().before(inter.getDate())) {
                                    inter = matches[i];
                                }
                            }
                            dao.deleteMatch(inter);
                        }

                        dao.insertMatch(newMatch);

                        finish();
                    }
                });
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
