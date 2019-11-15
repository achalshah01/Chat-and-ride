package com.vbanjan.inclassassignment02;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.android.volley.VolleyLog.TAG;


public class RiderRideDisplay extends Fragment implements
        OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseAuth mAuth;
    float distanceInMeters;
    Button FinishTrip;
    MapView mapView;
    private OnFragmentInteractionListener mListener;
    String messageId, DriverUid, DriverLat, DriverLng, fromLat, fromLng, toLat, toLng,timeTo,distanceTo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FragmentActivity myContext;
    GoogleMap gmap;
    private static final int overview = 0;
    TextView textDuration,textDistance;


    public RiderRideDisplay(String messageId, String uid, String fromLat, String fromLng, String toLat, String toLng) {
        this.messageId = messageId;
        this.DriverUid = uid;
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rider_ride_display, container, false);
        FinishTrip = view.findViewById(R.id.FinishTrip);
        mapView = view.findViewById(R.id.mapView);
        textDistance=view.findViewById(R.id.distance);
        textDuration=view.findViewById(R.id.duration);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        listenToTripCancel();
        FragmentManager fragManager = myContext.getFragmentManager();

        FinishTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DocumentReference washingtonRef = db.collection("Trips").document(messageId);
                final FragmentManager manager = ((Activity) view.getContext()).getFragmentManager();

                Map<String, Object> data = new HashMap<>();
                data.put("tripsTaken", FieldValue.arrayUnion(messageId));
                db.collection("Users")
                        .document(mAuth.getCurrentUser().getUid())
                        .set(data, SetOptions.merge());

                Map<String, Object> tripDetails = new HashMap<>();
                data.put("Status", "Cancelled");
                db.collection("Trips").document(messageId).set(tripDetails, SetOptions.merge());
                getActivity().finish();

//                washingtonRef
//                        .update("Status", "Cancelled")
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//                                mListener.finishTripByRider(1);
//
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(TAG, "Error updating document", e);
//                            }
//                        });

            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = (FragmentActivity) context;

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
    public void onMapReady(final GoogleMap googleMap) {
       // getUserLocation(DriverUid);
     gmap=googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(Double.parseDouble(fromLat),Double.parseDouble(fromLng)), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());

// Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        final DocumentReference docRef = db.collection("Users").document(DriverUid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    DriverLat = snapshot.getData().get("currentLat").toString();
                    DriverLng = snapshot.getData().get("currentLng").toString();
                      googleMap.clear();
                    distanceInMeters = distanceCalculateBtwRiderDriver(fromLat, fromLng, DriverLat, DriverLng);


                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target( new LatLng(Double.parseDouble(DriverLat),Double.parseDouble(DriverLng)))      // Sets the center of the map to Mountain View
                            .zoom(8)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    StringBuilder sb = new StringBuilder();
                    sb.append("https://maps.googleapis.com/maps/api/directions/json?origin="+DriverLat+","+DriverLng+
                            "&destination="+toLat+","+toLng+"&waypoints=via:"+fromLat+","+fromLng+"&key=AIzaSyCBIdjxTUh5TIC01R-c1RTRYx9YdOfPLeY");
                    Log.d("demoLocation",sb.toString());
                    new DownloadDirection().execute(String.valueOf(sb));
                    if (distanceInMeters <= 100) {
                        db.collection("Trips").document(messageId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot.get("Status").equals("Completed")) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Trip Completed")
                                            .setMessage("Driver picked you up!")
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    Map<String, Object> data = new HashMap<>();
                                                    data.put("tripsTaken", FieldValue.arrayUnion(messageId));
                                                    db.collection("Users")
                                                            .document(mAuth.getCurrentUser().getUid())
                                                            .set(data, SetOptions.merge());


                                                    mListener.finishTripByRider(1);
                                                }
                                            });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            }
                        });
                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }

        });

    }

    private float distanceCalculateBtwRiderDriver(String fromLat, String fromLng, String driverLat, String driverLng) {
        Location loc1 = new Location("");
        loc1.setLatitude(Double.parseDouble(fromLat));
        loc1.setLongitude(Double.parseDouble(fromLng));

        Location loc2 = new Location("");
        loc2.setLatitude(Double.parseDouble(driverLat));
        loc2.setLongitude(Double.parseDouble(driverLng));

        return (float) loc1.distanceTo(loc2);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void finishTripByRider(int flag);
    }

    public void listenToTripCancel() {
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
                            Log.d(TAG, "Trip Modified: " + dc.getDocument().getId().equals(messageId) + " "
                                    + dc.getDocument().getId() + " \n" + dc.getDocument().getData());
                            if (dc.getDocument().getId().equals(messageId)
                                    && dc.getDocument().get("Status").equals("Cancelled")
                                    && dc.getDocument().get("createdBy") != null
                                    && !dc.getDocument().get("createdBy").equals("")
                                    && dc.getDocument().get("createdBy").equals(mAuth.getCurrentUser().getUid())) {
                                Log.d(TAG, "onEvent: You got cancelled");

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Driver Cancelled!")
                                        .setMessage("Unfortunately Driver had to cancel.")
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
            gmap.addPolyline(lineOptions);
            textDistance.setText("Distance="+distanceTo);
            textDuration.setText("Time to reach="+timeTo);

            Circle circle = gmap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(fromLat),Double.parseDouble( fromLng)))
                    .strokeWidth(4)
                    .radius(distanceInMeters)
                    .strokeColor(0xFF008577)
                    .fillColor(0x220000FF));
            gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(DriverLat), Double.parseDouble(DriverLng)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                    .title("Driver")
            );
            gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(fromLat), Double.parseDouble(fromLng)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                    .title("Rider")
            );
            gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(toLat), Double.parseDouble(toLng)))
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
                          //  Log.d("ayappa----", position.toString());
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
