package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ConfirmFromToFragment extends Fragment implements
        OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;
    Double FromLat, FromLng, ToLat, ToLng;
    String ToLocationName;
    String TAG = "demo";

    private GoogleMap mMap;
    private MapView mapView;
    LocationManager locationManager;
    LocationListener mLocListener;

    public ConfirmFromToFragment(Double fromLat, Double fromLng, Double toLat, Double toLng, String toLocationName) {
        FromLat = fromLat;
        FromLng = fromLng;
        ToLat = toLat;
        ToLng = toLng;
        ToLocationName = toLocationName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_from_to, container, false);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        view.findViewById(R.id.confirmTripButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.confirmTrip(FromLat, FromLng, ToLat, ToLng, ToLocationName);
            }
        });
        view.findViewById(R.id.cancelTripButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return view;
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

    float distanceFromTo;

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(getResources().getString(R.string.api_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private DirectionsResult getDirectionsDetails(String origin, String destination, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: ");
        final Double FROM_LAT = this.FromLat;
        final Double FROM_LNG = this.FromLng;
        final Double TO_LAT = this.ToLat;
        final Double TO_LNG = this.ToLng;

        DirectionsResult results = getDirectionsDetails(FROM_LAT + ", " + FROM_LNG, TO_LAT + ", " + TO_LNG, TravelMode.DRIVING);
        if (results != null) {
            addPolyline(results, googleMap);
            positionCamera(results.routes[overview], googleMap);
            addMarkersToMap(results, googleMap);
        }

    }

    private static final int overview = 0;

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview]
                .legs[overview].startLocation.lat, results.routes[overview]
                .legs[overview].startLocation.lng)).title("Start Point: " + results.routes[overview]
                .legs[overview].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview]
                .legs[overview].endLocation.lat, results.routes[overview]
                .legs[overview].endLocation.lng)).title("End Point: " + results.routes[overview]
                .legs[overview].endAddress).snippet(getEndLocationTitle(results)));
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


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

        void confirmTrip(Double FromLat, Double FromLng, Double ToLat, Double ToLng, String ToLocationName);
    }
}
