package com.pinkstain.stainedmall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splash extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        /*super.onCreate(savedInstanceState);
                   new Handler().postDelayed(new Runnable() {

*/

           /* @Override
            public void run() {
              //  Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
               onStart();
            }
        }, 3000);*/

        firebaseAuth = FirebaseAuth.getInstance();
        SystemClock.sleep(3000);
    }

        @Override
        protected void onStart() {
            super.onStart();

            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

            if (currentUser == null) {
                Intent registerIntent = new Intent(splash.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();


            } else {
                Intent mainIntent = new Intent(splash.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }

        //3000);

    }
