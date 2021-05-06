package com.github.jitnegii.swc.adapters;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.activities.MainActivity;
import com.github.jitnegii.swc.models.Report;
import com.github.jitnegii.swc.utils.FirebaseUtils;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostRecyclerViewHolder> {

    private Activity activity;
    private List<Report> reports;

    public PostAdapter(Activity activity, List<Report> reports) {
        this.activity = activity;
        this.reports = reports;
    }

    static class PostRecyclerViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView text, username;
        ImageButton menu;
        LinearLayout locationLayout;

        PostRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            username = itemView.findViewById(R.id.name);
            menu = itemView.findViewById(R.id.menu);
            locationLayout = itemView.findViewById(R.id.locationLayout);

        }
    }

    @NonNull
    @Override
    public PostRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.post_layout, parent, false);

        return new PostRecyclerViewHolder(view);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(@NonNull PostRecyclerViewHolder holder, final int position) {

        Report report = reports.get(position);
        holder.username.setText(report.getUsername());
        holder.text.setText(report.getText());

        if (!report.getImage().equals("null")) {

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(activity).load(report.getImage()).centerCrop().into(holder.image);
                    holder.image.setVisibility(View.VISIBLE);
                }
            });
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) activity).toggleFullImage(report.getImage());
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(activity, view);
                popupMenu.inflate(R.menu.post_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete) {
                            deletePost(position);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(activity)
                        .setMessage("Open location")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) activity).getViewPager().setCurrentItem(1);

                                if (report.getLocation().contains(":"))
                                    ((MainActivity) activity).getMapFragment().geoLocate(report.getLocation(), true);
                                else
                                    ((MainActivity) activity).getMapFragment().geoLocate(report.getLocation(), false);
                            }
                        }).show();

            }
        });

    }

    private void deletePost(int position) {

        String id = reports.get(position).getTime();
        FirebaseUtils.getFirebaseDRef().child("wastes").child(id).setValue(null);
        reports.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }


    @Override
    public int getItemViewType(int position) {
        return (int) Long.parseLong(reports.get(position).getTime());
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(reports.get(position).getTime());
    }
}
