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
    Paint bitmapPaint;
    private float mCurrentBeat = 0;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int mContentWidth;
    private int mContentHeight;

    HashMap<Integer, Bitmap> mPatternImgDataBase = null;

    boolean mSelectable = false;

    public Event selectedNote = null;

    float mRowHeight = 0;
    float mColumnWidth = 0;
    int mChannels = 0;
    float mLength = 0;

    PatternBase mPattern = null;

    enum ViewMode
    {
        MAIN,
        PIANO,
        CHORDS,
        DRUMS
    };

    ViewMode mViewMode;

    PatternBase GetPattern() {
        return mPattern;
    }

    Viewport mViewport;
    TimeLine mTimeLine;
    void SetPattern(PatternBase pattern, TimeLine timeLine, boolean selectable, ViewMode viewMode) {
        mViewMode = viewMode;
        mTimeLine = timeLine;
        mViewport = mTimeLine.mViewport;
        mSelectable = selectable;

        mPattern = pattern;

        if (mViewMode == ViewMode.PIANO)
        {
            bInvertY = true;
        }

        pattern.SetBeatListener(new PatternBase.BeatListener() {
            @Override
            public void beat(float currentBeat) {
                mCurrentBeat = currentBeat;
                invalidate();
            }
        });
    }

    void patternImgDataBase(HashMap<Integer, Bitmap> patternImgDataBase) {
        mPatternImgDataBase = patternImgDataBase;
    }

    public PatternBaseView(Context context) {
        super(context);
        init(null, 0);
    }

    public PatternBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode())
        {
            PatternPianoRoll pattern = new PatternPianoRoll("caca","caca",16, 16);

            TimeLine timeLine = new TimeLine();
            timeLine.init(pattern, 64);
            timeLine.setViewSize(getWidth(), getHeight());

            SetPattern(pattern, timeLine,false, ViewMode.PIANO);
        }

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

        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(false);

        ltgray = new Paint();
        ltgray.setColor(Color.LTGRAY);
        ltgray.setStyle(Paint.Style.FILL);

        dkgray = new Paint();
        dkgray.setColor(Color.DKGRAY);
        dkgray.setStyle(Paint.Style.FILL);

        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);

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

    public void setBaseNote(int baseNote) {
        mBaseNote = baseNote;
    }

    int indexToNote(int y) {
        return (bInvertY) ? (88 - y) : y;
    }

    public void setCurrentBeat(float currentBeat) {
        mCurrentBeat = currentBeat;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        if (mTimeLine!=null) {
            mTimeLine.setViewSize(getWidth(), getHeight());

            centerViewInNotes();

            mTimeLine.mViewport.updateViewport();
        }
    }

    void centerViewInNotes()
    {
        int min = 88;
        int max = 0;
        for(int i=0;;i++) {
            Event note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;
            if (note.channel>max)
                max = note.channel;
            if (note.channel<min)
                min = note.channel;
        }



        if (mViewMode == ViewMode.PIANO) {

            mChannels = max - min +1;
            if (max == 0) {
                max = (40 + 12) -1;
                mChannels = 12;
            }
        }
        else if (mViewMode == ViewMode.MAIN) {
            if (mChannels < 8) {
                mChannels = 8;
                max = 0;
            }
        }
        else if (mViewMode == ViewMode.CHORDS)
        {
            mChannels = 3*4;
            max = 0;
        }

        mRowHeight = getHeight()/ (float)mChannels;

        max = indexToNote(max);
        mViewport.mPosY = (-max)* mRowHeight;

        if (instrumentListener!=null) {
            instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float contentHeight = getHeight();

        mColumnWidth = mTimeLine.getTickWidth();
        mLength = mPattern.GetLength();
        mRowHeight = contentHeight/ (float)mChannels;

        // Set canvas zoom and pan
        //
        canvas.translate(mViewport.mPosX, mViewport.mPosY);
        canvas.scale(mViewport.mScaleX, mViewport.mScaleY);

        // channels
        int iniTop = (int)Math.floor(mViewport.mRect.top / mRowHeight);
        int finBottom = (int)Math.ceil(mViewport.mRect.bottom / mRowHeight);
        if (iniTop<0) iniTop = 0;
        if (finBottom>88) finBottom = 88;

        // ticks
        int columnLeft = mTimeLine.getLeftTick(mTimeLine.getTickWidth()/mViewport.getLod());
        int columnRight = mTimeLine.getRightTick(mTimeLine.getTickWidth()/mViewport.getLod());
        if (columnLeft<0) columnLeft = 0;
        if (columnRight>mLength*mViewport.getLod()) columnRight = (int)(mLength*mViewport.getLod());

        // Draw background
        //
        if (mLOD==0) {

            float endTime =  (mTimeLine.getLength() * mTimeLine.getTickWidth());

            // horizontal tracks
            for (int i = iniTop; i < finBottom; i++) {

                RectF rf = new RectF();
                rf.left = 0;
                rf.right = endTime;
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

            //show selected block
            if (mSelectable && selectedNote!=null) {
                float x = selectedNote.time;
                float y = indexToNote(selectedNote.channel);

                RectF rf = new RectF();
                rf.left = selectedNote.time*mColumnWidth;
                rf.right = (selectedNote.time + selectedNote.durantion)*mColumnWidth;
                rf.top = y * mRowHeight;
                rf.bottom = (y + 1) * mRowHeight;
                canvas.drawRect(rf, selectedColor);
            }

            //vertical lines
            float yTop = iniTop * mRowHeight;
            float yBottom = finBottom * mRowHeight;

            for (int i=columnLeft;i<columnRight;i++) {

                float x = i* mTimeLine.getTickWidth()/mViewport.getLod();

                if (((i%16)==0)) {
                    canvas.drawLine(x, yTop, x, yBottom, white);
                }
                else if (i%4==0) {
                    canvas.drawLine(x, yTop, x, yBottom, ltgray);
                }
            }

            // Draw cursor
            canvas.drawRect((mCurrentBeat * mColumnWidth), 0, ((mCurrentBeat + 1) * mColumnWidth), yBottom, blue);
        }

        // show blocks
        for(int i=0;;i++) {
            Event note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;
            //*ticksPerColumn;
            float x1 = note.time*(mTimeLine.getTickWidth());
            float x2 = (note.time + note.durantion)*(mTimeLine.getTickWidth());

            int y = indexToNote(note.channel);

            int padTL = (mLOD==0)?2:1;
            int padDR = (mLOD==0)?2:1;

            RectF rf = new RectF();
            rf.left = x1 + padTL;
            rf.top = y* mRowHeight + padTL;
            rf.right = x2 - padDR;
            rf.bottom = (y+1)* mRowHeight - padDR;

            Integer id = note.mGen.sampleId;
            if (mPatternImgDataBase!=null && mPatternImgDataBase.containsKey(id)) {
                Bitmap bmp = mPatternImgDataBase.get(id);
                if (bmp!=null) {
                    rf.left = x1;
                    rf.top =  y* mRowHeight;
                    rf.right = x2;
                    rf.bottom = (y+1)* mRowHeight;

                    canvas.drawBitmap(bmp, null, rf, bitmapPaint);
                    canvas.drawRoundRect( rf, 10,10, green);
                }
            }
            else {
                canvas.drawRect(rf, greenFill);
            }
        }

        // spring to center the track
        //
        if (mViewport.springToScreen())
        {
            if (instrumentListener != null) {
                instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
            }
            invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        if (instrumentListener != null) {

            float thumbTime = mTimeLine.getTimeFromScreen(event.getX());
            int row = (int)(mViewport.removePosScaleY(event.getY())/mRowHeight);
            row = indexToNote(row);
            MotionEvent ev2 = MotionEvent.obtain(0, 0, event.getAction(), thumbTime, row, 0);
            if (instrumentListener.onTouchEvent(ev2))
                return true;

        }


        boolean b1 =  mScaleGestureDetector.onTouchEvent(event);
        boolean b2 = mGestureDetector.onTouchEvent(event);
        return b1 || b2;
    }

    public void SetCurrentBeatCursor(int currentBeat) {
        this.mCurrentBeat =currentBeat;
        postInvalidate();
    }

    //  Pan & zoom gestures -----------------------------------------------------
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private final GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp (MotionEvent event)
        {
            float thumbTime = mTimeLine.getTimeFromScreen(event.getX());
            int row = (int)(mViewport.removePosScaleY(event.getY())/mRowHeight);
            row = indexToNote(row);

            if (instrumentListener!=null) {
                return instrumentListener.noteTouched(row, thumbTime);
            }

            return false;
        }

        @Override
        public void onLongPress (MotionEvent event)
        {
            float thumbTime = mTimeLine.getTimeFromScreen(event.getX());
            int row = (int)(mViewport.removePosScaleY(event.getY())/mRowHeight);
            row = indexToNote(row);

            if (instrumentListener!=null) {
                instrumentListener.longPress(row, thumbTime);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mViewport.onDown(e);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            mViewport.onFling(velocityX/100.0f, velocityY/100.0f);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            mViewport.onDrag(distanceX, distanceY);

            if (instrumentListener!=null) {
                instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
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

            float scale = detector.getScaleFactor();
            float currSpanX = detector.getCurrentSpanX();
            float currSpanY = detector.getCurrentSpanY();

            float scaleX = scale;
            float scaleY = scale;

            if (currSpanY>currSpanX*2)
                scaleX=1;

            if (currSpanX>currSpanY*2)
                scaleY=1;

            mViewport.onScale(detector.getFocusX(), detector.getFocusY(), scaleX, scaleY);

            if (instrumentListener!=null) {
                instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
            }

            invalidate();
            return true;
        }
    };

    //-----------------------------------------------------

    // instrument touched listener
    //
    public interface InstrumentListener {
        boolean onTouchEvent(MotionEvent event);
        boolean noteTouched(int rowSelected, float time);
        void longPress(int rowSelected, float time);
        void scaling(float x, float y, float scale, float mTrackHeight);
    }
    public void  setInstrumentListener(InstrumentListener instrumentTouched) {
        this.instrumentListener = instrumentTouched;
    }
    private InstrumentListener instrumentListener;

    //-----------------------------------------------------

    public Bitmap getBitmapFromView(int width, int height) {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layout(0, 0, width, height);
        centerViewInNotes();
        mLOD = 1;
        draw(canvas);
        mLOD = 0;
        return bitmap;
    }
}
