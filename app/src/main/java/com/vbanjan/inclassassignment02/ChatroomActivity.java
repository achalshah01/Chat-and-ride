package com.vbanjan.inclassassignment02;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ChatroomActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener,
        DisplayChatRooms.OnFragmentInteractionListener, DisplayProfile.OnFragmentInteractionListener,
        DisplayTripHistory.OnFragmentInteractionListener, DisplayAllUsers.OnFragmentInteractionListener,
        DisplayChatRoomsAdapter.OnAdapterInteractionListener, DisplayChats.OnFragmentInteractionListener, EditDetails.OnFragmentInteractionListener {

    private DrawerLayout drawer;
    private Menu menu;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth;

    String TAG = "demo";
    ArrayList<Chatroom> chatRoomsList = new ArrayList<>();
    ArrayList<Message> messageArrayList = new ArrayList<>();
    ArrayList<User> activeUserArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mAuth = FirebaseAuth.getInstance();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            firebaseGetChatRoomsList();
            navigationView.setCheckedItem(R.id.nav_chat);
        }

        getLoggedInUserNavBar(navigationView);

    }

    public void getLoggedInUserNavBar(final NavigationView navigationView) {
        db.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d(TAG, "Current data: " + snapshot.getData());

                            View headerView = navigationView.getHeaderView(0);
                            TextView navUserName = headerView.findViewById(R.id.userName);
                            TextView navUserEmail = headerView.findViewById(R.id.userEmail);
                            ImageView navUserImage = headerView.findViewById(R.id.profileImage);
                            navUserName.setText(snapshot.get("userFirstName") + " " + snapshot.get("userLastName"));
                            navUserEmail.setText((String) snapshot.get("userEmail"));

                            if (snapshot.get("userProfileImage") != null) {
                                Picasso.get().load((String) snapshot.get("userProfileImage")).into(navUserImage, new Callback() {
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
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });


    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                if (currentChatRoomId != null) {
                    DocumentReference activeUsersReference = db.collection("ActiveUsers").document(currentChatRoomId);
                    activeUsersReference.update("onlineUsers", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));
                    getSupportFragmentManager().popBackStack("overChatRoomList", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    public void makeUserOffline() {
        if (currentChatRoomId != null) {
            DocumentReference activeUsersReference = db.collection("ActiveUsers").document(currentChatRoomId);
            activeUsersReference.update("onlineUsers", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));
            getSupportFragmentManager().popBackStack("overChatRoomList", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        FragmentManager fm = getSupportFragmentManager();
        switch (menuItem.getItemId()) {

            case R.id.nav_chat:
                firebaseGetChatRoomsList();
                break;
            case R.id.nav_profile:
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                displayProfileFragment();
                makeUserOffline();
                break;
            case R.id.nav_trip:
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DisplayTripHistory()).commit();
                makeUserOffline();
                break;
            case R.id.nav_users:
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                displayAllUsersFragment();
                makeUserOffline();
                break;
            case R.id.nav_logout:
                Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
                makeUserOffline();
                logout();
                finish();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        messageArrayList.clear();
        activeUserArrayList.clear();
        chatRoomsList.clear();
    }

    public void displayAllUsersFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DisplayAllUsers()).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void displayEditDetailsFragment() {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("editUserProfile")
                .replace(R.id.fragment_container, new EditDetails())
                .commit();
    }

    public void firebaseGetChatRoomsList() {
        CollectionReference docRef = db.collection("Chatrooms");
        docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                chatRoomsList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    if (document.getData() != null) {
                        chatRoomsList.add(new Chatroom((String) document.getData().get("chatRoomName"), document.getId()));
                        chatRoomsList.sort(new Comparator<Chatroom>() {
                            @Override
                            public int compare(Chatroom o1, Chatroom o2) {
                                return o1.roomName.compareTo(o2.roomName);
                            }
                        });
                    } else {
                        Toast.makeText(ChatroomActivity.this, "Oops! No Chatrooms Present. Create One?", Toast.LENGTH_SHORT).show();
                    }
                }
                displayChatroomsFragment();
            }
        });
    }


    public void displayChatroomsFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DisplayChatRooms()).commit();
    }

    public void displayChatsFragment(Chatroom chatRoom) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("overChatRoomList")
                .replace(R.id.fragment_container,
                        new DisplayChats(chatRoom)).commit();
    }

    @Override
    public ArrayList<Chatroom> getChatRoomList() {
        return chatRoomsList;
    }

    @Override
    public void createChatroom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new Chatroom, Name?");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Alright", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().trim().equals("")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("chatRoomName", input.getText().toString());
                    db.collection("Chatrooms").add(data)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                    Toast.makeText(ChatroomActivity.this, "Chatroom Created Successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                    Toast.makeText(ChatroomActivity.this, "Oops! Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ChatroomActivity.this, "Chatroom name cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Maybe Not", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    String currentChatRoomId;

    @Override
    public void enterChatRoom(final Chatroom chatRoom) {
        currentChatRoomId = chatRoom.roomId;

        Map<String, Object> data = new HashMap<>();
        data.put("onlineUsers", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));
        db.collection("ActiveUsers").document(chatRoom.roomId).set(data, SetOptions.merge());
        activeUserArrayList.clear();
        messageArrayList.clear();

        CollectionReference chatRoomMessagesRef = db.collection("Chatrooms")
                .document(chatRoom.roomId)
                .collection("Messages");
        chatRoomMessagesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                messageArrayList.clear();
                if (queryDocumentSnapshots.size() == 0) {
                    getChatsAdapter.notifyDataSetChanged();
                }
                for (final QueryDocumentSnapshot messageDoc : queryDocumentSnapshots) {
                    if (!messageDoc.getData().isEmpty()) {
                        final Message message = new Message();
                        message.chatRoomId = chatRoom.roomId;
                        message.userId = (String) messageDoc.getData().get("createdBy");
                        message.messageText = (String) messageDoc.get("messageText");
                        if (messageDoc.getData().get("messageUpvotedBy") != null)
                            message.upvotedBy = (ArrayList<String>) messageDoc.getData().get("messageUpvotedBy");
                        else message.upvotedBy = new ArrayList<>();
                        message.messageTimeStamp = messageDoc.get("messageTime") + "";
                        message.messageId = messageDoc.getId();
                        if (messageDoc.getData().get("isTrip") != null
                                && (boolean) messageDoc.getData().get("isTrip")) {
                            message.isTrip = (Boolean) messageDoc.getData().get("isTrip");
                        } else {
                            message.isTrip = false;
                        }
                        db.collection("Users").document((String) messageDoc.getData().get("createdBy")).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot userInfoDoc = task.getResult();
                                            if (userInfoDoc.exists()) {
                                                message.userName = userInfoDoc.getData().get("userFirstName")
                                                        + " " + userInfoDoc.getData().get("userLastName");
                                                if (message.isTrip) {
                                                    db.collection("Trips").document(messageDoc.getId()).get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    DocumentSnapshot tripInfoDoc = task.getResult();
                                                                    if (tripInfoDoc.exists()) {
                                                                        message.FromLat = (Double) tripInfoDoc.get("FromLat");
                                                                        message.FromLng = (Double) tripInfoDoc.get("FromLng");
                                                                        message.ToLat = (Double) tripInfoDoc.get("ToLat");
                                                                        message.ToLng = (Double) tripInfoDoc.get("ToLng");
                                                                        message.FromLocationName = (String) tripInfoDoc.get("FromLocationName");
                                                                        message.ToLocationName = (String) tripInfoDoc.get("ToLocationName");
                                                                        if (tripInfoDoc.getData().get("Status") != null) {
                                                                            message.status = (String) tripInfoDoc.getData().get("Status");
                                                                        } else {
                                                                            message.status = "Unavailable";
                                                                        }
                                                                        if (!messageArrayList.contains(message))
                                                                            messageArrayList.add(message);

                                                                        messageArrayList.sort(new Comparator<Message>() {
                                                                            DateFormat f = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

                                                                            @Override
                                                                            public int compare(Message o1, Message o2) {
                                                                                try {
                                                                                    return f.parse(o1.messageTimeStamp).compareTo(f.parse(o2.messageTimeStamp));
                                                                                } catch (ParseException e) {
                                                                                    throw new IllegalArgumentException(e);
                                                                                }
                                                                            }
                                                                        });
                                                                        getChatsAdapter.notifyDataSetChanged();
                                                                    } else {
                                                                        Log.d(TAG, "No such Trip document");
                                                                    }
                                                                }
                                                            });
                                                }
                                                if (!messageArrayList.contains(message))
                                                    messageArrayList.add(message);
                                                messageArrayList.sort(new Comparator<Message>() {
                                                    DateFormat f = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

                                                    @Override
                                                    public int compare(Message o1, Message o2) {
                                                        try {
                                                            return f.parse(o1.messageTimeStamp).compareTo(f.parse(o2.messageTimeStamp));
                                                        } catch (ParseException e) {
                                                            throw new IllegalArgumentException(e);
                                                        }
                                                    }
                                                });
                                                getChatsAdapter.notifyDataSetChanged();
                                            } else {
                                                Log.d(TAG, "No such User document");
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
        });

        DocumentReference activeUserReference = db.collection("ActiveUsers").document(chatRoom.roomId);
        activeUserReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                activeUserArrayList.clear();
                if (snapshot != null && snapshot.exists()) {
                    for (final String userId : (ArrayList<String>) snapshot.get("onlineUsers")) {
                        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot userInfoDoc = task.getResult();
                                    if (userInfoDoc.exists()) {
                                        User user = new User();
                                        user.userId = userId;
                                        user.firstName = (String) userInfoDoc.get("userFirstName");
                                        user.profileImage = (String) userInfoDoc.get("userProfileImage");
                                        Log.d(TAG, "onComplete: " + user);
                                        if (!activeUserArrayList.contains(user))
                                            activeUserArrayList.add(user);
                                    }
                                }
                                getActiveUserAdapter.notifyDataSetChanged();
                            }
                        });

                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        displayChatsFragment(chatRoom);

    }

    @Override
    public ArrayList<Message> getChatList() {
        return messageArrayList;
    }

    @Override
    public ArrayList<User> getActiveUserList() {
        return activeUserArrayList;
    }

    @Override
    public void displayMakeTripActivity(String chatroomId) {
        Log.d(TAG, "displayMakeTripActivity: ");
        Intent intent = new Intent(this, MakeTripActivity.class);
        intent.putExtra("chatRoomId", chatroomId);
        startActivity(intent);
    }

    RecyclerView.Adapter getChatsAdapter = null;

    @Override
    public void getAdapter(RecyclerView.Adapter mAdapter) {
        getChatsAdapter = mAdapter;
    }

    RecyclerView.Adapter getActiveUserAdapter;

    @Override
    public void getActiveUserAdapter(RecyclerView.Adapter mAdapter) {
        getActiveUserAdapter = mAdapter;
    }

    @Override
    public void sendMessage(Message message, final EditText editTextMessage) {
        CollectionReference messageRef = db.collection("Chatrooms").document(message.chatRoomId)
                .collection("Messages");
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageText", message.messageText);
        messageMap.put("messageTime", message.messageTimeStamp);
        messageMap.put("createdBy", message.userId);
        messageRef
                .add(messageMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        editTextMessage.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        editTextMessage.setText("");
                        Toast.makeText(ChatroomActivity.this, "Oops! Something went wrong, try again!", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    @Override
    public void updateUser(final User user) {
        final FirebaseUser editUser = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.firstName + " " + user.lastName)
                .build();


        editUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            Toast.makeText(ChatroomActivity.this, "Edit Update Successful", Toast.LENGTH_SHORT).show();
                            createUserDB(user);
                        }
                    }
                });


        createUserDB(user);


        displayProfileFragment();
    }

    public void displayProfileFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DisplayProfile()).commit();
    }


    public void createUserDB(final User user) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (cameraBitmap != null || galleryBitmap != null) {
            Bitmap bitmap = (cameraBitmap != null) ? cameraBitmap : galleryBitmap;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            String path = "UserProfile/" + mAuth.getCurrentUser().getUid() + ".png";
            final StorageReference imageRef = mStorage.getReference(path);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("ID", mAuth.getCurrentUser().getUid())
                    .build();
            UploadTask uploadTask = imageRef.putBytes(data, metadata);
            uploadTask.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    mStorage.getReference().child("/UserProfile/" + mAuth.getCurrentUser().getUid() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("userFirstName", user.firstName);
                            data.put("userLastName", user.lastName);
                            data.put("userEmail", user.userEmail);
                            data.put("userProfileImage", uri.toString());
                            data.put("userGender", user.gender);
                            data.put("userCity", user.city);
                            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                                    .set(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                            cameraBitmap = null;
                            galleryBitmap = null;
                        }
                    });
                }
            });
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("userFirstName", user.firstName);
            data.put("userLastName", user.lastName);
            data.put("userEmail", user.userEmail);
            data.put("userGender", user.gender);
            data.put("userCity", user.city);

            if (user.profileImage.length() != 0) {
                data.put("userProfileImage", user.profileImage);

            } else {
                data.put("userProfileImage", "https://icon-library.net/images/default-profile-icon/default-profile-icon-24.jpg");
            }
            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }

    ImageView userProfileImage;
    String[] chooseCameraOption = {"Take from Camera", "Choose from Gallery"};

    @Override
    public void selectImage(ImageView imageUpload) {
        userProfileImage = imageUpload;
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose Option");
        builder.setItems(chooseCameraOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    dispatchTakePictureIntent();
                } else {
                    dispatchSelectPictureIntent();
                }
            }
        });
        builder.create().show();
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_FROM_GALLERY = 2;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, REQUEST_IMAGE_FROM_GALLERY);
        }
    }

    Bitmap galleryBitmap = null;
    Bitmap cameraBitmap = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            cameraBitmap = (Bitmap) extras.get("data");
            cameraBitmap = Bitmap.createScaledBitmap(cameraBitmap, 600, 600, true);
            userProfileImage.setImageBitmap(cameraBitmap);
        } else if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                galleryBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                galleryBitmap = Bitmap.createScaledBitmap(galleryBitmap, 600, 600, true);
                userProfileImage.setImageBitmap(galleryBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
