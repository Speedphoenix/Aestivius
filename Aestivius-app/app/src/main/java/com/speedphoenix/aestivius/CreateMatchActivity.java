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

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.intellij.lang.annotations.JdkConstants;
import org.json.JSONException;
import org.json.JSONObject;

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
            if (locatedlocation != null) {
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
                if (locatedlocation != null) {
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
                saveMatchExternally(newMatch);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        MatchDBHelper helper = MainActivity.getDbHelper();
                        List<Match> matches = helper.getAllMatches();
                        // We don't want to have more than 5 matches saved locally
                        if (matches.size() >= MainActivity.LOCAL_MATCHES_COUNT) {
                            Match inter = matches.get(0);
                            for (int i = 1; i < matches.size(); i++) {
                                if (matches.get(i).getDate().before(inter.getDate())) {
                                    inter = matches.get(i);
                                }
                            }
                            helper.deleteMatch(inter.getId());
                        }

                        helper.insertMatch(newMatch);

                        finish();
                    }
                });
                break;
        }
    }

    public void saveMatchExternally(Match match) {
        final Context context = this.getApplicationContext();
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = MainActivity.EXTERNAL_DB_URL + MainActivity.getPhoneID();

        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            object.put("_id", match.getId());
            object.put("date", match.getDateTimestamp());
            object.put("location", match.getLocation());
            object.put("winner", match.getWinner());
            object.put("loser", match.getLoser());
            object.put("score", match.getFinalScore());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("Created match");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.err.println(error.toString());
                }
        });
        requestQueue.add(jsonObjectRequest);
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
