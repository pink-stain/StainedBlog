package com.pinkstain.stainedmall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;


//import io.grpc.Compressor;
//import id.zelory.compressor.Compressor;

public class newPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100 ;
    private ImageView imageView;
    private ImageView post;

    private TextView share;
    private EditText caption;
    private Uri postImageUri = null;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

      //  getSupportActionBar().setTitle("New Post");


        storageReference = FirebaseStorage.getInstance().getReference();

        imageView = findViewById(R.id.imageView_new_post_back);
        post = findViewById(R.id.imageView_postedpic);
        caption = findViewById(R.id.editText_caption);
        share = findViewById(R.id.textView_share);
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar_posting);
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();





        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(3,4)
                        .start(newPostActivity.this);


            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                final String desc = caption.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null){



                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {



                            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();



                            while(!urlTask.isSuccessful());

                            final String download_uri = urlTask.getResult().toString();


                            if (task.isSuccessful()){

                                File newImageFile = new File(postImageUri.getPath());



                               try {
                                    compressedImageFile = new Compressor(newPostActivity.this)
                                            .setMaxHeight(16)
                                            .setMaxWidth(4)
                                            .setQuality(3)
                                            .compressToBitmap(newImageFile);
                                } catch(IOException e){
                                    e.printStackTrace();

                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbdata = baos.toByteArray();


                                //   String filePath = SiliCompressor.with(newPostActivity.this).compress(newImageFile.toString(), "post_images/thumbs");
                                UploadTask uploadTask= storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbdata);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Task<Uri> t_urlTask = task.getResult().getStorage().getDownloadUrl();



                                        while(!t_urlTask.isSuccessful());

                                        final String downloadthumbUri = t_urlTask.getResult().toString();

                                      //  String downloadthumbUri = taskSnapshot.getStorage().getDownloadUrl().toString();
                                      //  String real_url = task.toString();

                                        Map<String,Object> postMap = new HashMap<>();
                                        postMap.put("image_url", download_uri);
                                        postMap.put("image_thumb", downloadthumbUri);
                                        postMap.put("desc",desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()){

                                                    Intent mainIntent = new Intent(newPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();


                                                }else{

                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(newPostActivity.this,"(FIRESTORE ERROR) : " + error, Toast.LENGTH_LONG).show();
                                                    progressBar.setVisibility(View.GONE);

                                                }

                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });



                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //error

                                    }
                                });



                            }else{

                                String error = task.getException().getMessage();
                                Toast.makeText(newPostActivity.this,"(FIRESTORE ERROR) : " + error, Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);

                            }

                        }
                    });

                }else{

                    Toast.makeText(newPostActivity.this,"Please upload an image and Caption", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(newPostActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Intent mainIntent = new Intent(newPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

              postImageUri = result.getUri();

              post.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
  /*  private void compressAndUpload(){
        if(postImageUri != null){

            final File file = new File(SiliCompressor.with(this)
                    .compress(FileUtils.getPath(this,postImageUri), new File(this.getCacheDir(), "temp")));
            Uri uri = Uri.fromFile(file);
            storageReference.child("");
        }
    }*/
}
