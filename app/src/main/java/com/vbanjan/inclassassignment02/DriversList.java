package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriversList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * <p>
 * create an instance of this fragment.
 */
public class DriversList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    String messageId;
    TextView textView;
    ArrayList<Driver> driverList = new ArrayList<>();

    public DriversList(String messageId) {
        this.messageId = messageId;
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
        View view = inflater.inflate(R.layout.fragment_drivers_list, container, false);
        ;
        final CircularProgressView progressView = (CircularProgressView) view.findViewById(R.id.progress_view);
        progressView.startAnimation();
        textView = view.findViewById(R.id.textView);
        recyclerView = view.findViewById(R.id.driversList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);


        final DocumentReference docRef = db.collection("Trips").document(messageId);
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
                    ArrayList<String> drivers = new ArrayList<>();
                    if (snapshot.getData().get("offeredBy") == null) {
                        drivers.clear();
                    } else {
                        drivers = (ArrayList<String>) snapshot.getData().get("offeredBy");
                    }
                    if (drivers.size() <= 0) {
                        driverList.clear();
                        progressView.startAnimation();
                        textView.setText("Waiting for Drivers to accept you ride...");
                        progressView.setVisibility(View.VISIBLE);
                        mAdapter = new DriverListAdapter(driverList, messageId);
                        recyclerView.setAdapter(mAdapter);
                    } else {
                        driverList.clear();
                        drivers = (ArrayList<String>) snapshot.getData().get("offeredBy");
                        progressView.stopAnimation();
                        progressView.setVisibility(View.INVISIBLE);
                        textView.setText("");
                        for (int i = 0; i < drivers.size(); i++) {
                            findDistance(drivers.get(i).toString(), snapshot.getData().get("FromLat").toString()
                                    , snapshot.getData().get("FromLng").toString(),
                                    snapshot.getData().get("ToLat").toString(),
                                    snapshot.getData().get("ToLng").toString()
                            );
                        }
                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        return view;
    }

    private void findDistance(final String uid, final String fromLat, final String fromLng, final String toLat, final String toLng) {


        DocumentReference docRef = db.collection("Users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        document.getData().get("currentLat").toString();
                        document.getData().get("currentLng").toString();
                        Location loc1 = new Location("");
                        loc1.setLatitude(Double.parseDouble(fromLat));
                        loc1.setLongitude(Double.parseDouble(fromLng));

                        Location loc2 = new Location("");
                        loc2.setLatitude(Double.parseDouble(document.getData().get("currentLat").toString()));
                        loc2.setLongitude(Double.parseDouble(document.getData().get("currentLng").toString()));
                        float distanceinMiles = (float) (loc1.distanceTo(loc2) * 0.000621371);
                        Driver driver = new Driver();
                        driver.setUid(uid);
                        driver.setFirstName(document.getData().get("userFirstName").toString());
                        driver.setLastName(document.getData().get("userLastName").toString());
                        driver.setImage(document.getData().get("userProfileImage").toString());
                        driver.setDistance(String.format("%.2f", distanceinMiles));
                        driver.setFromLat(fromLat);
                        driver.setToLng(toLng);
                        driver.setToLat(toLat);
                        driver.setFromLng(fromLng);
                        driver.setPlace(document.getData().get("userCity").toString());

                        driverList.add(driver);
                        mAdapter = new DriverListAdapter(driverList, messageId);
                        recyclerView.setAdapter(mAdapter);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
