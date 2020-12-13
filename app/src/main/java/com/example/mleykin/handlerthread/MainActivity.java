package com.example.mleykin.handlerthread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
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
    private Handler mUiHandler;
    private MyWorkerThread mWorkerThread;
    private ImageView imageView;
    private String[] imagePaths;
    private Button startButton;
    private Button cancelButton;
    private boolean isStarted = false;


    @SuppressLint("HandlerLeak")
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
            onClicks();
        }
        mWorkerThread = new MyWorkerThread("myWorkerThread");
        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        mUiHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bitmap bit = BitmapFactory.decodeFile(msg.getData().getString("imagePath"));
                imageView.setImageBitmap(bit);
            }
        };
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_IMAGES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    onClicks();
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

    public void onClicks(){
        readImagesFromGallery();
        startButton.setOnClickListener(v -> {loadImage(); isStarted = true;});
        cancelButton.setOnClickListener(v -> {isStarted = false;});
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
        Log.e("MY_TAG_START", "START");

        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                for (String imagePath : imagePaths) {
                    try {
                        if(!isStarted){
                            return;
                        }
                        Log.e("MY_TAG", "BEFORE SLEEP");
                        Bundle bundle = new Bundle();
                        bundle.putString("imagePath", imagePath);
                        Message message = new Message();
                        message.setData(bundle);
                        mUiHandler.sendMessage(message);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mWorkerThread.postTask(task1);
        Log.e("MY_TAG", "END");
    }

    @Override
    protected void onDestroy() {
        mWorkerThread.quit();
        super.onDestroy();
    }
}
