package com.example.trackcomposer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TrackComposer";
    ApplicationClass mAppState;
    PatternView masterView;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mAppState = ((ApplicationClass)this.getApplication());
        mAppState.Init();

        toolbar.setSubtitle("Test Subtitle");
        toolbar.inflateMenu(R.menu.menu_main);

        ImageButton fab = (ImageButton)findViewById(R.id.play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppState.PlayPause();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }


        masterView = (PatternView) findViewById(R.id.masterView);
        masterView.SetPattern(mAppState.mPatternMaster);
        masterView.setInstrumentListener(new PatternView.InstrumentListener() {
            @Override
            public void instrumentTouched(int channel) {
                instrumentChooser(channel);
            }

            @Override
            public String getInstrumentName(int n)
            {
                Pattern pattern = mAppState.mPatternMaster.mChannels[n];
                if (pattern==null)
                    return "--";
                return pattern.GetName();
            }
        });

        isStoragePermissionGranted();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_load) {
            mAppState.Load("mysong");
            masterView.SetPattern(mAppState.mPatternMaster);
            masterView.invalidate();
            return true;
        }

        if (id == R.id.action_save) {
            mAppState.Save("mysong");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void instrumentChooser(final int channel) {

        if (mAppState.mPatternMaster.mChannels[channel]!=null)
        {
            mAppState.mLastPatternAdded = mAppState.mPatternMaster.mChannels[channel];

            Intent intent = null;

            if (mAppState.mLastPatternAdded instanceof PatternPercussion) {
                intent = new Intent(mContext, ActivityPercussion.class);
            }
            else if (mAppState.mLastPatternAdded instanceof PatternNote) {
                intent = new Intent(mContext, ActivityNote.class);
            }

            startActivity(intent);
        }
        else
        {
            final Dialog dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.new_pattern);
            dialog.setTitle("Name your pattern");

            final EditText editTextKeywordToBlock = (EditText) dialog.findViewById(R.id.editTextKeywordsToBlock);

            // Create Percussion
            Button btnNewPercussion = (Button) dialog.findViewById(R.id.buttonNewPercussion);
            btnNewPercussion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String filename = editTextKeywordToBlock.getText().toString();
                    mAppState.mLastPatternAdded = new PatternPercussion(filename, mAppState.extStoreDir+ "/"+filename, 16, 16);
                    mAppState.mPatternMaster.NewPatterns(mAppState.mLastPatternAdded, channel);
                    dialog.dismiss();

                    Intent intent = new Intent(mContext, ActivityPercussion.class);
                    startActivity(intent);
                    masterView.invalidate();
                }
            });

            // Create Notes
            Button btnNewNote = (Button) dialog.findViewById(R.id.buttonNewNote);
            btnNewNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String filename = editTextKeywordToBlock.getText().toString();
                    mAppState.mLastPatternAdded = new PatternNote(filename, mAppState.extStoreDir+ "/"+filename, 16, 16);
                    mAppState.mPatternMaster.NewPatterns(mAppState.mLastPatternAdded, channel);
                    dialog.dismiss();

                    Intent intent = new Intent(mContext, ActivityNote.class);
                    startActivity(intent);
                    masterView.invalidate();
                }
            });

            // Cancel
            Button btnCancel = (Button) dialog.findViewById(R.id.buttonCancelBlockKeyword);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }


    public  boolean isStoragePermissionGranted() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
