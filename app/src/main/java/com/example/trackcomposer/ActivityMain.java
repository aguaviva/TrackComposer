package com.example.trackcomposer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;



public class ActivityMain extends AppCompatActivity {
    private static final String TAG = "TrackComposer";
    ApplicationClass mAppState;
    PatternBaseView masterView;
    Context mContext;
    View[] trackControls;
    TextView[] trackNames;
    SeekBar[] trackVolumes;
    TimeLine mTimeLine = new TimeLine();
    TimeLineView timeLineView;
    int mNote=-1, mBeat=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        //SoundNative sn = new SoundNative();
        //sn.Init(this);
        //String str = stringFromJNI();

        mAppState = ((ApplicationClass)this.getApplication());
        mAppState.Init();

        toolbar.setSubtitle("Test Subtitle");
        toolbar.inflateMenu(R.menu.menu_main);

        //View noteControls = getLayoutInflater().inflate(R.layout.note_controls, null);
        //toolbar.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        rigControls();

        final ImageButton fab = (ImageButton)findViewById(R.id.previous);
        fab.setImageResource(android.R.drawable.ic_media_previous);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppState.setLoop((int) 0, (int) (1 * 16 * 16));
                mTimeLine.setTime(0);
                timeLineView.invalidate();
            }
        });

        final ImageButton fab2 = (ImageButton)findViewById(R.id.play);
        fab2.setImageResource(android.R.drawable.ic_media_play);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean playing = mAppState.PlayPause();
                fab2.setImageResource(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
            }
        });

        final ImageButton fab3 = (ImageButton)findViewById(R.id.loop);
        fab3.setImageResource(android.R.drawable.ic_menu_revert);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float ini = timeLineView.getSelection();
                mAppState.setLoop((int)(ini), (int)(16*16));
                boolean playing = mAppState.PlayPause();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        mTimeLine.init(mAppState.mPatternMaster, 1);

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(mAppState.mPatternMaster, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
                mAppState.setLoop((int) time, (int) (1 * 16 * 16));
            }
            @Override
            public void onPatternEnd(float time)
            {
                masterView.GetPattern().length = (int)time;
                masterView.invalidate();
            }
        });

        //
        masterView = (PatternBaseView) findViewById(R.id.masterView);
        masterView.init(mAppState.mPatternMaster, mTimeLine);
        masterView.SetPattern(mAppState.mPatternMaster, 8,32,true, false);
        masterView.setInstrumentListener(new PatternBaseView.InstrumentListener() {
            @Override
            public void scaling(float x, float y, float scale, float trackHeight) {
                timeLineView.init(mAppState.mPatternMaster, mTimeLine);
                timeLineView.invalidate();
            }
            @Override
            public void longPress(Point p, PointF pf)
            {
                mNote = p.y;
                mBeat = p.x;
                createPopUpMenu(pf);
            }
            @Override
            public boolean noteTouched(int note, int beat) {
                mNote = note;
                mBeat = beat;
                return false;
            }
        });

        //overwrite listener with our own
        mAppState.mPatternMaster.SetBeatListener(new PatternBase.BeatListener() {
            @Override
            public void beat(int currentBeat) {
                masterView.setCurrentBeat(currentBeat);
                masterView.invalidate();
                mTimeLine.setTime(mAppState.getTime());
                timeLineView.invalidate();
            }
        });

        isStoragePermissionGranted();

        // Tempo controls
        View noteControls = WidgetNoteTransposer.AddUpAndDownKey(this, String.valueOf(mAppState.mPatternMaster.mBPM), new WidgetNoteTransposer.Listener() {
            @Override
            public String update(int inc) {
                mAppState.mPatternMaster.mBPM+=inc;
                mAppState.soundPool.setBmp(mAppState.mPatternMaster.mBPM);
                return String.valueOf(mAppState.mPatternMaster.mBPM);
            }
        });
        toolbar.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
    }

    @Override
    public void onResume(){
        super.onResume();

        mAppState.setLoop((int) (0 * 16 * 16), (int) (1 * 16 * 16));

        generateIcons();
    }

    void rigControls()
    {
        LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
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

    View.OnClickListener btnEditingListent = new View.OnClickListener() {
        int copiedId = -1;
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btnNew:
                    addPattern(mNote, mBeat); break;
                case R.id.btnEdit:
                    mAppState.setLoop((int)(mBeat*16), (int)((mBeat+1)*16));
                    editPattern(mNote, mBeat); break;
                case R.id.btnCopy: {
                    GeneratorInfo genInf = mAppState.mPatternMaster.Get(mNote, mBeat);
                    if (genInf != null)
                        copiedId = genInf.sampleId;
                    else
                        copiedId = -1;
                    break;
                }
                case R.id.btnPaste: {
                    if (copiedId != -1) {
                        GeneratorInfo genInf = new GeneratorInfo();
                        genInf.sampleId = copiedId;
                        mAppState.mPatternMaster.Set(mNote, mBeat, genInf);
                        break;
                    }
                    else
                    {
                        Toast.makeText(mContext, "Nothing to paste", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.btnDelete:
                    mAppState.mPatternMaster.Set(mNote, mBeat, null); break;
            }
            masterView.invalidate();
        }
    };

    public void createPopUpMenu(PointF pf) {
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View btnEditing = layoutInflater.inflate(R.layout.buttons_pattern_editing, null);
        final PopupWindow popupWindow = new PopupWindow(
                btnEditing,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ShapeDrawable());
        View parent = masterView.getRootView();

        Button btnNew = btnEditing.findViewById(R.id.btnNew);
        btnNew.setOnClickListener(btnEditingListent);
        Button btnEdit = btnEditing.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(btnEditingListent);
        Button btnCopy = btnEditing.findViewById(R.id.btnCopy);
        btnCopy.setOnClickListener(btnEditingListent);
        Button btnPaste = btnEditing.findViewById(R.id.btnPaste);
        btnPaste.setOnClickListener(btnEditingListent);
        Button btnDelete = btnEditing.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(btnEditingListent);


        popupWindow.showAtLocation(masterView, Gravity.NO_GRAVITY, (int)pf.x, (int)pf.y);
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
                    setTrackNames();
                    masterView.SetPattern(mAppState.mPatternMaster, 8,16,true,false);
                    masterView.patternImgDataBase(mAppState.mPatternImgDataBase);
                    generateIcons();
                    masterView.invalidate();

                    //overwrite listener with our own
                    mAppState.mPatternMaster.SetBeatListener(new PatternBase.BeatListener() {
                        @Override
                        public void beat(int currentBeat) {
                            masterView.setCurrentBeat(currentBeat);
                            masterView.invalidate();
                            mTimeLine.setTime(mAppState.getTime());
                            timeLineView.invalidate();
                        }
                    });
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

    private void editPattern(final int channel, final int beat)
    {
        GeneratorInfo genInf = mAppState.mPatternMaster.Get(channel, beat);
        if (genInf==null)
        {
            Toast.makeText(mContext, "Nothing to edit ", Toast.LENGTH_SHORT).show();
            return;
        }

        PatternBase pattern = mAppState.mPatternMaster.mPatternDataBase.get(genInf.sampleId);
        mAppState.mLastPatternAdded = pattern;

        Intent intent = null;

        if (pattern instanceof PatternPercussion) {
            intent = new Intent(mContext, ActivityPercussion.class);
        }
        else if (pattern instanceof PatternPianoRoll) {
            intent = new Intent(mContext, ActivityPianoRoll.class);
        }
        else if (pattern instanceof PatternChord) {
            intent = new Intent(mContext, ActivityChord.class);
        }

        startActivity(intent);
    }

    private void addPattern(final int channel, final int beat)
    {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.new_pattern);
        dialog.setTitle("Name your pattern");

        final EditText editTextKeywordToBlock = (EditText) dialog.findViewById(R.id.editTextKeywordsToBlock);
        final String filename = editTextKeywordToBlock.getText().toString();

        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                PatternBase pattern = null;
                Intent intent = null;

                switch (v.getId())
                {
                    case R.id.buttonNewPercussion:
                        pattern = new PatternPercussion(filename, mAppState.extStoreDir+ "/"+filename, 16, 16);
                        intent = new Intent(mContext, ActivityPercussion.class);
                        break;
                    case R.id.buttonNewChord:
                        pattern = new PatternChord(filename, mAppState.extStoreDir+ "/"+filename, 4*3, 16);
                        intent = new Intent(mContext, ActivityChord.class);
                        break;
                    case R.id.buttonNewNote:
                        pattern = new PatternPianoRoll(filename, mAppState.extStoreDir+ "/"+filename, 24, 16);
                        intent = new Intent(mContext, ActivityPianoRoll.class);
                        break;
                }

                int id = mAppState.mPatternMaster.mPatternDataBase.size();
                mAppState.mPatternMaster.mPatternDataBase.put(id , pattern);
                mAppState.mLastPatternAdded = pattern;

                GeneratorInfo genInf = new GeneratorInfo();
                genInf.sampleId = id;
                mAppState.mPatternMaster.Set(channel, beat, genInf);
                dialog.dismiss();

                startActivity(intent);
                masterView.invalidate();
            }
        };

        // Create Percussion
        dialog.findViewById(R.id.buttonNewPercussion).setOnClickListener(clickListener);
        dialog.findViewById(R.id.buttonNewChord).setOnClickListener(clickListener);
        dialog.findViewById(R.id.buttonNewNote).setOnClickListener(clickListener);

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
/*
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
    }
*/
    public void generateIcons()
    {
        PatternBaseView pbv = new PatternBaseView(this);
        pbv.patternImgDataBase(null);
        for (Integer key : mAppState.mPatternMaster.mPatternDataBase.keySet()) {
            PatternBase pattern = mAppState.mPatternMaster.mPatternDataBase.get(key);

            pbv.SetPattern(pattern, 16,16,false, true);
            Bitmap b = pbv.getBitmapFromView(5 * 16, 3 * 16);
            mAppState.mPatternImgDataBase.put(key, b);
        }
    }


    public boolean isStoragePermissionGranted() {
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
