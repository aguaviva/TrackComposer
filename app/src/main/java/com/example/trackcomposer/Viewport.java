package com.example.trackcomposer;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

public class Viewport {
    protected float mPosX = 0.0f, mPosY = 0.0f;
    protected float mScaleX = 1.0f, mScaleY = 1.0f;
    protected float mVelX = 0.0f, mVelY = 0.0f;
    private float mLOD = 1; // for ticks

    RectF mRect = new RectF();

    float mScreenHeight, mScreenWidth;

    public void setViewSize(float width, float height) {
        mScreenHeight = height;
        mScreenWidth = width;
    }

    public void updateViewport() {
        mRect.top = (0 - mPosY) / mScaleY;
        mRect.bottom = (mScreenHeight - mPosY) / mScaleY;
        mRect.left = (0 - mPosX) / mScaleX;
        mRect.right = (mScreenWidth - mPosX) / mScaleX;
    }

    public float getLOD() { return mLOD; }

    public float applyPosScaleX(float x) {
        return (x * mScaleX) + mPosX;
    }

    public float applyPosScaleY(float y) {
        return (y * mScaleY) + mPosY;
    }

    public float removePosScaleX(float x) {
        return (x - mPosX) / mScaleX;
    }

    public float removePosScaleY(float y) {
        return (y - mPosY) / mScaleY;
    }

    public void onDrag(float distanceX, float distanceY) {

        mVelX = 0; mVelY = 0;

        mPosX -= distanceX;
        mPosY -= distanceY;
        updateViewport();
    }

    public void onScale(float focusX, float focusY, float scaleX, float scaleY) {

        mVelX = 0; mVelY = 0;

        float oldScaleFactorX = mScaleX;
        mScaleX *= (scaleX*scaleX);

        float oldScaleFactorY = mScaleY;
        mScaleY *= (scaleY*scaleY);

        // Don't let the object get too small or too large.
        mScaleX = Math.max(0.1f, Math.min(mScaleX, 20.0f));
        mScaleY = Math.max(0.1f, Math.min(mScaleY, 20.0f));

        //distance between focus and old origin
        float dx = focusX-mPosX;
        float dy = focusY-mPosY;

        //distance between focus and new origin after rescale
        float dxSc = dx * mScaleX / oldScaleFactorX;
        float dySc = dy * mScaleY / oldScaleFactorY;

        // calcul of the new origin
        mPosX = focusX - dxSc;
        mPosY = focusY - dySc;

        mLOD = (float)Math.pow(2, Math.floor(Math.log(mScaleX)/Math.log(2)));

        updateViewport();
    }

    public void onFling(float velX, float velY)
    {
        mVelX = velX; mVelY = velY;
    }

    public boolean onDown(MotionEvent e) {
        mVelX = 0; mVelY = 0;
        return true;
    }

    public boolean springToScreen()
    {
        boolean bDoInvalidate = false;

        if (Math.abs(mVelX) > 0.001f || Math.abs(mVelY) > 0.001f) {
            mPosX += mVelX ;
            mPosY += mVelY;

            mVelX *= 0.99f;
            mVelY *= 0.99f;

            bDoInvalidate = true;
        }

        // spring to center the track
        //
        if (mPosX > 0.001) {
            mVelX = 0;
            mPosX += (0 - mPosX) * .1;
            bDoInvalidate = true;
        }

        if (mPosY > 0.001) {
            mVelY = 0;
            mPosY += (0 - mPosY) * .1;
            bDoInvalidate = true;
        }

        if (bDoInvalidate) {
            updateViewport();
            return true;
        }

        return false;
    }
}
