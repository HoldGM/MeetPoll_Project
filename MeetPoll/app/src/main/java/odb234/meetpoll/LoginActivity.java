package odb234.meetpoll;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button login;
    Button register;
    CheckBox remember_me;
    static EditText username;
    static EditText password;
    static LayoutInflater inflater;
    Firebase fb;
    Firebase.AuthResultHandler handler;
    public static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private static final String TAG = "Login Screen:";
    static boolean registerFlag;
    static Toast t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);
        fb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");

        t = Toast.makeText(this, "", Toast.LENGTH_LONG);
        registerFlag = false;
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    editor.putString("Uid", user.getUid());
                    editor.apply();
                    if (registerFlag) {
                        registerFlag = false;
                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }else {
                        (fb.child(user.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                editor.putString("phone", dataSnapshot.child("phone").getValue().toString());
                                editor.putString("name", dataSnapshot.child("username").getValue().toString());
                                editor.apply();
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    Log.d(TAG, "User signed in.");
                } else {
                    Log.d(TAG, "user signed out.");
                }
            }
        };
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sp.edit();
        login = (Button) findViewById(R.id.login_login);
        register = (Button) findViewById(R.id.login_register);
        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);

        remember_me = (CheckBox) findViewById(R.id.remember_check);
        remember_me.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b) {
                    editor.remove("username");
                    editor.apply();
                }
            }
        });
        String usernameStr = sp.getString("username","");
        if(!usernameStr.equals("")){
            username.setText(usernameStr);
            remember_me.setChecked(true);
        }

        inflater = getLayoutInflater();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void login(View v){
        if(remember_me.isChecked()){
            editor.putString("username",username.getText().toString());
            editor.apply();
        }
        if(username.getText().equals("") || password.getText().equals("")){
            Toast.makeText(this, "Username or Password missing.", Toast.LENGTH_LONG).show();
        }else{
            mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    Log.d(TAG, "Login OnComplete: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    public void register(View v){
        dialogFragment();

//        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//        startActivityForResult(intent, 10);
    }

    public void resetPassword(View v) {
        resetPasswordFragment reset = new resetPasswordFragment();
        reset.show(getFragmentManager(),"resetPassword");
    }
    public static class resetPasswordFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText userInput = new EditText(getActivity());
            userInput.setHint("Enter email");
            builder.setView(userInput);
            builder.setMessage("Send reset email to ")
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String email = userInput.getText().toString();
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
//                                                Toast.makeText(getView().getContext(), "Email sent to " + email + ".", Toast.LENGTH_LONG).show();
                                                t.setText("Email sent to " + email + ".");
                                                t.show();
                                            } else {
//                                                Toast.makeText(getView().getContext(), "Email failed to send to " + email + ".", Toast.LENGTH_LONG).show();
                                                t.setText("Email failed to send to " + email + ".");
                                                t.show();
                                            }
                                        }
                                    });
                            dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }

    public void dialogFragment(){
        final AlertDialog.Builder registerDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = (View) inflater.inflate(R.layout.register_dialog, null);

        registerDialog.setView(dialogView);
        registerDialog.setTitle("Register");
        registerDialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (((EditText) dialogView.findViewById(R.id.new_password)).getText().toString().equals(((EditText) dialogView.findViewById(R.id.confirm_password)).getText().toString()) &&
                        ((EditText) dialogView.findViewById(R.id.new_password)).getText().toString().length() >= 6) {
                    registerComplete(((EditText) dialogView.findViewById(R.id.new_user)).getText().toString(), ((EditText) dialogView.findViewById(R.id.new_password)).getText().toString());
                    editor.remove("username");
                    editor.apply();
                    registerFlag = true;
                    dialogInterface.dismiss();
                } else if (!((EditText) dialogView.findViewById(R.id.new_password)).getText().toString().equals(((EditText) dialogView.findViewById(R.id.confirm_password)).getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Password entered does not match", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Password too short. Password must be at least 6 characters", Toast.LENGTH_LONG).show();
                }
            }
    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        registerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case 10:
                String newUser = data.getStringExtra("username");
                String newPass = data.getStringExtra("password");
                registerComplete(newUser, newPass);
        }
    }

    public  void registerComplete(String user, String pass){
        final String newUser = user;
        Toast.makeText(this, "Attempting to login.", Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(newUser, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                Log.d(TAG, "create user OnComplete: " + task.isSuccessful());
//                Log.d(TAG, mAuth.getCurrentUser().getUid().toString());
                if(!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Registration failed. Account for " + newUser +" may already exist.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
