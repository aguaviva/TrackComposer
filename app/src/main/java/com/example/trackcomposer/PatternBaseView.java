package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
public class PatternBaseView extends View {
    int mLOD = 0;
    boolean bInvertY = false;
    Paint black;
    Paint box;
    Paint gray;
    Paint blue;
    private int mCurrentBeat = 0;

    private int mHeader = 50;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private int mContentWidth;
    private int mContentHeight;

    PatternBase mPattern = null;
    PatternBase GetPattern() { return mPattern; }
    void SetPattern(PatternBase pattern, boolean bInvertY)
    {
        mPattern = pattern;
        this.bInvertY = bInvertY;
        pattern.SetBeatListener(new PatternBase.BeatListener() {
            @Override
            public void beat(int currentBeat) {
                mCurrentBeat = currentBeat;
                invalidate();
            }
        });

    }

    public PatternBaseView(Context context) {
        super(context);
        init(null, 0);
    }

    public PatternBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PatternBaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PatternBaseView, defStyle, 0);

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

        int header = mHeader;
        if (mLOD>0)
            header = 0;

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

        if (mLOD==0)
        {
            canvas.drawLine(contentWidth - 1, 0, contentWidth - 1, contentHeight, black);
            canvas.drawLine(0, contentHeight - 1, contentWidth, contentHeight - 1, black);
            canvas.drawLine(0, 0, 0, contentHeight, black);
        }

        PatternBase mPattern = GetPattern();
        int xx,yy;
        if (mPattern!=null) {
            xx = mPattern.GetLength();
            yy = mPattern.GetChannelCount();
        }else
        {
            xx = 16;
            yy = 16;
        }

        // Draw cursor
        //
        if (mLOD==0)
        {
            canvas.drawRect(paddingLeft + header + (mCurrentBeat * trackWidth / xx), 0, paddingLeft + header + ((mCurrentBeat + 1) * trackWidth / xx), contentHeight, blue);
        }

        // Draw text
        //
        if (mLOD==0) {
            for (int i = 0; i < yy; i++) {

                int ii = (bInvertY) ? (mPattern.GetChannelCount() - 1 - i) : i;

                String str = "--";
                if (mPattern != null) {
                    if (instrumentListener != null) {
                        str = instrumentListener.getInstrumentName(i);
                    }
                    if (str == null) str = "--";
                }

                float y = paddingTop + (i * contentHeight / yy);
                y += ((contentHeight / yy) + mTextPaint.getTextSize()) / 2;
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
        }

        if (mPattern==null)
            return;

        int padTL = 0;
        int padBR = 1;
        if (mLOD==0) {
            padTL = 5;
            padBR = 2*padTL;
        }


        for(int i=0;;i++)
        {
            SortedListOfNotes.Note note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;

            int x = note.time;
            int y = note.channel;

            y = (bInvertY)?(mPattern.GetChannelCount() -1 - y):y;
            float _x = paddingLeft + header + ((x*trackWidth)/xx) + padTL;
            float _y = paddingTop  + ((y*contentHeight)/yy) + padTL;
            canvas.drawRect(_x,_y,_x+(trackWidth/xx)-(padBR),_y+(contentHeight/yy)-(padBR), box);
        }

    }

    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        PatternBase mPattern = GetPattern();
        int xx = mPattern.GetLength();
        int yy = mPattern.GetChannelCount();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = (getWidth() - paddingLeft - paddingRight)/ xx;
        int contentHeight = (getHeight() - paddingTop - paddingBottom)/ yy;
        int trackWidth = (getWidth() - paddingLeft - paddingRight - mHeader)/ xx;

        // you may need the x/y location
        int x = (int)event.getX() - getPaddingLeft();
        int y = (int)event.getY() - getPaddingTop();

        int beat = (x-mHeader)/trackWidth;
        int channel = (y-paddingTop)/contentHeight;

        if (bInvertY)
            channel = mPattern.GetChannelCount()-1 - channel;

        // put your code in here to handle the event
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:

                if (x<mHeader) {
                    if (instrumentListener !=null) {
                        instrumentListener.instrumentTouched(channel);
                        invalidate();
                    }
                    break;
                }

                if (channel< mPattern.GetChannelCount() && beat<mPattern.GetLength()) {
                    onTouchEvent(channel, beat);
                    if (instrumentListener!=null) {
                        instrumentListener.noteTouched(channel, beat);
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
        PatternBase mPattern = GetPattern();

        GeneratorInfo gen = mPattern.Get(x,y);
        if (gen==null)
        {
            mPattern.Set(x,y, new GeneratorInfo());
        }
        else
        {
            mPattern.Set(x,y, null);
        }
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
        void noteTouched(int note, int beat);
    }
    public PatternBaseView setInstrumentListener(InstrumentListener instrumentTouched) {
        this.instrumentListener = instrumentTouched;
        return this;
    }
    private InstrumentListener instrumentListener;


    public Bitmap getBitmapFromView(int width, int height) {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layout(0, 0, width, height);
        mLOD = 1;
        draw(canvas);
        mLOD = 0;
        return bitmap;
    }

}
