package com.example.trackcomposer;

import android.graphics.PointF;
import android.graphics.RectF;

public class Viewport {
    protected float mPosX = 0.0f;
    protected float mPosY = 0.0f;
    protected float mScaleFactor = 1.0f;

    float mLOD = 1; // for ticks

    RectF mRect = new RectF();

    float mScreenHeight, mScreenWidth;

    public void setViewSize(float width, float height) {
        mScreenHeight = height;
        mScreenWidth = width;
    }

    public void updateViewport() {
        mRect.top = (0 - mPosY) / mScaleFactor;
        mRect.bottom = (mScreenHeight - mPosY) / mScaleFactor;
        mRect.left = (0 - mPosX) / mScaleFactor;
        mRect.right = (mScreenWidth - mPosX) / mScaleFactor;
    }

    public float applyPosScale(float x) {
        return (x * mScaleFactor) + mPosX;
    }

    public float removePosScaleX(float x) {
        return (x - mPosX) / mScaleFactor;
    }

    public float removePosScaleY(float y) {
        return (y - mPosY) / mScaleFactor;
    }

    public void removePosScale(float x, float y, PointF point) {
        point.x = (x - mPosX) / mScaleFactor;
        point.y = (y - mPosY) / mScaleFactor;
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

    public boolean springToScreen()
    {
        // spring to center the track
        //
        if (mPosX > 0.001 || mPosY > 0.001) {

            if (mPosX > 0) {
                mPosX += (0 - mPosX) * .1;
            }
            if (mPosY > 0) {
                mPosY += (0 - mPosY) * .1;
            }

            updateViewport();
            return true;
        }
        return false;
    }
}
