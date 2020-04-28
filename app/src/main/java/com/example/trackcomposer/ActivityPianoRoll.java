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
    PatternHeaderView mPatternHeaderView;
    PatternPianoRoll patternPianoRoll;
    Context mContext;
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

        final ImageButton fab = (ImageButton)findViewById(R.id.play);
        fab.setImageResource(android.R.drawable.ic_media_play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean playing = mAppState.PlayPause();
                fab.setImageResource(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
            }
        });

        mTimeLine.init(patternPianoRoll, 1);

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(patternPianoRoll, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
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
        mPatternHeaderView.SetPattern(mTimeLine, patternPianoRoll.channels, patternPianoRoll.GetLength(),true);
        mPatternHeaderView.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) {}
            @Override
            public void actionMove(int y)
            {
                patternPianoRoll.baseNote+=y;
                mNoteView.setBaseNote(patternPianoRoll.baseNote);
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
        mNoteView.setBaseNote(patternPianoRoll.baseNote);
        mNoteView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
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
            public boolean noteTouched(int rowSelected, float time) {

                Event noteTouched = patternPianoRoll.get(rowSelected, time);
                if (noteTouched==null) {
                    Event note = new Event();
                    note.time = time;
                    note.channel = rowSelected;
                    note.durantion = 1;
                    note.mGen = new GeneratorInfo();
                    note.mGen.sampleId = patternPianoRoll.sampleId;
                    patternPianoRoll.Set(note);
                }
                else {
                    patternPianoRoll.Clear(rowSelected, time);
                }

                //patternPianoRoll.Play(mAppState.mixer, rowSelected, 1);
                mNoteView.invalidate();
                return true;
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
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternPianoRoll.sampleId, new InstrumentChooser.InstrumentChooserListener()
        //InstrumentChooser2 instrumentChooser = new InstrumentChooser2(this, mAppState.instrumentList, patternNote.sampleId, new InstrumentChooser2.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(Generator generator) {
                patternPianoRoll.sampleId = generator.sampleId;
            }
        });
    }

    void rigControls(ViewGroup headers) {
        //LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
        //headers.removeAllViews();
        //Toolbar headers = findViewById(R.id.toolbar);

        View noteControls = WidgetNoteTransposer.AddUpAndDownKey(this, String.valueOf(patternPianoRoll.baseNote), new WidgetNoteTransposer.Listener() {
            @Override
            public String update(int inc) {
                        patternPianoRoll.baseNote+=inc;
                        mNoteView.invalidate();
                        mPatternHeaderView.invalidate();
                        return String.valueOf(patternPianoRoll.baseNote);
            }
        });
        headers.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

        //headers.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
    }
}
