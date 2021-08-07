package com.droid7technolabs.planetpredictor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.droid7technolabs.planetpredictor.UserDetails.BirthProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class Splash extends AppCompatActivity {

    TextView appname;
    ImageView logo;

    Animation zoomIn,top,bottom;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    SharedPreferences onBoardingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Objects.requireNonNull(getSupportActionBar()).hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        appname = findViewById(R.id.appname);
        logo = findViewById(R.id.appLogo);

        //for logo animation
        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        top = AnimationUtils.loadAnimation(this,R.anim.top);
        bottom = AnimationUtils.loadAnimation(this,R.anim.bottom_anim);
        logo.setAnimation(zoomIn);
        appname.setAnimation(bottom);

        //handler for timing
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onBoardingScreen = getSharedPreferences("onBoradingScreen", MODE_PRIVATE);

                Intent i = new Intent(getApplicationContext(), OnBoarding.class);
                startActivity(i);
                finish();

            }
        }, 3000);

    }
}
