package com.meri_sg.places_finder;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class LoadingPage extends AppCompatActivity {

    Thread welcomeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_page);

        welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(6000) ; //Delay of 6 seconds
                } catch (Exception e) {

                } finally {

                    Intent intent = new Intent(LoadingPage.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };

        if (ActivityCompat.checkSelfPermission(LoadingPage.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
               &&ActivityCompat.checkSelfPermission(LoadingPage.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //6 seconds of animation to amuse while waiting to map and list load
            if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
                ImageView glass = (ImageView)findViewById(R.id.glassIV);
                ObjectAnimator left = ObjectAnimator.ofFloat(glass, "TranslationX", -300, -600);
                left.setDuration(2000);
                ObjectAnimator down = ObjectAnimator.ofFloat(glass, "TranslationY", 0, 280);
                down.setDuration(2000);
                ObjectAnimator right = ObjectAnimator.ofFloat(glass, "TranslationX", -600, -100);
                right.setDuration(2000);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(down).before(right).after(left);
                animatorSet.start();

            }else {
                ImageView glass = (ImageView)findViewById(R.id.glassIV);
                ObjectAnimator left = ObjectAnimator.ofFloat(glass, "TranslationX", -10, -260);
                left.setDuration(1500);
                ObjectAnimator down = ObjectAnimator.ofFloat(glass, "TranslationY", 0, 610);
                down.setDuration(3000);
                ObjectAnimator right = ObjectAnimator.ofFloat(glass, "TranslationX", -260, 100);
                right.setDuration(1500);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(down).before(right).after(left);
                animatorSet.start();

            } //end of animation

            welcomeThread.start();



        }



        //permission check for ANDROID 6
        if (ActivityCompat.checkSelfPermission(LoadingPage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(LoadingPage.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(LoadingPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION} ,1);

        }



    }//end of onCreate


    //permission result check
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //6 seconds of animation to amuse while waiting to map and list load
                    if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
                        ImageView glass = (ImageView)findViewById(R.id.glassIV);
                        ObjectAnimator left = ObjectAnimator.ofFloat(glass, "TranslationX", -300, -600);
                        left.setDuration(2000);
                        ObjectAnimator down = ObjectAnimator.ofFloat(glass, "TranslationY", 0, 280);
                        down.setDuration(2000);
                        ObjectAnimator right = ObjectAnimator.ofFloat(glass, "TranslationX", -600, -100);
                        right.setDuration(2000);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.play(down).before(right).after(left);
                        animatorSet.start();

                    }else {
                        ImageView glass = (ImageView)findViewById(R.id.glassIV);
                        ObjectAnimator left = ObjectAnimator.ofFloat(glass, "TranslationX", -10, -260);
                        left.setDuration(1500);
                        ObjectAnimator down = ObjectAnimator.ofFloat(glass, "TranslationY", 0, 610);
                        down.setDuration(3000);
                        ObjectAnimator right = ObjectAnimator.ofFloat(glass, "TranslationX", -260, 100);
                        right.setDuration(1500);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.play(down).before(right).after(left);
                        animatorSet.start();

                    } //end of animation

                    welcomeThread.start();

                    //permission was granted!
                    
                } else {

                    //if permission denied, too bad!
                    
                }
                return;
            }

          
          
        }
    }



}//end of LoadingPage
