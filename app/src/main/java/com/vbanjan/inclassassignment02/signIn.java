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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import static android.widget.Toast.LENGTH_SHORT;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link signIn.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link signIn#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signIn extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button login;
    Button signUp;
    Button forget;
    EditText email;
    EditText password;
    private FirebaseAuth mAuth;
    Context context;
    private OnFragmentInteractionListener mListener;

    public signIn() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signIn.
     */
    // TODO: Rename and change types and number of parameters
    public static signIn newInstance(String param1, String param2) {
        signIn fragment = new signIn();
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
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        login = view.findViewById(R.id.login);
        forget = view.findViewById(R.id.forget);
        signUp = view.findViewById(R.id.signup);
        email = view.findViewById(R.id.editLastName);
        password = view.findViewById(R.id.editPassword);
        context = view.getContext();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack("signUp")
                        .replace(R.id.mapContainer, new signUp(), "DisplayAccount")
                        .commit();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter Email", LENGTH_SHORT).show();
                    email.setError("This field can not be blank");
                } else if (password.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getContext(), "Enter Password", LENGTH_SHORT).show();
                    password.setError("This field can not be blank");
                } else {

                    String logInEmail = email.getText().toString();
                    String logInPassword = password.getText().toString();
                    User user = new User();
                    user.userEmail = logInEmail;
                    user.userPassword = logInPassword;
                    Toast.makeText(context, logInEmail + " " + logInPassword, LENGTH_SHORT).show();
                    mListener.login(user);
//                    mAuth.signInWithEmailAndPassword(logInEmail, logInPassword)
//                            .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        // Sign in success, update UI with the signed-in user's information
//                                        Log.d(TAG, "signInWithEmail:success");
//                                        Toast.makeText(getContext(), "Log In Successful", LENGTH_SHORT).show();
//                                        FirebaseUser LogInUser = mAuth.getCurrentUser();
//
//                                    } else {
//                                        // If sign in fails, display a message to the user.
//                                        Log.w(TAG, "signInWithEmail:failure ", task.getException());
//                                        Toast.makeText(getContext(), "Authentication failed. " + task.getException().getMessage(),
//                                                LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
                }
            }
        });

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.displayResetPasswordFragment();
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

        void login(User user);

        void displayResetPasswordFragment();
    }
}
