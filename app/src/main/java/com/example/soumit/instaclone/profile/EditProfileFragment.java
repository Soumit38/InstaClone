package com.example.soumit.instaclone.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soumit.instaclone.R;
import com.example.soumit.instaclone.dialogs.ConfirmPasswordDialog;
import com.example.soumit.instaclone.models.User;
import com.example.soumit.instaclone.models.UserAccountSettings;
import com.example.soumit.instaclone.models.UserSettings;
import com.example.soumit.instaclone.share.ShareActivity;
import com.example.soumit.instaclone.utils.FirebaseMethods;
import com.example.soumit.instaclone.utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Soumit on 2/23/2018.
 */

public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDialog.OnConfirmPasswordListener{

    //custom interface method
    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: captured password : " + password);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Get Auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        //prompt user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            try {
                                Log.d(TAG, "onComplete: User re-authenticated...");

                                mAuth.fetchProvidersForEmail(mEmail.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                                if(task.isSuccessful()){
                                                    if(task.getResult().getProviders().size() == 1){
                                                        Log.d(TAG, "onComplete: the email is already in use!");
                                                        Toast.makeText(getActivity(), "This email is already in use!", Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        Log.d(TAG, "onComplete: That email is available...");
                                                        user.updateEmail(mEmail.getText().toString())
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Log.d(TAG, "onComplete: User email address updated!");
                                                                            Toast.makeText(getActivity(), "email updated!", Toast.LENGTH_SHORT).show();
                                                                            mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            } catch (NullPointerException e) {
                                Log.e(TAG, "onComplete: NullPointerException " + e.getMessage());
                            }

                        } else {
                            Log.d(TAG, "onComplete: re-authentication failed...");
                        }
                    }
                });

    }

    private static final String TAG = "EditProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

//    EditProfileFragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    //variables
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());

//        setProfileImage();
        setupFirebaseAuth();

        //backarrow for navigating to previous acitivity
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to profileActivity");
                getActivity().finish();
            }
        });

        ImageView checkMark = (ImageView) view.findViewById(R.id.saveChanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: attempting to save changes...");
                saveProfileSettings();
            }
        });


        return view;
    }


    private void saveProfileSettings(){
        Log.d(TAG, "saveProfileSettings: saving...");
        final String displayName = mDisplayName.getText().toString();
        final String userName = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
//        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());
        final String phoneNumber = mPhoneNumber.getText().toString();

                //case 1: user didn't change their username (easy)
                if(!mUserSettings.getUser().getUsername().equals(userName)){
                    checkIfUsernameExists(userName);
                }
                //case 2: if user made a change to their email
                if(!mUserSettings.getUser().getEmail().equals(email)){

                    //Step 1. Re-authenticate --> confirm password and email
                    ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                    dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
                    dialog.setTargetFragment(EditProfileFragment.this, 1);

                    //Step 2. check if email already registered --> fetchProvidersForEmail(String email)
                    //Step 3. change the email --> submit the new email to database and authentication

                }
                    /**
                     * changing rest of the settings that doesn't require uniqueness
                     */
                    if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
                        //update displayName
                        mFirebaseMethods.updateUserAccountSettings(displayName, null, null, null);
                    }

                    if(!mUserSettings.getSettings().getWebsite().equals(website)){
                        //update website
                        mFirebaseMethods.updateUserAccountSettings(null, website, null, null);
                    }

                    if(!mUserSettings.getSettings().getDescription().equals(description)){
                        //update description
                        mFirebaseMethods.updateUserAccountSettings(null, null, description, null);
                    }

                    if (!mUserSettings.getSettings().getProfile_photo().equals(phoneNumber)) {
                        //update description
                        mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
                    }

    }

    /**
     * checks if username already exists in the database
     * @param userName
     */
    private void checkIfUsernameExists(final String userName) {
        Log.d(TAG, "checkIfUsernameExists: checking if " +  userName + " already exists in database...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(userName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    //add the username
                    mFirebaseMethods.updateUsername(userName);
                    Toast.makeText(getActivity(), "saved usename", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: FOUND A MATCH : " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exists !", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //**********************************************Firebase-staff**************************************************

    /**
     * setup firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();

                if(user != null){
                    //user signed in
                    Log.d(TAG, "onAuthStateChanged: signed in : " + user.getUid());
                }else {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out : ");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve user info from database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * setProfileWidgets
     * @param userSettings
     */
    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving from firebase database : " + userSettings.toString());
//        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving from firebase database : " + userSettings.getSettings().getUsername());
//        User user = new User();
        mUserSettings = userSettings;

        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: changing profile photo ");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
