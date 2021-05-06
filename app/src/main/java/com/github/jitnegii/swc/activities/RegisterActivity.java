package com.github.jitnegii.swc.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.fragments.RegisterFragment;
import com.github.jitnegii.swc.utils.AppUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends Activity {

    Button citizenBtn, workerBtn;
    String type;
    RegisterFragment registerFragment;
    FragmentManager fragmentManager;
    FrameLayout frameLayout;

    FirebaseAuth auth;
    DatabaseReference dataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        citizenBtn = findViewById(R.id.citizenBtn);
        workerBtn = findViewById(R.id.workerBtn);
        frameLayout = findViewById(R.id.fragmentContainer);


        fragmentManager = getSupportFragmentManager();
        auth = FirebaseAuth.getInstance();

        citizenBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "citizen";
                toggleRegisterFragment();
            }
        });

        workerBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "worker";
                toggleRegisterFragment();
            }
        });
    }

    private void toggleRegisterFragment(){

        if(frameLayout.getVisibility() == View.GONE) {
            registerFragment = new RegisterFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, registerFragment).commit();
            fragmentManager.beginTransaction().show(registerFragment);
            frameLayout.setVisibility(View.VISIBLE);
        } else{
            registerFragment = null;
            frameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {

        if (registerFragment != null && frameLayout.getVisibility() == View.VISIBLE){
            toggleRegisterFragment();
        } else{
            super.onBackPressed();
        }

    }

    public void registerUser(final String email, String password, final String state, final String city){

        if(auth == null)
            auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userID = firebaseUser.getUid();
                            dataReference = FirebaseDatabase.getInstance().getReference("users").child(userID);
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("id",userID);
                            hashMap.put("type",type);
                            hashMap.put("email",email);

                            String username = email.split("@")[0];
                            username = username.substring(0,1).toUpperCase() + username.substring(1);
                            hashMap.put("username",username);
                            hashMap.put("state",state);
                            hashMap.put("city",city);

                            dataReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        AppUtils.fetchUserDetails(getApplicationContext(),false);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });

                        } else{
                            Toast.makeText(RegisterActivity.this, "Unable to register try again!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
