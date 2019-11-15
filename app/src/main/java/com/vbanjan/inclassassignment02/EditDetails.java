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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditDetails extends Fragment {

    private EditDetails.OnFragmentInteractionListener mListener;
    EditText lastname, firstname, email, city;
    String gender = "Male";
    String FirstName, LastName, Email, Password, Gender = "Male", City, imageUrl;

    //    ImageButton profileImage;
    CircleImageView profileImage;
    Button update;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth;
    RadioGroup genderRadioGroup;


    String TAG = "demo";

    public EditDetails() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (EditDetails.OnFragmentInteractionListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_details, container, false);

        firstname = (EditText) view.findViewById(R.id.editFirstname);
        lastname = view.findViewById(R.id.editLastname);
        email = view.findViewById(R.id.editEmailid);
        city = view.findViewById(R.id.editCity);

        update = view.findViewById(R.id.editUpdate);
        mAuth = FirebaseAuth.getInstance();
        profileImage = view.findViewById(R.id.imageButton);
        genderRadioGroup = view.findViewById(R.id.EditRadioGroup);


        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.maleRadioButton:
                        gender = "Male";
                        break;
                    case R.id.femaleRadioButton:
                        gender = "Female";
                        break;
                    default:
                        break;
                }
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selectImage(profileImage);
            }
        });
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
                        email.setEnabled(false);
                        Email = document.getString("userEmail");
                        imageUrl = document.getString("userProfileImage");
                        city.setText(document.getString("userCity"));

                        if (document.getString("userGender").contentEquals("Male")) {
                            genderRadioGroup.check(R.id.maleRadioButton);
                        } else genderRadioGroup.check(R.id.femaleRadioButton);
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

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstname.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter First Name", Toast.LENGTH_SHORT).show();
                    firstname.setError("This field can not be blank");
                } else if (lastname.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter Last Name", Toast.LENGTH_SHORT).show();
                    lastname.setError("This field can not be blank");

                } else if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getContext(), "Select Gender", Toast.LENGTH_SHORT).show();
                } else {

                    FirstName = firstname.getText().toString().trim();
                    LastName = lastname.getText().toString().trim();
                    Email = email.getText().toString().trim();
                    City = city.getText().toString().trim();
                    User user = new User();
                    user.firstName = FirstName;
                    user.lastName = LastName;
                    user.userEmail = Email;
                    user.gender = gender;
                    user.city = City;
                    user.profileImage = imageUrl;
                    mListener.updateUser(user);
                }
            }
        });

        return view;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void updateUser(User user);

        void selectImage(ImageView imageUpload);
    }
}
