package com.example.trackcomposer;

import android.graphics.RectF;
import android.view.MotionEvent;

class Viewport {
    protected float mPosX = 0.0f, mPosY = 0.0f;
    protected float mScaleX = 1.0f, mScaleY = 1.0f;
    protected float mVelX = 0.0f, mVelY = 0.0f;
    private float mLod = 1; // for ticks
    private float mLodFactor = 1;
    RectF mRect = new RectF();

    float mScreenHeight, mScreenWidth;

    public void scalePos2Viewport() {
        mRect.top = (0 - mPosY) / mScaleY;
        mRect.bottom = (mScreenHeight - mPosY) / mScaleY;
        mRect.left = (0 - mPosX) / mScaleX;
        mRect.right = (mScreenWidth - mPosX) / mScaleX;
    }

    public void viewport2ScalePos() {
        mScaleX = mScreenWidth/(mRect.right-mRect.left);
        mPosX = -mRect.left * mScaleX;

        mScaleY = mScreenHeight/(mRect.bottom-mRect.top);
        mPosY = -mRect.top * mScaleY;

        mLod = (float) Math.pow(2, Math.floor(Math.log(mScaleX) / Math.log(2) + 0.0f));
    }

    public float getLod() { return mLodFactor * mLod; }
    public void setLodFactor(float factor) { mLodFactor = factor; }

    public void setSpanHorizontal(float x1, float x2) {
        mRect.left = x1;
        mRect.right = x2;
        viewport2ScalePos();
    }

    public void setSpanVertical(float y1, float y2) {
        mRect.top = y1;
        mRect.bottom = y2;
        viewport2ScalePos();
    }

    public void setViewSize(float width, float height) {
        mScreenHeight = height;
        mScreenWidth = width;

        viewport2ScalePos();
    }

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

    public void applyPosScaleRect(RectF rect)
    {
        rect.left = applyPosScaleX(rect.left);
        rect.right = applyPosScaleX(rect.right);
        rect.top = applyPosScaleY(rect.top);
        rect.bottom = applyPosScaleY(rect.bottom);
    }

    public void onDrag(float distanceX, float distanceY) {

        mVelX = 0; mVelY = 0;

        viewport2ScalePos();

        mPosX -= distanceX;
        mPosY -= distanceY;

        scalePos2Viewport();
    }

    public void onScale(float focusX, float focusY, float scaleX, float scaleY) {

        mVelX = 0; mVelY = 0;

        viewport2ScalePos();

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

        mLod = (float)Math.pow(2, Math.floor(Math.log(mScaleX)/Math.log(2)+0.0f));

        scalePos2Viewport();
    }

    public void onFling(float velX, float velY)
    {
        mVelX = -velX/mScaleX;
        mVelY = -velY/mScaleY;
    }

    public boolean onDown(MotionEvent e) {
        mVelX = 0; mVelY = 0;
        return true;
    }

    public boolean springToScreen()
    {
        boolean bDoInvalidate = false;

        if (Math.abs(mVelX) > 0.001f || Math.abs(mVelY) > 0.001f) {
            mRect.left += mVelX ;
            mRect.right += mVelX ;
            mRect.top += mVelY;
            mRect.bottom += mVelY;

            mVelX *= 0.99f;
            mVelY *= 0.99f;

            bDoInvalidate = true;
        }

        // spring to center the track
        //
        if (mRect.left < -0.001) {
            mVelX = 0;
            float v = (0 - mRect.left) * 0.1f;
            mRect.left += v;
            mRect.right += v;
            bDoInvalidate = true;
        }

        if (mRect.top < -0.001) {
            mVelY = 0;
            float v = (0 - mRect.top) * 0.1f;
            mRect.top += v;
            mRect.bottom += v;
            bDoInvalidate = true;
        }

        if (bDoInvalidate)
        {
            viewport2ScalePos();
        }

        return bDoInvalidate;
    }
}
