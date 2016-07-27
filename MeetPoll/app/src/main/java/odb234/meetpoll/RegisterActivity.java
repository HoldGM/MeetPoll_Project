package odb234.meetpoll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    EditText password2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = (EditText) findViewById(R.id.register_username);
        password = (EditText) findViewById(R.id.register_password1);
        password2 = (EditText) findViewById(R.id.register_password2);
    }

    public void register(View view){
        if(password.getText().toString().equals(password2.getText().toString())) {
            Intent intent = new Intent();
            intent.putExtra("username", username.getText().toString());
            intent.putExtra("password", password.getText().toString());
            setResult(10, intent);
            finish();
        }else{
            Toast.makeText(this, "Password does not match,", Toast.LENGTH_LONG).show();
            password.setText("");
            password2.setText("");
        }
    }

    public void cancel(View view){
        finish();
    }
}
