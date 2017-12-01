package com.example.itai.smilingapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.itai.smilingapp.SilentCamera.IMAGE_READY;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiver;
    private SilentCamera mCamera;
    private Button restartButton ;
    private static final int REQUESTS = 100;
    public final static int REQUEST_DENIED_PERMISSION = 1232;
    private String[] permissions = {Manifest.permission.CAMERA, WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restartButton = (Button) findViewById(R.id.restart);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartButton.setVisibility(View.INVISIBLE);
                showDialog();
            }
        });
        if ( ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)< 0 ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)<0){
            ActivityCompat.requestPermissions(this, permissions, REQUESTS);
        }else {
            showDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUESTS:
                boolean[] arrOfPermissions = new boolean[grantResults.length];
                for (int i = 0; i < arrOfPermissions.length; i++) {
                    arrOfPermissions[i] = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    String permission = permissions[i];
                    if (!arrOfPermissions[i]) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);
                        }
                        if (!showRationale) {
                            startSettings(REQUEST_DENIED_PERMISSION,
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            return;
                        } else {
                            popUpForRequest(null,0);
                            return;
                        }
                    }
                }
                showDialog();
                break;
        }
    }


    private void popUpForRequest(final Intent intent, final int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(R.string.permission_requested_message)
                .setTitle(R.string.alert_dialog_Permission_Request);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (intent != null)
                    startActivityForResult(intent, code);
                else
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUESTS);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void startSettings(final int code, String settings) {
        final Intent intent = new Intent(settings,
                Uri.parse("package:" + getPackageName()));
        popUpForRequest(intent,code);

    }


    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("I can see if you smile!")
                .setTitle("Look at me");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startCamera();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void startCamera() {
        mCamera = new SilentCamera(this);
        mCamera.getCameraInstanceSilentMode();
        mCamera.takePicture();
    }


    /**
     * Called when image is ready to be analyzed
     * @param bitmapImage - The image taken without the user's knowledge
     */
    private void continueAnalyze(final Bitmap bitmapImage) {
            FaceOverlayView mFaceOverlayView = (FaceOverlayView) findViewById(R.id.face_overlay);
            mCamera.save(bitmapImage);
            mFaceOverlayView.setBitmap(bitmapImage);
            double smilingProbability = mFaceOverlayView.getSmilingProb();
            if (smilingProbability > -1) {
                if (smilingProbability < 0.7) {
                    mFaceOverlayView.invalidateThis();
                    Toast.makeText(MainActivity.this, "you don't smile, you lied to me!",
                            Toast.LENGTH_LONG).show();
                } else {
                    mFaceOverlayView.invalidateThis();
                    Toast.makeText(MainActivity.this, "you  smile!",
                            Toast.LENGTH_LONG).show();
                }
            }
        restartButton.setVisibility(View.VISIBLE);
        }


    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mReceiver);    //Unregister our receiver
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(IMAGE_READY);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //Get message from intent
                Bitmap imageBitmap = mCamera.getImageBitmap();
                continueAnalyze(imageBitmap);
            }
        };
        //Registering receiver
        this.registerReceiver(mReceiver, intentFilter);
    }
}