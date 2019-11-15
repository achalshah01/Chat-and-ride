package com.vbanjan.inclassassignment02;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.ViewHolder> {
    ArrayList<User> allUsers;
    EditText name;
    ImageView dp;
    User singleUser;

    public AllUsersAdapter(ArrayList<User> allUsers) {
        this.allUsers = allUsers;
    }


    @NonNull
    @Override
    public AllUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_of_all_users, parent, false);
        AllUsersAdapter.ViewHolder viewHolder = new AllUsersAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AllUsersAdapter.ViewHolder holder, int position) {
        User user=allUsers.get(position);
        name.setText(user.firstName+ " "+user.lastName);
        Picasso.get().load(user.profileImage).into(dp);
        singleUser=user;


    }

    @Override
    public int getItemCount() {
        {
            return allUsers.size();
        }


    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.name);
            name.setEnabled(false);
            dp=itemView.findViewById(R.id.image);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    singleUser=allUsers.get(getAdapterPosition());
                    FragmentManager manager = ((AppCompatActivity)view.getContext()).getSupportFragmentManager();
                   DisplayProfile displayProfile=new DisplayProfile();
                    Bundle bundle = new Bundle();
                    bundle.putString("id",singleUser.userId);

                    bundle.putString("first",singleUser.firstName);
                    bundle.putString("last",singleUser.lastName);
                    bundle.putString("email",singleUser.userEmail);
                    bundle.putString("city",singleUser.city);
                    bundle.putString("gender",singleUser.gender);
                    bundle.putString("image",singleUser.profileImage);


                    displayProfile.setArguments(bundle);
                   manager.beginTransaction().addToBackStack("displaysingleUser").replace(R.id.fragment_container,
                            displayProfile).commit();

                }
            });
        }
    }
}

