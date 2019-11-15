package com.vbanjan.inclassassignment02;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.android.volley.VolleyLog.TAG;

public class DriverListAdapter extends RecyclerView.Adapter<DriverListAdapter.ViewHolder> {

    ArrayList<Driver> driverList=new ArrayList<>();
    String messageId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public  DriverListAdapter(ArrayList<Driver> driverList,String messageId){
        this.driverList=driverList;
        this.messageId=messageId;
    }


    @NonNull
    @Override
    public DriverListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.driver_details, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DriverListAdapter.ViewHolder holder, int position) {
        Driver driver=driverList.get(position);
        holder.driverName.setText(driver.FirstName+" "+driver.LastName);
        holder.driverDistance.setText(driver.Distance+" "+"Miles");
        holder.driverLocation.setText(driver.Place);
        Picasso.get().load(driver.image).into(holder.dp);

    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        EditText driverName, driverLocation, driverDistance;
        ImageView dp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            driverName=itemView.findViewById(R.id.driverName);
            driverLocation=itemView.findViewById(R.id.driverLocation);
            driverDistance=itemView.findViewById(R.id.driverDistance);
            dp=itemView.findViewById(R.id.image);
            driverName.setEnabled(false);
            driverLocation.setEnabled(false);
            driverDistance.setEnabled(false);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final FragmentManager manager = ((AppCompatActivity)view.getContext()).getSupportFragmentManager();
                    final Driver driver=driverList.get(getAdapterPosition());
                    DocumentReference washingtonRef = db.collection("Trips").document(messageId);
                    washingtonRef
                            .update("Status", "Ongoing",
                                    "fulfilledBy",driver.uid)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    manager.beginTransaction().replace(R.id.mapContainer,
                                            new RiderRideDisplay(messageId,driver.uid,driver.FromLat,driver.FromLng,
                                                    driver.ToLat,driver.ToLng)).commit();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });

                }
            });
        }
    }
}
