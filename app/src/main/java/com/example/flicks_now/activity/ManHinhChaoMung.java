package com.example.flicks_now.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;

public class ManHinhChaoMung extends AppCompatActivity {
    private static final long HienThiManHinh = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chaomung);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ManHinhChaoMung.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, HienThiManHinh);
        ChuyenDongHinh();
    }
    private void ChuyenDongHinh(){
        ImageView heartImageView = findViewById(R.id.ivHinh);

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(heartImageView, "scaleX", 0.8f, 1.2f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(heartImageView, "scaleY", 0.8f, 1.2f);

        int animationDuration = 1000;
        scaleXAnimator.setDuration(animationDuration);
        scaleYAnimator.setDuration(animationDuration);

        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);

        scaleXAnimator.start();
        scaleYAnimator.start();
    }
}
