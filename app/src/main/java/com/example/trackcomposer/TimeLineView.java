package com.example.trackcomposer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class TimeLineView extends View {

    Paint black;
    TextPaint mTextBlack;

    Bitmap bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_mylocation);
    Bitmap end = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);

    boolean mMovingEnd = false;
    float mDownX;

    int mIni, mFin, mLod;

    public TimeLineView(Context context) {
        super(context);

        init(null, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        black = new Paint();
        black.setColor(Color.BLACK);

        // Set up a default TextPaint object
        mTextBlack = new TextPaint();
        mTextBlack.setColor(Color.BLACK);
        mTextBlack.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextBlack.setTextAlign(Paint.Align.LEFT);
        mTextBlack.setTextSize(20);
    }

    TimeLine mTimeLine;
    Viewport mViewport;

    public void init(PatternBase pattern, TimeLine timeLine)
    {
        mTimeLine = timeLine;
        mViewport = timeLine.mViewport;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mIni = mTimeLine.getLeftTick(mTimeLine.getTickWidth()/mViewport.mLOD);
        mFin = mTimeLine.getRightTick(mTimeLine.getTickWidth()/mViewport.mLOD);

        for (int i=mIni;i<mFin;i++) {

            float x = mViewport.applyPosScale(i* mTimeLine.getTickWidth()/mViewport.mLOD);

            float h = 0;
            if (((i%16)==0)) {
                h = 10;
                canvas.drawText(String.valueOf((int)(i/mViewport.mLOD)), x+5,mTextBlack.getTextSize()+5, mTextBlack);
            }
            else if (i%4==0)
                h = 10+10;
            else
                h = 10+10+10;

            canvas.drawLine(x, 10+h, x, getHeight()-10, black);
        }

        // draw time pos marker
        {
            float time =  (mTimeLine.getTime() * mTimeLine.getTickWidth());
            float x = mViewport.applyPosScale(time);
            RectF rf = new RectF();
            rf.top = 0;
            rf.bottom = getHeight();
            rf.left = x - getHeight() / 2;
            rf.right = x + getHeight() / 2;
            canvas.drawBitmap(bmp, null, rf, null);
        }

        // draw track length marker
        {
            float endTime =  (mTimeLine.getLength() * mTimeLine.getTickWidth());
            float x = mViewport.applyPosScale(endTime);
            RectF rf = new RectF();
            rf.top = 0;
            rf.bottom = getHeight();
            rf.left = x;
            rf.right = x + getHeight();
            canvas.drawBitmap(end, null, rf, null);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        float x = ((event.getX() - mViewport.mPosX) / mViewport.mScaleFactor);
        float thumbTime = (float)Math.floor(x / ((mTimeLine.getTickWidth()/mViewport.mLOD)))/mViewport.mLOD;

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (thumbTime<mTimeLine.getLength() ) {
                    mTimeLine.setTime(thumbTime);
                    if (mTimeLineListener!=null) {
                        mTimeLineListener.onTimeChanged(mTimeLine.getTime());
                    }
                }
                else {
                    mMovingEnd = true;
                    mDownX = thumbTime;
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mMovingEnd == true) {
                    float delta = thumbTime - mDownX;
                    mDownX = thumbTime;
                    if (mTimeLineListener!=null) {
                        mTimeLineListener.onPatternEnd(mTimeLine.getLength()  + delta);
                    }
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                mMovingEnd = false;
                return true;
        }
        return false;
    }

    // instrument touched listener
    //
    public interface TimeLineListener {
        void onTimeChanged(float time);
        void onPatternEnd(float time);
    }
    public void setTimeLineListener( TimeLineListener timeLineListener) {
        mTimeLineListener = timeLineListener;
    }
    private TimeLineListener mTimeLineListener;

    float getSelection()
    {
        return mTimeLine.getTime();
    }
}
