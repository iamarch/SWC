package com.github.jitnegii.swc.activities;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.utils.AppUtils;
import com.github.jitnegii.swc.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class StartActivity extends Activity {

    EditText email, password;
    TextView register;
    Button login;

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseUtils.getFirebaseUser() != null) {

            AppUtils.fetchUserDetails(getApplicationContext(),true);
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        login = findViewById(R.id.loginBtn);

        login.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(StartActivity.this, "Both fields are required to login", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUtils.getFirebaseAuthentication().signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                        AppUtils.fetchUserDetails(getApplicationContext(),true);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(StartActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(StartActivity.this, RegisterActivity.class));

            }
        });
    }
}
