package com.example.trackcomposer;

import android.app.Activity;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Generator {
    public int sampleId = -1;
    public String instrumentName = "none";

    // will make this private at some point
    public int baseNote = 40;
    public float baseNoteFreq = 0.0f;

    private int mSampleRate = 0;
    private float mInvSampleRate = 0;

    protected int mTracks = 0;


    public Generator() {
        baseNoteFreq = Misc.GetFrequency(baseNote);
    }

    void SetSampleRate(int sampleRate) { mSampleRate = sampleRate; mInvSampleRate = 1.0f/ (float)sampleRate; }
    int  GetSampleRate() { return mSampleRate; }
    float GetInvSampleRate() { return mInvSampleRate; }
    int  GetTrackCount() { return mTracks; }

    public int getLengthInFrames() {
        return -1;
    }

    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin) {
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException {
        jsonObj.put("sampleId", sampleId);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException {
        sampleId = jsonObj.getInt("sampleId");
    }

    public void load(String path)
    {
    }

    private void instrumentChooser(Activity activity, final int channel)
    {
        File directory = new File(Environment.getExternalStorageDirectory() + "/TrackComposer");
        FileChooser filesChooser = new FileChooser(activity, directory, "Load");
        filesChooser.setExtension("ogg");
        filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final String file) { load(file); }
            @Override public void fileTouched(final String file) { load(file); }
        });

        filesChooser.showDialog();
    }
}
