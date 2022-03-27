package com.pinkstain.stainedmall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_setup extends AppCompatActivity {

    private String user_id;
    private CircleImageView profilepic;
    private Uri mainImageURI = null;
    private EditText setupName;
    private Button setupBtn;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

//        user_id = firebaseAuth.getCurrentUser().getUid();
        profilepic = findViewById(R.id.profilepic);
        setupName = findViewById(R.id.editText_name);
        setupBtn = findViewById(R.id.button_save);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.setup_progress);
        firebaseFirestore = FirebaseFirestore.getInstance();







        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String user_name = setupName.getText().toString();

                if(!TextUtils.isEmpty(user_name) && mainImageURI != null) {

                    progressBar.setVisibility(View.VISIBLE);
                    user_id = firebaseAuth.getCurrentUser().getUid();


                    StorageReference image_path = storageReference.child("profile_images").child(user_id + "jpg");
                    image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();

                            while(!urlTask.isSuccessful());

                            final String download_uri = urlTask.getResult().toString();

                            if(task.isSuccessful()){

                               // Task<Uri> download_uri = task.getResult().getStorage().getDownloadUrl();

                                Map<String, String> userMap = new HashMap<>();
                                userMap.put("name", user_name);
                                userMap.put("image", download_uri);

                                firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if(task.isSuccessful()){



                                            Intent mainIntent = new Intent(activity_setup.this, MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();

                                        }else{

                                            String error = task.getException().getMessage();
                                            Toast.makeText(activity_setup.this,"(FIRESTORE ERROR) : " + error, Toast.LENGTH_LONG).show();



                                        }
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });

                            }else{

                                String error = task.getException().getMessage();
                                Toast.makeText(activity_setup.this,"(IMAGE ERROR) : " + error, Toast.LENGTH_LONG).show();

                            }


                        }
                    });


                }else{

                    Toast.makeText(activity_setup.this,"Please Enter your full name and upload profile picture", Toast.LENGTH_LONG).show();

                }

            }
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(activity_setup.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(activity_setup.this,"Please grant permissions",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(activity_setup.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);


                    }



                    BringImagePicker();
                }else{

                    BringImagePicker();

                }




            }
        });
    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(activity_setup.this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();

                profilepic.setImageURI(mainImageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }



}
