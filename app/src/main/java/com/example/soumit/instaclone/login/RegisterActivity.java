package com.example.soumit.instaclone.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soumit.instaclone.R;
import com.example.soumit.instaclone.models.User;
import com.example.soumit.instaclone.utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Soumit on 2/23/2018.
 */

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Context mContext = RegisterActivity.this;
    private String email, username, password;
    private EditText mEmail, mUsername, mPassword;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private TextView loadiingPleaseWait;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: started");

        firebaseMethods = new FirebaseMethods(mContext);

        initWidgets();
        init();
        setupFirebaseAuth();



    }

    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                Log.d(TAG, "onClick: " + email +  " " + username + " " + password);

//                firebaseMethods.registerNewEmail(email, password, username);

                if(checkInputs(email, username, password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadiingPleaseWait.setVisibility(View.VISIBLE);

                    firebaseMethods.registerNewEmail(email, password, username);
                }
            }
        });
    }

    /**
     *  Initializing activity widgets
     */
    private void initWidgets(){
        Log.d(TAG, "initWidgets: initializing widgets");
        mEmail = (EditText) findViewById(R.id.input_email);
        mUsername = (EditText) findViewById(R.id.input_username);
        mPassword = (EditText) findViewById(R.id.input_password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnRegister = (Button) findViewById(R.id.btn_register);
        loadiingPleaseWait = (TextView) findViewById(R.id.loadingPleaseWait);
        mProgressBar.setVisibility(View.GONE);
        loadiingPleaseWait.setVisibility(View.GONE);
    }

    /**
     * checking if any input is null
     * @param email
     * @param password
     * @param username
     * @return boolean
     */
    private boolean checkInputs(String email, String username, String password){
        Log.d(TAG, "checkInputs: checking inputs");
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(mContext, "All fields must be filled out!", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    /**
     *  check String is null or not
     * @param string
     * @return
     */
    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null");

        if(string.equals("")){
            return true;
        }else {
            return false;
        }
    }

    /*
      -------------------------------------firebase---------------------------------------
     */

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

                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: FOUND A MATCH : " + singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: Username already exists : appending random String : " + append);
                    }
                }

                String mUsername = "";
                mUsername = username + append;
                //adding new user to firebase
                firebaseMethods.addNewUser(email, mUsername, "", "", "");
                Toast.makeText(mContext, "Signup successful. Sending verification email!", Toast.LENGTH_SHORT).show();

                mAuth.signOut();
                //--------------------------------------------------------------------------------

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * check if user is logged in
     *
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in!");
        if (user == null) {
            Log.d(TAG, "checkCurrentUser: user is null");
//            Intent intent = new Intent(mContext, LoginActivity.class);
//            startActivity(intent);
        }
    }

    /**
     * setup firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    //user signed in
                    Log.d(TAG, "onAuthStateChanged: signed in : " + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    finish();

                } else {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out : ");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        //  check if user is logged in
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
