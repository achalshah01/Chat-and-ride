package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayAllUsers.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DisplayAllUsers extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<User> allUsers=new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();

    public DisplayAllUsers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view=inflater.inflate(R.layout.fragment_display_all_users, container, false);

        recyclerView = view.findViewById(R.id.allUsers);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);

        displayAllUsers();


        return view;
    }

    private void displayAllUsers() {
        allUsers.clear();
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
//                            allUserNIds.clear();
//                            allUserNames.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = new User();
                                user.userId = document.getId();
                                user.firstName = (String) document.getData().get("userFirstName");
                                user.lastName = (String) document.getData().get("userLastName");
                                user.userEmail = (String) document.getData().get("userEmail");
                                user.gender = (String) document.getData().get("userGender");
                                user.city = (String) document.getData().get("userCity");
                                user.profileImage = (String) document.getData().get("userProfileImage");
                                allUsers.add(user);
                              //  Toast.makeText(getContext(), user.firstName, Toast.LENGTH_SHORT).show();
                                Log.d("demo",document.toString());
                            }
                             mAdapter = new AllUsersAdapter(allUsers);
                             recyclerView.setAdapter(mAdapter);
                        }
                    }
                });

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
