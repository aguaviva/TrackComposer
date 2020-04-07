package com.example.trackcomposer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TimeLineView extends View {

    Paint black;
    Paint box;
    Paint gray;
    Paint blue;
    Bitmap bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_next);

    int[] mMarkerPos = new int[2];
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

        mMarkerPos[0]=0;
        mMarkerPos[1]=500;

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
    }

    protected void drawMarker(Canvas canvas,float x) {
        RectF rf = new RectF();
        rf.top = 0;
        rf.bottom = getHeight();
        rf.left = x - getHeight()/2;
        rf.right = x + getHeight()/2;
        canvas.drawBitmap(bmp, null, rf, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0, getHeight()/2, getWidth(), getHeight()/2, black);
        for (int i=0;i<16;i++)
        {
            float x = i * getWidth() / 16;
            canvas.drawLine(x, 0, x, getHeight(), black);
        }

        drawMarker(canvas, mMarkerPos[0]);
        drawMarker(canvas, mMarkerPos[1]);

        RectF rf = new RectF();
        rf.top = 0;
        rf.bottom = getHeight();
        rf.left = mMarkerPos[0];
        rf.right = mMarkerPos[1];
        canvas.drawRect(rf, blue);
    }

    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();
        float x = event.getX();
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (x<(mMarkerPos[0]+mMarkerPos[1])/2)
                {
                    mMoving = 0;
                }
                else
                {
                    mMoving = 1;
                }
                mDownX = x;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                mMarkerPos[mMoving] += x -mDownX;
                mDownX = x;
                invalidate();
                break;
        }

        // tell the View that we handled the event
        return true;
    }

    float getSelection(int i)
    {
        int x = mMarkerPos[i];
        return (x*100)/getWidth();
    }
}
