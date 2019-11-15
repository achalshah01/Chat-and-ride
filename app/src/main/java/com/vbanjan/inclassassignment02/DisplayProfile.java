package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;


public class DisplayProfile extends Fragment {

    EditText lastname, firstname, email, city, gender;
    ImageView profileImage;
    Button edit;
    private OnFragmentInteractionListener mListener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth;

    String TAG = "demo";

    public DisplayProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_profile, container, false);
        firstname = (EditText) view.findViewById(R.id.firstname);
        lastname = view.findViewById(R.id.lastname);
        email = view.findViewById(R.id.email);
        city = view.findViewById(R.id.cty);
        gender = view.findViewById(R.id.gender);
        edit = view.findViewById(R.id.edit);
        mAuth = FirebaseAuth.getInstance();
        email.setEnabled(false);
        firstname.setEnabled(false);
        lastname.setEnabled(false);
        city.setEnabled(false);
        gender.setEnabled(false);
        profileImage = (ImageView) view.findViewById(R.id.profilePhoto);
        if (getArguments() != null) {
            Log.d("demo", getArguments().get("id").toString() + "is this equal   " + mAuth.getCurrentUser().getUid());
            if (getArguments().get("id").toString().contentEquals(mAuth.getCurrentUser().getUid())) {
                edit.setVisibility(View.VISIBLE);
            } else {
                edit.setVisibility(View.GONE);
            }
            firstname.setText(getArguments().get("first").toString());
            lastname.setText(getArguments().get("last").toString());
            email.setText(getArguments().get("email").toString());
            city.setText(getArguments().get("city").toString());
            gender.setText(getArguments().get("gender").toString());
            Picasso.get().load(getArguments().get("image").toString()).into(profileImage);


        } else {
            DocumentReference docRef;
            String id = mAuth.getCurrentUser().getUid();
            docRef = db.collection("Users").document(id);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            firstname.setText(document.getString("userFirstName"));
                            lastname.setText(document.getString("userLastName"));
                            email.setText(document.getString("userEmail"));
                            city.setText(document.getString("userCity"));
                            gender.setText(document.getString("userGender"));
                            email.setEnabled(false);
                            firstname.setEnabled(false);
                            lastname.setEnabled(false);
                            city.setEnabled(false);
                            gender.setEnabled(false);

                            Picasso.get().load(document.getString("userProfileImage")).into(profileImage);

                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.displayEditDetailsFragment();
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

        void displayEditDetailsFragment();

    }
}
