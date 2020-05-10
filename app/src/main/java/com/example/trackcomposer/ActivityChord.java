package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityChord extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternHeaderView chordNames;
    PatternHeaderView patternHeaderView;
    PatternBaseView mNoteView;
    PatternChord patternChord;
    Context mContext;
    TimeLine mTimeLine = new TimeLine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mAppState = ((ApplicationClass)this.getApplication());
        patternChord = (PatternChord)mAppState.mLastPatternAdded;

        mTimeLine.init(patternChord, 1);

        chordNames = (PatternHeaderView)findViewById(R.id.chordNames);
        chordNames.SetPattern(mTimeLine, patternChord.channels/3, patternChord.GetLength(),true);
        chordNames.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) {}
            @Override
            public void actionMove(int y) {}
            @Override
            public String getInstrumentName(int n)
            {
                return patternChord.TrackToChord(n);
            }
        });

        patternHeaderView = (PatternHeaderView)findViewById(R.id.patternHeaderView);
        patternHeaderView.SetPattern(mTimeLine, patternChord.channels, patternChord.GetLength(),true);
        patternHeaderView.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) {}
            @Override
            public void actionMove(int y) {}
            @Override
            public String getInstrumentName(int n)
            {
                return Misc.getNoteName(patternChord.TrackToNote(n));
            }
        });

        mNoteView = (PatternBaseView)findViewById(R.id.noteView);
        mNoteView.SetPattern(patternChord, mTimeLine,false, PatternBaseView.ViewMode.CHORDS);
        mNoteView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return false;
            }
            @Override
            public boolean onDoubleTap(int rowSelected, float time) {
                return false;
            }
            @Override
            public void scaling(float x, float y, float scale, float mTrackHeight) {
            }
            @Override
            public void longPress(int rowSelected, float time) {}
            @Override
            public boolean noteTouched(int rowSelected, float time) {
                //patternChord.Play(mAppState.mixer, noteTouched.channel, 1);
                return true; // keep on processing
            }
        });

        View noteControls = WidgetChordTransposer.AddUpAndDownKey(this, patternHeaderView, patternChord);
        toolbar.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

        rigControls();

        // choose instrument
        Button chooseInstrument = new Button(this);
        chooseInstrument.setText("Instrument");
        chooseInstrument.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick(View view) { instrumentChooser(); }

        });
        toolbar.addView(chooseInstrument, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

    }

    private void instrumentChooser()
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternChord.sampleId, new InstrumentChooser.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(InstrumentBase generator) {
                patternChord.sampleId = generator.sampleId;
                rigControls();
            }
        });
    }

    void rigControls()
    {
        /*
        LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
        headers.removeAllViews();

        if (patternChord.sampleId>=0) {
            Generator gen = mAppState.instrumentList.get(patternChord.sampleId);
            if (gen instanceof GeneratorSynth) {
                GeneratorSynth genSynth = (GeneratorSynth) gen;
                View synthControls = WidgetSynthEdit.SynthEditor(this, (GeneratorSynth) gen);
                headers.addView(synthControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            }
        }

         */
    }
}