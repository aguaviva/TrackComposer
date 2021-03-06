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

        float timeBegin = getIntent().getFloatExtra("PATTERN_TIME_BEGIN",0);
        float timeEnd = getIntent().getFloatExtra("PATTERN_TIME_END",256);
        int channel = getIntent().getIntExtra("PATTERN_CHANNEL", -1);
        int patternId = getIntent().getIntExtra("PATTERN_ID",-1);

        mTimeLine.init(mAppState.mPatternMaster, 16.0f); // at scale 1, draw 1 vertical line every tick
        mTimeLine.setTimeSpan(timeBegin,timeEnd);
        mTimeLine.mViewport.setSpanVertical(0, 8);
        mTimeLine.mViewport.setLimits(0,0,256,8);

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(mAppState.mPatternMaster, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
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
        mPatternHeaderView.SetPattern(mTimeLine, mPattern.mChannels, mPattern.GetLength(),false);
        mPatternHeaderView.setInstrumentListener(new PatternHeaderView.InstrumentListener() {
            @Override
            public void noteTouched(int note) { instrumentChooser(note); }
            @Override
            public void actionMove(int y) {}
            @Override
            public String getInstrumentName(int channel)
            {
                if (mPattern.mInstrumentId >=0) {
                    InstrumentPercussion p = (InstrumentPercussion)mAppState.instrumentList.get(mPattern.mInstrumentId);
                    if (channel<p.mChannels.length)
                        return p.GetChannelName(channel);
                    else
                        return "---";
                }
                return "none";
            }
        });

        mDrumTracker = (PatternBaseView)findViewById(R.id.tracksView);
        mDrumTracker.SetPattern(mAppState.mPatternMaster, channel, mTimeLine,false, PatternBaseView.ViewMode.DRUMS);
        mDrumTracker.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public boolean onMoveSelectedEvents(MotionEvent event) {
                return false;
            }
            @Override
            public void scaling(float x, float y, float scale, float trackHeight) {
                timeLineView.init(mPattern, mTimeLine);
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
                Event noteTouched = mPattern.get(rowSelected, time);
                if (noteTouched==null) {
                    noteTouched = new Event();
                    noteTouched.mTime = time;
                    noteTouched.mChannel = rowSelected;
                    noteTouched.mDuration = 1;
                    noteTouched.mId = -1;
                    mPattern.Set(noteTouched);
                }
                else {
                    mPattern.Clear(rowSelected, time);
                }

                if (rowSelected>=0) {
                    mPattern.play(noteTouched);
                }
                mDrumTracker.invalidate();
                return true;}
        });
    }

    private void instrumentChooser(final int channel)
    {
        final FileChooser filesChooser = new FileChooser(this, mAppState.extStoreDir, "Load Sample");
        filesChooser.setExtension("ogg");
        filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final String file) {
            }  // we are loading the instrument on touch

            @Override
            public void fileTouched(final String file) {
                InstrumentPercussion g = (InstrumentPercussion)InstrumentList.getInstance().get(mPattern.mInstrumentId);
                g.loadSample(channel, file);
            }
        });

        filesChooser.showDialog();
    }
}
