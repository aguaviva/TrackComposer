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
        mTextBlack.setTextSize(20);

        mTextWhite = new TextPaint();
        mTextWhite.setColor(Color.WHITE);
        mTextWhite.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextWhite.setTextAlign(Paint.Align.LEFT);
        mTextWhite.setTextSize(20);

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
    int mLength = -1;
    void SetPattern(int channels, int length,  boolean bInvertY)
    {
        mLength = length;
        mChannels = channels;
        this.bInvertY = bInvertY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int contentWidth = getWidth();
        int contentHeight = getHeight();

        int xx=16,yy=16;
        if (mLength>0 && mChannels>0) {
            xx = mLength;
            yy = mChannels;
        }

        // Draw text
        //
        for (int ii = 0; ii < yy; ii++) {

            int i = (bInvertY) ? (mChannels - 1 - ii) : ii;

            String str = "--";
            if (instrumentListener != null) {
                str = instrumentListener.getInstrumentName(i);
            }
            if (str == null) str = "--";

            boolean bIsBlack = str.indexOf("#")>=0;

            RectF rf = new RectF();
            rf.top = (i * contentHeight / yy);
            rf.bottom = ((i+1) * contentHeight / yy);
            rf.left = 0;
            rf.right = getWidth();

            if (bIsBlack) {
                canvas.drawRect(rf, box);
            }

            float y = ((rf.top + rf.bottom)/2) + (mTextBlack.getTextSize() / 2);
            float x = 5;
            canvas.drawText(str, x, y, bIsBlack?mTextWhite:mTextBlack);
        }

        // horizontal lines
        for(int i=0;i<=yy;i++) {
            float y = (i*contentHeight)/yy;
            canvas.drawLine(0, y, contentWidth, y, black);
        }
    }

    int lastTouchY = -1;
    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        int touchY = (int)(event.getY()/ (getHeight()/mChannels));

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
