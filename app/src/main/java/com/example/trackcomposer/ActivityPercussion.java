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
                if (mPattern.sampleId>=0) {
                    InstrumentPercussion p = (InstrumentPercussion)mAppState.instrumentList.get(mPattern.sampleId);
                    return p.GetChannelName(channel);
                }
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
                    noteTouched.id = -1;
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
                InstrumentPercussion g = (InstrumentPercussion)InstrumentList.getInstance().get(mPattern.sampleId);
                g.loadSample(channel, file);
            }
        });

        filesChooser.showDialog();
    }
}
