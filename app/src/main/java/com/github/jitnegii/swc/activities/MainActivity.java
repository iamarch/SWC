package com.github.jitnegii.swc.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.jitnegii.swc.Activity;
import com.github.jitnegii.swc.R;
import com.github.jitnegii.swc.fragments.MapFragment;
import com.github.jitnegii.swc.fragments.ProfileFragment;
import com.github.jitnegii.swc.fragments.UpdateFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends Activity {


    TabLayout tabLayout;
    ViewPager viewPager;

    FrameLayout fullImageView;
    ImageView fullImage;
    ImageButton closeFullView;

    private int[] tabIcons = {
            R.drawable.ic_post,
            R.drawable.ic_map,
            R.drawable.ic_profile,

    };

    UpdateFragment updateFragment;
    MapFragment mapFragment;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SWC");
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        fullImageView = findViewById(R.id.fullImageView);
        closeFullView = findViewById(R.id.closeFullView);
        fullImage = findViewById(R.id.fullImage);

        closeFullView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullImage(" ");
            }
        });

        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        updateFragment = new UpdateFragment();
        mapFragment = new MapFragment();
        profileFragment = new ProfileFragment();

        viewPagerAdapter.addFragment(updateFragment, "");
        viewPagerAdapter.addFragment(mapFragment, "");
        viewPagerAdapter.addFragment(profileFragment, "");


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(tabIcons[0]).getIcon().setAlpha(255);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]).getIcon().setAlpha(128);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]).getIcon().setAlpha(128);

        tabLayout.getTabAt(0).getIcon().setTint(Color.WHITE);
        tabLayout.getTabAt(1).getIcon().setTint(Color.WHITE);
        tabLayout.getTabAt(2).getIcon().setTint(Color.WHITE);

        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                ViewPagerAdapter viewPagerAdapter1 = viewPagerAdapter;
                Fragment currentFragment = viewPagerAdapter1.getItem(position);

                if (currentFragment instanceof UpdateFragment) {
                    tabLayout.getTabAt(0).getIcon().setAlpha(255);
                    tabLayout.getTabAt(1).getIcon().setAlpha(128);
                    tabLayout.getTabAt(2).getIcon().setAlpha(128);
                }

                if (currentFragment instanceof MapFragment) {
                    tabLayout.getTabAt(0).getIcon().setAlpha(128);
                    tabLayout.getTabAt(1).getIcon().setAlpha(255);
                    tabLayout.getTabAt(2).getIcon().setAlpha(128);

                    ((MapFragment) currentFragment).addBinMarker();
                }

                if (currentFragment instanceof ProfileFragment) {

                    tabLayout.getTabAt(0).getIcon().setAlpha(128);
                    tabLayout.getTabAt(1).getIcon().setAlpha(128);
                    tabLayout.getTabAt(2).getIcon().setAlpha(255);

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    public ViewPager getViewPager() {
        return viewPager;

    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    public UpdateFragment getUpdateFragment() {
        return updateFragment;
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);

            fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        public Fragment getFragmentAt(int position) {
            return fragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mapFragment.locPermissionGranted = false;

        switch (requestCode) {
            case MapFragment.LOC_PERMISSION_RQST_CODE: {
                if (grantResults.length > 0) {

                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mapFragment.locPermissionGranted = false;
                            return;
                        }
                        mapFragment.locPermissionGranted = true;

                        //Initializing map
                        mapFragment.initMap();
                    }

                }
            }

        }
    }

    public void toggleFullImage(String url) {

        if (url.replaceAll(" ", "").length() > 0 && fullImageView.getVisibility() == View.GONE) {
            Glide.with(this).load(url).into(fullImage);
            fullImageView.setVisibility(View.VISIBLE);
        } else {
            fullImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {

        if (fullImageView.getVisibility() == View.VISIBLE)
            fullImageView.setVisibility(View.GONE);
        else
            super.onBackPressed();
    }
}
