package com.vbanjan.inclassassignment02;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MakeTripActivity extends AppCompatActivity implements
        SelectWhereToFragment.OnFragmentInteractionListener, DisplayCurrentLocation.OnFragmentInteractionListener,
        ConfirmFromToFragment.OnFragmentInteractionListener, DriversList.OnFragmentInteractionListener, RiderRideDisplay.OnFragmentInteractionListener {
    String TAG = "demo";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth;
    LocationManager locationManager;
    String chatRoomId;
    int endTripByRider;

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_trip);

        mAuth = FirebaseAuth.getInstance();

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        Log.d(TAG, "onCreate: " + chatRoomId);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.mapContainer,
                                        new DisplayCurrentLocation(chatRoomId)).commit();
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
            if (this.toLat == null && this.toLng == null && this.toPlace == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer,
                        new DisplayCurrentLocation(chatRoomId)).commit();
            } else {
                displayConfirmFromToFragment(this.fromLat, this.fromLng, this.toLat, this.toLng, this.toPlace);
            }

        }
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
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void finishTripByRider(int flag) {
        endTripByRider = flag;
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (endTripByRider == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Trip Completed ")
                    .setPositiveButton("Back to chatRoom", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
            super.onBackPressed();
        } else {
            //  getSupportFragmentManager().popBackStack();
            super.onBackPressed();

        }
    }

    @Override
    public void confirmTrip(final Double FromLat, final Double FromLng, final Double ToLat, final Double ToLng, final String ToLocationName) {

        CollectionReference messageRef = db.collection("Chatrooms").document(chatRoomId)
                .collection("Messages");
        final Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageText", "Hey! I need a Ride");
        final Date currentTime = Calendar.getInstance().getTime();
        messageMap.put("messageTime", String.valueOf(currentTime));
        messageMap.put("createdBy", mAuth.getCurrentUser().getUid());
        messageMap.put("isTrip", true);
        messageRef
                .add(messageMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Map<String, Object> tripMap = new HashMap<>();
                        tripMap.put("createdBy", mAuth.getCurrentUser().getUid());
                        tripMap.put("chatroomId", chatRoomId);
                        tripMap.put("FromLat", FromLat);
                        tripMap.put("FromLng", FromLng);
                        tripMap.put("ToLat", ToLat);
                        tripMap.put("ToLng", ToLng);
                        tripMap.put("ToLocationName", ToLocationName);
                        tripMap.put("FromLocationName", fromLocationName);
                        tripMap.put("Status", "Created");
//                        tripMap.put("fulfilledBy", "");
//                        tripMap.put("offeredBy", Arrays.asList());

                        db.collection("Trips").document(documentReference.getId())
                                .set(tripMap, SetOptions.merge());
                        getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer,
                                new DriversList(documentReference.getId().toString())).commit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MakeTripActivity.this, "Oops! Something went wrong, try again!", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    int AUTOCOMPLETE_REQUEST_CODE = 1;

    Double fromLat, fromLng;
    Double toLat, toLng;
    String toPlace;

    @Override
    public void openAutoCompleteIntent(Double fromLat, Double fromLng) {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
        this.fromLat = fromLat;
        this.fromLng = fromLng;

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    String fromLocationName;

    @Override
    public void fromLocationName(String fromName) {
        fromLocationName = fromName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: ");
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.d(TAG, "Place: " + place.getName() + ", " + place.getLatLng());

                this.toLat = place.getLatLng().latitude;
                this.toLng = place.getLatLng().longitude;
                this.toPlace = place.getName();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.d(TAG, status.getStatusMessage());
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer,
                        new DisplayCurrentLocation(chatRoomId)).commit();

            } else if (resultCode == RESULT_CANCELED) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer,
                        new DisplayCurrentLocation(chatRoomId)).commit();
                // The user canceled the operation.
            }
        }
    }

    public void displayConfirmFromToFragment(Double fromLat, Double fromLng, Double toLat, Double toLng, String toPlace) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapContainer,
                        new ConfirmFromToFragment(fromLat,
                                fromLng,
                                toLat,
                                toLng,
                                toPlace))
                .commit();

    }
}
