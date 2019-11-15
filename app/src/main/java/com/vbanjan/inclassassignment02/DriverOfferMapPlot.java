package com.vbanjan.inclassassignment02;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class DriverOfferMapPlot extends Fragment implements
        OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    LocationManager locationManager;
    LocationListener mLocListener;
    public String TAG = "demo";
    Double currentLat;
    Double currentLong;
    private ProgressDialog dialog;
    Double fromLat;
    Double fromLng;
    Double toLat;
    Double toLng;
    String fromLocationName;
    String toLocationName;
    Message message;

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    private OnFragmentInteractionListener mListener;

    public DriverOfferMapPlot(Message message) {
        this.message = message;
        this.fromLat = message.FromLat;
        this.fromLng = message.FromLng;
        this.toLat = message.ToLat;
        this.toLng = message.ToLng;
        this.fromLocationName = message.FromLocationName;
        this.toLocationName = message.FromLocationName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_offer_map_plot, container, false);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mapView = view.findViewById(R.id.map);
        mAuth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Almost there... Getting ride details!");
        dialog.show();


        db.collection("Trips").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New Trip Added: " + dc.getDocument().getData());
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Trip Modified: " + dc.getDocument().getId().equals(message.messageId) + " "
                                    + dc.getDocument().getId() + " \n" + dc.getDocument().getData());
                            Log.d(TAG, "onEvent: " + dc.getDocument().get("fulfilledBy") + " " + mAuth.getCurrentUser().getUid());
                            if (dc.getDocument().getId().equals(message.messageId)
                                    && dc.getDocument().get("Status").equals("Ongoing")
                                    && dc.getDocument().get("fulfilledBy") != null
                                    && !dc.getDocument().get("fulfilledBy").equals("")
                                    && dc.getDocument().get("fulfilledBy").equals(mAuth.getCurrentUser().getUid())) {
                                Log.d(TAG, "onEvent: You got selected");

                                alertDialog.cancel();
                                Log.d(TAG, "onEvent: " + message + "\n"
                                        + fromLat + "\n"
                                        + fromLng + "\n"
                                        + toLat + "\n"
                                        + toLng + "\n"
                                        + fromLocationName + "\n"
                                        + toLocationName);

//                                Map<String, Object> data = new HashMap<>();
//                                data.put("Status", "Ongoing");
//                                db.collection("Trips").document(message.messageId).set(data, SetOptions.merge());

                                mListener.displayDriverToRider(message, fromLat, fromLng, toLat, toLng, fromLocationName, toLocationName);

                            } else if (dc.getDocument().getId().equals(message.messageId)
                                    && dc.getDocument().get("fulfilledBy") != null
                                    && !dc.getDocument().get("fulfilledBy").equals("")
                                    && !dc.getDocument().get("fulfilledBy").equals(mAuth.getCurrentUser().getUid())) {
                                alertDialog.cancel();

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Oh no!")
                                        .setMessage("Rider selected someone else.")
                                        .setPositiveButton("GO BACK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                getActivity().finish();
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.setCancelable(false);
                                alertDialog.show();

                            }
                            break;
                        case REMOVED:
                            Log.d(TAG, "Trip Removed: " + dc.getDocument().getData());
                            break;
                    }
                }
            }
        });


        view.findViewById(R.id.offerRideBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Offer Rider");

                Map<String, Object> updateOffer = new HashMap<>();
                updateOffer.put("offeredBy", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));
                db.collection("Trips").document(message.messageId).set(updateOffer, SetOptions.merge());

                Map<String, Object> data = new HashMap<>();
                data.put("currentLat", currentLat);
                data.put("currentLng", currentLong);

                db.collection("Users").document(mAuth.getCurrentUser().getUid())
                        .set(data, SetOptions.merge());

                builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Please Wait")
                        .setMessage("Waiting for Rider to accept your request..");
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        view.findViewById(R.id.cancelOfferBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Cancel Offer");
                getActivity().finish();
            }
        });

        if (mListener.checkLocationPermission()) {
//            getCurrentLocation();
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        } else {
            Log.d(TAG, "onResume: PERMISSION DENIED");
        }

        return view;
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview]
                .legs[overview].startLocation.lat, results.routes[overview]
                .legs[overview].startLocation.lng)).title("My Location: " + results.routes[overview]
                .legs[overview].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview]
                .legs[overview].endLocation.lat, results.routes[overview]
                .legs[overview].endLocation.lng)).title("Destination: " + results.routes[overview]
                .legs[overview].endAddress).snippet(getEndLocationTitle(results)));
    }

    private static final int overview = 0;

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey("AIzaSyCBIdjxTUh5TIC01R-c1RTRYx9YdOfPLeY")
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        LatLngBounds.Builder latLongBuilder = new LatLngBounds.Builder();
        ArrayList<LatLng> latLngArrayList = new ArrayList<>();
        latLngArrayList.add(new LatLng(route.legs[overview]
                .startLocation.lat, route.legs[overview].startLocation.lng));
        latLngArrayList.add(new LatLng(route.legs[overview]
                .endLocation.lat, route.legs[overview].endLocation.lng));
        if (latLngArrayList.size() > 0) {
            for (LatLng p : latLngArrayList) {
                latLongBuilder.include(p);
            }
        }
        LatLngBounds bounds = latLongBuilder.build();
        mMap.setLatLngBoundsForCameraTarget(bounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLongBuilder.build(), 50));
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath)
                .width(30)
                .color(getContext().getResources()
                        .getColor(R.color.colorPrimary)
                ));
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[overview]
                .legs[overview].duration.humanReadable + " Distance :" + results.routes[overview]
                .legs[overview].distance.humanReadable;
    }


    private DirectionsResult getDirectionsDetails(String origin, String destination, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .waypoints("via:" + fromLat + ", " + fromLng)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        if (locationManager != null && mListener != null)
            locationManager.removeUpdates(mLocListener);
        super.onDestroyView();
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(final GoogleMap googleMap) {
        mLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();

                Log.d(TAG, "onMapReady: " + currentLat + " " + currentLong);
                LatLng riderLocation = new LatLng(fromLat, fromLng);

                DirectionsResult results = getDirectionsDetails(currentLat + ", " + currentLong, toLat + ", " + toLng, TravelMode.DRIVING);
                if (results != null) {
                    dialog.cancel();
                    addPolyline(results, googleMap);
                    positionCamera(results.routes[overview], googleMap);
                    addMarkersToMap(results, googleMap);
                }
                mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider's Location"));
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 100, mLocListener);
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
                getCurrentLocation(mMap);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        boolean checkLocationPermission();

        void displayDriverToRider(Message message, Double fromLat, Double fromLng, Double toLat, Double toLng, String fromLocationName, String toLocationName);
    }
}
