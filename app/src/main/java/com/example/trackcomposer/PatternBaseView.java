package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
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

    Point selected = null;

    float mRowHeight = 0;
    float mColumnWidth = 0;
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

    TimeLine mTimeLine;
    public void init(PatternBase pattern, TimeLine timeLine)
    {
        mTimeLine = timeLine;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mTimeLine.setViewSize(getWidth(), getHeight());
        mTimeLine.updateViewport();

        float contentHeight = getHeight();

        PatternBase mPattern = GetPattern();
        int ticksPerTrack = mPattern.length;

        float columnsPerCanvasWidth = 16; // at zoom 1
        float ticksPerColumn = 1;

        float columns = ticksPerTrack/ticksPerColumn;
        float distanceBetweenTicks = getWidth()/(columnsPerCanvasWidth*ticksPerColumn);

        mColumnWidth = mTimeLine.getTickWidth();//getWidth() / columnsPerCanvasWidth;
/*
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

            if (bInvertY) {
                if (max == 0) {
                    max = (40 + 12) -1;
                    mChannels = 12;
                }
            }
            else {
                if (mChannels < 8) {
                    mChannels = 8;
                    max = 0;
                }
            }
            mRowHeight = contentHeight/ (float)mChannels;

            max = indexToNote(max);
            mPosY = (-max)* mRowHeight;


            if (instrumentListener!=null) {
                instrumentListener.scaling(mPosX, mPosY, mScaleFactor, mRowHeight);
            }
        }
*/
        mChannels = 8;
        mRowHeight = contentHeight/ (float)mChannels;


        // Set canvas zoom and pan
        //
        canvas.translate(mTimeLine.mPosX, mTimeLine.mPosY);
        canvas.scale(mTimeLine.mScaleFactor, mTimeLine.mScaleFactor);

        float z = (float)(Math.log(mTimeLine.mScaleFactor)/Math.log(2));
        z = (float)Math.pow(2, Math.floor(z));

        // channels
        int iniTop = (int)Math.floor(mTimeLine.mViewport.top / mRowHeight);
        int finBottom = (int)Math.ceil(mTimeLine.mViewport.bottom / mRowHeight);
        if (iniTop<0) iniTop = 0;
        if (finBottom>88) finBottom = 88;

        // ticks
        int columnLeft = mTimeLine.getLeftTick(mTimeLine.getTickWidth()/z);
        int columnRight = mTimeLine.getRightTick(mTimeLine.getTickWidth()/z);
        if (columnLeft<0) columnLeft = 0;
        if (columnRight>columns*z) columnRight = (int)(columns*z);

        // Draw background
        //
        if (mLOD==0) {

            // horizontal tracks
            for (int i = iniTop; i < finBottom; i++) {

                RectF rf = new RectF();
                rf.left = 0;
                rf.right = columnRight* mTimeLine.getTickWidth()/z;
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
            if (mSelectable && selected!=null) {
                int x = selected.x;
                int y = indexToNote(selected.y);

                RectF rf = new RectF();
                rf.left = x*mColumnWidth;
                rf.right = (x+1)*mColumnWidth;
                rf.top = y * mRowHeight;
                rf.bottom = (y + 1) * mRowHeight;
                canvas.drawRect(rf, selectedColor);
            }


            //vertical lines
            float yTop = iniTop * mRowHeight;
            float yBottom = finBottom * mRowHeight;

            for (int i=columnLeft;i<columnRight;i++) {

                float x = i* mTimeLine.getTickWidth()/z;

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
            SortedListOfNotes.Note note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;
            //*ticksPerColumn;
            float x1 = note.time*distanceBetweenTicks;
            float x2 = (note.time + note.durantion)*distanceBetweenTicks;
            int y = indexToNote(note.channel);

            int padTL = (mLOD==0)?2:1;
            int padDR = (mLOD==0)?2:0;

            RectF rf = new RectF();
            rf.left = x1 + padTL;
            rf.top = y* mRowHeight + padTL;
            rf.right = x2 - padDR;
            rf.bottom = (y+1)* mRowHeight - padDR;

            Integer id = note.mGen.sampleId;
            if (mPatternImgDataBase!=null && mPatternImgDataBase.containsKey(id)) {
                Bitmap bmp = mPatternImgDataBase.get(id);
                if (bmp!=null) {
                    canvas.drawBitmap(bmp, null, rf, null);

                    rf.left = x1;
                    rf.top =  y* mRowHeight;
                    rf.right = x2;
                    rf.bottom = (y+1)* mRowHeight;

                    canvas.drawRoundRect( rf, 10,10, green);
                }
            }
            else {
                canvas.drawRect(rf, greenFill);
            }
        }

        // spring to center the track
        //
        if (mTimeLine.mPosX>0 ) {
            mTimeLine.mPosX += (0 - mTimeLine.mPosX) * .1;
        }
        if (mTimeLine.mPosY>0) {
            mTimeLine.mPosY += (0 - mTimeLine.mPosY) * .1;
        }

        if (mTimeLine.mPosX>0 || mTimeLine.mPosY>0) {
            if (instrumentListener!=null) {
                instrumentListener.scaling(mTimeLine.mPosX, mTimeLine.mPosY, mTimeLine.mScaleFactor, mRowHeight);
            }
            invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean b1 =  mScaleGestureDetector.onTouchEvent(event);
        boolean b2 = mGestureDetector.onTouchEvent(event);
        return b1 || b2;
    }

    public void onTouchEvent(int x, int y) {
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

    public void SetCurrentBeatCursor(int currentBeat) {
        this.mCurrentBeat =currentBeat;
        postInvalidate();
    }

    //  Pan & zoom gestures -----------------------------------------------------
    Point ToPattern(PointF point) {
        if (point.x<0 || point.y<0)
            return null;

        PatternBase mPattern = GetPattern();

        float contentWidth = getWidth() / 16.0f;
        float contentHeight = mRowHeight;

        int beat = (int)(point.x/contentWidth);
        int channel = (int)(point.y/contentHeight);

        channel = indexToNote(channel);

        if ((channel >= 108) || (beat>=mPattern.GetLength()))
            return null;

        return new Point(beat, channel);
    }

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private final GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {

        PointF point = new PointF();

        protected float mPosX;
        protected float mPosY;
        protected float mScaleFactor = -1.0f;

        @Override
        public boolean onSingleTapUp (MotionEvent e)
        {
            mTimeLine.removePosScale(e.getX(), e.getY(), point);
            if (point==null)
                return false;

            Point p = ToPattern(point);

            if (mSelectable) {
                selected = p;
            }

            boolean bProcessTouch = false;

            if (instrumentListener!=null) {
                bProcessTouch = instrumentListener.noteTouched(p.y, p.x);
            }

            if (bProcessTouch)
                onTouchEvent(p.y, p.x);

            invalidate();
            return true;
        }

        @Override
        public void onLongPress (MotionEvent e)
        {
            mTimeLine.removePosScale(e.getX(), e.getY(), point);
            Point p = ToPattern(point);
            if (p==null)
                return;

            if (instrumentListener!=null) {
                instrumentListener.longPress(p, point);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            mTimeLine.onDrag(distanceX, distanceY);

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

        mTimeLine.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());

        if (instrumentListener!=null) {
            instrumentListener.scaling(mTimeLine.mPosX, mTimeLine.mPosY, mTimeLine.mScaleFactor, mRowHeight);
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
        void longPress(Point p, PointF pf);
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
        mLOD = 1;
        mTimeLine.mScaleFactor = -1;
        draw(canvas);
        mLOD = 0;
        return bitmap;
    }
}
