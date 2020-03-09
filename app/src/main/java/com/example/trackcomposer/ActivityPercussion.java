package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.view.View;

import java.io.File;

public class ActivityPercussion extends AppCompatActivity {
    ApplicationClass mAppState;
    PatternView mDrumTracker;
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
        mDrumTracker = (PatternView)findViewById(R.id.drumView);
        mDrumTracker.SetPattern(mAppState.mLastPatternAdded);
        mDrumTracker.setInstrumentTouchedListener(new PatternView.InstrumentTouchedListener() {
            @Override
            public void instrumentTouched(int channel) {

                instrumentChooser(channel);
                mDrumTracker.invalidate();
            }
        });
    }

    private void instrumentChooser(final int channel)
    {
        String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
        File directory = new File(extStore);

        FileChooser filesChooser = new FileChooser(this, directory);
        filesChooser.setExtension("ogg");

        filesChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final File file) {
                patternPercussion.mChannels[channel].LoadSample(mAppState.soundPool, file.getPath());
            }
        });

        filesChooser.setFileTouchedListener(new FileChooser.FileTouchedListener() {
            @Override public void fileTouched(final File file) {
                patternPercussion.mChannels[channel].LoadSample(mAppState.soundPool, file.getPath());
            }
        });

        filesChooser.showDialog();
    }

}
