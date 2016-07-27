package odb234.meetpoll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button login;
    Button register;
    static EditText username;
    static EditText password;
    static LayoutInflater inflater;
    Firebase fb;
    Firebase.AuthResultHandler handler;
    private static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "Login Screen:";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(this);
        fb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "User signed in.");
                } else {
                    Log.d(TAG, "user signed out.");
                }
            }
        };

        login = (Button) findViewById(R.id.login_login);
        register = (Button) findViewById(R.id.login_register);
        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);

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
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivityForResult(intent, 10);
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
                if(!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Registration failed. Account for " + newUser +" may already exist.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
