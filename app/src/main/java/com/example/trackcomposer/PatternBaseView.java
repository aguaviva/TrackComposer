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

import java.util.ArrayList;
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
    Paint selectionLine;
    private float mCurrentBeat = 0;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int mContentWidth;
    private int mContentHeight;

    HashMap<Integer, Bitmap> mPatternImgDataBase = null;

    boolean mSelectable = false;

    private ArrayList<Event> mSelectedEvents = new ArrayList<Event>();

    float mRowHeight = 0;
    float mColumnWidth = 0;
    int mChannels = 0;
    float mLength = 0;

    PatternBase mPattern = null;

    float diskX,diskY, diskRadius;

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

        mSelectedEvents.clear();

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

        selectionLine = new Paint();
        selectionLine.setColor(ContextCompat.getColor(getContext(), R.color.selectionLine));
        selectionLine.setStyle(Paint.Style.STROKE);
        selectionLine.setStrokeWidth(2);

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
            if (note.mChannel >max)
                max = note.mChannel;
            if (note.mChannel <min)
                min = note.mChannel;
        }



        if (mViewMode == ViewMode.PIANO) {

            mChannels = max - min +1;
            if (max == 0) {
                max = (40 + 12) -1;
                mChannels = 12;
            }
        }
        else if (mViewMode == ViewMode.DRUMS) {
            if (mChannels < 8) {
                mChannels = 8;
                max = 0;
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


    void EventToRect(Event event, RectF rf, float padding)
    {
        float x1 = event.mTime * (mTimeLine.getTickWidth());
        float x2 = (event.mTime + event.mDuration) * (mTimeLine.getTickWidth());

        int y = indexToNote(event.mChannel);

        rf.left = x1 + padding;
        rf.top = y * mRowHeight + padding;
        rf.right = x2 - padding;
        rf.bottom = (y + 1) * mRowHeight - padding;
        mViewport.applyPosScaleRect(rf);
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
        canvas.save();
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

        canvas.restore();

        //show selected block
        //if (mLOD==0 && mSelectable) {
        if (mLOD==0) {
            for(Event selectedNote : mSelectedEvents) {
                RectF rf = new RectF();
                EventToRect(selectedNote, rf, 0);
                canvas.drawRect(rf, selectedColor);
            }
/*
            //draw disk
            diskRadius = (rf.bottom- rf.top)/4;
            diskX = rf.right+diskRadius*1.1f;
            diskY = (rf.top+rf.bottom)/2;

            canvas.drawCircle(diskX ,diskY, diskRadius, white) ;
 */
        }

        // draw events
        for(int i=0;;i++) {
            Event note = mPattern.GetNoteByIndex(i);
            if (note==null)
                break;

            RectF rf = new RectF();
            Integer id = note.mId;
            if (mPatternImgDataBase!=null && mPatternImgDataBase.containsKey(id)) {
                Bitmap bmp = mPatternImgDataBase.get(id);
                if (bmp!=null) {
                    EventToRect(note, rf, 0);
                    canvas.drawBitmap(bmp, null, rf, bitmapPaint);
                    canvas.drawRoundRect( rf, 10,10, green);
                }
            }
            else {

                EventToRect(note, rf, (mLOD==0)?2:1);
                canvas.drawRect(rf, greenFill);
            }
        }

        if (boxSelectingInProgress) {
            canvas.drawRect(boxSelectionSorted, selectionLine);
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

    private MotionEvent TranslateEvent(MotionEvent event) {
        float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());
        int row = (int) (mViewport.removePosScaleY(event.getY()) / mRowHeight);
        row = indexToNote(row);
        return MotionEvent.obtain(0, 0, event.getAction(), thumbTime, row, 0);
    }

    //--------------------------------selection

    public void selectClear() {
        mSelectedEvents.clear();
    }

    public void selectSingleEvent(Event event) {
        selectClear();
        mSelectedEvents.add(event);
    }

    public boolean isEventSelected(Event event) {
        for(Event selectedEvent : mSelectedEvents) {
            if (selectedEvent==event)
                return true;
        }
        return false;
    }


    public int  selectItemCount() {
        return mSelectedEvents.size();
    }

    public void selectRect(RectF rectSelection) {
        mSelectedEvents.clear();

        for (int i = 0; ; i++) {
            Event note = mPattern.GetNoteByIndex(i);
            if (note == null)
                break;
            //*ticksPerColumn;
            RectF rf = new RectF();
            EventToRect(note, rf, 0);

            if (RectF.intersects(rectSelection, rf)) {
                mSelectedEvents.add(note);
            }
        }
    }

    public void selectMove(float x, int y) {
        for(Event selectedNote : mSelectedEvents) {
            selectedNote.mTime += x;
            selectedNote.mChannel += y;
        }
        mPattern.sortEvents();
    }

    // --------------------------------------------

    float eventDuration;
    boolean boxDraggingDiskInProgress = false;
    public boolean onDiskDraggedEvent(MotionEvent event, boolean bInit) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (bInit==true) {
                float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());
                boolean diskTouched = (Math.pow((event.getX() - diskX), 2) + Math.pow((event.getY() - diskY), 2)) <= diskRadius * diskRadius;
                if (diskTouched) {
                    eventDuration = thumbTime;
                    boxDraggingDiskInProgress = true;
                    return true;
                }
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (boxDraggingDiskInProgress) {
                float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());
                for(Event selectedNote : mSelectedEvents) {
                    float duration = selectedNote.mDuration + (thumbTime - eventDuration);
                    if (duration > 0) {
                        selectedNote.mDuration = duration;
                        eventDuration = thumbTime;
                    }
                }
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (boxDraggingDiskInProgress) {
                boxDraggingDiskInProgress = false;
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------- update selection box
    RectF boxSelection = new RectF();
    RectF boxSelectionSorted = new RectF();
    boolean boxSelectingInProgress = false;
    public boolean onBoxSelection(MotionEvent event, boolean bInit) {
        float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (bInit==true) {
                boxSelectingInProgress = true;
                boxSelection.top = event.getY();
                boxSelection.left = event.getX();
                boxSelection.bottom = event.getY() + 10;
                boxSelection.right = event.getX() + 10;
                boxSelectionSorted.set(boxSelection);
                boxSelectionSorted.sort();
                selectRect(boxSelectionSorted);
                invalidate();
                return true;
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (boxSelectingInProgress) {
                boxSelection.bottom = event.getY();
                boxSelection.right = event.getX();
                boxSelectionSorted.set(boxSelection);
                boxSelectionSorted.sort();
                selectRect(boxSelectionSorted);
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (boxSelectingInProgress) {
                boxSelectingInProgress = false;
                invalidate();
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------- drag notes
    boolean boxDraggingEventsInProgress = false;
    public boolean onDragEvents(MotionEvent event, boolean bInit) {

        if (instrumentListener != null) {
            MotionEvent ev2 = TranslateEvent(event);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (bInit==true) {
                    if (instrumentListener.onMoveSelectedEvents(ev2)) {
                        boxDraggingEventsInProgress = true;
                        return true;
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (boxDraggingEventsInProgress==true) {
                    if (instrumentListener.onMoveSelectedEvents(ev2)) {
                        return true;
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (boxDraggingEventsInProgress==true) {
                    if (instrumentListener.onMoveSelectedEvents(ev2)) {
                        boxDraggingEventsInProgress = false;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {

        // dragging disk
        if (onDiskDraggedEvent(event, false)) {
           return true;
        }

        // LongPress enables box selection
        if (onBoxSelection(event, false)) {
            return true;
        }

        if (onDragEvents(event, false)) {
            return true;
        }

        //  no other handlers, then drag & scale
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
            selectClear();

            MotionEvent ev = TranslateEvent(event);

            if (instrumentListener!=null) {
                return instrumentListener.noteTouched(ev);
            }

            return false;
        }

        @Override
        public boolean onDoubleTap (MotionEvent event)
        {
            MotionEvent ev = TranslateEvent(event);

            if (instrumentListener!=null) {
                return instrumentListener.onDoubleTap(ev);
            }

            return false;
        }

        @Override
        public void onLongPress (MotionEvent event)
        {
            MotionEvent ev = TranslateEvent(event);

            if (instrumentListener!=null) {
                if (instrumentListener.longPress(ev))
                    return;
            }

            if (onBoxSelection(event, true)) {
                return;
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

            /*
            if (onDiskDraggedEvent(e1, true)) {
                return true;
            }
            */
            if (onDragEvents(e1, true)) {
                return true;
            }

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
        boolean onMoveSelectedEvents(MotionEvent event);
        boolean noteTouched(MotionEvent event);
        boolean longPress(MotionEvent event);
        boolean onDoubleTap(MotionEvent event);
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
