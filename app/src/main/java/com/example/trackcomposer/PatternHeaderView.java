package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PatternHeaderView extends View {
    int mLOD = 0;
    boolean bInvertY = false;
    Paint black;
    Paint box;
    Paint gray;
    Paint blue;
    Paint selectedColor;
    TextPaint mTextBlack, mTextWhite;

    public PatternHeaderView(Context context) {
        super(context);
        init(null, 0);
    }

    public PatternHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PatternHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PatternBaseView, defStyle, 0);

        // Set up a default TextPaint object
        mTextBlack = new TextPaint();
        mTextBlack.setColor(Color.BLACK);
        mTextBlack.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextBlack.setTextAlign(Paint.Align.LEFT);
        mTextBlack.setTextSize(40);

        mTextWhite = new TextPaint();
        mTextWhite.setColor(Color.WHITE);
        mTextWhite.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextWhite.setTextAlign(Paint.Align.LEFT);
        mTextWhite.setTextSize(40);

        black = new Paint();
        black.setColor(Color.BLACK);

        box = new Paint();
        box.setColor(Color.BLACK);
        box.setStyle(Paint.Style.FILL);

        selectedColor = new Paint();
        selectedColor.setColor(Color.BLUE);
        selectedColor.setStyle(Paint.Style.FILL);

        gray = new Paint();
        gray.setColor(Color.LTGRAY);
        gray.setStyle(Paint.Style.FILL);

        blue = new Paint();
        blue.setColor(Color.rgb(200, 191, 231));
        blue.setStyle(Paint.Style.FILL);
    }

    int mChannels = -1;
    float mLength = -1;
    TimeLine mTimeLine;
    void SetPattern(TimeLine timeLine, int channels, float length,  boolean bInvertY)
    {
        mLength = length;
        mChannels = channels;
        mTimeLine = timeLine;
        this.bInvertY = bInvertY;
    }

    protected float mPosX;
    protected float mPosY;
    protected float mScaleFactor = -1.0f;
    protected float mRowHeight = 0;

    public void setPosScale(float x, float y, float s, float trackHeight)
    {
        mPosX = 0;
        mPosY = y;
        mScaleFactor = s;
        mRowHeight = trackHeight;
    }

    int indexToNote(int y)
    {
        return (bInvertY)?(88 - y):y;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(0, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);

        // Draw background
        //
        int ini = (int)Math.floor(mTimeLine.mViewport.mRect.top / mRowHeight);
        int fin = (int)Math.ceil(mTimeLine.mViewport.mRect.bottom / mRowHeight);

        if (ini<0) ini = 0;
        if (fin>88) fin = 88;


        float left = 0;
        float right = getWidth()/mScaleFactor;

        // Draw text
        //
        for (int i = ini; i < fin; i++) {
            String str = "--";
            if (instrumentListener != null) {
                str = instrumentListener.getInstrumentName(indexToNote(i));
            }
            if (str == null) str = "--";

            boolean bIsBlack = str.indexOf("#")>=0;

            RectF rf = new RectF();
            rf.left = left;
            rf.right = right;
            rf.top = i * mRowHeight;
            rf.bottom = (i + 1) * mRowHeight;

            if (bIsBlack) {
                canvas.drawRect(rf, box);
            }

            float y = ((rf.top + rf.bottom)/2) + (mTextBlack.getTextSize() / 2);
            float x = 5;
            canvas.drawText(str, x, y, bIsBlack?mTextWhite:mTextBlack);
        }

        // horizontal lines
        for (int i = ini; i < fin; i++) {
            float y = i* mRowHeight;
            canvas.drawLine(0, y, mTimeLine.mViewport.mRect.right, y, black);
        }
    }

    int lastTouchY = -1;
    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        int touchY = (int)(event.getY()/ mRowHeight);

        // put your code in here to handle the event
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (instrumentListener!=null) {
                    instrumentListener.noteTouched(touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (instrumentListener!=null && lastTouchY>0) {
                    instrumentListener.actionMove(touchY-lastTouchY);
                }
                break;
        }
        lastTouchY = touchY;

        invalidate();

        // tell the View that we handled the event
        return true;
    }

    // instrument touched listener
    //
    public interface InstrumentListener {
        String getInstrumentName(int i);
        void noteTouched(int note);
        void actionMove(int y);
    }
    public PatternHeaderView setInstrumentListener(PatternHeaderView.InstrumentListener instrumentTouched) {
        this.instrumentListener = instrumentTouched;
        return this;
    }
    private PatternHeaderView.InstrumentListener instrumentListener;
}
