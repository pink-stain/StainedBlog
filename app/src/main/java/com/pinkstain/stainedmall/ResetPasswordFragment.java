package com.pinkstain.stainedmall;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPasswordFragment extends Fragment {


    public ResetPasswordFragment() {
        // Required empty public constructor
    }

        private EditText registeredEmail;
        private Button reset;
        private TextView back;
        private ProgressBar progressBar;

        private FrameLayout parentFrameLayout;
        private FirebaseAuth firebaseAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        registeredEmail = view.findViewById(R.id.reset_email);
        reset = view.findViewById(R.id.button_reset);
        back = view.findViewById(R.id.textView_back);
        progressBar = view.findViewById(R.id.progressBar_sent);
        parentFrameLayout = getActivity().findViewById(R.id.register_frameLayout);
        firebaseAuth = FirebaseAuth.getInstance();

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registeredEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                checkInputs();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){



                if(!TextUtils.isEmpty(registeredEmail.getText())) {
                    reset.setEnabled(false);
                    reset.setTextColor(Color.rgb(255,255,255));
                    progressBar.setVisibility(View.VISIBLE);

                    firebaseAuth.sendPasswordResetEmail(registeredEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {


                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Email sent successfully", Toast.LENGTH_LONG).show();


                                    } else {
                                   /* progressBar.setVisibility(View.INVISIBLE);
                                    reset.setEnabled(false);
                                    reset.setTextColor(Color.rgb(255,255,255));*/

                                        String error = task.getException().getMessage();
                                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    reset.setEnabled(true);
                                    reset.setTextColor(Color.rgb(255, 255, 255));
                                }

                            });
                }else {

                        //  String error = task.getException().getMessage();
                        Toast.makeText(getActivity(), "Please input E-mail Address", Toast.LENGTH_SHORT).show();

                }
            }
        });

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setFragment(new SignInFragment());
            }
        });
    }

    private void checkInputs() {
        if(!TextUtils.isEmpty(registeredEmail.getText())){
            reset.setEnabled(true);
            reset.setTextColor(Color.rgb(255,255,255));
        }else{
            reset.setEnabled(false);

        }
    }
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction =getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parentFrameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}
