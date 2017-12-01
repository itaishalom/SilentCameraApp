package com.example.itai.smilingapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by Itai on 21/04/2017.
 */

public class SilentCamera {
    public static final String IMAGE_READY = "IMAGE_READY";
    private Context context;
    private boolean hasCamera;
    private Camera camera;
    private int cameraId;
    private Bitmap imageBitmap;
    private final String TAG = "SilentCamera";

    public SilentCamera(Context c) {
        context = c.getApplicationContext();
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            cameraId = getFrontCameraId();

            if (cameraId != -1) {
                hasCamera = true;
                open();
            } else {
                hasCamera = false;
            }
        } else {
            hasCamera = false;
        }
    }


    public void getCameraInstanceSilentMode() {
        SurfaceView view = new SurfaceView(context);
        try {
            camera.setPreviewDisplay(view.getHolder());

        } catch (IOException e) {
            e.printStackTrace();
        }
        SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
        try {
            camera.setPreviewTexture(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        Camera.Parameters params = camera.getParameters();
        params.setJpegQuality(50);
        params.setRotation(270);
        camera.setParameters(params);
    }

    private int getFrontCameraId() {
        int camId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo ci = new Camera.CameraInfo();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camId = i;
            }
        }
        return camId;
    }

    private void open() {
        camera = null;
        if (hasCamera) {
            try {
                camera = Camera.open(cameraId);
            } catch (Exception e) {
                hasCamera = false;
                Log.e(TAG, "open: Mo camera was found");
            }
        }
    }

    public void takePicture() {
        if (hasCamera) {
            camera.takePicture(null, null, mPicture);
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] imageByteArray, Camera camera) {
            releaseCamera();
            imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            Intent i = new Intent(IMAGE_READY);
            context.sendBroadcast(i);
        }
    };

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }


    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public String save(Bitmap bmp) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        String mPathImage = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) +
                "/" + now + ".jpeg";
        FileOutputStream outImage = null;
        try {
            outImage = new FileOutputStream(mPathImage);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outImage);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "save: Error saving image");
        } finally {
            try {
                if (outImage != null) {
                    outImage.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "save: Error saving image");
            }
        }
        return mPathImage;
    }
}