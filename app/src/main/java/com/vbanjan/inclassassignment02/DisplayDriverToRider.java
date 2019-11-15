package com.vbanjan.inclassassignment02;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DisplayDriverToRider extends Fragment
        implements OnMapReadyCallback {
    private OnFragmentInteractionListener mListener;
    Double fromLat;
    Double fromLng;
    Double toLat;
    Double toLng;
    String fromLocationName;
    String toLocationName;
    String timeTo,distanceTo;
    private GoogleMap mMap;
    private MapView mapView;
    LocationManager locationManager;
    LocationListener mLocListener;
    public String TAG = "demo";
    Double currentLat;
    Double currentLong;
    Marker currentDriverLocation;
    FirebaseAuth mAuth;
    Message message;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    float distanceInMeters;
    TextView textDuration,textDistance;

    public DisplayDriverToRider(Message message, Double fromLat, Double fromLng, Double toLat, Double toLng, String fromLocationName, String toLocationName) {
        this.message = message;
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
        this.fromLocationName = fromLocationName;
        this.toLocationName = toLocationName;


    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_driver_to_rider, container, false);
        mAuth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mapView = view.findViewById(R.id.map);
        textDistance=view.findViewById(R.id.driverDistance);
        textDuration=view.findViewById(R.id.driverDuration);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);


        mLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();

               mMap.clear();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(currentLat, currentLong))      // Sets the center of the map to Mountain View
                        .zoom(8)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                StringBuilder sb = new StringBuilder();
                sb.append("https://maps.googleapis.com/maps/api/directions/json?origin="+currentLat+","+currentLong+
                        "&destination="+toLat+","+toLng+"&waypoints=via:"+fromLat+","+fromLng+"&key=AIzaSyCBIdjxTUh5TIC01R-c1RTRYx9YdOfPLeY");
                Log.d("demoLocation",sb.toString());
                new DownloadDirection().execute(String.valueOf(sb));



                // Update one field, creating the document if it does not already exist.
                Map<String, Object> data = new HashMap<>();
                data.put("currentLat", currentLat);
                data.put("currentLng", currentLong);

                db.collection("Users").document(mAuth.getCurrentUser().getUid())
                        .set(data, SetOptions.merge());

                Log.d(TAG, "onLocationChanged: " + location.getLongitude() + " - " + location.getLatitude());

                LatLng currentLocation = new LatLng(currentLat, currentLong);
                LatLng RiderLocation = new LatLng(fromLat, fromLng);
                LatLng RiderDestinationLocation = new LatLng(toLat, toLng);

                float[] results = new float[1];
                Location.distanceBetween(currentLat, currentLong,
                        fromLat, fromLng, results);
                Log.d(TAG, "onMapReady: " + results[0]);

                if (results[0] < 100) {
                    Toast.makeText(getContext(), "Within 100m of Rider!", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Pick Up Rider?")
                            .setMessage("Within 100m of Rider!")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("Status", "Completed");
                                    db.collection("Trips").document(message.messageId).set(data, SetOptions.merge());

                                    db.collection("Chatrooms").document(message.chatRoomId)
                                            .collection("Messages").document(message.messageId).set(data, SetOptions.merge());

                                    getActivity().finish();

                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }

                if (currentDriverLocation == null) {
                   // currentDriverLocation = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                  //  mMap.addMarker(new MarkerOptions().position(RiderLocation).title("Rider Location"));
                  //  mMap.addMarker(new MarkerOptions().position(RiderDestinationLocation).title("Rider Destination Location"));
                } else {
                    currentDriverLocation.setPosition(currentLocation);
                }
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

        if (mListener.checkLocationPermission()) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 100, 10, mLocListener);
        }

        view.findViewById(R.id.cancelTripButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Ride Cancelled!", Toast.LENGTH_SHORT).show();

                Map<String, Object> data = new HashMap<>();
                data.put("Status", "Cancelled");
                db.collection("Trips").document(message.messageId).set(data, SetOptions.merge());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapLoaded() {
                mMap.clear();
                LatLng currentLocation = new LatLng(currentLat, currentLong);
                LatLng riderLocation = new LatLng(fromLat, fromLng);
                LatLng riderDestinationLocation = new LatLng(toLat, toLng);
                distanceInMeters = distanceCalculateBtwRiderDriver(fromLat, fromLng, currentLat, currentLong);
                currentLat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                currentLong = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(currentLat, currentLong))      // Sets the center of the map to Mountain View
                        .zoom(8)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                StringBuilder sb = new StringBuilder();
                sb.append("https://maps.googleapis.com/maps/api/directions/json?origin="+currentLat+","+currentLong+
                        "&destination="+toLat+","+toLng+"&waypoints=via:"+fromLat+","+fromLng+"&key=AIzaSyCBIdjxTUh5TIC01R-c1RTRYx9YdOfPLeY");
                Log.d("demoLocation",sb.toString());
                new DownloadDirection().execute(String.valueOf(sb));
//                LatLngBounds.Builder latLongBuilder = new LatLngBounds.Builder();
//                ArrayList<LatLng> latLngArrayList = new ArrayList<>();
//
//                latLngArrayList.add(currentLocation);
//                latLngArrayList.add(riderLocation);
//                latLngArrayList.add(riderDestinationLocation);
//
//                if (latLngArrayList.size() > 0) {
//                    for (LatLng p : latLngArrayList) {
//                        latLongBuilder.include(p);
//                    }
//                }
//
//                LatLngBounds bounds = latLongBuilder.build();
//                mMap.setLatLngBoundsForCameraTarget(bounds);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLongBuilder.build(), 50));
            }
        });
    }
    private float distanceCalculateBtwRiderDriver(double fromLat, double fromLng, double driverLat, double driverLng) {
        Location loc1 = new Location("");
        loc1.setLatitude((fromLat));
        loc1.setLongitude(fromLng);

        Location loc2 = new Location("");
        loc2.setLatitude((driverLat));
        loc2.setLongitude((driverLng));

        return (float) loc1.distanceTo(loc2);
    }

    public interface OnFragmentInteractionListener {
        boolean checkLocationPermission();
    }

    class DownloadDirection extends AsyncTask<String,Void,ArrayList> {
        ArrayList points =  new ArrayList();
        List<List<HashMap<String, String>>> point = new ArrayList<List<HashMap<String, String>>>();

        PolylineOptions lineOptions = null;
        List<LatLng> poly = new ArrayList<LatLng>();

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            lineOptions = new PolylineOptions();
            lineOptions.addAll(points);
            lineOptions.width(25);
            lineOptions.color(R.color.colorPrimary);
            lineOptions.geodesic(true);
            textDistance.setText("Distance="+distanceTo);
            textDuration.setText("Time to reach="+timeTo);
            mMap.addPolyline(lineOptions);

            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng((fromLat),( fromLng)))
                    .strokeWidth(4)
                    .radius(distanceInMeters)
                    .strokeColor(0xFF008577)
                    .fillColor(0x220000FF));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentLat, currentLong))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                    .title("Driver")
            );
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng((fromLat),( fromLng)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                    .title("Rider")
            );
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng((toLat),( toLng)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .title("Destination")
            );
        }


        @Override
        protected ArrayList doInBackground(String... strings) {
            try {
                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                URL urls = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
                conn.setReadTimeout(150000); //milliseconds
                conn.setConnectTimeout(15000); // milliseconds
                conn.setRequestMethod("GET");
                Log.d("Demo",strings[0]);
                conn.connect();
                InputStream inputStream;
                inputStream = conn.getInputStream();
                String paths = IOUtils.toString(conn.getInputStream(), "UTF-8");
                JSONObject routePath = new JSONObject(paths);
                JSONArray routes = routePath.getJSONArray("routes");
                JSONObject routesObj = routes.getJSONObject(0);
                JSONArray legs = routesObj.getJSONArray("legs");
                for(int j=0;j<legs.length();j++) {
                    JSONObject legsobj = legs.getJSONObject(j);
                    JSONObject distance = legsobj.getJSONObject("distance");
                    JSONObject duration = legsobj.getJSONObject("duration");
                    timeTo=duration.getString("text");
                    distanceTo=distance.getString("text");
                    JSONArray steps = legsobj.getJSONArray("steps");
                    JSONObject overview_polyline = routesObj.getJSONObject("overview_polyline");
                    // String polyline = overview_polyline.getString("points");
//Log.d("ayappa","poinys====="+polyline);
                    Log.d("ayappa", "overview_polyline=====" + overview_polyline.toString());
                    //List<LatLng> list = decodePoly(polyline);

                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject stepsObj = steps.getJSONObject(i);
                        JSONObject liveLocation = stepsObj.getJSONObject("start_location");
                        JSONObject polulineObjsct = stepsObj.getJSONObject("polyline");
                        String polyline = polulineObjsct.getString("points");
                        List<LatLng> list = decodePoly(polyline);
                        double lat = Double.parseDouble(String.valueOf(liveLocation.get("lat")));
                        double lng = Double.parseDouble(String.valueOf(liveLocation.get("lng")));
                        Log.d("ayappa-list---", list.toString());

                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                            LatLng position = new LatLng(list.get(l).latitude, list.get(l).longitude);
                         //   Log.d("ayappa----", position.toString());
                            points.add(position);
                        }

                    }

                }
                return points;
            }
            catch (Exception e){

            }
            return null;
        }


        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }
}
