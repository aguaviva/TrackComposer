package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityNote extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternView mNoteView;
    PatternNote patternNote;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mAppState = ((ApplicationClass)this.getApplication());
        patternNote = (PatternNote)mAppState.mLastPatternAdded;

        mNoteView = (PatternView)findViewById(R.id.noteView);
        mNoteView.SetPattern(patternNote);
        mNoteView.setInstrumentListener(new PatternView.InstrumentListener() {
            @Override
            public void instrumentTouched(int channel) {
                instrumentChooser();
                mNoteView.invalidate();
            }

            @Override
            public String getInstrumentName(int n)
            {
                return Misc.getNoteName(n + patternNote.baseNote);
            }
        });

        mNoteView.setNoteTouchedListener(new PatternView.NoteTouchedListener() {
            @Override
            public void noteTouched(int note, int beat) {
                patternNote.Play(mAppState.mixer, note);
            }
        });
    }

    private void instrumentChooser()
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternNote.sampleId, new InstrumentChooser.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(Generator generator) {
                patternNote.sampleId = generator.sampleId;
            }
        });
    }

}
