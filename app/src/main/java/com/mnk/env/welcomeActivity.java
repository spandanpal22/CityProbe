package com.mnk.env;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class welcomeActivity extends Activity {
    LinearLayout l1,l2;
    Button btnsub;
    Animation uptodown,downtoup;
    static int SPLASH_TIME = 1000;

    static Handler handler;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welecome);

        ImageView imageView = (ImageView) findViewById(R.id.splash_ican);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.blick);
        imageView.startAnimation(animation);

        //ActionBar actionBar = getSupportActionBar();
        // actionBar.hide();
        Thread splash = new Thread() {
            public void run() {
                try {
                    //set sleep time
                    sleep(3 * 1000);
                    Intent i = new Intent(welcomeActivity.this, CardMenu.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {

                }
            }
        };
        splash.start();
    }
}
