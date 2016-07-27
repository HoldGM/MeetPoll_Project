package odb234.meetpoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Thread timerThread = new  Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
//                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//                    String uid = sp.getString("uid", null);
//                    if(uid == null){
                        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(intent);
//                    }else{
//                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
//                        startActivity(intent);
//                    }
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
}
