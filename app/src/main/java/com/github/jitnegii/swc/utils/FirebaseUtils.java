package com.github.jitnegii.swc.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    private static FirebaseUser fUser;
    private static DatabaseReference dRef;
    private static FirebaseAuth auth;

    public static FirebaseUser getFirebaseUser() {

        if (fUser == null)
            fUser = FirebaseAuth.getInstance().getCurrentUser();

        return fUser;
    }

    public static DatabaseReference getFirebaseDRef() {

        if (dRef == null)
            dRef = FirebaseDatabase.getInstance().getReference();

        return dRef;
    }

    public static FirebaseAuth getFirebaseAuthentication() {

        if (auth == null)
            auth = FirebaseAuth.getInstance();

        return auth;
    }

    public static void reset() {

        fUser = null;
        dRef = null;
        auth = null;
    }
}
