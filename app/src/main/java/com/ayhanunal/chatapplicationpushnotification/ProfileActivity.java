package com.ayhanunal.chatapplicationpushnotification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    EditText ageText;
    ImageView userImageView;
    Uri selected;
    Button uploadButton;


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private StorageReference storageReference;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ageText = findViewById(R.id.profile_age_text);
        userImageView = findViewById(R.id.profile_image_view);
        uploadButton = findViewById(R.id.profile_upload_button);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();

        getData();


    }


    public void getData(){

        DatabaseReference newReferance = firebaseDatabase.getReference("Profiles");


        newReferance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap = (HashMap<String, String>) snapshot.getValue();

                    String gelenUserEmail = hashMap.get("useremail");

                    if(gelenUserEmail.matches(firebaseAuth.getCurrentUser().getEmail().toString())){

                        String gelenUserAge = hashMap.get("userage");
                        String gelenUserImage = hashMap.get("userimageurl");

                        if(gelenUserAge != null && gelenUserImage != null){
                            ageText.setText(gelenUserAge);

                            Picasso.get().load(gelenUserImage).into(userImageView);

                            uploadButton.setVisibility(View.INVISIBLE);
                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void upload(View view){

        final UUID uuid = UUID.randomUUID();

        String imageName = "images/"+uuid+".jpg";

        StorageReference reference = storageReference.child(imageName);
        reference.putFile(selected).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                StorageReference profileImageReferance = FirebaseStorage.getInstance().getReference("images/"+uuid+".jpg");

                profileImageReferance.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String downloadURL = uri.toString();

                        UUID imageUuid = UUID.randomUUID();
                        String randomImage = imageUuid.toString();

                        databaseReference.child("Profiles").child(randomImage).child("userimageurl").setValue(downloadURL);

                        String userAge = ageText.getText().toString();
                        databaseReference.child("Profiles").child(randomImage).child("userage").setValue(userAge);

                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        String userEmail = firebaseUser.getEmail().toString();
                        databaseReference.child("Profiles").child(randomImage).child("useremail").setValue(userEmail);

                        Toast.makeText(ProfileActivity.this, "Uploaded",Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                        startActivity(intent);

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });

    }


    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);


        }else {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 2 && resultCode == RESULT_OK && data != null){

            selected = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selected);
                userImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}