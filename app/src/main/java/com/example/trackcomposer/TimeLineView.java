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
    Paint box;
    Paint gray;
    Paint blue;
    TextPaint mTextBlack, mTextWhite;

    Bitmap bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_mylocation);

    int mMoving = -1;
    float mDownX;

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

        box = new Paint();
        box.setColor(Color.BLACK);
        box.setStyle(Paint.Style.FILL);

        gray = new Paint();
        gray.setColor(Color.LTGRAY);
        gray.setStyle(Paint.Style.FILL);

        blue = new Paint();
        blue.setColor(Color.rgb(200, 191, 231));
        blue.setStyle(Paint.Style.FILL);

        // Set up a default TextPaint object
        mTextBlack = new TextPaint();
        mTextBlack.setColor(Color.BLACK);
        mTextBlack.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextBlack.setTextAlign(Paint.Align.LEFT);
        mTextBlack.setTextSize(20);

        mTextWhite = new TextPaint();
        mTextWhite.setColor(Color.WHITE);
        mTextWhite.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextWhite.setTextAlign(Paint.Align.LEFT);
        mTextWhite.setTextSize(20);
    }

    protected void drawMarker(Canvas canvas,float x) {
    }

    float mTime = 0;

    public void setTime(float time) {
        mTime = time;
    }

    protected float mPosX;
    protected float mPosY;
    protected float mScaleFactor = -1.0f;
    protected float mRowHeight = 0;

    public void setPosScale(float x, float y, float s, float trackHeight)
    {
        mPosX = x;
        mPosY = 0;
        mScaleFactor = s;
        mRowHeight = trackHeight;
    }

    float mTickWidth = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float viewportTop = (0 - mPosY)/mScaleFactor;
        float viewportBottom = (getHeight() - mPosY)/mScaleFactor;
        float viewportLeft = (0 - mPosX)/mScaleFactor;
        float viewportRight = (getWidth() - mPosX)/mScaleFactor;

        float columnWidth = getWidth()/16;

        int lod = (int)Math.floor(mScaleFactor*2);

        if (lod==3)
            lod=4;

        canvas.drawText(String.valueOf(lod), 5,mTextBlack.getTextSize()+5, mTextBlack);

        mTickWidth = columnWidth / (4 * lod);
        int ini = (int)Math.floor(viewportLeft / mTickWidth);
        int fin = (int)Math.ceil(viewportRight / mTickWidth);

        if (ini<0)
            ini = 0;

        for (int i=ini;i<fin;i++) {
            float x = (i * mTickWidth) * mScaleFactor + mPosX;
            float h = 0;

            if (((i%16)==0)) {
                h = 10;
                canvas.drawText(String.valueOf(i/(4 * lod)), x+5,mTextBlack.getTextSize()+5, mTextBlack);
            }
            else if (i%4==0)
                h = 10+10;
            else
                h = 10+10+10;

            canvas.drawLine(x, 10+h, x, getHeight()-10, black);
        }

        {
            mTickWidth = columnWidth / 16;
            float x = mTime *mTickWidth * mScaleFactor + mPosX;
            RectF rf = new RectF();
            rf.top = 0;
            rf.bottom = getHeight();
            rf.left = x - getHeight() / 2;
            rf.right = x + getHeight() / 2;
            canvas.drawBitmap(bmp, null, rf, null);
        }

        //float x  = mTime * getWidth();
        //canvas.drawLine(x, 0, x, getHeight(), black);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        float x = ((event.getX() - mPosX) / mScaleFactor);
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                mTime = x/mTickWidth;
                mTimeLineListener.onTimeChanged(mTime);
                invalidate();
                return true;
        }
        return false;
    }

    // instrument touched listener
    //
    public interface TimeLineListener {
        void onTimeChanged(float time);
    }
    public void setTimeLineListener( TimeLineListener timeLineListener) {
        mTimeLineListener = timeLineListener;
    }
    private TimeLineListener mTimeLineListener;

    float getSelection()
    {
        return mTime;
    }
}
