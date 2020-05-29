package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
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

    PatternMaster mMasterPattern = null;
    PatternBase mPattern = null;

    float diskX,diskY, diskRadius;

    enum ViewMode
    {
        MAIN,
        PIANO,
        CHORDS,
        DRUMS
    };

    enum GentureInProgress
    {
        idle,
        eventDragging,
        boxSelecting,
        diskDragging,
        patternPanning
    };

    GentureInProgress gestureInProgress = GentureInProgress.idle;

    ViewMode mViewMode;
    int mChannel = -1;
    PatternBase GetPattern() {return mMasterPattern;    }

    Viewport mViewport;
    TimeLine mTimeLine;
    void SetPattern(PatternMaster pattern, int channel, TimeLine timeLine, boolean selectable, ViewMode viewMode) {
        mViewMode = viewMode;
        mTimeLine = timeLine;
        mViewport = mTimeLine.mViewport;
        mSelectable = selectable;

        mMasterPattern = pattern;
        mChannel = channel;
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

            SetPattern(null, 0, timeLine,false, ViewMode.PIANO);
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

            //mTimeLine.mViewport.updateViewport();
        }
    }

    void centerViewInNotes()
    {
        int min = 88;
        int max = 0;
        for(int i=0;;i++) {
            Event note = mMasterPattern.GetNoteByIndex(i);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float contentHeight = getHeight();

        mColumnWidth = 1.0f;
        mLength = mMasterPattern.GetLength();
        mRowHeight = 1.0f;//contentHeight/ (float)mChannels;

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
        int columnLeft = mTimeLine.getLeftTick(1.0f/mViewport.getLod());
        int columnRight = mTimeLine.getRightTick(1.0f/mViewport.getLod());
        if (columnLeft<0) columnLeft = 0;
        if (columnRight>mLength*mViewport.getLod()) columnRight = (int)(mLength*mViewport.getLod());

        // Draw background
        //
        if (mLOD==0) {
            // horizontal tracks
            for (int i = iniTop; i < finBottom; i++) {

                RectF rf = new RectF();
                rf.left = 0;
                rf.right = mMasterPattern.GetLength();
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

                float x = i/mViewport.getLod();

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

        if (mChannel==-1) {
            RectF rectBackground = new RectF();
            rectBackground.top = 0;
            rectBackground.bottom = 8 * mRowHeight;
            rectBackground.left = 0;
            rectBackground.right = mMasterPattern.GetLength();
            mViewport.applyPosScaleRect(rectBackground);

            RectF rectMasterPattern = new RectF();
            rectMasterPattern.top = 0;
            rectMasterPattern.bottom = mMasterPattern.GetChannelCount();
            rectMasterPattern.left = 0;
            rectMasterPattern.right = mMasterPattern.GetLength();

            DrawMasterEvents(canvas, mMasterPattern, rectBackground, rectMasterPattern);
        } else {
            RectF rectBackground = new RectF();
            rectBackground.top = 0;
            rectBackground.bottom = 88 * mRowHeight;
            rectBackground.left = 0;
            rectBackground.right = mMasterPattern.GetLength();
            mViewport.applyPosScaleRect(rectBackground);

            RectF rectMasterPattern = new RectF();
            rectMasterPattern.top = 0;
            rectMasterPattern.bottom = 1;
            rectMasterPattern.left = 0;
            rectMasterPattern.right = mMasterPattern.GetLength();


            DrawChannelEvents(canvas, mMasterPattern, rectBackground, rectMasterPattern);
        }

        if (gestureInProgress==GentureInProgress.boxSelecting) {
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

    private void TransformRect(RectF rectParent, RectF rectChild, RectF rectEvent, RectF rectOut) {
        rectOut.top = Misc.map(rectEvent.top, rectChild.top, rectChild.bottom, rectParent.top, rectParent.bottom);
        rectOut.bottom = Misc.map(rectEvent.bottom, rectChild.top, rectChild.bottom, rectParent.top, rectParent.bottom);
        rectOut.left = Misc.map(rectEvent.left, rectChild.left, rectChild.right, rectParent.left, rectParent.right);
        rectOut.right = Misc.map(rectEvent.right, rectChild.left, rectChild.right, rectParent.left, rectParent.right);
    }

    private void EventToRect(Event event, RectF rectOut) {
        rectOut.top = event.mChannel;
        rectOut.bottom = event.mChannel + 1;
        rectOut.left = event.mTime;
        rectOut.right = event.mTime+event.mDuration;
    }

    private void PadRect(RectF rf, float padding) {
        rf.top -=padding;
        rf.bottom +=padding;
        rf.left -=padding;
        rf.right +=padding;
    }

    //------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------

    private void DrawChannelEvents(Canvas canvas, PatternMaster pb, RectF rectBackground, RectF rectMasterPattern) {

        canvas.drawRoundRect(rectBackground, 10, 10, green);

        RectF rf = new RectF();
        RectF rectEvent = new RectF();

        // draw icons
        for (int i = 0; ; i++) {
            Event event = pb.GetNoteByIndex(i);
            if (event == null)
                break;

            EventToRect(event, rectEvent);
            TransformRect(rectBackground, rectMasterPattern, rectEvent, rf);
            PatternBase pb2 = mMasterPattern.mPatternDataBase.get(event.mId);
            DrawEvents(canvas, pb2, rf);
        }
    }

    // draw events
    private void DrawEvents(Canvas canvas, PatternBase pb, RectF rectParent) {
        canvas.drawRoundRect(rectParent, 10, 10, green);

        RectF rf = new RectF();
        RectF rectEvent = new RectF();

        RectF rectChild = new RectF();
        rectChild.top = 0;
        rectChild.bottom = 88;
        rectChild.left = 0;
        rectChild.right = pb.GetLength();

        // draw selection rects
        for(Event selectedNote : mSelectedEvents) {
            EventToRect(selectedNote, rectEvent);
            TransformRect(rectParent, rectChild, rectEvent, rf);
            PadRect(rf, 5);
            canvas.drawRect(rf, selectedColor);
        }

        for (int i = 0; ; i++) {
            Event event = pb.GetNoteByIndex(i);
            if (event == null)
                break;

            EventToRect(event, rectEvent);
            TransformRect(rectParent, rectChild, rectEvent, rf);
            PadRect(rf, -5);
            canvas.drawRect(rf, greenFill);
        }

        //draw disk
        if (mSelectedEvents.size()==1) {

            EventToRect(mSelectedEvents.get(0), rectEvent);
            TransformRect(rectParent, rectChild, rectEvent, rf);

            //draw disk
            diskRadius = (rf.bottom - rf.top) / 4;
            diskX = rf.right + diskRadius * 1.1f;
            diskY = (rf.top + rf.bottom) / 2;

            canvas.drawCircle(diskX, diskY, diskRadius, white);
        }
    }

    //------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------

    private void DrawMasterEvents(Canvas canvas, PatternBase pb, RectF rectBackground, RectF rectMasterPattern) {

        canvas.drawRoundRect(rectBackground, 10, 10, green);

        RectF rf = new RectF();
        RectF rectEvent = new RectF();

        // draw selection rects
        for(Event selectedNote : mSelectedEvents) {
            EventToRect(selectedNote, rectEvent);
            TransformRect(rectBackground, rectMasterPattern, rectEvent, rf);
            canvas.drawRect(rf, selectedColor);
        }

        for (int i = 0; ; i++) {
            Event event = pb.GetNoteByIndex(i);
            if (event == null)
                break;

            EventToRect(event, rectEvent);
            TransformRect(rectBackground, rectMasterPattern, rectEvent, rf);
            PatternBase pb2 = mMasterPattern.mPatternDataBase.get(event.mId);
            DrawIcon(canvas, pb2, rf);
        }
    }

    // draw events
    private void DrawIcon(Canvas canvas, PatternBase pb, RectF rectParent) {

        canvas.drawRoundRect(rectParent, 10, 10, green);

        RectF rf = new RectF();
        RectF rectEvent = new RectF();

        RectF rectChild = new RectF();
        rectChild.top = 0;
        rectChild.bottom = pb.GetChannelCount();
        rectChild.left = 0;
        rectChild.right = pb.GetLength();

        if (mChannel==-1) {
            rectChild.top = pb.getMinChannel();
            rectChild.bottom = pb.getMaxChannel();
        } else {
            rectChild.top = 0;
            rectChild.bottom = 88;
        }

        for (int i = 0; ; i++) {
            Event event = pb.GetNoteByIndex(i);
            if (event == null)
                break;

            EventToRect(event, rectEvent);
            TransformRect(rectParent, rectChild, rectEvent, rf);
            canvas.drawRect(rf, greenFill);
        }
    }

    //------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------
    private boolean PatternCoords2ScreenCoords(PatternBase pb, float x, float y, PointF pointF) {
        // get global time
        switch(mViewMode) {
            case PIANO: {
                for (int i = 0; ; i++) {
                    Event event = mMasterPattern.GetNoteByIndex(i);
                    if (event == null)
                        break;

                    if (mMasterPattern.mPatternDataBase.get(event.mId) == pb) {
                        pointF.x = mViewport.applyPosScaleX(x + event.mTime);

                        float rowF = Misc.map(y, 0, 88, 0, 88 * mRowHeight);
                        pointF.y = mViewport.applyPosScaleY(rowF);
                        return true;
                    }
                }
            }
            case MAIN: {
                pointF.x = mViewport.applyPosScaleX(x);

                float rowF = Misc.map(y, 0, 8, 0, 8 * mRowHeight);
                pointF.y = mViewport.applyPosScaleY(rowF);
                return true;
            }
        }
        return false;
    }


    private boolean ScreenCoords2PatternCoords(float x, float y, PointF pointF) {

        float time = mViewport.removePosScaleX(x) / 1.0f;

        float rowF = mViewport.removePosScaleY(y);
        switch(mViewMode) {
            case PIANO: {
                rowF = Misc.map(rowF, 0, 88 * mRowHeight, 0, 88);

                Event event = mMasterPattern.get(mChannel, time);
                if (event == null)
                    return false;

                time -= event.mTime;
                mPattern = mMasterPattern.mPatternDataBase.get(event.mId);
                break;
            }
            case MAIN: {
                rowF = Misc.map(rowF, 0, 8 * mRowHeight, 0, 8);

                mPattern = mMasterPattern;
                break;
            }
            case CHORDS:
                break;
            case DRUMS:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mViewMode);
        }

        pointF.x = time;
        pointF.y = rowF;
        return true;
    }
    private MotionEvent ScreenCoords2PatternCoords(MotionEvent motionEvent) {
        PointF pointF = new PointF();
        if (ScreenCoords2PatternCoords(motionEvent.getX(), motionEvent.getY(), pointF)==false)
            return null;

        //snap to grid
        float snap = 1;
        float sx = (float)Math.floor(pointF.x/snap)*snap;
        float sy = (float)Math.floor(pointF.y);

        return MotionEvent.obtain(0, 0, motionEvent.getAction(), sx, sy, 0);
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
            EventToRect(note, rf);

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
    public boolean onDiskDraggedEvent(MotionEvent event, boolean bInit) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gestureInProgress == GentureInProgress.idle && bInit==true) {
                float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());
                boolean diskTouched = (Math.pow((event.getX() - diskX), 2) + Math.pow((event.getY() - diskY), 2)) <= diskRadius * diskRadius;
                if (diskTouched) {
                    eventDuration = thumbTime;
                    gestureInProgress = GentureInProgress.diskDragging;
                    return true;
                }
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (gestureInProgress == GentureInProgress.diskDragging) {
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
            if (gestureInProgress == GentureInProgress.diskDragging) {
                gestureInProgress = GentureInProgress.idle;
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------- update selection box
    PointF boxSelectionCorner1 = new PointF();
    PointF boxSelectionCorner2 = new PointF();
    RectF boxSelection = new RectF();
    RectF boxSelectionSorted = new RectF();
    RectF boxSelectionSortedTranslated = new RectF();
    public boolean onBoxSelection(MotionEvent event, boolean bInit) {
        float thumbTime = mTimeLine.getRoundedTimeFromScreen(event.getX());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gestureInProgress == GentureInProgress.idle && bInit==true) {
                gestureInProgress = GentureInProgress.boxSelecting;
                boxSelection.top = event.getY();
                boxSelection.left = event.getX();
                boxSelection.bottom = event.getY() + 10;
                boxSelection.right = event.getX() + 10;
                boxSelectionSorted.set(boxSelection);
                boxSelectionSorted.sort();
                ScreenCoords2PatternCoords(boxSelectionSorted.left, boxSelectionSorted.top, boxSelectionCorner1);
                ScreenCoords2PatternCoords(boxSelectionSorted.right, boxSelectionSorted.bottom, boxSelectionCorner2);
                boxSelectionSortedTranslated.set(boxSelectionCorner1.x, boxSelectionCorner1.y, boxSelectionCorner2.x, boxSelectionCorner2.y);
                selectRect(boxSelectionSortedTranslated);
                invalidate();
                return true;
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (gestureInProgress == GentureInProgress.boxSelecting) {
                boxSelection.bottom = event.getY();
                boxSelection.right = event.getX();
                boxSelectionSorted.set(boxSelection);
                boxSelectionSorted.sort();
                ScreenCoords2PatternCoords(boxSelectionSorted.left, boxSelectionSorted.top, boxSelectionCorner1);
                ScreenCoords2PatternCoords(boxSelectionSorted.right, boxSelectionSorted.bottom, boxSelectionCorner2);
                boxSelectionSortedTranslated.set(boxSelectionCorner1.x, boxSelectionCorner1.y, boxSelectionCorner2.x, boxSelectionCorner2.y);
                selectRect(boxSelectionSortedTranslated);
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (gestureInProgress == GentureInProgress.boxSelecting) {
                gestureInProgress = GentureInProgress.idle;
                selectRect(boxSelectionSortedTranslated);
                invalidate();
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------- drag notes
    public boolean onDragEvents(MotionEvent event, boolean bInit) {

        if (instrumentListener != null) {
            MotionEvent ev2 = ScreenCoords2PatternCoords(event);
            switch(event.getAction())
            {
                case  MotionEvent.ACTION_DOWN: {
                    if (gestureInProgress == GentureInProgress.idle && bInit == true) {
                        if (instrumentListener.onMoveSelectedEvents(ev2)) {
                            gestureInProgress = GentureInProgress.eventDragging;
                            return true;
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (gestureInProgress == GentureInProgress.eventDragging) {
                        if (instrumentListener.onMoveSelectedEvents(ev2)) {
                            return true;
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    if (gestureInProgress == GentureInProgress.eventDragging) {
                        if (instrumentListener.onMoveSelectedEvents(ev2)) {
                            gestureInProgress = GentureInProgress.idle;
                            return true;
                        }
                    }
                    break;
                }
            }
        }

        return false;
    }

    // -------------------------------------------- drag notes
/*
    float distanceX, distanceY;
    public boolean onDragBackGround(MotionEvent event, boolean bInit) {

        //Log.d("MouseEvent", ""+MotionEvent.ACTION_DOWN);
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (gestureInProgress == GentureInProgress.idle && bInit == true) {
                    gestureInProgress = GentureInProgress.patternPanning;

                    distanceX = event.getX();
                    distanceY = event.getY();
                    mViewport.onDrag(0, 0);

                    if (instrumentListener != null) {
                        instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
                    }
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (gestureInProgress == GentureInProgress.patternPanning) {
                    distanceX = distanceX - event.getX();
                    distanceY = distanceY - event.getY();
                    mViewport.onDrag(distanceX, distanceY);
                    distanceX = event.getX();
                    distanceY = event.getY();

                    if (instrumentListener != null) {
                        instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
                    }
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (gestureInProgress == GentureInProgress.patternPanning) {
                    gestureInProgress = GentureInProgress.idle;

                    distanceX = distanceX - event.getX();
                    distanceY = distanceY - event.getY();
                    mViewport.onDrag(distanceX, distanceY);

                    if (instrumentListener != null) {
                        instrumentListener.scaling(mViewport.mPosX, mViewport.mPosY, mViewport.mScaleX, mRowHeight);
                    }
                    return true;
                }
                break;
            }
        }

        return false;
    }
*/

    public boolean onTouchEvent(MotionEvent event) {

        MotionEvent ev2 = ScreenCoords2PatternCoords(event);
        if (ev2!=null) {
            // dragging disk
            if (onDiskDraggedEvent(event, false)) {
                invalidate();
                return true;
            }

            // LongPress enables box selection
            if (onBoxSelection(event, false)) {
                invalidate();
                return true;
            }

            if (onDragEvents(event, false)) {
                invalidate();
                return true;
            }
        }

        if (event.getAction() ==  MotionEvent.ACTION_UP) {
            if (gestureInProgress == GentureInProgress.patternPanning) {
                gestureInProgress = GentureInProgress.idle;
            }
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

            MotionEvent ev = ScreenCoords2PatternCoords(event);
            if (ev!=null) {
                if (instrumentListener != null) {
                    return instrumentListener.noteTouched(ev);
                }
            }

            return false;
        }

        @Override
        public boolean onDoubleTap (MotionEvent event)
        {
            MotionEvent ev = ScreenCoords2PatternCoords(event);
            if (ev!=null) {
                if (instrumentListener != null) {
                    return instrumentListener.onDoubleTap(ev);
                }
            }

            return false;
        }

        @Override
        public void onLongPress (MotionEvent event)
        {
            MotionEvent ev = ScreenCoords2PatternCoords(event);
            if (ev!=null) {
                // long press on background -> square selection
                if (mMasterPattern.get((int) ev.getY(), ev.getX()) == null) {
                    if (onBoxSelection(event, true)) {
                        return;
                    }
                }

                //otherwise pass to activity
                if (instrumentListener != null) {
                    if (instrumentListener.longPress(ev))
                        return;
                }
            }
        }

        @Override
        public boolean onDown(MotionEvent event) {
            mViewport.onDown(event);
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

            MotionEvent ev2 = ScreenCoords2PatternCoords(e1);
            if (ev2!=null) {
                if (onDiskDraggedEvent(e1, true)) {
                    invalidate();
                    return true;
                }

                if (onDragEvents(e1, true)) {
                    invalidate();
                    return true;
                }
            }
            //background panning
            gestureInProgress = GentureInProgress.patternPanning;
            mViewport.onDrag(distanceX, distanceY);
            if (instrumentListener != null) {
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
