package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityPercussion extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternBaseView mDrumTracker;
    Context mContext;
    PatternHeaderView mPatternHeaderView;
    PatternPercussion mPattern;
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
        mPattern = (PatternPercussion)mAppState.mLastPatternAdded;

        mTimeLine.init(mPattern, 1); //

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

        mPatternHeaderView = (PatternHeaderView)findViewById(R.id.patternHeaderView);
        mPatternHeaderView.SetPattern(mTimeLine, mPattern.channels, mPattern.GetLength(),false);
        mPatternHeaderView.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) { instrumentChooser(note); }
            @Override
            public void actionMove(int y) {}
            @Override
            public String getInstrumentName(int channel)
            {
                int sampleId = mPattern.mChannels[channel];
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
                timeLineView.init(mPattern, mTimeLine);
                timeLineView.invalidate();

                mPatternHeaderView.setPosScale(trackHeight);
                mPatternHeaderView.invalidate();
            }
            @Override
            public void longPress(int rowSelected, float time) {}
            @Override
            public boolean noteTouched(int rowSelected, float time) {
                Event noteTouched = mPattern.get(rowSelected, time);
                if (noteTouched==null) {
                    noteTouched = new Event();
                    noteTouched.time = time;
                    noteTouched.channel = rowSelected;
                    noteTouched.durantion = 1;
                    noteTouched.id = mPattern.mChannels[rowSelected];
                    mPattern.Set(noteTouched);
                }
                else {
                    mPattern.Clear(rowSelected, time);
                }

                if (mPattern.mChannels[rowSelected]>=0) {
                    mAppState.mLastPatternMixer.play(noteTouched, rowSelected, 1, 1);
                }
                mDrumTracker.invalidate();
                return true;}
        });
    }

    private void instrumentChooser(final int channel)
    {
        InstrumentChooser instrumentChooser = new InstrumentChooser(this, mAppState.instrumentList, mPattern.mChannels[channel], new InstrumentChooser.InstrumentChooserListener()
        {
            @Override
            public void GetSelectedInstrumentId(InstrumentBase generator) {
                mPattern.mChannels[channel] = generator.sampleId;
            }
        });
    }
}
