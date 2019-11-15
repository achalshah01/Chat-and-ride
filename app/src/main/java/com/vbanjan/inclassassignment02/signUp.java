package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link signUp.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link signUp#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signUp extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText confirmPassword;
    EditText city;
    EditText gender;
    Button signUp;
    ImageButton imageButton;
    RadioGroup  genderRadioGroup;
    String FirstName, LastName, Email, Password, Gender="Male", City;
    public signUp() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signUp.
     */
    // TODO: Rename and change types and number of parameters
    public static signUp newInstance(String param1, String param2) {
        signUp fragment = new signUp();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        View view=inflater.inflate(R.layout.fragment_sign_up, container, false);

        firstName=view.findViewById(R.id.editFirstName);
        lastName=view.findViewById(R.id.editLastName);
        email=view.findViewById(R.id.editEmailId);
        password=view.findViewById(R.id.editPassword);
        confirmPassword=view.findViewById(R.id.confirmPassword);
       city=view.findViewById(R.id.cty);
        signUp=view.findViewById(R.id.button2);
        imageButton=view.findViewById(R.id.editImageButton);
        genderRadioGroup = view.findViewById(R.id.radioGroup);




        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.maleRadioButton:
                        Gender = "Male";
                        break;
                    case R.id.femaleRadioButton:
                        Gender = "Female";
                        break;
                    default:
                        break;
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.selectImage(imageButton);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (firstName.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter First Name", Toast.LENGTH_SHORT).show();
                    firstName.setError("This field can not be blank");
                } else if (lastName.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter Last Name", Toast.LENGTH_SHORT).show();
                    lastName.setError("This field can not be blank");
                } else if (email.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter Email ID", Toast.LENGTH_SHORT).show();
                    email.setError("This field can not be blank");
                } else if ((password.getText().toString().trim().equalsIgnoreCase(""))) {
                    Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                    password.setError("This field can not be blank");
                } else if ((confirmPassword.getText().toString().trim().equalsIgnoreCase(""))) {
                    Toast.makeText(getContext(), "Enter Confirm Password", Toast.LENGTH_SHORT).show();
                    confirmPassword.setError("This field can not be blank");
                } else if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getContext(), "Select Gender", Toast.LENGTH_SHORT).show();
                } else {
                    if (password.getText().toString().equals(confirmPassword.getText().toString())) {
                        FirstName = firstName.getText().toString().trim();
                        LastName = lastName.getText().toString().trim();
                        Email = email.getText().toString().trim();
                        Password = password.getText().toString().trim();
                        City = city.getText().toString().trim();
                        User user = new User();
                        user.firstName = FirstName;
                        user.lastName = LastName;
                        user.userEmail = Email;
                        user.userPassword = Password;
                        user.gender = Gender;
                        user.city = City;
                        mListener.signUp(user);
                    } else {
                        password.setError("Passwords do not match");
                        confirmPassword.setError("Passwords do not match");
                    }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void signUp(User user);

        void selectImage(ImageView imageUpload);

    }
}
