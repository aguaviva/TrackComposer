package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityPianoRoll extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternBaseView mNoteView;
    Context mContext;
    PatternHeaderView mPatternHeaderView;
    PatternPianoRoll patternPianoRoll;
    TimeLine mTimeLine = new TimeLine();
    TimeLineView timeLineView;
    WidgetVcrControl mWidgetVcrControl;
    CheckBox mCanEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pianoroll);

        //main_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Piano roll");

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.left_side);
        linearLayout.addView(getLayoutInflater().inflate(R.layout.edit_checkbox, null));
        mCanEdit = (CheckBox) findViewById(R.id.edit_checkbox);

        mContext = getApplicationContext();

        mAppState = ((ApplicationClass)this.getApplication());

        float timeBegin = getIntent().getFloatExtra("PATTERN_TIME_BEGIN",0);
        float timeEnd = getIntent().getFloatExtra("PATTERN_TIME_END",256);
        int channel = getIntent().getIntExtra("PATTERN_CHANNEL", -1);
        int patternId = getIntent().getIntExtra("PATTERN_ID",-1);

        patternPianoRoll = (PatternPianoRoll)mAppState.mPatternMaster.mPatternDataBase.get(patternId);

        mTimeLine.init(mAppState.mPatternMaster, 16); // at scale 1, draw 1 vertical line every tick
        mTimeLine.setTimeSpan(timeBegin,timeEnd);
        int min = patternPianoRoll.getMinChannel();
        int max = patternPianoRoll.getMaxChannel();
        if (max-min<=0) { min = 40; max = 64;}
        mTimeLine.mViewport.setSpanVertical(min, max);
        mTimeLine.mViewport.setLimits(0,0,256,88);

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(mAppState.mPatternMaster, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
                mAppState.mPatternMaster.setTime(time);
                mNoteView.invalidate();
            }
            @Override
            public void onPatternEnd(float time)
            {
                mNoteView.GetPattern().SetLength(time);
                mNoteView.invalidate();
            }
        });

        //
        mPatternHeaderView = (PatternHeaderView)findViewById(R.id.patternHeaderView);
        mPatternHeaderView.SetPattern(mTimeLine, patternPianoRoll.mChannels, patternPianoRoll.GetLength(),true);
        mPatternHeaderView.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) {}
            @Override
            public void actionMove(int y)
            {
                patternPianoRoll.mBaseNote +=y;
                mNoteView.setBaseNote(patternPianoRoll.mBaseNote);
                mNoteView.invalidate();
            }
            @Override
            public String getInstrumentName(int n)
            {
                return Misc.getNoteName(n);
            }
        });

        //
        mNoteView = (PatternBaseView)findViewById(R.id.noteView);
        mNoteView.SetPattern(mAppState.mPatternMaster, channel, mTimeLine,false,PatternBaseView.ViewMode.PIANO);
        mNoteView.setBaseNote(patternPianoRoll.mBaseNote);
        mNoteView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            Event noteDown;
            float orgTime = 0;
            int orgChannel = 0;
            @Override
            public boolean onMoveSelectedEvents(MotionEvent event) {
                int rowSelected = (int)event.getY();
                float time = event.getX();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    noteDown = patternPianoRoll.get(rowSelected, time);
                    if (noteDown!=null) {
                        orgTime = time;
                        orgChannel = rowSelected;

                        if (mNoteView.isEventSelected(noteDown)) {
                        } else {
                            mNoteView.selectClear();
                            mNoteView.selectSingleEvent(noteDown);
                        }
                        return true;
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (noteDown!=null)
                    {
                        float deltaTime = time - orgTime;
                        int deltaChannel = rowSelected - orgChannel;

                        mNoteView.selectMove(deltaTime, deltaChannel);

                        orgTime = time;
                        orgChannel = rowSelected;

                        if (deltaChannel!=0) {
                            patternPianoRoll.play(noteDown);
                        }
                        mNoteView.invalidate();
                        return true;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    noteDown = null;
                    return true; //prevent other handlers from using this motion
                }

                return false;
            }
            @Override
            public void scaling(float x, float y, float scale, float trackHeight)
            {
                timeLineView.init(mAppState.mPatternMaster, mTimeLine);
                timeLineView.invalidate();
                mPatternHeaderView.invalidate();
            }
            @Override
            public boolean longPress(MotionEvent event) { return false; }
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                return false;
            }
            @Override
            public boolean noteTouched(MotionEvent event) {

                int rowSelected = (int)event.getY();
                float time = event.getX();
                Event noteTouched = patternPianoRoll.get(rowSelected, time);

                if (mCanEdit.isChecked()) {
                    if (noteTouched == null) {
                        noteTouched = new Event();
                        noteTouched.mTime = time;
                        noteTouched.mChannel = rowSelected;
                        noteTouched.mDuration = 1;
                        noteTouched.mId = patternPianoRoll.mInstrumentId;
                        patternPianoRoll.Set(noteTouched);
                    } else {
                        patternPianoRoll.Clear(noteTouched);
                    }

                    if (patternPianoRoll.mInstrumentId >=0) {
                        patternPianoRoll.play(noteTouched);
                    }
                } else {
                    if (noteTouched != null) {
                        mNoteView.selectClear();
                        mNoteView.selectSingleEvent(noteTouched);
                    }
                }

                mNoteView.invalidate();
                return true;
            }
        });

        //
        Button chooseInstrument = new Button(this);
        chooseInstrument.setText("Instrument");
        chooseInstrument.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                instrumentChooser();
            }
        });

        toolbar.addView(chooseInstrument, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

        mWidgetVcrControl = new WidgetVcrControl(toolbar, mAppState);
    }

    @Override
    public void onResume(){
        super.onResume();

        mWidgetVcrControl.onResume(mTimeLine, timeLineView);

        //overwrite listener with our own
        mAppState.mPatternMaster.SetBeatListener(new PatternBase.BeatListener() {
            @Override
            public void beat(float currentBeat) {
                mNoteView.setCurrentBeat(currentBeat);
                mNoteView.invalidate();
                mTimeLine.setTime(mAppState.mPatternMaster.getTime());
                timeLineView.invalidate();
            }
        });
    }


    private void instrumentChooser()
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternPianoRoll.mInstrumentId, new InstrumentChooser.InstrumentChooserListener()
        //InstrumentChooser2 instrumentChooser = new InstrumentChooser2(this, mAppState.instrumentList, patternNote.sampleId, new InstrumentChooser2.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(InstrumentBase generator) {
                patternPianoRoll.mInstrumentId = generator.mInstrumentId;
            }
        });
    }
}
