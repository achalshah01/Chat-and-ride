package com.vbanjan.inclassassignment02;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class DisplayCurrentLocation extends Fragment implements
        OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;
    private GoogleMap mMap;
    private MapView mapView;
    private ProgressDialog dialog;
    LocationManager locationManager;
    LocationListener mLocListener;
    public String TAG = "demo";
    String chatRoomId;
    Double currentLat;
    Double currentLong;

    public DisplayCurrentLocation(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_current_location, container, false);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("We're figuring out where you are... Hang on tight!");
        dialog.show();
        mapView = view.findViewById(R.id.map);


        view.findViewById(R.id.whereToTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack("overDisplayCurrentLocation")
                        .replace(R.id.mapContainer,
                                new SelectWhereToFragment(currentLat, currentLong)).commit();
            }
        });

        if (mListener.checkLocationPermission()) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        } else {
            Log.d(TAG, "onResume: PERMISSION DENIED");
        }
        return view;
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        mLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();

                Log.d(TAG, "onMapReady: " + currentLat + " " + currentLong);
                LatLng currentLocation = new LatLng(currentLat, currentLong);
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                dialog.cancel();
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
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, mLocListener);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                getCurrentLocation();
            }
        });

    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);

        boolean checkLocationPermission();
    }
}
