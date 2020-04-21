package com.example.trackcomposer;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

public class TimeLine {

    PatternBase mPattern;
    int mTicksPerTrack;
    float mTicksPerColumn;
    float mColumns;

    Viewport mViewport = new Viewport();

    public void init(PatternBase pattern, float ticksPerColumn) {
        mPattern = pattern;
        mTicksPerTrack = (int)pattern.length;
        mTicksPerColumn = ticksPerColumn;
        mColumns = mTicksPerTrack / mTicksPerColumn;
    }

    float mTickWidth;

    public void setViewSize(float width, float height) {

        mViewport.setViewSize(width, height);

        int length = 16;
        if (mPattern!=null)
            length = (int)mPattern.GetLength();
        mTickWidth = mViewport.mScreenWidth / length;
    }

    public float getTickWidth() {
        return mTickWidth;
    }

    public float getLength() {
        return mPattern.GetLength();
    }

    float mTime = 0;
    public void setTime(float time) {
        mTime = time;
    }
    public float getTime() {
        return mTime;
    }

    public int getLeftTick(float tickWidth) {
        return (int) Math.max(Math.floor(mViewport.mRect.left / tickWidth), 0); // 0 ticks
    }

    public int getRightTick(float tickWidth) {
        return (int)Math.ceil(mViewport.mRect.right / tickWidth); // 256 ticks
    }
}
