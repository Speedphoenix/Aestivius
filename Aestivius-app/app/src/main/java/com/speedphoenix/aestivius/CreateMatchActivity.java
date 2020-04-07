package com.speedphoenix.aestivius;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.speedphoenix.aestivius.Match.MatchEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

// note: most code to save a picture was taken from https://developer.android.com/training/camera/photobasics.html

public class CreateMatchActivity extends AppCompatActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 40;
    public static final int REQUEST_IMAGE_CAPTURE = 3;

    TextView loserView;
    TextView winnerView;
    TextView dateView;
    TextView locationView;
    TextView scoreView;
    TextView picturePrompt;
    ImageView imageView;
    View pictureLayout;
    String location;
    Date date;
    Location locatedlocation;
    Handler handler;
    LocationManager locationManager;
    String provider;
    Criteria criteria;
    String currentPhotoPath;
    boolean gotPicture;
    Bitmap bitmap = null;


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
        imageView = (ImageView) findViewById(R.id.picture);
        picturePrompt = (TextView) findViewById(R.id.pictureprompt);
        date = new Date();
        dateView.setText(date.toString());
        location = "14 rue de la Boustifaille";
        locationView.setText(location);
        handler = new Handler();


        pictureLayout = findViewById(R.id.takepicturelayout);
        pictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePicture();
            }
        });
        gotPicture = false;

        PackageManager packageManager = this.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            ;//do stuff

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

                // this could happen if we have permission to create a file but not to take a picture
                // in this case we don't want it
                if (!gotPicture)
                    currentPhotoPath = null;

                // TODO, have the actual damned picture
                final Match newMatch = new Match(date, location, winner, loser, score, currentPhotoPath);
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

                        if (bitmap != null) {
                            bitmap.recycle();
                            bitmap = null;
                        }

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
            object.put(MatchEntry._ID, match.getId());
            object.put(MatchEntry.COLUMN_NAME_DATE, match.getDateTimestamp());
            object.put(MatchEntry.COLUMN_NAME_LOCATION, match.getLocation());
            object.put(MatchEntry.COLUMN_NAME_WINNER, match.getWinner());
            object.put(MatchEntry.COLUMN_NAME_LOSER, match.getLoser());
            object.put(MatchEntry.COLUMN_NAME_SCORE, match.getFinalScore());
            object.put(MatchEntry.COLUMN_NAME_PICTURE, match.getPicturePath());
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

    // note: most code to save a picture was taken from https://developer.android.com/training/camera/photobasics.html
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void dispatchTakePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.speedphoenix.aestivius.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*
            // this is to get a thumbnail
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    gotPicture = true;
                    int targetW = (int) getResources().getDimension(R.dimen.picture_match_creation_width);
                    int targetH = (int) getResources().getDimension(R.dimen.picture_match_creation_height);
                    if (bitmap != null)
                        bitmap.recycle();
                    bitmap = SomeUtils.getPic(currentPhotoPath, targetW, targetH);
                    imageView.setImageBitmap(bitmap);
                    picturePrompt.setText(R.string.change_picture);
                }
            });
            // galleryAddPic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /*
    //this makes the picture visible by the user outside the app (from his photo gallery)
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    */

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
