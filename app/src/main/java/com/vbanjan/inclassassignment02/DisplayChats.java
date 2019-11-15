package com.vbanjan.inclassassignment02;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.mapbox.android.core.permissions.PermissionsManager;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;


public class DisplayChats extends Fragment {
    private static final String CHANNEL_ID = "my_channel_01";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView recyclerViewActiveUser;
    private RecyclerView.Adapter mAdapterActiveUser;
    private RecyclerView.LayoutManager layoutManagerActiveUser;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private FirebaseAuth mAuth;
String id;
    Chatroom chatRoom;
    String TAG = "demo";
    EditText editTextMessage;
    ImageView sendButton;
    private OnFragmentInteractionListener mListener;
    private static final int overview = 0;
    Double fromLat;
    Double fromLng;
    LocationManager    locationManager;
    Double   currentLat,currentLong;
    DisplayChats(){};
    public DisplayChats(Chatroom chatRoom) {
        this.chatRoom = chatRoom;
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_display_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        setHasOptionsMenu(true);

        setUpActiveUserRecyclerView(view);
        setUpDisplayChatsRecyclerView(view);
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService((getActivity()).NOTIFICATION_SERVICE);
        editTextMessage = view.findViewById(R.id.sendMessageEditText);
        sendButton = view.findViewById(R.id.sendBtnImageView);
        createNotificationChannel();
        id=chatRoom.roomId;
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
LocationListener locationListener=new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
        currentLat =location.getLatitude();
        currentLong =location.getLongitude();
        LatLng currentLocation = new LatLng(currentLat, currentLong);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
};

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
               1);
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
                            if (!dc.getDocument().getData().get("createdBy").equals(mAuth.getCurrentUser().getUid())
                                    && dc.getDocument().getData().get("Status").equals("Created")
                                    && dc.getDocument().get("chatroomId").equals(chatRoom.roomId)) {
                                LatLng riderLocation = new LatLng(Double.parseDouble(dc.getDocument().get("FromLat").toString()),
                                        Double.parseDouble(dc.getDocument().get("FromLng").toString()));

                                Double toLat=Double.parseDouble(dc.getDocument().get("ToLat").toString());
                                Double toLng=Double.parseDouble(dc.getDocument().get("ToLng").toString());
                                fromLat=Double.parseDouble(dc.getDocument().get("FromLat").toString());
                                 fromLng=Double.parseDouble(dc.getDocument().get("FromLng").toString());
//                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                                builder.setTitle("Need a Ride!")
//
//                                        .setMessage("Someone need a ride. Go check it out!")
//                                        .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                dialogInterface.dismiss();
//                                            }
//                                        });
//                                AlertDialog alertDialog = builder.create();
//                                alertDialog.show();

                                Dialog dialog = new Dialog(getActivity());
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                               // dialog.setTitle("Need a Ride!");

                            dialog.setContentView(R.layout.dialog_map);
                                dialog.show();
                                GoogleMap googleMap;


                                MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
                                Button button= dialog.findViewById(R.id.dialogButton);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.cancel();
                                    }
                                });
                                MapsInitializer.initialize(getActivity());

                                mMapView = (MapView) dialog.findViewById(R.id.dialogMap);
                                mMapView.onCreate(dialog.onSaveInstanceState());
                                mMapView.onResume();
                                mMapView.getMapAsync(new OnMapReadyCallback() {

                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {


                                        DirectionsResult results = getDirectionsDetails(fromLat + ", " + toLng, toLat + ", " + toLng, TravelMode.DRIVING);
                                        if (results != null) {

                                            addPolyline(results, googleMap);
                                            positionCamera(results.routes[overview], googleMap);
                                            addMarkersToMap(results, googleMap);
                                           // Toast.makeText(getContext(), "hi", Toast.LENGTH_SHORT).show();
                                        }
                                        googleMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider's Location"));
                                    }
                                });// needed to get the map to display immediately
                               // googleMap = mMapView.getMap();


                                String notificationId= (String) dc.getDocument().get("chatroomId");
                                if(notificationId.contentEquals(id)) {
                                    Notification builderNoti = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                                            .setSmallIcon(R.drawable.user)
                                            .setContentTitle("Need a Ride!")
                                            .setContentText("Someone need a ride."+chatRoom.roomName+" Go check it out!")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                                            .build();
                                    //

                                    notificationManager.notify(0, builderNoti);
                                    id="";
                                }
                            }
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Trip Modified: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Trip Removed: " + dc.getDocument().getData());
                            break;
                    }
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextMessage.getText().toString().trim().equals("")) {
                    Message message = new Message();
                    message.messageText = editTextMessage.getText().toString();
                    message.userId = mAuth.getCurrentUser().getUid();
                    Date currentTime = Calendar.getInstance().getTime();
                    message.messageTimeStamp = String.valueOf(currentTime);
                    message.chatRoomId = chatRoom.roomId;
                    mListener.sendMessage(message, editTextMessage);
                } else {
                    Toast.makeText(getContext(), "Cannot send blank message", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[overview]
                .legs[overview].duration.humanReadable + " Distance :" + results.routes[overview]
                .legs[overview].distance.humanReadable;
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
    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey("AIzaSyCBIdjxTUh5TIC01R-c1RTRYx9YdOfPLeY")
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
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.more_options_menu, menu);
        menu.findItem(R.id.startTrip).setVisible(true);
        menu.findItem(R.id.addChatroom).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.startTrip:
                mListener.displayMakeTripActivity(this.chatRoom.roomId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUpActiveUserRecyclerView(View view) {

        recyclerViewActiveUser = view.findViewById(R.id.displayActiveUserRecyclerView);
        recyclerViewActiveUser.setHasFixedSize(true);
        layoutManagerActiveUser = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewActiveUser.setLayoutManager(layoutManagerActiveUser);

        // specify an adapter
        mAdapterActiveUser = new DisplayActiveUsersAdapter(mListener.getActiveUserList(), (Context) mListener);
        recyclerViewActiveUser.setAdapter(mAdapterActiveUser);
        mListener.getActiveUserAdapter(mAdapterActiveUser);
    }

    public void setUpDisplayChatsRecyclerView(View view) {

        recyclerView = view.findViewById(R.id.displayChatRecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new DisplayChatsAdapter(mListener.getChatList(), (Context) mListener);
        recyclerView.setAdapter(mAdapter);
        mListener.getAdapter(mAdapter);
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


    public interface OnFragmentInteractionListener {
        ArrayList<Message> getChatList();

        ArrayList<User> getActiveUserList();

        void displayMakeTripActivity(String chatroomId);

        void getAdapter(RecyclerView.Adapter mAdapter);

        void getActiveUserAdapter(RecyclerView.Adapter mAdapter);

        void sendMessage(Message message, EditText editTextMessage);

    }
}
