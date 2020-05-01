package com.example.trackcomposer;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityPercussion extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternBaseView mDrumTracker;
    Context mContext;
    PatternHeaderView patternHeaderView;
    PatternPercussion patternPercussion;
    TimeLine mTimeLine = new TimeLine();
    TimeLineView timeLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percussion);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mAppState = ((ApplicationClass)this.getApplication());
        patternPercussion = (PatternPercussion)mAppState.mLastPatternAdded;

        mTimeLine.init(patternPercussion, 1); //

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(mAppState.mPatternMaster, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
                //mAppState.setLoop((int) time, (int) (1 * 16 * 16));
                mAppState.mPatternMaster.setTime(time);
                mDrumTracker.invalidate();
            }
            @Override
            public void onPatternEnd(float time)
            {
                mDrumTracker.GetPattern().SetLength(time);
                mDrumTracker.invalidate();
            }
        });

        patternHeaderView = (PatternHeaderView)findViewById(R.id.patternHeaderView);
        patternHeaderView.SetPattern(mTimeLine, patternPercussion.channels, patternPercussion.GetLength(),true);
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

        mDrumTracker = (PatternBaseView)findViewById(R.id.tracksView);
        mDrumTracker.SetPattern(mAppState.mLastPatternAdded, mTimeLine,false, PatternBaseView.ViewMode.DRUMS);
        mDrumTracker.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return false;
            }
            @Override
            public void scaling(float x, float y, float scale, float trackHeight) {
            }
            @Override
            public void longPress(int rowSelected, float time) {}
            @Override
            public boolean noteTouched(int rowSelected, float time) { return false;}
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
