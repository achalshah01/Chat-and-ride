package com.vbanjan.inclassassignment02;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DriverToRivedNavigation extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private OnFragmentInteractionListener mListener;
    private MapView mapViewMapbox;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    LocationManager locationManager;
    LocationListener mLocListener;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    Button cancel, start;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth;
    Message message;
    Double currentLat;
    Double currentLong;
    int flag = 0;
    Double DriverLat;
    Double DriverLng;
    Double fromLat;
    Double fromLng;
    Double toLat;
    Double toLng;
    String fromLocationName;
    String toLocationName;

    public DriverToRivedNavigation() {
        // Required empty public constructor
    }

    public DriverToRivedNavigation(Message message, Double fromLat, Double fromLng,
                                   Double toLat, Double toLng, String fromLocationName, String toLocationName) {
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
        this.fromLocationName = fromLocationName;
        this.toLocationName = toLocationName;
        this.message = message;

    }


    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        //  setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mapViewMapbox = view.findViewById(R.id.mapMapBox);
        mapViewMapbox.onCreate(savedInstanceState);
        mapViewMapbox.getMapAsync(this);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        cancel = view.findViewById(R.id.navigationCancel);
        start = view.findViewById(R.id.navigationStart);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Ride Cancelled!", Toast.LENGTH_SHORT).show();

                Map<String, Object> data = new HashMap<>();
                data.put("Status", "Cancelled");
                db.collection("Trips").document(message.messageId).set(data, SetOptions.merge());

                Map<String, Object> tripComplete = new HashMap<>();
                tripComplete.put("tripsFulfilled", FieldValue.arrayUnion(message.messageId));
                db.collection("Users")
                        .document(mAuth.getCurrentUser().getUid())
                        .set(tripComplete, SetOptions.merge());

                getActivity().finish();
            }
        });


        currentLat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        currentLong = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();




        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MapboxNavigation navigation = new MapboxNavigation(context,
               "pk.eyJ1IjoiYXlhcHBzIiwiYSI6ImNrMGxxOXdtNDB6MnAzbHFkbGswNGltM2EifQ.8Focp52264e20CtKebA1xg");

        Mapbox.getInstance(getContext(),
                "pk.eyJ1IjoiYXlhcHBzIiwiYSI6ImNrMGxxOXdtNDB6MnAzbHFkbGswNGltM2EifQ.8Focp52264e20CtKebA1xg");
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
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        locationComponent = mapboxMap.getLocationComponent();
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if(style.isFullyLoaded()) {
                    enableLocationComponent(style);
                    Point destinationPoint = Point.fromLngLat(fromLng, fromLat);
                    Point originPoint = Point.fromLngLat(currentLong
                            , currentLat);
                    getRoute(originPoint, destinationPoint);

                    mLocListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            currentLat = location.getLatitude();
                            currentLong = location.getLongitude();
                            Point destinationPoint = Point.fromLngLat(fromLng, fromLat);
                            Point originPoint = Point.fromLngLat(currentLong,currentLat);
                            getRoute(originPoint, destinationPoint);
                            // Update one field, creating the document if it does not already exist.
                            Map<String, Object> data = new HashMap<>();
                            data.put("currentLat", currentLat);
                            data.put("currentLng", currentLong);

                            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                                    .set(data, SetOptions.merge());

                            Log.d(TAG, "onLocationChanged: " + location.getLongitude() + " - " + location.getLatitude());

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

                }
            }
        });
    }

    @SuppressWarnings({"MissingPermission", "deprecation"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
// Activate the MapboxMap LocationComponent to show user location
// Adding in LocationComponentOptions is also an optional parameter
            locationComponent.activateLocationComponent(getContext(), loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);



        } else {
            permissionsManager=new PermissionsManager(this);
            //permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(getContext())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
// Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapViewMapbox, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                        flag=1;

                    }
                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    private void navigate() {
        if(flag==1) {
            boolean simulateRoute = true;
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(simulateRoute)
                    .build();
// Call this method with Context from within an Activity
            NavigationLauncher.startNavigation(getActivity(), options);

        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
