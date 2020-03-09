package com.example.trackcomposer;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

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
        mNoteView.setInstrumentTouchedListener(new PatternView.InstrumentTouchedListener() {
            @Override
            public void instrumentTouched(int channel) {
                instrumentChooser();
                mNoteView.invalidate();
            }
        });

        mNoteView.setNoteTouchedListener(new PatternView.NoteTouchedListener() {
            @Override
            public void noteTouched(int note, int beat) {
                patternNote.Play(mAppState.soundPool, note);
            }
        });

        if (patternNote.sample==null)
        {
            instrumentChooser();
        }
    }

    private void instrumentChooser()
    {
        String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
        File directory = new File(extStore);

        FileChooser filesChooser = new FileChooser(this, directory);
        filesChooser.setExtension("ogg");

        filesChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final File file) {
                patternNote.sample.LoadSample(mAppState.soundPool, file.getPath());
            }
        });

        filesChooser.setFileTouchedListener(new FileChooser.FileTouchedListener() {
            @Override public void fileTouched(final File file) {
                patternNote.sample.LoadSample(mAppState.soundPool, file.getPath());
            }
        });

        filesChooser.showDialog();
    }

}
