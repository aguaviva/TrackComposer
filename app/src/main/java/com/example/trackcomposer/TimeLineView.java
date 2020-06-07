package com.example.trackcomposer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
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

        if (isInEditMode())
        {
            PatternPianoRoll pattern = new PatternPianoRoll("caca","caca",16, 16);

            TimeLine timeLine = new TimeLine();
            timeLine.init(pattern, 64);
            timeLine.setViewSize(getWidth(), getHeight());

            init(pattern, timeLine);
        }

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

    void setRectCenter(float x, RectF rect) {
        rect.top = 0;
        rect.bottom = getHeight();
        rect.left = x - getHeight() / 2;
        rect.right = x + getHeight() / 2;
    }

    void setRectRight(float x, RectF rect) {
        rect.top = 0;
        rect.bottom = getHeight();
        rect.left = x;
        rect.right = x + getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mIni = mTimeLine.getLeftTick(mViewport.getLod());
        mFin = mTimeLine.getRightTick(mViewport.getLod());

        float hh = getHeight() / 5.0f;

        for (int i=mIni;i<mFin;i++) {

            float x = mViewport.applyPosScaleX(i / mViewport.getLod());

            float h = 0;
            if (i % 4 == 0) {
                h = hh * 2;
            canvas.drawText(String.valueOf((int) (i / mViewport.getLod())), x + 5, mTextBlack.getTextSize() + 5, mTextBlack);
            }
            else
                h = hh*3;

            canvas.drawLine(x, h, x, hh*4, black);
        }

        // draw time pos marker
        {
            float time =  (mTimeLine.getTime());
            float x = mViewport.applyPosScaleX(time);
            RectF rf = new RectF();
            setRectCenter(x, rf);
            canvas.drawBitmap(bmp, null, rf, null);
        }

        // draw track length marker
        {
            float endTime =  (mTimeLine.getLength());
            float x = mViewport.applyPosScaleX(endTime);
            RectF rf = new RectF();
            setRectRight(x, rf);
            canvas.drawBitmap(end, null, rf, null);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());

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
