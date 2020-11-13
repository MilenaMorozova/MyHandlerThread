package com.example.mleykin.handlerthread;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_IMAGES = 0;
    private Handler mUiHandler = new Handler();
    private MyWorkerThread mWorkerThread;
    private ImageView imageView;
    private String[] imagePaths;
    private Button startButton;
    private Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.IMAGE_VIEW);
        startButton = findViewById(R.id.StartButton);
        cancelButton = findViewById(R.id.CancelButton);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_IMAGES);
        } else {
            readImagesFromGallery();
            startButton.setOnClickListener(v -> loadImage());
        }
        /*mWorkerThread = new MyWorkerThread("myWorkerThread");

        mWorkerThread.start();
        mWorkerThread.prepareHandler();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_IMAGES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    readImagesFromGallery();
                    startButton.setOnClickListener(v -> loadImage());
                    Log.e("MY_TAG", "PERMISSION_GRANTED");
                } else {
                    Log.e("MY_TAG", "PERMISSION_DENIED");
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
        // other 'case' lines to check for other permissions this app might request
    }


    private void readImagesFromGallery() {
        Cursor imageCursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null,
                null, null);
        if (imageCursor == null) {
            return;
        }
        imagePaths = new String[imageCursor.getCount()];
        for (int i = 0; i < imagePaths.length; i++) {
            imageCursor.moveToNext();
            imagePaths[i] = imageCursor.getString(0);
        }
        imageCursor.close();
    }

    private void loadImage(){
        Bitmap bit = BitmapFactory.decodeFile(imagePaths[0]);
        Log.e("MY_TAG", bit + "");
        imageView.setImageBitmap(bit);
        /*Drawable drawable = Drawable.createFromPath(imagePaths[0]);
        Log.e("MY_TAG_DRAW", drawable + "");
        imageView.setImageDrawable(drawable);*/
        Toast.makeText(MainActivity.this, "LOADED!!! " + imagePaths[0], Toast.LENGTH_SHORT).show();
        /*Runnable task1 = new Runnable() {
            @Override
            public void run() {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageDrawable(Drawable.createFromPath(imagePaths[0]));
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        mWorkerThread.postTask(task1);*/
    }

    @Override
    protected void onDestroy() {
        mWorkerThread.quit();
        super.onDestroy();
    }
}
