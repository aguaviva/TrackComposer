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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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

    float mRowHeight = 0;
    int mChannels = 0;
    int length = 0;

    PatternBase mPattern = null;
    PatternBase GetPattern() { return mPattern; }
    void SetPattern(PatternBase pattern, int channels, int length, boolean selectable, boolean bInvertY)
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

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mScaleGestureListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener);
    }

    int mBaseNote = -1;
    public void setBaseNote(int baseNote)
    {
        mBaseNote = baseNote;
    }

    int indexToNote(int y)
    {
        return (bInvertY)?(88 - y):y;
    }

    public void setCurrentBeat(int currentBeat)
    {
        mCurrentBeat = currentBeat;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float contentWidth = getWidth();
        float contentHeight = getHeight();

        float xx = 16;

        // compute max/min notes so we show the part of the keyboard where there is data
        if (mScaleFactor==-1 && mPattern!=null)
        {
            mScaleFactor = 1;

            int min = 88;
            int max = 0;
            for(int i=0;;i++) {
                SortedListOfNotes.Note note = mPattern.GetNoteByIndex(i);
                if (note==null)
                    break;
                if (note.channel>max)
                    max = note.channel;
                if (note.channel<min)
                    min = note.channel;
            }

            mChannels = max - min +1;
            if (mChannels <8)
                mChannels = 8;

            mRowHeight = contentHeight/ (float)mChannels;

            max = indexToNote(max);
            mPosY = (-max)* mRowHeight;


            if (instrumentListener!=null) {
                instrumentListener.scaling(mPosX, mPosY, mScaleFactor, mRowHeight);
            }
        }

        // Set canvas zoom and pan
        //
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);

        float viewportTop = (0 - mPosY)/mScaleFactor;
        float viewportBottom = (getHeight() - mPosY)/mScaleFactor;
        float viewportLeft = (0 - mPosX)/mScaleFactor;
        float viewportRight = (getWidth() - mPosX)/mScaleFactor;

        // Draw background
        //
        int ini = (int)Math.floor(viewportTop / mRowHeight);
        int fin = (int)Math.ceil(viewportBottom / mRowHeight);

        if (ini<0) ini = 0;
        if (fin>88) fin = 88;

        if (mLOD==0) {
            for (int i = ini; i < fin; i++) {

                RectF rf = new RectF();
                rf.left = 0;
                rf.right = contentWidth;
                rf.top = i * mRowHeight;
                rf.bottom = (i + 1) * mRowHeight;

                boolean isWhite = true;
                if (mBaseNote >=0)
                {
                    isWhite = Misc.isWhiteNote(indexToNote(i));
                }
                else
                {
                    isWhite = ((i & 1) == 0);
                }
                canvas.drawRect(rf,  isWhite ? dkgray:black);
            }

            float yTop = ini * mRowHeight;
            float yBottom = fin * mRowHeight;
            for (int i = 0; i < xx; i++) {
                float x = i * (contentWidth / xx);
                canvas.drawLine(x, yTop, x, yBottom, ((i & 3) == 0) ? white : ltgray);
            }
        }

        // Draw cursor
        //
        if (mLOD==0) {
            float yBottom = fin * mRowHeight;
            canvas.drawRect((mCurrentBeat * contentWidth / xx), 0, ((mCurrentBeat + 1) * contentWidth / xx), yBottom, blue);
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
        if (mSelectable) {
            int x = selectedX;
            if (x>=0) {
                int y = selectedY;
                y = indexToNote(y);
                float _x = x * (contentWidth / xx);
                float _y = y * mRowHeight;
                canvas.drawRect(_x,_y,_x+(contentWidth/xx),_y+ mRowHeight, selectedColor);
            }
        }

        PatternBase mPattern = GetPattern();

        // show blocks
        for(int i=0;;i++) {
            SortedListOfNotes.Note note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;

            int x = note.time;
            int y = indexToNote(note.channel);

            float _x = x*(contentWidth/xx) + padTL;
            float _y = y* mRowHeight + padTL;

            RectF rf = new RectF();
            rf.left = _x;
            rf.top = _y;
            rf.right = _x + (contentWidth / xx)- (padBR);
            rf.bottom = _y + mRowHeight - (padBR);

            Integer id = note.mGen.sampleId;
            if (mPatternImgDataBase!=null && mPatternImgDataBase.containsKey(id)) {
                Bitmap bmp = mPatternImgDataBase.get(id);
                if (bmp!=null) {

                    canvas.drawBitmap(bmp, null, rf, null);

                    rf.left = x*(contentWidth/xx);
                    rf.top =  y* mRowHeight;
                    rf.right = (x+1)*(contentWidth/xx);
                    rf.bottom = (y+1)* mRowHeight;

                    canvas.drawRoundRect( rf, 10,10, green);
                }
            }
            else {
                canvas.drawRect(_x, _y, _x + (contentWidth / xx) - (padBR), _y + mRowHeight - (padBR), greenFill);
            }
        }

        // spring to center the track
        //
        if (mPosX>0 ) {
            mPosX += (0 - mPosX) * .1;
        }
        if (mPosY>0) {
            mPosY += (0 - mPosY) * .1;
        }

        if (mPosX>0 || mPosY>0) {
            if (instrumentListener!=null) {
                instrumentListener.scaling(mPosX, mPosY, mScaleFactor, mRowHeight);
            }
            invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        boolean b1 =  mScaleGestureDetector.onTouchEvent(event);
        boolean b2 = mGestureDetector.onTouchEvent(event);
        return b1 || b2;
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

    //  Pan & zoom gestures -----------------------------------------------------

    protected float mPosX;
    protected float mPosY;
    protected float mScaleFactor = -1.0f;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private final GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp (MotionEvent e)
        {
            PatternBase mPattern = GetPattern();

            float contentWidth = getWidth() / 16.0f;
            float contentHeight = mRowHeight;

            float x = ((e.getX() - mPosX) / mScaleFactor);
            float y = ((e.getY() - mPosY) / mScaleFactor);

            if (x<0 || y<0)
                return true;

            int beat = (int)(x/contentWidth);
            int channel = (int)(y/contentHeight);

            channel = indexToNote(channel);

            if (channel< 108 && beat<mPattern.GetLength()) {
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

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mPosX -= distanceX;
            mPosY -= distanceY;

            if (instrumentListener!=null) {
                instrumentListener.scaling(mPosX, mPosY, mScaleFactor, mRowHeight);
            }

            invalidate();
            return true;
        }
    };

    // The scale listener, used for handling multi-finger scale gestures.
    //
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScaleFator = mScaleFactor;
            mScaleFactor *= (detector.getScaleFactor()*detector.getScaleFactor());

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));

            float mFocusX = detector.getFocusX ();
            float mFocusY = detector.getFocusY ();

            //distance between focus and old origin
            float dx = mFocusX-mPosX;
            float dy = mFocusY-mPosY;
            //distance between focus and new origin after rescale
            float dxSc = dx * mScaleFactor / oldScaleFator;
            float dySc = dy * mScaleFactor / oldScaleFator;

            // calcul of the new origin
            mPosX = mFocusX - dxSc;
            mPosY = mFocusY - dySc;

            if (instrumentListener!=null) {
                instrumentListener.scaling(mPosX, mPosY, mScaleFactor, mRowHeight);
            }

            invalidate();
            return true;
        }
    };

    //-----------------------------------------------------

    // instrument touched listener
    //
    public interface InstrumentListener {
        boolean noteTouched(int note, int beat);
        void scaling(float x, float y, float scale, float mTrackHeight);
    }
    public PatternBaseView setInstrumentListener(InstrumentListener instrumentTouched) {
        this.instrumentListener = instrumentTouched;
        return this;
    }
    private InstrumentListener instrumentListener;

    //-----------------------------------------------------

    public Bitmap getBitmapFromView(int width, int height) {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layout(0, 0, width, height);
        mLOD = 1;
        mScaleFactor = -1;
        draw(canvas);
        mLOD = 0;
        return bitmap;
    }
}
