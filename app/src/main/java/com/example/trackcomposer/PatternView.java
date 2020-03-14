package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.SoundPool;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class PatternView extends View {
    SoundPool mSp;

    Paint black;
    Paint box;
    Paint gray;
    Paint blue;
    private int mCurrentBeat = 0;

    private int header = 200;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private int mContentWidth;
    private int mContentHeight;

    Pattern mPattern = null;
    Pattern GetPattern() { return mPattern; }
    void SetPattern(Pattern pattern)
    {
        mPattern = pattern;
        pattern.SetBeatListener(new Pattern.BeatListener() {
            @Override
            public void beat(int currentBeat) {
                mCurrentBeat = currentBeat;
                invalidate();
            }
        });

    }

    public PatternView(Context context) {
        super(context);
        init(null, 0);
    }

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PatternView, defStyle, 0);

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(20);

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas==null)
            return;

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;
        int trackWidth = getWidth() - paddingLeft - paddingRight - header;


        for(int i=0;i<trackWidth;i+=trackWidth/2) {
            float x = paddingLeft + header + i;
            canvas.drawRect(x, 0, x+trackWidth/4, contentHeight, gray);
        }

        canvas.drawLine(contentWidth-1, 0, contentWidth-1, contentHeight, black);
        canvas.drawLine(0, contentHeight-1, contentWidth, contentHeight-1, black);
        canvas.drawLine(0, 0, 0, contentHeight, black);

        Pattern mPattern = GetPattern();
        int xx,yy;
        if (mPattern!=null) {
            xx = mPattern.GetLength();
            yy = mPattern.GetChannelCount();
        }else
        {
            xx = 16;
            yy = 16;
        }

        canvas.drawRect(paddingLeft + header + (mCurrentBeat *trackWidth/xx), 0,  paddingLeft + header + ((mCurrentBeat +1)*trackWidth/xx), contentHeight,blue);

        for(int i=0;i<yy;i++) {

            String str = "--";
            if (mPattern!=null) {
                if (instrumentListener!=null) {
                    str = instrumentListener.getInstrumentName(i);
                }
                if (str == null) str = "--";
            }

            float y = getHeight() - (paddingBottom + (i*contentHeight / yy));
            y -= ((contentHeight / yy)-mTextPaint.getTextSize())/2;
            float x = paddingLeft + 5;
            canvas.drawText(str, x, y, mTextPaint);
        }

        // horizontal lines
        for(int i=0;i<=yy;i++) {
            float y = paddingTop + (i*contentHeight)/yy;
            canvas.drawLine(0, y, contentWidth, y, black);
        }

        for(int i=0;i<=xx;i++) {
            float x = header + paddingLeft + (i*trackWidth)/xx;
            canvas.drawLine(x, 0, x, contentHeight, black);
        }

        if (mPattern==null)
            return;

        for(int x=0;x<xx;x++) {
            for(int y=0;y<yy;y++) {
                if (mPattern.Get(y,x).hit>0) {
                    float _x = paddingLeft + header + ((x*trackWidth)/xx) + 5;
                    float _y = getHeight() - (paddingBottom  + ((y*contentHeight)/yy) + 5);
                    canvas.drawRect(_x,_y-((contentHeight/yy)-10),_x+(trackWidth/xx)-10,_y, box);
                }
            }
        }

    }

    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        Pattern mPattern = GetPattern();
        int xx = mPattern.GetLength();
        int yy = mPattern.GetChannelCount();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = (getWidth() - paddingLeft - paddingRight)/ xx;
        int contentHeight = (getHeight() - paddingTop - paddingBottom)/ yy;
        int trackWidth = (getWidth() - paddingLeft - paddingRight - header)/ xx;

        // you may need the x/y location
        int x = (int)event.getX() - getPaddingLeft();
        int y = (int)event.getY() - getPaddingTop();

        int beat = (x-header)/trackWidth;
        int channel = (getHeight() - paddingBottom - y)/contentHeight;

        // put your code in here to handle the event
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:

                if (x<header) {
                    if (instrumentListener !=null) {
                        instrumentListener.instrumentTouched(channel);
                        invalidate();
                    }
                    break;
                }

                if (channel< mPattern.GetChannelCount() && beat<mPattern.GetLength()) {
                    onTouchEvent(channel, beat);
                    if (noteTouchedListener!=null) {
                        noteTouchedListener.noteTouched(channel, beat);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }

        // tell the View that we handled the event
        return true;
    }

    public void onTouchEvent(int x, int y)
    {
        Pattern mPattern = GetPattern();
        mPattern.Get(x,y).hit = mPattern.Get(x,y).hit==1?0:1;
    }

    void SetSoundPool(SoundPool sp)
    {
        mSp = sp;
    }

    public void SetCurrentBeatCursor(int currentBeat)
    {
        this.mCurrentBeat =currentBeat;
        postInvalidate();
    }

    //-----------------------------------------------------

    // instrument touched listener
    //
    public interface InstrumentListener {
        void instrumentTouched(int channel);
        String getInstrumentName(int i);
    }
    public PatternView setInstrumentListener(InstrumentListener instrumentTouched) {
        this.instrumentListener = instrumentTouched;
        return this;
    }
    private InstrumentListener instrumentListener;

    // note touched listener
    //
    public interface NoteTouchedListener {
        void noteTouched(int note, int beat);
    }
    public PatternView setNoteTouchedListener(NoteTouchedListener noteTouched) {
        this.noteTouchedListener = noteTouched;
        return this;
    }
    private NoteTouchedListener noteTouchedListener;

}
