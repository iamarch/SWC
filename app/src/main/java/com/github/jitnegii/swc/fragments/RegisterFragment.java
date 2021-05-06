package com.github.jitnegii.swc.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.activities.RegisterActivity;

public class RegisterFragment extends Fragment {

    EditText email,password,state,city;
    Button signUp;
    Context activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_register, container, false);

        email = v.findViewById(R.id.email);
        password = v.findViewById(R.id.password);
        state = v.findViewById(R.id.state);
        city = v.findViewById(R.id.city);
        signUp = v.findViewById(R.id.signUp);

        activity = getActivity();

        signUp.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String txt_state = state.getText().toString();
                String txt_city = city.getText().toString();

                if(TextUtils.isEmpty(txt_email )
                        || TextUtils.isEmpty(txt_password )
                        || TextUtils.isEmpty(txt_state )
                        || TextUtils.isEmpty(txt_city )) {
                    Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                } else if(!txt_email.contains("@")){
                    Toast.makeText(getActivity(), "Invalid email address", Toast.LENGTH_SHORT).show();
                }else {

                    ((RegisterActivity) activity).registerUser(txt_email,txt_password,txt_state,txt_city);
                }

            }
        });
        return v;
    }
}
