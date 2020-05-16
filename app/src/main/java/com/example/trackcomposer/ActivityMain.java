package com.example.trackcomposer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.trackcomposer.ActivityMain.PatternType.*;
import static com.example.trackcomposer.ActivityMain.PatternType.PianoRoll;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = "TrackComposer";
    ApplicationClass mAppState;
    PatternBaseView masterView;
    Context mContext;
    TimeLine mTimeLine = new TimeLine();
    TimeLineView timeLineView;
    int mNote=-1, mBeat=-1;

    enum PatternType
    {
        none,
        PianoRoll,
        Percussion,
        Chords
    };

    public class Track
    {
        PatternType patternType;
        View trackControls;
        TextView trackNames;
        SeekBar trackVolumes;
    }

    Track [] mTracks;

    int mRowSelected;
    Event eventSelected;
    CheckBox mPlay;

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
                //mAppState.setLoop((int) 0, (int) (1 * 16 * 16));
                mTimeLine.setTime(0);
                mAppState.mPatternMaster.setTime(0);
                timeLineView.invalidate();
            }
        });

        mPlay = (CheckBox)findViewById(R.id.play);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppState.playing(mPlay.isChecked());
            }
        });

        final ImageButton fab3 = (ImageButton)findViewById(R.id.add);
        fab3.setImageResource(android.R.drawable.ic_menu_revert);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float ini = timeLineView.getSelection();
                addPattern(mRowSelected, mTimeLine.getTime());
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        mTimeLine.init(mAppState.mPatternMaster, 16.0f);  //at scale 1 draw a vertical line every 16 ticks

        //
        timeLineView = (TimeLineView)findViewById(R.id.timeLineView);
        timeLineView.init(mAppState.mPatternMaster, mTimeLine);
        timeLineView.setTimeLineListener(new TimeLineView.TimeLineListener() {
            @Override
            public void onTimeChanged(float time)
            {
                //mAppState.setLoop((int) time, (int) (1 * 16 * 16));
                mAppState.mPatternMaster.setTime(time);
                masterView.invalidate();
            }
            @Override
            public void onPatternEnd(float time)
            {
                masterView.GetPattern().SetLength(time);
                masterView.invalidate();
            }
        });

        //
        masterView = (PatternBaseView) findViewById(R.id.masterView);
        masterView.SetPattern(mAppState.mPatternMaster, mTimeLine,true, PatternBaseView.ViewMode.MAIN);
        masterView.patternImgDataBase(mAppState.mPatternImgDataBase);
        masterView.setInstrumentListener(new PatternBaseView.InstrumentListener() {

            Event noteDown;
            float orgTime = 0;
            @Override
            public boolean onMoveSelectedEvents(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    noteDown = mAppState.mPatternMaster.get((int)event.getY(), event.getX());
                    if (noteDown!=null) {
                        orgTime = event.getX();
                        eventSelected = noteDown;
                        mRowSelected = (int)event.getY();
                        return true;
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    if (noteDown!=null)
                    {
                        if (masterView.selectItemCount()==0) {
                            masterView.selectSingleEvent(noteDown);
                        }
                        float deltaTime = event.getX() - orgTime;
                        masterView.selectMove(deltaTime, 0);
                        orgTime = event.getX();
                        masterView.invalidate();
                        return true;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    noteDown = null;
                    return true;
                }

                return false;
            }
            @Override
            public void scaling(float x, float y, float scale, float trackHeight) {
                timeLineView.init(mAppState.mPatternMaster, mTimeLine);
                timeLineView.invalidate();
            }
            @Override
            public boolean longPress(MotionEvent event)
            {
                int rowSelected = (int)event.getY();
                float time = event.getX();
                Event noteTouched = mAppState.mPatternMaster.get(rowSelected, time);
                if (noteTouched!=null) {
                    eventSelected = noteTouched;
                    mRowSelected = rowSelected;
                    createPopUpMenu(new PointF());
                }

                //mRowSelected = rowSelected;
                //createPopUpMenu(new PointF());
                return false;

            }
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                int rowSelected = (int)event.getY();
                float time = event.getX();
                Event noteTouched = mAppState.mPatternMaster.get(rowSelected, time);
                editPattern(noteTouched);
                return noteTouched!=null;
            }
            @Override
            public boolean noteTouched(MotionEvent event) {
                int rowSelected = (int)event.getY();
                float time = event.getX();
                Event noteTouched = mAppState.mPatternMaster.get(rowSelected, time);
                if (noteTouched!=null) {
                    masterView.selectSingleEvent(noteTouched);
                    eventSelected = noteTouched;
                    mRowSelected = rowSelected;
                }
                masterView.invalidate();
                return true;
            }
        });

        //overwrite listener with our own
        mAppState.mPatternMaster.SetBeatListener(new PatternBase.BeatListener() {
            @Override
            public void beat(float currentBeat) {
                masterView.setCurrentBeat(currentBeat);
                masterView.invalidate();
                mTimeLine.setTime(mAppState.mPatternMaster.getTime());
                timeLineView.invalidate();
            }
        });

        isStoragePermissionGranted();

        // Tempo controls
        View noteControls = WidgetNoteTransposer.AddUpAndDownKey(this, String.valueOf(mAppState.mPatternMaster.mBPM), new WidgetNoteTransposer.Listener() {
            @Override
            public String update(int inc) {
                mAppState.mPatternMaster.setBmp(mAppState.mPatternMaster.getBmp()+inc);
                return String.valueOf(mAppState.mPatternMaster.mBPM);
            }
        });
        toolbar.addView(noteControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
    }

    @Override
    public void onResume(){
        super.onResume();

        //mAppState.setLoop((int) (0 * 16 * 16), (int) (1 * 16 * 16));
        mPlay.setChecked(mAppState.isPlaying());

        if (eventSelected!=null)
        {
            eventSelected.mDuration = mAppState.mPatternMaster.mPatternDataBase.get(eventSelected.mId).GetLength();
        }

        generateIcons();
    }

    void rigControls()
    {
        LinearLayout headers = (LinearLayout) findViewById(R.id.headers);
        headers.removeAllViews();
        int channelCount = mAppState.mPatternMaster.GetChannelCount();

        mTracks = new Track[channelCount];

        for(int i=0;i<mTracks.length;i++) {
            final int finalI = i;
            mTracks[i] = new Track();
            mTracks[i].patternType = none;
            mTracks[i].trackControls = getLayoutInflater().inflate(R.layout.track_header, null);

            Button button = (Button)mTracks[i].trackControls.findViewById(R.id.set_instrument);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseTrackType(finalI);
                }
            });

            mTracks[i].trackNames = (TextView)mTracks[i].trackControls.findViewById(R.id.instrumentName);
            mTracks[i].trackNames.setText(String.valueOf(i));
            mTracks[i].trackControls.setOnTouchListener(new TextView.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        Toast.makeText(mContext, "Touched", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            // mute
            ToggleButton toggleMute = (ToggleButton)mTracks[i].trackControls.findViewById(R.id.toggleMute);
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
            mTracks[i].trackVolumes = (SeekBar)mTracks[i].trackControls.findViewById(R.id.seekBar);
            mTracks[i].trackVolumes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
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

            headers.addView(mTracks[i].trackControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 0, 1.0f));
        }

        setTrackNames();
    }


    void setTrackNames()
    {
        for(int i=0;i<mTracks.length;i++) {
            mTracks[i].trackVolumes.setProgress((int)(100*mAppState.mPatternMaster.getVolume(i)), false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    View.OnClickListener btnEditingListent = new View.OnClickListener() {
        Event copiedEvent = null;
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btnNew:
                    addPattern(mRowSelected, mTimeLine.getTime());
                    break;
                case R.id.btnEdit:
                    if (eventSelected!=null) {
                        //mAppState.setLoop(eventSelected.time, eventSelected.time + eventSelected.durantion);
                        editPattern(eventSelected);
                    }
                    break;
                case R.id.btnCopy: {
                    if (eventSelected != null)
                        copiedEvent = eventSelected;
                    else
                        copiedEvent = null;
                    break;
                }
                case R.id.btnPaste: {
                    if (copiedEvent != null) {
                        Event note = new Event();
                        note.mTime = mTimeLine.getTime();
                        note.mChannel = mRowSelected;
                        note.mDuration = copiedEvent.mDuration;
                        note.mId = copiedEvent.mId;

                        mAppState.mPatternMaster.Set(note);
                        break;
                    }
                    else
                    {
                        Toast.makeText(mContext, "Nothing to paste", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.btnDelete:
                    if (eventSelected!=null) {
                        mAppState.mPatternMaster.Clear(eventSelected.mChannel, eventSelected.mTime);
                        masterView.selectClear();
                        eventSelected = null;
                    } else {
                        Toast.makeText(mContext, "Nothing to delete", Toast.LENGTH_SHORT).show();
                    }

                    break;
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
            final FileChooser filesChooser = new FileChooser(this, mAppState.extStoreDir, "Load Song");
            filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                @Override
                public void fileSelected(String file)
                {
                    mAppState.Load(file, new ApplicationClass.EditorMetadata() {
                        @Override
                        public void Listener(JSONObject jsonObj) throws JSONException {
                            //here we can load some metadata for the editor
                            JSONArray jsonTracks = jsonObj.getJSONArray("tracks");
                            for(int i=0;i<jsonTracks.length();i++)
                            {
                                JSONObject jsonTrack = jsonTracks.getJSONObject(i);
                                String type = jsonTrack.getString("type");
                                PatternType pt = PatternType.valueOf(type);
                                mTracks[i].patternType = pt;
                            }
                        }
                    });
                    mTimeLine.init(mAppState.mPatternMaster, 16.0f);  //at scale 1 draw a vertical line every 16 ticks
                    timeLineView.init(mAppState.mPatternMaster, mTimeLine);
                    setTrackNames();
                    masterView.SetPattern(mAppState.mPatternMaster, mTimeLine,true,PatternBaseView.ViewMode.MAIN);
                    masterView.patternImgDataBase(mAppState.mPatternImgDataBase);
                    generateIcons();
                    masterView.invalidate();

                    //overwrite listener with our own
                    mAppState.mPatternMaster.SetBeatListener(new PatternBase.BeatListener() {
                        @Override
                        public void beat(float currentBeat) {
                            masterView.setCurrentBeat(currentBeat);
                            masterView.invalidate();
                            mTimeLine.setTime(mAppState.mPatternMaster.getTime());
                            timeLineView.invalidate();
                        }
                    });


                    InstrumentList instruments = InstrumentList.getInstance();
                    for(int i=0;i<mTracks.length;i++) {
                        InstrumentBase instrument = instruments.get(i);
                        if (instrument!=null) {
/*
                            if (instrument instanceof InstrumentPercussion)
                                mTracks[i].patternType = Percussion;
                            else if (instrument instanceof InstrumentSampler)
                                mTracks[i].patternType = PianoRoll;
                            else if (instrument instanceof PatternChord)
                                mTracks[i].patternType = Chords;
*/
                            mTracks[i].trackNames.setText(instrument.mInstrumentName);
                        }
                    }
                }

                @Override
                public void fileTouched(String file) {}
            });
            filesChooser.setExtension("json");
            filesChooser.showDialog();
            return true;
        }

        if (id == R.id.action_save) {
            final FileChooser filesChooser = new FileChooser(this, mAppState.extStoreDir, "Save Song");
            filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                @Override
                public void fileSelected(String file) {
                    mAppState.Save(file, new ApplicationClass.EditorMetadata() {
                        @Override
                        public void Listener(JSONObject jsonObj) throws JSONException {
                            //here we can save some metadata for the editor
                            JSONArray jsonTracks = new JSONArray();
                            for(int i=0;i<mTracks.length;i++)
                            {
                                JSONObject jsonTrack = new JSONObject();
                                jsonTrack.put("type", mTracks[i].patternType);
                                jsonTracks.put(jsonTrack);
                            }
                            jsonObj.put("tracks", jsonTracks);
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

        return super.onOptionsItemSelected(item);
    }

    private void editPattern(Event event)
    {
        if (event==null)
        {
            Toast.makeText(mContext, "Nothing to edit ", Toast.LENGTH_SHORT).show();
            return;
        }

        PatternBase pattern = mAppState.mPatternMaster.mPatternDataBase.get(event.mId);
        mAppState.mLastPatternAdded = pattern;
        mAppState.mLastPatternMixer = mAppState.mPatternMaster.mTracks[mRowSelected];

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

    void chooseTrackType(final int track)
    {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Choose track type");

        if (mTracks[track].patternType== none) {
            String[] animals = {"\uD83C\uDFB9 Piano Roll", "\uD83E\uDD41 Percussion", "\uD83C\uDFB5 Chords"};
            final PatternType[] patternTypes = {PianoRoll, Percussion, Chords};
            builder.setItems(animals, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mTracks[track].patternType = patternTypes[which];
                    chooseInstrument(mTracks[track].patternType, track);
                }
            });
            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else
        {
            chooseInstrument(mTracks[track].patternType, track);
        }
    }

    void chooseInstrument(PatternType patternType, int track)
    {
        switch (patternType) {
            case PianoRoll: choosePianoRollInstrument(track); break;
            case Percussion: choosePercussionInstrument(track); break;
            case Chords: // camel
        }
    }

    void choosePercussionInstrument(final int track) {
        InstrumentPercussion sample = new InstrumentPercussion();
        InstrumentList.getInstance().add(track, sample);
    }


    void choosePianoRollInstrument(final int track)
    {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Choose instrument");

        // add a list
        String[] animals = {"Sampler", "Synth basic", "Karplus-Strong"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: createInstrumentSampler(track); break;
                    case 1: createInstrumentSynthBasic(track); break;
                    case 2: createInstrumentKarplusStrong(track); break;
                }

                dialog.dismiss();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void createInstrumentSampler(final int track)
    {
        final FileChooser filesChooser = new FileChooser(this, mAppState.extStoreDir, "Load Sample");
        filesChooser.setExtension("ogg");
        filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final String file) {
            }  // we are loading the instrument on touch

            @Override
            public void fileTouched(final String file) {
                InstrumentSampler sample = new InstrumentSampler();
                sample.mSample.load(file);
                InstrumentList.getInstance().add(track, sample);
            }
        });

        filesChooser.showDialog();
    }

    void createInstrumentKarplusStrong(final int track)
    {
        InstrumentKarplusStrong sample = new InstrumentKarplusStrong();
        InstrumentList.getInstance().add(track, sample);
    }

    void createInstrumentSynthBasic(final int track)
    {
        InstrumentSynthBasic sample = new InstrumentSynthBasic();
        InstrumentList.getInstance().add(track, sample);
    }

    void CreatePianoRollActivity(final int track) {
        String filename = "filename";

        PatternPianoRoll pattern = new PatternPianoRoll(filename, mAppState.extStoreDir + "/" + filename, 24, 16);
        pattern.mInstrumentId = track;
        int id = mAppState.mPatternMaster.mPatternDataBase.size();
        mAppState.mPatternMaster.mPatternDataBase.put(id, pattern);
        mAppState.mLastPatternAdded = pattern;
        Intent intent = new Intent(mContext, ActivityPianoRoll.class);
        startActivity(intent);
    }


    private void addPattern(final int channel, final float time)
    {
        String filename = "";
        Intent intent = null;
        PatternBase pattern = null;
        switch(mTracks[channel].patternType)
        {
            case none:
                break;
            case Percussion:
                PatternPercussion patternPercussion = new PatternPercussion(filename, mAppState.extStoreDir+ "/"+filename, 16, 16);
                patternPercussion.mInstrumentId = channel;
                pattern = patternPercussion;
                intent = new Intent(mContext, ActivityPercussion.class);
                break;
            case Chords:
                pattern = new PatternChord(filename, mAppState.extStoreDir+ "/"+filename, 4*3, 16);
                intent = new Intent(mContext, ActivityChord.class);
                break;
            case PianoRoll:
                PatternPianoRoll patternPianoRoll = new PatternPianoRoll(filename, mAppState.extStoreDir+ "/"+filename, 24, 16);
                patternPianoRoll.mInstrumentId = channel;
                pattern = patternPianoRoll;
                intent = new Intent(mContext, ActivityPianoRoll.class);
                break;
            default:
                return;
        }

        int id = mAppState.mPatternMaster.mPatternDataBase.size();
        mAppState.mPatternMaster.mPatternDataBase.put(id , pattern);
        mAppState.mLastPatternAdded = pattern;
        mAppState.mLastPatternMixer = mAppState.mPatternMaster.mTracks[mRowSelected];

        Event note = new Event();
        note.mTime = time;
        note.mChannel = channel;
        note.mDuration = 16;
        note.mId = id;
        mAppState.mPatternMaster.Set(note);
        eventSelected = note;

        startActivity(intent);
        masterView.invalidate();
    }

    public void generateIcons()
    {
        PatternBaseView pbv = new PatternBaseView(this);
        TimeLine timeLine = new TimeLine();
        pbv.patternImgDataBase(null);
        for (Integer key : mAppState.mPatternMaster.mPatternDataBase.keySet()) {
            PatternBase pattern = mAppState.mPatternMaster.mPatternDataBase.get(key);

            timeLine.init(pattern, 1);
            pbv.SetPattern(pattern, timeLine,false, PatternBaseView.ViewMode.PIANO);
            Bitmap b = pbv.getBitmapFromView((int)(25 * pattern.GetLength()), 3 * pattern.mChannels);
            mAppState.mPatternImgDataBase.put(key, b);
        }
    }


    public boolean isStoragePermissionGranted() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
        }

        Log.v(TAG,"Permission is granted");

        try{
            if(mAppState.extStoreDir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
