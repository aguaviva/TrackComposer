package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class InstrumentBase {

    private boolean [] mPlayingChannels = new boolean [10];

    class ChannelStateBase
    {
        int mNote = 0;
        int mTimeInSamples = 0;
        float mVolume = 0;
        float mFreq = 0;
    };

    public int mInstrumentId = -1;
    protected String mInstrumentName = "none";
    boolean mPlaying = false;
    // will make this private at some point
    public int mBaseNote = 40;
    public float mBaseNoteFreq = 0.0f;

    private int mSampleRate = 0;
    private float mInvSampleRate = 0;

    public InstrumentBase() {
        mBaseNoteFreq = Misc.GetFrequency(mBaseNote);
    }

    void SetSampleRate(int sampleRate) { mSampleRate = sampleRate; mInvSampleRate = 1.0f/ (float)sampleRate; }
    int  GetSampleRate() { return mSampleRate; }
    float GetInvSampleRate() { return mInvSampleRate; }
    public int getLengthInFrames() {
        return -1;
    }
    ChannelStateBase getNewChannelState() { return new ChannelStateBase(); }

    public void playSample(int channel, float velociy) {
    }

    public void playSample(short[] chunk, int ini, int fin) {
    }

    public void reset() {
        for(int i=0;i<mPlayingChannels.length;i++) {
            mPlayingChannels[i] = false;
        }
    }

    public boolean isPlaying() {
        for(int i=0;i<mPlayingChannels.length;i++) {
            if (mPlayingChannels[i] == true)
                return true;
        }
        return false;
    }

    public boolean isChannelPlaying(int channel) {
        return mPlayingChannels[channel];
    }

    protected int GetAvailableChannel()
    {
        for(int i=0;i<mPlayingChannels.length;i++) {
            if (mPlayingChannels[i] == false) {
                mPlayingChannels[i] = true;
                return i;
            }
        }
        return -1;
    }

    protected void StopChannel(int channel)
    {
        mPlayingChannels[channel] = false;
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException {
        jsonObj.put("sampleId", mInstrumentId);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException {
        mInstrumentId = jsonObj.getInt("sampleId");
    }
}
