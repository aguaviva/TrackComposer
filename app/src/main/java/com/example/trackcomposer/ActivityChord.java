package com.example.trackcomposer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityChord extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternBaseView mNoteView;
    PatternChord patternChord;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mAppState = ((ApplicationClass)this.getApplication());
        patternChord = (PatternChord)mAppState.mLastPatternAdded;

        mNoteView = (PatternBaseView)findViewById(R.id.noteView);
        mNoteView.SetPattern(patternChord, false);
        mNoteView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public void instrumentTouched(int channel) {
                instrumentChooser();
                mNoteView.invalidate();
            }

            @Override
            public String getInstrumentName(int n) {
                return Misc.getNoteName(patternChord.KeyToNote(n));
            }

            @Override
            public void noteTouched(int note, int beat) {
                patternChord.Play(mAppState.mixer, note, 1);
            }
        });

        rigControls();
    }

    private void instrumentChooser()
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternChord.sampleId, new InstrumentChooser.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(Generator generator) {
                patternChord.sampleId = generator.sampleId;
                rigControls();
            }
        });
    }

    void rigControls()
    {
        LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
        headers.removeAllViews();

        if (patternChord.sampleId>=0) {
            Generator gen = mAppState.instrumentList.get(patternChord.sampleId);
            if (gen instanceof GeneratorSynth) {
                GeneratorSynth genSynth = (GeneratorSynth) gen;
                View synthControls = Widgets.SynthEditor(this, (GeneratorSynth) gen);
                headers.addView(synthControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            }
        }
    }
}