package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        mAppState = ((ApplicationClass)this.getApplication());
        patternPianoRoll = (PatternPianoRoll)mAppState.mLastPatternAdded;

        mTimeLine.init(patternPianoRoll, 1); // at scale 1, draw 1 vertical line every tick

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(patternPianoRoll, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
                mNoteView.invalidate();
                //mAppState.setLoop(time, (1 * 16 * 16));
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


        mNoteView = (PatternBaseView)findViewById(R.id.noteView);
        mNoteView.SetPattern(patternPianoRoll, mTimeLine,false,PatternBaseView.ViewMode.PIANO);
        mNoteView.setBaseNote(patternPianoRoll.mBaseNote);
        mNoteView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            Event noteDown;
            float dx = 0, dy = 0;
            boolean bMoved=false;
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    noteDown = mAppState.mPatternMaster.get((int)event.getY(), event.getX());
                    if (noteDown!=null) {
                        dx = event.getX() - noteDown.mTime;
                        dy = event.getY() - noteDown.mChannel;

                        mNoteView.selectedNote = noteDown;
                        //eventSelected = noteDown;
                        //mRowSelected = (int)event.getY();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    if (noteDown!=null)
                    {
                        noteDown.mTime = event.getX() - dx;
                        noteDown.mChannel = (int)(event.getY() - dy);
                        //if ()
                        mNoteView.invalidate();
                        return true;
                    }
                } if (event.getAction() == MotionEvent.ACTION_UP) {
                    noteDown = null;
                    if (bMoved)
                        return true; //prevent other handlers from using this motion
                }

                return false;
            }
            @Override
            public void scaling(float x, float y, float scale, float trackHeight)
            {
                timeLineView.init(patternPianoRoll, mTimeLine);
                timeLineView.invalidate();

                mPatternHeaderView.setPosScale(trackHeight);
                mPatternHeaderView.invalidate();
            }
            @Override
            public void longPress(int rowSelected, float time) {}
            @Override
            public boolean onDoubleTap(int rowSelected, float time) {
                return false;
            }
            @Override
            public boolean noteTouched(int rowSelected, float time) {

                Event noteTouched = patternPianoRoll.get(rowSelected, time);
                if (noteTouched==null) {
                    noteTouched = new Event();
                    noteTouched.mTime = time;
                    noteTouched.mChannel = rowSelected;
                    noteTouched.mDuration = 1;
                    noteTouched.mId = patternPianoRoll.mInstrumentId;
                    patternPianoRoll.Set(noteTouched);
                }
                else {
                    patternPianoRoll.Clear(rowSelected, time);
                }

                if (patternPianoRoll.mInstrumentId >=0) {
                    patternPianoRoll.play(noteTouched);
                }
                mNoteView.invalidate();
                return true;
            }
        });

        final ImageButton fab = (ImageButton)findViewById(R.id.play);
        fab.setImageResource(android.R.drawable.ic_media_play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean playing = mAppState.PlayPause();
                fab.setImageResource(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
            }
        });


        // set note controls up in the toolbar
        //View noteControls = WidgetNoteTransposer.AddUpAndDownKey(this, mNoteView, patternPianoRoll);
        //toolbar.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

        Button chooseInstrument = new Button(this);
        chooseInstrument.setText("Instrument");
        chooseInstrument.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                instrumentChooser();
            }

        });
        toolbar.addView(chooseInstrument, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
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

    void rigControls(ViewGroup headers) {
        //LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
        //headers.removeAllViews();
        //Toolbar headers = findViewById(R.id.toolbar);

        View noteControls = WidgetNoteTransposer.AddUpAndDownKey(this, String.valueOf(patternPianoRoll.mBaseNote), new WidgetNoteTransposer.Listener() {
            @Override
            public String update(int inc) {
                        patternPianoRoll.mBaseNote +=inc;
                        mNoteView.invalidate();
                        mPatternHeaderView.invalidate();
                        return String.valueOf(patternPianoRoll.mBaseNote);
            }
        });
        headers.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

        //headers.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
    }
}
