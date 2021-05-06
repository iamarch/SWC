package com.github.jitnegii.swc.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.database.AccessDatabase;
import com.github.jitnegii.swc.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AppUtils {

    private static AccessDatabase database;
    private static User currentUser;
    private static ClipboardManager clipboardManager;;

    public static AccessDatabase getDb(Context context) {

        if (database == null)
            database = new AccessDatabase(context);

        return database;

    }

    public static User fetchUserDetails(Context context, boolean checkDb) {

        if (currentUser == null)
            currentUser = new User();
        else
            return currentUser;

        if (checkDb) {
            User user = AppUtils.getDb(context).getUserDetail(AccessDatabase.USER_TABLE, FirebaseUtils.getFirebaseUser().getUid());

            if (user != null) {

                currentUser.setId(user.getId());
                currentUser.setUsername(user.getUsername());
                currentUser.setEmail(user.getEmail());
                currentUser.setState(user.getState());
                currentUser.setCity(user.getCity());
                currentUser.setType(user.getType());

                return currentUser;
            }
        }

        FirebaseUtils.getFirebaseDRef()
                .child("users")
                .child(FirebaseUtils.getFirebaseUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    currentUser.setId(user.getId());
                    currentUser.setUsername(user.getUsername());
                    currentUser.setEmail(user.getEmail());
                    currentUser.setState(user.getState());
                    currentUser.setCity(user.getCity());
                    currentUser.setType(user.getType());

                    AppUtils.getDb(context).setUserDetails(currentUser, AccessDatabase.USER_TABLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return currentUser;

    }


    public static User getUserFromDb(Context context) {

      //  getDb(context).getUserCount();
        return fetchUserDetails(context, true);

    }

    public static void resetUser() {
        currentUser = null;
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }


    public static void copyText(String text,Context context) {

        if (!text.isEmpty()) {

            ClipData clipData = ClipData.newPlainText("key", text);
            getClipboardManager(context).setPrimaryClip(clipData);
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No text to be copied", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getCopiedText(Context context) {
        ClipData clipData = getClipboardManager(context).getPrimaryClip();
        assert clipData != null;
        ClipData.Item item = clipData.getItemAt(0);
        return  item.getText().toString();
    }

    private static ClipboardManager getClipboardManager(Context context){
        if(clipboardManager == null)
            clipboardManager = (ClipboardManager) context.getApplicationContext() .getSystemService(Context.CLIPBOARD_SERVICE);
        return clipboardManager;
    }

    public static <T> void reverse(List<T> object){

        int len = object.size();
        for(int i=0;i<len/2;i++){
            T temp = object.get(i);
            object.set(i,object.get(len-1-i));
            object.set(len-1-i,temp);
        }
    }
}
