package com.example.downloadimagehttpurlconnection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private final static String imagePath =
            "https://www.pbcexpo.com.au/assets/Uploads/_resampled/CroppedFocusedImageWzEyMDAsNjMwLCJ5Iiw4M10/Baby-6.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView image = findViewById(R.id.image_view);
        ProgressBar progressBar = findViewById(R.id.progress_circular);

        DownloadImage downloadImage = new DownloadImage(image, progressBar);
        downloadImage.execute(imagePath);
    }
}
