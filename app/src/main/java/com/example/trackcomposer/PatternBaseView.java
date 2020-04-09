package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.HashMap;

/**
 * TODO: document your custom view class.
 */
public class PatternBaseView extends View {
    int mLOD = 0;
    boolean bInvertY = false;
    Paint black;
    Paint box;
    Paint ltgray;
    Paint dkgray;
    Paint white;
    Paint blue;
    Paint green, greenFill;
    Paint selectedColor;
    private int mCurrentBeat = 0;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int mContentWidth;
    private int mContentHeight;

    HashMap<Integer, Bitmap> mPatternImgDataBase = null;

    boolean mSelectable = false;

    int selectedX = -1;
    int selectedY = -1;

    PatternBase mPattern = null;
    PatternBase GetPattern() { return mPattern; }
    void SetPattern(PatternBase pattern, boolean selectable, boolean bInvertY)
    {
        mSelectable = selectable;

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

    void patternImgDataBase(HashMap<Integer, Bitmap> patternImgDataBase)
    {
        mPatternImgDataBase = patternImgDataBase;
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

        selectedColor = new Paint();
        selectedColor.setColor(Color.BLUE);
        selectedColor.setStyle(Paint.Style.FILL);

        ltgray = new Paint();
        ltgray.setColor(Color.LTGRAY);
        ltgray.setStyle(Paint.Style.FILL);

        dkgray = new Paint();
        dkgray.setColor(Color.DKGRAY);
        dkgray.setStyle(Paint.Style.FILL);

        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);
        white.setStrokeWidth(2);

        green = new Paint();
        green.setColor(ContextCompat.getColor(getContext(), R.color.darkGreen));
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(2);

        greenFill = new Paint();
        greenFill.setColor(ContextCompat.getColor(getContext(), R.color.darkGreen));
        greenFill.setStyle(Paint.Style.FILL);

        blue = new Paint();
        blue.setColor(ContextCompat.getColor(getContext(), R.color.cursorVertical));
        blue.setStyle(Paint.Style.FILL);
    }

    int mBaseNote = -1;
    public void setBaseNote(int baseNote)
    {
        mBaseNote = baseNote;
    }

    public void setCurrentBeat(int currentBeat)
    {
        mCurrentBeat = currentBeat;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas==null)
            return;

        int contentWidth = getWidth();
        int contentHeight = getHeight();
        int trackWidth = getWidth();

        if (mLOD==0) {
            for (int i = 0; i < trackWidth; i += trackWidth / 2) {
                float x = i;
                canvas.drawRect(x, 0, x + trackWidth / 4, contentHeight, ltgray);
            }
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


        // Draw background
        //
        if (mLOD==0) {
            for (int i = 0; i < yy; i++) {
                RectF rf = new RectF();
                rf.left = 0;
                rf.top = (i * contentHeight) / yy;
                rf.right = getWidth();
                rf.bottom = ((i + 1) * contentHeight) / yy;

                boolean isWhite = true;
                if (mBaseNote >=0)
                {
                    int ii = (bInvertY) ? (mPattern.GetChannelCount() - 1 - i) : i;
                    isWhite = Misc.isWhiteNote(mBaseNote +ii);
                }
                else
                {
                    isWhite = ((i & 1) == 0);
                }

                canvas.drawRect(rf,  isWhite ? dkgray:black);
            }

            for (int i = 0; i < xx; i++) {
                float x = (i * getWidth()) / xx;
                canvas.drawLine(x, 0, x, getHeight(), ((i & 3) == 0) ? white : ltgray);
            }
        }

        // Draw cursor
        //
        if (mLOD==0)
        {
            canvas.drawRect((mCurrentBeat * trackWidth / xx), 0, ((mCurrentBeat + 1) * trackWidth / xx), contentHeight, blue);
        }


        if (mPattern==null)
            return;

        int padTL = 0;
        int padBR = 1;
        if (mLOD==0) {
            padTL = 5;
            padBR = 2*padTL;
        }

        //show selected block
        if (mSelectable)
        {
            int x = selectedX;
            if (x>=0) {
                int y = selectedY;

                y = (bInvertY) ? (mPattern.GetChannelCount() - 1 - y) : y;
                float _x = ((x * trackWidth) / xx);
                float _y = ((y * contentHeight) / yy);
                canvas.drawRect(_x,_y,_x+(trackWidth/xx),_y+(contentHeight/yy), selectedColor);
            }
        }

        // show blocks
        for(int i=0;;i++)
        {
            SortedListOfNotes.Note note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;

            int x = note.time;
            int y = note.channel;

            y = (bInvertY)?(mPattern.GetChannelCount() -1 - y):y;
            float _x = ((x*trackWidth)/xx) + padTL;
            float _y = ((y*contentHeight)/yy) + padTL;

            RectF rf = new RectF();
            rf.left = _x;
            rf.top = _y;
            rf.right = _x + (trackWidth / xx)- (padBR);
            rf.bottom = _y + (contentHeight / yy) - (padBR);

            Integer id = note.mGen.sampleId;
            if (mPatternImgDataBase!=null && mPatternImgDataBase.containsKey(id))
            {
                Bitmap bmp = mPatternImgDataBase.get(id);
                if (bmp!=null) {

                    canvas.drawBitmap(bmp, null, rf, null);

                    rf.left = (x*getWidth())/xx;
                    rf.top =  (y*getHeight())/yy;
                    rf.right = ((x+1)*getWidth())/xx;
                    rf.bottom = ((y+1)*getHeight())/yy;


                    canvas.drawRoundRect( rf, 10,10, green);
                }
            }
            else {
                canvas.drawRect(_x, _y, _x + (trackWidth / xx) - (padBR), _y + (contentHeight / yy) - (padBR), greenFill);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        PatternBase mPattern = GetPattern();
        int xx = mPattern.GetLength();
        int yy = mPattern.GetChannelCount();

        int contentWidth = getWidth() / xx;
        int contentHeight = getHeight()/ yy;
        int trackWidth = getWidth()/ xx;

        // you may need the x/y location
        int x = (int)event.getX();
        int y = (int)event.getY();

        int beat = x/trackWidth;
        int channel = y/contentHeight;

        if (bInvertY)
            channel = mPattern.GetChannelCount()-1 - channel;

        // put your code in here to handle the event
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (channel< mPattern.GetChannelCount() && beat<mPattern.GetLength()) {
                    boolean bProcessTouch = false;

                    if (mSelectable) {
                        selectedX = beat;
                        selectedY = channel;
                    }


                    if (instrumentListener!=null) {
                        bProcessTouch = instrumentListener.noteTouched(channel, beat);
                    }

                    if (bProcessTouch)
                        onTouchEvent(channel, beat);

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
        boolean noteTouched(int note, int beat);
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
