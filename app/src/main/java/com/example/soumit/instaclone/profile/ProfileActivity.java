package com.example.soumit.instaclone.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.soumit.instaclone.R;
import com.example.soumit.instaclone.utils.ViewCommentsFragment;
import com.example.soumit.instaclone.utils.ViewPostFragment;
import com.example.soumit.instaclone.models.Photo;
import com.example.soumit.instaclone.utils.ViewProfileFragment;

/**
 * Created by Soumit on 2/20/2018.
 */

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener{

    private static final String TAG = "ProfileActivity";

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview : " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();

    }

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;
    private Context context = ProfileActivity.this;

    private ProgressBar progressBar;
    private ImageView profilePhoto;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started");

        init();

    }

    private void init() {
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: searching for user object attached as intent extra...");

            if(intent.hasExtra(getString(R.string.intent_user))) {
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_user),
                        intent.getParcelableExtra(getString(R.string.intent_user)));
                fragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment));
                transaction.commit();

            }else {
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }

        } else {

            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }


//    private void tempGridSetup(){
//        ArrayList<String> imgUrls = new ArrayList<>();
//        imgUrls.add("http://static.flickr.com/99/300889926_1e3604050b.jpg");
//        imgUrls.add("http://static.flickr.com/2358/2040260677_99a28c3b81.jpg");
//        imgUrls.add("http://www.housekeepingchannel.com/objimages/422LM950_cat_Hires.jpg");
//        imgUrls.add("http://gigazine.jp/img/2008/11/21/cat_box_fight/Snap4.jpg");
//        imgUrls.add("http://static.flickr.com/2153/2503582343_80e8dc0f66.jpg");
//        imgUrls.add("http://static.flickr.com/2216/2479038545_40976258ae.jpg");
//        imgUrls.add("http://static.flickr.com/7/6200482_18f2bce9bc.jpg");
//        imgUrls.add("http://static.flickr.com/2161/2170036739_a2babe03ca.jpg");
//        imgUrls.add("http://static.flickr.com/3021/3014350632_8798a613b3.jpg");
//        imgUrls.add("http://static.flickr.com/3021/3014350632_8798a613b3.jpg");
//        imgUrls.add("http://static.flickr.com/3021/3014350632_8798a613b3.jpg");
//        imgUrls.add("http://static.flickr.com/3021/3014350632_8798a613b3.jpg");
//
//        setupImageGrid(imgUrls);
//    }
//
//
//
//    private void setupImageGrid(ArrayList<String> imgUrls){
//        GridView gridView = (GridView) findViewById(R.id.gridView);
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter adapter = new GridImageAdapter(context, R.layout.layout_grid_imageview, "", imgUrls);
//        gridView.setAdapter(adapter);
//    }
//
//
//    private void setProfileImage(){
//        Log.d(TAG, "setProfileImage: setting profile photo");
//        String imgUrl = "http://static.flickr.com/3146/2777742625_e307b3b9fe.jpg";
//        UniversalImageLoader.setImage(imgUrl, profilePhoto, progressBar, "");
//    }
//
//    private void setupActivityWidgets(){
//        progressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
//        progressBar.setVisibility(View.GONE);
//        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
//    }
//
//    private void setupToolbar(){
//        Log.d(TAG, "setupToolbar: setting up");
//        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
//        profileMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: Navigating to account settings");
////                Toast.makeText(context, "profile button clicked!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context, AccountSettingsActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    /**
//     *  BottomNavigationView setup
//     */
//    private void setUpBottomNavigationView(){
//        Log.d(TAG, "setUpBottomNavigationView: setting up BottomNavigationView");
//        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavigationViewHelper.enableNavigation(ProfileActivity.this, bottomNavigationViewEx);
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.profile_menu, menu);
//        return true;
//    }

}













