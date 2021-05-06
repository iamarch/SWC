package com.github.jitnegii.swc.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.adapters.PostAdapter;
import com.github.jitnegii.swc.models.Report;
import com.github.jitnegii.swc.models.User;
import com.github.jitnegii.swc.utils.AppUtils;
import com.github.jitnegii.swc.utils.FirebaseUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class UpdateFragment extends Fragment {

    private static final String TAG = "UpdateFragment";
    private ImageButton addImage, sendBtn, addLocBtn;
    private EditText text;
    private TextView emptyReports;
    private RecyclerView recyclerView;
    private RelativeLayout reportLayout;


    private final static int IMG_REQUEST = 1;

    private FirebaseUser fUser;
    private DatabaseReference dRef;
    private StorageReference sRef;
    private User currentUser;

    private List<Report> reports;
    private PostAdapter postAdapter;

    private Activity activity;

    private String txt;

    private Uri imgUri;
    private String locationText = "";
    private boolean locationAdded = false;
    private boolean imageAttached = false;
    private StorageTask uploadTask;
    private ProgressDialog pd;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_update, container, false);

        activity = (Activity) getActivity();

        currentUser = AppUtils.getUserFromDb(activity.getApplicationContext());

        addImage = view.findViewById(R.id.addImage);
        sendBtn = view.findViewById(R.id.sendBtn);
        addLocBtn = view.findViewById(R.id.addLoc);
        text = view.findViewById(R.id.text);
        emptyReports = view.findViewById(R.id.emptyReports);
        reportLayout = view.findViewById(R.id.reportLayout);


        if (currentUser.getType().equals("worker"))
            reportLayout.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        fUser = FirebaseUtils.getFirebaseUser();
        sRef = FirebaseStorage.getInstance().getReference("uploads");

        reports = new ArrayList<>();

        getReports();

        init();

        return view;
    }


    private void init() {

        addImage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!imageAttached) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Open file explorer")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    openImage();
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(activity)
                            .setMessage("Remove image")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    imgUri = null;
                                    imageAttached = false;
                                    addImage.setColorFilter(ContextCompat.getColor(activity, R.color.White));
                                }
                            }).show();

                }

            }
        });

        addLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!locationAdded) {
                    String text = AppUtils.getCopiedText(activity);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setMessage("Paste copied location.\n\nTo copy location :\nMap > Select marker > Copy")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Paste", new DialogInterface.OnClickListener() {
                                @SuppressLint("ResourceAsColor")
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    addLocBtn.setColorFilter(ContextCompat.getColor(activity, R.color.Red));

                                    locationAdded = true;
                                }
                            });

                    AlertDialog dialog = builder.create();


                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {

                            if (!text.contains("latlong")) {
                                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            } else {
                                locationText = text;
                            }
                        }
                    });

                    dialog.show();


                } else {

                    new AlertDialog.Builder(activity)
                            .setMessage("Remove location")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    locationText = "";
                                    locationAdded = false;
                                    addLocBtn.setColorFilter(ContextCompat.getColor(activity, R.color.White));
                                }
                            }).show();

                }
            }
        });

        sendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppUtils.hideSoftKeyboard(activity, view);

                if (pd == null)
                    pd = new ProgressDialog(getContext());

                pd.setMessage("Sending");
                pd.show();

                txt = text.getText().toString();

                if (!TextUtils.isEmpty(txt)) {
                    sendReport();
                } else {
                    pd.dismiss();
                }
            }
        });



    }

    //https://www.youtube.com/watch?v=xLPGtQoRUbk

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeType = MimeTypeMap.getSingleton();
        return mimeType.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                imageAttached = true;
                addImage.setColorFilter(ContextCompat.getColor(activity, R.color.Red));
            }
        }
    }


    private void sendReport() {

        HashMap<String, Object> hashMap = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        dRef = FirebaseDatabase.getInstance().getReference("wastes").child(time);

        if (imgUri != null) {
            final StorageReference fileReference = sRef.child(System.currentTimeMillis() + "." + getFileExtension(imgUri));

            uploadTask = fileReference.putFile(imgUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        hashMap.put("image", mUri);
                        updateOther(hashMap, time);

                    } else {

                        hashMap.put("image", "null");
                        updateOther(hashMap, time);

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hashMap.put("image", "null");
                    updateOther(hashMap, time);
                    Toast.makeText(activity, "Error while uploading image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {

            hashMap.put("image", "null");
            updateOther(hashMap, time);

        }


    }

    private void updateOther(HashMap<String, Object> hashMap, String time) {


        if (locationText.contains("latlong")) {
            String[] latlng = locationText.replaceAll(" ", "").split(":");
            if (latlng.length == 3) {
                hashMap.put("location", latlng[1] + ":" + latlng[2]);
            } else {
                hashMap.put("location", currentUser.getState() + "," + currentUser.getCity());
            }
        } else {
            hashMap.put("location", currentUser.getState() + "," + currentUser.getCity());
        }

        hashMap.put("username", currentUser.getUsername());
        hashMap.put("id", currentUser.getId());
        hashMap.put("text", txt);
        hashMap.put("time", time);

        dRef.setValue(hashMap).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Error while uploading report", Toast.LENGTH_SHORT).show();
            }
        });


        text.setText("");

        imgUri = null;
        imageAttached = false;
        addImage.setColorFilter(ContextCompat.getColor(activity, R.color.White));

        locationText = "";
        locationAdded = false;
        addLocBtn.setColorFilter(ContextCompat.getColor(activity, R.color.White));

        pd.dismiss();
    }

    private void getReports() {

        dRef = FirebaseUtils.getFirebaseDRef().child("wastes");

        if (currentUser.getType().equals("citizen")) {
            dRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    reports.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Report report = dataSnapshot.getValue(Report.class);

                        if (report != null)

                            if (report.getId().equals(currentUser.getId()))
                                reports.add(report);

                    }

                    Log.d(TAG, String.valueOf(reports.size()));

                    if (reports.size() == 0)
                        emptyReports.setVisibility(View.VISIBLE);
                    else
                        emptyReports.setVisibility(View.INVISIBLE);

                    AppUtils.reverse(reports);
                    postAdapter = new PostAdapter(activity, reports);
                    recyclerView.setAdapter(postAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            dRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    reports.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Report report = dataSnapshot.getValue(Report.class);

                        if (report != null)
                            reports.add(report);

                    }

                    Log.d(TAG, String.valueOf(reports.size()));

                    if (reports.size() == 0)
                        emptyReports.setVisibility(View.VISIBLE);
                    else
                        emptyReports.setVisibility(View.INVISIBLE);

                    AppUtils.reverse(reports);
                    postAdapter = new PostAdapter(activity, reports);
                    recyclerView.setAdapter(postAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


}

