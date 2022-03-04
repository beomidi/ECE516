package org.anothermonitor;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pl.droidsonroids.gif.GifImageView;

public class ActivityStart extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        TextView textView = (TextView) findViewById(R.id.startTextView);
        Button skipButton = (Button) findViewById(R.id.skipButton);
        textView.setVisibility(View.INVISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        gifImageView.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, ActivityMain.class);

        getSupportActionBar().hide();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                gifImageView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.VISIBLE);
                skipButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(intent);
                    }
                });
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    public void run() {
//                        startActivity(intent);
//                    }
//
//                }, 3000);
            }
        }, 3000);   //5 seconds






    }
}
