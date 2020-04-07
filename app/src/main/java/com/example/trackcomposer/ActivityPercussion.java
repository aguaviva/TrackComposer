package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityPercussion extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternBaseView mDrumTracker;
    Context mContext;
    PatternPercussion patternPercussion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percussion);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mAppState = ((ApplicationClass)this.getApplication());
        patternPercussion = (PatternPercussion)mAppState.mLastPatternAdded;

        final PatternHeaderView patternHeaderView = (PatternHeaderView)findViewById(R.id.patternHeaderView);
        patternHeaderView.SetPattern(patternPercussion.channels, patternPercussion.length,true);
        patternHeaderView.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) { instrumentChooser(note); }
            @Override
            public void actionMove(int y) {}
            @Override
            public String getInstrumentName(int channel)
            {
                int sampleId = patternPercussion.mChannels[channel];
                if (sampleId>=0)
                    return mAppState.instrumentList.get(sampleId).instrumentName;
                return "none";
            }
        });

        mDrumTracker = (PatternBaseView)findViewById(R.id.drumView);
        mDrumTracker.SetPattern(mAppState.mLastPatternAdded, false, false);
        mDrumTracker.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public boolean noteTouched(int note, int beat) { return true;}
        });
    }

    private void instrumentChooser(final int channel)
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, patternPercussion.mChannels[channel], new InstrumentChooser.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(Generator generator) {
                patternPercussion.mChannels[channel] = generator.sampleId;
            }
        });
    }
}
