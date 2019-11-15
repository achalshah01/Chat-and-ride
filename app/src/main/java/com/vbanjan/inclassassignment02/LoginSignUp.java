package com.vbanjan.inclassassignment02;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginSignUp extends AppCompatActivity implements signIn.OnFragmentInteractionListener, signUp.OnFragmentInteractionListener, ResetPassword.OnFragmentInteractionListener {
    private FirebaseAuth mAuth;
    FirebaseStorage mStorage ;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "demo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapContainer, new signIn())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void resetEmail(User user) {
        mAuth.sendPasswordResetEmail(user.userEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent." + task.toString());
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginSignUp.this);
                            builder.setTitle("Password requent sent!")
                                    .setMessage("We've emailed you instructions on how to reset your password. If you don't see it, don't forget to check your spam folder.")
                                    .setPositiveButton("Login Again", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getSupportFragmentManager().popBackStack();
                                        }
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Toast.makeText(LoginSignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void signUp(final User user) {
        mAuth.createUserWithEmailAndPassword(user.userEmail, user.userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("demo", "createUserWithEmail:success");
                            final FirebaseUser SignUpUser = mAuth.getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user.firstName + " " + user.lastName)
                                    .build();
                            SignUpUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("demo", "User profile updated.");
                                                Toast.makeText(LoginSignUp.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                                createUserDB(user);
                                                Intent intent = new Intent(LoginSignUp.this, ChatroomActivity.class);
                                                startActivity(intent);


                                                //setLoggedInUser(SignUpUser);
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("demo", "createUserWithEmail:failure ", task.getException());
                            Toast.makeText(LoginSignUp.this, "Authentication failed. " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
//                            data.put("tripsFulfilled", Arrays.asList());
//                            data.put("tripsTaken","");

                            db.collection("Users").document(mAuth.getCurrentUser().getUid())
                                    .set(data, SetOptions.merge())
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
            data.put("userProfileImage", "https://icon-library.net/images/default-profile-icon/default-profile-icon-24.jpg");

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

    @Override
    public void login(User user) {
        mAuth.signInWithEmailAndPassword(user.userEmail, user.userPassword)
                .addOnCompleteListener(LoginSignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("demo", "signInWithEmail:success");
                            Toast.makeText(LoginSignUp.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser LogInUser = mAuth.getCurrentUser();
                            // setLoggedInUser(LogInUser);
                            TextView tvLastName = findViewById(R.id.editLastName);
                            TextView tvPassword = findViewById(R.id.editPassword);
                            tvLastName.setText("");
                            tvPassword.setText("");

                            Intent intent = new Intent(LoginSignUp.this, ChatroomActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("demo", "signInWithEmail:failure ", task.getException());
                            Toast.makeText(LoginSignUp.this, "Authentication failed. " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void displayResetPasswordFragment() {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("forgotPassword")
                .replace(R.id.mapContainer, new ResetPassword(), "ResetPassword")
                .commit();
    }
}
