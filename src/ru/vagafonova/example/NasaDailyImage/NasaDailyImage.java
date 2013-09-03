package ru.vagafonova.example.NasaDailyImage;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class NasaDailyImage extends Activity {
    private static final String TAG = NasaDailyImage.class.getSimpleName();
    Handler handler;
    IotdHandler iotdHandler;
    ProgressDialog dialog;
    Bitmap image;

    private static Bitmap loadBitmap(String stringUrl) {
        Bitmap bitmap = null;
        URL url;
        try {
            url = new URL(stringUrl);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler = new Handler();
        iotdHandler = new IotdHandler();
        refreshFromFeed();
    }

    public void onRefresh(View view) {
        refreshFromFeed();
    }

    private void resetDisplay(String title, String date,
                              Bitmap image, String description) {

        TextView descriptionView = (TextView) findViewById(R.id.imageDescription);
        descriptionView.setText(description);
        TextView titleView = (TextView) findViewById(R.id.imageTitle);
        titleView.setText(title);
        TextView dateView = (TextView) findViewById(R.id.imageDate);
        dateView.setText(date);
        ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        imageView.setImageBitmap(image);


    }

    public void refreshFromFeed() {
        dialog = ProgressDialog.show(this, "Loading", "Loading the Image of the Day");
        Thread th = new Thread() {
            public void run() {
                if (iotdHandler == null) {
                    iotdHandler = new IotdHandler();
                }
                iotdHandler.processFeed();
                image = iotdHandler.getImage();
                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                resetDisplay(iotdHandler.getTitle(), iotdHandler.getDate(), image, iotdHandler.getDescription());
                                dialog.dismiss();
                            }
                        }
                );
            }
        };
        th.start();
    }

    public void onSetWallpaper(View view) {
        Thread th = new Thread() {
            public void run() {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(NasaDailyImage.this);
                try {
                    wallpaperManager.setBitmap(image);
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NasaDailyImage.this, "Done! :)", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NasaDailyImage.this,"Oups! I'm failed :(",Toast.LENGTH_SHORT).show();
                                }}
                    );
                }
            }
        };
        th.start();
    }
}
