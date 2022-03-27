package com.pinkstain.stainedmall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton addPostBtn;
    private Toolbar toolbar;
    private BottomNavigationView mainNavigationView;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private TextView header;
    private ImageView comment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();
        mainNavigationView = findViewById(R.id.main_navigation);
        header = findViewById(R.id.textView_logo);
        comment = findViewById(R.id.imageView_comment);


        replaceFragment(homeFragment);

        mainNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.home_menu :

                        replaceFragment(homeFragment);
                        header.setText("STAINED BLOG");
                        return true;

                    case R.id.notifications_menu  :

                        replaceFragment(notificationFragment);
                        header.setText("Activity");

                        return true;

                    case R.id.me_menu :

                        replaceFragment(accountFragment);
                        header.setText("Profile");

                        return true;

                        default:
                            return false;


                }



            }
        });





       // getSupportActionBar().setTitle("STAINED BLOG");
        addPostBtn = findViewById(R.id.add_post_btn);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mainIntent = new Intent(MainActivity.this, newPostActivity.class);
                startActivity(mainIntent);
                finish();

            }
        });


    }
    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout_main, fragment);
        fragmentTransaction.commit();

    }
}
