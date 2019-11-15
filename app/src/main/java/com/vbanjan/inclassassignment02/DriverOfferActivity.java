package com.vbanjan.inclassassignment02;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class DriverOfferActivity extends AppCompatActivity implements DriverOfferMapPlot.OnFragmentInteractionListener,
        DisplayDriverToRider.OnFragmentInteractionListener, DriverToRivedNavigation.OnFragmentInteractionListener {
    String TAG = "demo";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth;
    LocationManager locationManager;
    Message tripDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_offer);

        mAuth = FirebaseAuth.getInstance();

        tripDetails = (Message) getIntent().getExtras().getSerializable("messageObj");
        Log.d(TAG, "onCreate: " + tripDetails.toString());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS not Enabled!")
                    .setMessage("Would you like to enable GPS settings?")
                    .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            finish();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container,
                    new DriverOfferMapPlot(tripDetails)).commit();
        }
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 867;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: ");
                        //Request location updates:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                                new DriverOfferMapPlot(tripDetails)).commit();
                    }
                } else {
                    finish();
                }
                return;
            }
        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d(TAG, "checkLocationPermission: SHOW EXPLANATION");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                Log.d(TAG, "checkLocationPermission: NO EXPLANATION NEEDED");
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        } else {
            Log.d(TAG, "checkLocationPermission: PERMISSION PRESENT ");
            return true;
        }
    }

    @Override
    public void displayDriverToRider(Message message, Double fromLat, Double fromLng, Double toLat, Double toLng, String fromLocationName, String toLocationName) {
        Log.d(TAG, "displayDriverToRider: ");
        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                new DisplayDriverToRider(message, fromLat, fromLng, toLat, toLng, fromLocationName, toLocationName)).commit();

//        getSupportFragmentManager().beginTransaction().replace(R.id.container,
//                new DriverToRivedNavigation(message, fromLat, fromLng, toLat, toLng, fromLocationName, toLocationName)).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
