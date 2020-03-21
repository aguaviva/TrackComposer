package com.example.trackcomposer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.tabs.TabLayout;

import java.io.File;

public class ActivityMain extends AppCompatActivity {

    private static final String TAG = "TrackComposer";
    ApplicationClass mAppState;
    PatternBaseView masterView;
    Context mContext;
    View[] trackControls;
    TextView[] trackNames;
    SeekBar[] trackVolumes;

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



        //View noteControls = getLayoutInflater().inflate(R.layout.note_controls, null);
        //toolbar.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        rigControls();

        final ImageButton fab = (ImageButton)findViewById(R.id.play);
        fab.setImageResource(android.R.drawable.ic_media_play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean playing = mAppState.PlayPause();
                fab.setImageResource(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        masterView = (PatternBaseView) findViewById(R.id.masterView);
        masterView.SetPattern(mAppState.mPatternMaster, false);
        masterView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public void instrumentTouched(int channel) {
                instrumentChooser(channel);
            }

            @Override
            public String getInstrumentName(int n)
            {
                PatternBase pattern = mAppState.mPatternMaster.mChannels[n];
                if (pattern==null)
                    return "--";
                return pattern.GetName();
            }

            @Override
            public void noteTouched(int note, int beat) {

            }

        });

        isStoragePermissionGranted();

        Bitmap b = masterView.getBitmapFromView(5*16,3*16);
        ImageView im = new ImageView(mContext);
        im.setImageBitmap(b);
        toolbar.addView(im);
    }

    void rigControls()
    {
        LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
        //TabLayout headers = (TabLayout) findViewById(R.id.headers);
        headers.removeAllViews();
        int channelCount = mAppState.mPatternMaster.GetChannelCount();

        trackControls = new View[channelCount];
        trackNames = new TextView[channelCount];
        trackVolumes = new SeekBar[channelCount];
        for(int i=0;i<channelCount;i++) {
            final int finalI = i;

            trackControls[i] = getLayoutInflater().inflate(R.layout.track_header, null);
            trackNames[i] = (TextView)trackControls[i].findViewById(R.id.instrumentName);
            trackControls[i].setOnTouchListener(new TextView.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        Toast.makeText(mContext, "Touched", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            // mute
            ToggleButton toggleMute = (ToggleButton)trackControls[i].findViewById(R.id.toggleMute);
            toggleMute.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    float vol = mAppState.mPatternMaster.getVolume(finalI);
                    if (isChecked)
                        mAppState.mPatternMaster.setVolume(finalI,-Math.abs(vol));
                    else
                        mAppState.mPatternMaster.setVolume(finalI,Math.abs(vol));
                }
            });

            // volume
            trackVolumes[i] = (SeekBar)trackControls[i].findViewById(R.id.seekBar);
            trackVolumes[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    mAppState.mPatternMaster.setVolume(finalI, ((float)progress)/100.0f);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar){}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar){}
            });


            headers.addView(trackControls[i], new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 0, 1.0f));
        }

        setTrackNames();
    }

    void setTrackNames()
    {
        for(int i=0;i<trackNames.length;i++) {
            trackNames[i].setText(String.valueOf(i));
            PatternBase pattern = mAppState.mPatternMaster.mChannels[i];
            if (pattern!=null)
                trackNames[i].setText(pattern.GetName());
        }

        for(int i=0;i<trackNames.length;i++) {
            trackVolumes[i].setProgress((int)(100*mAppState.mPatternMaster.getVolume(i)), false);
        }
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
            String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
            File directory = new File(extStore);

            final FileChooser filesChooser = new FileChooser(this, directory, "Load Song");
            filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                @Override
                public void fileSelected(String file)
                {
                    mAppState.Load(file);
                    masterView.SetPattern(mAppState.mPatternMaster, false);
                    setTrackNames();
                    masterView.invalidate();
                }

                @Override
                public void fileTouched(String file) {}
            });
            filesChooser.setExtension("json");
            filesChooser.showDialog();
            return true;
        }

        if (id == R.id.action_save) {

            String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
            File directory = new File(extStore);

            final FileChooser filesChooser = new FileChooser(this, directory, "Save Song");
            filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                @Override
                public void fileSelected(String file) { mAppState.Save(file); }

                @Override
                public void fileTouched(String file) {}
            });
            filesChooser.setExtension("json");
            filesChooser.showDialog();
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
            else if (mAppState.mLastPatternAdded instanceof PatternChord) {
                intent = new Intent(mContext, ActivityChord.class);
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

            // Create Chords
            Button btnNewChord = (Button) dialog.findViewById(R.id.buttonNewChord);
            btnNewChord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String filename = editTextKeywordToBlock.getText().toString();
                    mAppState.mLastPatternAdded = new PatternChord(filename, mAppState.extStoreDir+ "/"+filename, 4*3, 16);
                    mAppState.mPatternMaster.NewPatterns(mAppState.mLastPatternAdded, channel);
                    dialog.dismiss();

                    Intent intent = new Intent(mContext, ActivityChord.class);
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
