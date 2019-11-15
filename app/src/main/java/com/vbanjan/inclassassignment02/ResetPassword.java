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


public class ResetPassword extends Fragment {

    private OnFragmentInteractionListener mListener;
    EditText resetEmail;
    Button reset;

    public ResetPassword() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        resetEmail = view.findViewById(R.id.resetEmail);
        reset = view.findViewById(R.id.forget);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resetEmail.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
                    resetEmail.setError("This field can not be blank");
                } else {

                    User user = new User();
                    user.userEmail = resetEmail.getText().toString();
                    mListener.resetEmail(user);
                    resetEmail.setText("");
                }
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void resetEmail(User user);
    }
}
