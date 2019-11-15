package com.vbanjan.inclassassignment02;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class SelectWhereToFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    Double fromLat, fromLong;

    public SelectWhereToFragment(Double fromLat, Double fromLong) {
        this.fromLat = fromLat;
        this.fromLong = fromLong;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_where_to, container, false);
        if (Geocoder.isPresent()) {
            new ReverseGeocodingTask(fromLat, fromLong, view).execute();
        } else {
            Toast.makeText(getContext(), "Geocoder not present", Toast.LENGTH_SHORT).show();
        }


        //Get API Key from manifest
        ApplicationInfo ai = null;
        try {
            ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = ai.metaData;
        String myApiKey = bundle.getString("com.google.android.geo.API_KEY");


        // Initialize the SDK
        Places.initialize(getContext(), myApiKey);

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(getContext());

        view.findViewById(R.id.ToEditText).setFocusable(false);
        view.findViewById(R.id.ToEditText).setClickable(true);
        view.findViewById(R.id.ToEditText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.openAutoCompleteIntent(fromLat, fromLong);
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

    String TAG = "demo";

    class ReverseGeocodingTask extends AsyncTask<Void, Void, Address> {
        Double lat, lng;
        View view;

        public ReverseGeocodingTask(Double lat, Double lng, View view) {
            this.lat = lat;
            this.lng = lng;
            this.view = view;
        }

        @Override
        protected Address doInBackground(Void... voids) {
            Geocoder geocoder = new Geocoder(getContext());
            Address address = null;
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null) {
                    address = addresses.get(0);
                } else {
                    Log.d(TAG, "doInBackground: isNull");
                }
            } catch (IOException e) {

            }
            return address;
        }

        @Override
        protected void onPostExecute(Address address) {
            Log.d(TAG, "onPostExecute: " + address.getAddressLine(0));
            EditText fromET = view.findViewById(R.id.FromEditText);
            fromET.setText(address.getAddressLine(0));
            fromET.setEnabled(false);
            mListener.fromLocationName(address.getAddressLine(0));
            super.onPostExecute(address);
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

        default void openAutoCompleteIntent(Double lat, Double lng) {
        }

        void fromLocationName(String fromName);
    }
}
