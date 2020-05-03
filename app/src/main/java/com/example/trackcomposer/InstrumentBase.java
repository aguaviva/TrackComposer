package com.example.trackcomposer;

import android.app.Activity;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class InstrumentBase {

    boolean [] mPlayingChannels = new boolean [10];

    public int sampleId = -1;
    public String instrumentName = "none";
    int timeInSamples= 0;
    boolean mPlaying = false;
    // will make this private at some point
    public int baseNote = 40;
    public float baseNoteFreq = 0.0f;

    private int mSampleRate = 0;
    private float mInvSampleRate = 0;

    protected int mTracks = 0;

    public InstrumentBase() {
        baseNoteFreq = Misc.GetFrequency(baseNote);
    }

    void SetSampleRate(int sampleRate) { mSampleRate = sampleRate; mInvSampleRate = 1.0f/ (float)sampleRate; }
    int  GetSampleRate() { return mSampleRate; }
    float GetInvSampleRate() { return mInvSampleRate; }

    public int getLengthInFrames() {
        return -1;
    }

    public void playSample(int channel, float velociy) {
    }

    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin) {
    }

    protected int GetAvailableChannel()
    {
        for(int i=0;i<10;i++) {
            if (mPlayingChannels[i] == false) {
                mPlayingChannels[i] = true;
                return i;
            }
        }
        return -1;
    }

    protected void StopChannel(int i)
    {
        mPlayingChannels[i] = false;
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException {
        jsonObj.put("sampleId", sampleId);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException {
        sampleId = jsonObj.getInt("sampleId");
    }
}
