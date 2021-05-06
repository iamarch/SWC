package com.github.jitnegii.swc.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.activities.StartActivity;
import com.github.jitnegii.swc.models.User;
import com.github.jitnegii.swc.utils.AppUtils;
import com.github.jitnegii.swc.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {


    User currentUser;
    Button logOut;
    Activity activity;

    TextView username,emailText,locText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        username = v.findViewById(R.id.username);
        emailText = v.findViewById(R.id.emailText);
        locText = v.findViewById(R.id.locText);
        logOut = v.findViewById(R.id.logOut);

        activity = (Activity) getActivity();

        currentUser = AppUtils.getUserFromDb(activity.getApplicationContext());

        if (currentUser != null) {
            username.setText(currentUser.getUsername());
            emailText.setText(currentUser.getEmail());
            locText.setText(currentUser.getState().trim()+", "+currentUser.getCity().trim());
        }

        logOut.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                FirebaseUtils.reset();
                AppUtils.resetUser();
                startActivity(new Intent(getActivity(), StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                getActivity().finish();
            }
        });
        return v;
    }
}
