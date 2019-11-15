package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripHistoryGiven.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripHistoryGiven#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripHistoryGiven extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<TripHistory> tripHistories = new ArrayList<>();
    private FirebaseAuth mAuth;
    TextView given;

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
        View view = inflater.inflate(R.layout.fragment_trip_history_given, container, false);
        given = view.findViewById(R.id.textView3);
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.given);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);

        DocumentReference docRef;
        docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        tripHistories.clear();
                        ArrayList<String> rides = new ArrayList<>();
                        if (document.getData().get("tripsFulfilled") == null) {
                            rides.clear();
                        } else {
                            rides = (ArrayList<String>) document.getData().get("tripsFulfilled");
                        }
                        if (rides.size() > 0) {
                            given.setText("");
                            rides = (ArrayList<String>) document.getData().get("tripsFulfilled");
                            for (int i = 0; i < rides.size(); i++) {
                                DocumentReference docRef = db.collection("Trips").document(rides.get(i));
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                TripHistory tripHistory = new TripHistory();
                                                tripHistory.setFrom(document.getData().get("FromLocationName").toString());
                                                tripHistory.setTo(document.getData().get("ToLocationName").toString());
                                                tripHistory.setStatus(document.getData().get("Status").toString());

                                                tripHistories.add(tripHistory);
                                                mAdapter = new TripHistoryAdapter2(tripHistories);
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

                        } else {
                            ///set text to to rides
                            given.setText("No Trips Offered yet...");
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


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
