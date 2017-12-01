package com.example.itai.smilingapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

/**
 * Created by Paul on 11/4/15.
 */
public class FaceOverlayView extends View {

    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;
    private double smilingProb;

    public double getSmilingProb() {
        return smilingProb;
    }

    public FaceOverlayView(Context context) {
        this(context, null);
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SparseArray<Face> setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        FaceDetector detector = new FaceDetector.Builder(getContext())
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE).setClassificationType
                        (FaceDetector.ALL_CLASSIFICATIONS).build();

        if (!detector.isOperational()) {
            return null;
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            mFaces = detector.detect(frame);
            detector.release();
        }
        if (mFaces.size() > 0)
            smilingProb = mFaces.valueAt(0).getIsSmilingProbability();
        else
            smilingProb = -1;
        return mFaces;
    }

    public void invalidateThis() {
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            drawFaceLandmarks(canvas, scale);
        }
    }

    private double drawBitmap(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        Rect destBounds = new Rect(0, 0, (int) (imageWidth * scale), (int) (imageHeight * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }


    private void drawFaceLandmarks(Canvas canvas, double scale) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < mFaces.size(); i++) {
            Face face = mFaces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x * scale);
                int cy = (int) (landmark.getPosition().y * scale);
                canvas.drawCircle(cx, cy, 10, paint);
            }
        }
    }
}