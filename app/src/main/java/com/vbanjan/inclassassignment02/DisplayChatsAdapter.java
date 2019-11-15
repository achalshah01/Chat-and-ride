package com.vbanjan.inclassassignment02;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayChatsAdapter extends RecyclerView.Adapter<DisplayChatsAdapter.ViewHolder> {
    ArrayList<Message> messages;
    Context context;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();

    public DisplayChatsAdapter(ArrayList<Message> messages, Context context) {

        this.messages = messages;
        this.context = (Context) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_displaychat, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        mAuth = FirebaseAuth.getInstance();
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Message message = messages.get(position);

        holder.userName.setText(message.userName);
        holder.messageText.setText(message.messageText);
        holder.messageTime.setText(String.valueOf(message.messageTimeStamp));

        getMessageProfileImage(holder.userProfileImage, message.userId);

        holder.upvoteCount.setText(String.valueOf(message.upvotedBy.size()));

        Date out = null;
        try {
//            DateTime Format "Sat Apr 06 10:39:32 EDT 2019" "EEE MMM dd HH:mm:ss zzz yyyy"
            out = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(message.messageTimeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        PrettyTime prettyTime = new PrettyTime();
        holder.messageTime.setText(prettyTime.format(out));

        if (message.userId.equals(mAuth.getCurrentUser().getUid())) {
            holder.upVoteButton.setVisibility(View.INVISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.upVoteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.INVISIBLE);
        }

        if (message.isTrip) {
            Log.d(TAG, "onBindViewHolder: " + message.toString());
            holder.fromLocation.setVisibility(View.VISIBLE);
            holder.toLocation.setVisibility(View.VISIBLE);
            holder.tripLabel.setVisibility(View.VISIBLE);
            holder.offerRideBtn.setVisibility(View.VISIBLE);
            holder.fromLocation.setText("From: " + message.FromLocationName);
            holder.toLocation.setText("To: " + message.ToLocationName);

            if (mAuth.getCurrentUser().getUid().equals(message.userId)) {
                holder.offerRideBtn.setVisibility(View.GONE);
            } else {
                holder.offerRideBtn.setVisibility(View.VISIBLE);
            }

            if (message.status != null && message.status.equals("Created")) {
                holder.offerRideBtn.setVisibility(View.VISIBLE);
            } else {
                holder.offerRideBtn.setVisibility(View.GONE);
            }


        } else {
            holder.offerRideBtn.setVisibility(View.GONE);
            holder.fromLocation.setVisibility(View.GONE);
            holder.toLocation.setVisibility(View.GONE);
            holder.tripLabel.setVisibility(View.GONE);
        }


        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClicked(messages.get(position));
            }
        });

        holder.upVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upvoteClicked(messages.get(position));
            }
        });

        holder.offerRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                offerRideClicked(message);
            }
        });
    }

    String TAG = "demo";

    public void offerRideClicked(Message message) {
        //DRIVER Activity to show maps
        Intent intent = new Intent(context, DriverOfferActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("messageObj", message);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void deleteClicked(Message message) {

        db.collection("Chatrooms")
                .document(message.chatRoomId)
                .collection("Messages")
                .document(message.messageId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully deleted!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

        if (message.isTrip) {
            db.collection("Trips")
                    .document(message.messageId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });
        }
    }

    public void upvoteClicked(Message message) {
        Map<String, Object> data = new HashMap<>();
        data.put("messageUpvotedBy", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));

        db.collection("Chatrooms")
                .document(message.chatRoomId)
                .collection("Messages")
                .document(message.messageId)
                .set(data, SetOptions.merge());
    }

    public void getMessageProfileImage(final ImageView imageView, String userId) {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get("userProfileImage") != null) {
                            Picasso.get().load((String) document.getData().get("userProfileImage")).into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("demo", "onSuccess: Success!");
                                }

                                @Override
                                public void onError(Exception e) {
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, messageText, messageTime, upvoteCount, fromLocation, toLocation, tripLabel;
        ImageView deleteButton, upVoteButton, userProfileImage, offerRideBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameTextView);
            messageText = itemView.findViewById(R.id.messageTextView);
            deleteButton = itemView.findViewById(R.id.deleteBtnImageView);
            upVoteButton = itemView.findViewById(R.id.upvoteBtnImageView);
            messageTime = itemView.findViewById(R.id.messageTimeTextView);
            upvoteCount = itemView.findViewById(R.id.upVotesTextView);
            userProfileImage = itemView.findViewById(R.id.userImageView);
            fromLocation = itemView.findViewById(R.id.tripFromTextView);
            toLocation = itemView.findViewById(R.id.tripToTextView);
            tripLabel = itemView.findViewById(R.id.labelTripTextView);
            offerRideBtn = itemView.findViewById(R.id.offerRideImageView);
        }
    }

}
