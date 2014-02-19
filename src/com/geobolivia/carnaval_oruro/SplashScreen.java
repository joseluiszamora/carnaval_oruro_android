package com.geobolivia.carnaval_oruro;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SplashScreen extends Activity {
	    
		public void onAttachedToWindow() {
	        super.onAttachedToWindow();
	        Window window = getWindow();
	        window.setFormat(PixelFormat.RGBA_8888);
	    }
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_splash);
			
			// SET RANDOM BACKGROUND
			LinearLayout back =(LinearLayout) findViewById(R.id.lin_lay);
			
			Random r = new Random();
			int rand = r.nextInt(5-0) + 0;
			
			switch (rand) {
			case 1:
				back.setBackgroundResource(R.drawable.c6);
				break;
			case 2:
				back.setBackgroundResource(R.drawable.c2);
				break;
			case 3:
				back.setBackgroundResource(R.drawable.c3);
				break;
			case 4:
				back.setBackgroundResource(R.drawable.c4);
				break;
			default:
				break;
			}
			
			
			
			StartAnimations();
			
			// Splash screen timer
		    int SPLASH_TIME_OUT = 3000;
		    
	        new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);
                    finish();
	            }
	        }, SPLASH_TIME_OUT);
		}
		
		private void StartAnimations() {
	        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
	        anim.reset();
	        LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
	        l.clearAnimation();
	        l.startAnimation(anim);
	 
	        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
	        anim.reset();
	        ImageView iv = (ImageView) findViewById(R.id.imageView1);
	        iv.clearAnimation();
	        iv.startAnimation(anim);
	    }
}
