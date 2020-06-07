package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class InstrumentVocals  extends InstrumentBase {

    public String instrumentFilename = "vocals";

    ChannelStateBase mChannel = new ChannelStateBase();

    public Sample mSample = new Sample();

    public InstrumentVocals()
    {
        super();
        SetSampleRate(44100);
        mInstrumentName = "InstrumentVocals";
    }

    float freqOne = Misc.GetFrequency(40);

    //@Override
    //ChannelStateBase getNewChannelState() { return new Channel(); }

    @Override
    public int getLengthInFrames() {
        return mSample.getLengthInFrames();
    }

    public float getLengthInSeconds() {
        return mSample.getLengthInSeconds();
    }

    public void playSample(int note, float frequency, float duration, int noteTime) {
        mChannel.mNote = note;
        mChannel.mTimeInSamples = noteTime;
        mChannel.mFreq = frequency;
        mChannel.mVolume = 1.0f;
        //mChannel.mSpeed = frequency/freqOne;
    }

    @Override
    public void playSample(int note, float frequency, float duration) {
        playSample(note, frequency, duration, 0);
    }

    @Override
    public void playSample(short[] chunk, int ini, int fin)
    {
        if (mSample.isSampleValid() == false)
        {
            return;
        }

        for (int i = ini; i < fin; i += 2) {
            int idx = (int) (mChannel.mTimeInSamples);
            if (idx >= mSample.getLengthInFrames()) {
                break;
            }

            mSample.copyFrame(i, chunk, idx, mChannel.mVolume);

            mChannel.mTimeInSamples++;
        }
    }

    @Override
    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);
        mSample.serializeToJson(jsonObj);
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);
        mSample.serializeFromJson(jsonObj);
    }

    public static String GetInstrumentType() { return "InstrumentVocals"; };
}
