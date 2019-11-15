package com.vbanjan.inclassassignment02;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DisplayActiveUsersAdapter extends RecyclerView.Adapter<DisplayActiveUsersAdapter.ViewHolder> {
    ArrayList<User> userArrayList;
    Context context;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();

    public DisplayActiveUsersAdapter(ArrayList<User> userArrayList, Context context) {
        this.userArrayList = userArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_activeuser, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userArrayList.get(position);
        holder.activeUserName.setText(user.firstName);
        getActiveUserProfileImage(holder.activeUserImage, user);

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public void getActiveUserProfileImage(final ImageView imageView, User user) {
        if (user.profileImage != null) {
            Picasso.get().load(user.profileImage).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d("demo", "onSuccess: Success!");
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView activeUserName;
        ImageView activeUserImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            activeUserName = itemView.findViewById(R.id.activeUserTextView);
            activeUserImage = itemView.findViewById(R.id.activeUserImageView);
        }
    }

}
