package com.pinasystems.servicebasketv2;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class PreviewMediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_media);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        VideoView mVideoView = (VideoView) findViewById(R.id.videoView);
        boolean isImage = intent.getBooleanExtra("isImage", false);

        if (isImage) {

            int loader = R.drawable.loadingimg;
            mVideoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            ImageLoader imgLoader = new ImageLoader(getApplicationContext());
            imgLoader.DisplayImage(url, loader, imageView);

        } else {
            mVideoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.setVideoURI(Uri.parse(url));
            Toast.makeText(getApplicationContext(),"Loading Video....",Toast.LENGTH_LONG).show();
        }
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

}

