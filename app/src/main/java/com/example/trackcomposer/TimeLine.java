package com.example.trackcomposer;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

public class TimeLine {

    PatternBase mPattern;
    int mTicksPerTrack;
    float mTicksPerColumn;
    float mColumns;
    float mDistanceBetweenTicks;
    float mColumnWidth;

    float mLOD = 1; // for ticks

    public void init(PatternBase pattern, float ticksPerColumn) {
        mPattern = pattern;
        mTicksPerTrack = pattern.length;
        mTicksPerColumn = ticksPerColumn;
        mColumns = mTicksPerTrack / mTicksPerColumn;
    }

    protected float mPosX = 0.0f;
    protected float mPosY = 0.0f;
    protected float mScaleFactor = 1.0f;

    float mScreenHeight, mScreenWidth;
    float mTickWidth;

    public void setViewSize(float width, float height) {
        mScreenHeight = height;
        mScreenWidth = width;

        int length = 16;
        if (mPattern!=null)
            length = mPattern.GetLength();
        mTickWidth = mScreenWidth / length;
    }

    public float getTickWidth() {
        return mTickWidth;
    }

    public float getLength() {
        return mPattern.GetLength();
    }

    RectF mViewport = new RectF();

    public void updateViewport() {
        mViewport.top = (0 - mPosY) / mScaleFactor;
        mViewport.bottom = (mScreenHeight - mPosY) / mScaleFactor;
        mViewport.left = (0 - mPosX) / mScaleFactor;
        mViewport.right = (mScreenWidth - mPosX) / mScaleFactor;
    }

    public float applyPosScale(float x) {
        return (x * mScaleFactor) + mPosX;
    }

    public void removePosScale(float x, float y, PointF point) {
        point.x = (x - mPosX) / mScaleFactor;
        point.y = (y - mPosY) / mScaleFactor;
    }

    float mTime = 0;
    public void setTime(float time) {
        mTime = time;
    }
    public float getTime() {
        return mTime;
    }

    public int getLeftTick(float tickWidth) {
        return (int) Math.max(Math.floor(mViewport.left / tickWidth), 0); // 0 ticks
    }

    public int getRightTick(float tickWidth) {
        return (int)Math.ceil(mViewport.right / tickWidth); // 256 ticks
    }

    public void onDrag(float distanceX, float distanceY) {
        mPosX -= distanceX;
        mPosY -= distanceY;
        updateViewport();
    }

    public void onScale(float focusX, float focusY, float scale) {
        float oldScaleFactor = mScaleFactor;
        mScaleFactor *= (scale*scale);

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 20.0f));

        //distance between focus and old origin
        float dx = focusX-mPosX;
        float dy = focusY-mPosY;

        //distance between focus and new origin after rescale
        float dxSc = dx * mScaleFactor / oldScaleFactor;
        float dySc = dy * mScaleFactor / oldScaleFactor;

        // calcul of the new origin
        mPosX = focusX - dxSc;
        mPosY = focusY - dySc;

        mLOD = (float)Math.pow(2, Math.floor(Math.log(mScaleFactor)/Math.log(2)));

        updateViewport();
    }
}
