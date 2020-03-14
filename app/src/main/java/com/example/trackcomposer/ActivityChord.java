package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityChord extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternView mNoteView;
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

        mNoteView = (PatternView)findViewById(R.id.noteView);
        mNoteView.SetPattern(patternChord);
        mNoteView.setInstrumentListener(new PatternView.InstrumentListener() {
            @Override
            public void instrumentTouched(int channel) {
                instrumentChooser();
                mNoteView.invalidate();
            }

            @Override
            public String getInstrumentName(int n)
            {
                return Misc.getNoteName(patternChord.KeyToNote(n));
            }
        });

        mNoteView.setNoteTouchedListener(new PatternView.NoteTouchedListener() {
            @Override
            public void noteTouched(int note, int beat) {
                patternChord.Play(mAppState.mixer, note);
            }
        });
    }

    private void instrumentChooser()
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternChord.sampleId, new InstrumentChooser.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(Generator generator) {
                patternChord.sampleId = generator.sampleId;
            }
        });
    }
}