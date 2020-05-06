package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class InstrumentSampler extends InstrumentBase {

    public String instrumentFilename = "none";

    class Channel extends ChannelStateBase
    {
        float mSpeed;
    };

    Channel [] mChannels = new Channel[10];

    Sample mSample = new Sample();

    public InstrumentSampler()
    {
        super();
        SetSampleRate(44100);
        instrumentName = "InstrumentSampler";

        for(int c=0;c<mChannels.length;c++) {
            mChannels[c] = new Channel();
        }
    }

    float freqOne = Misc.GetFrequency(40);

    short GetSample(int i) { return mSample.sampleData[i]; }

    @Override
    ChannelStateBase getNewChannelState() { return new Channel(); }

    @Override
    public void playSample(int note, float freq) {
        int channelId = GetAvailableChannel();
        if (channelId>=0) {
            Channel channel = mChannels[channelId];
            channel.mNote = note;
            channel.mTimeInSamples = 0;
            channel.mFreq = freq;
            channel.mVolume = 1.0f;
            channel.mSpeed = freq/freqOne;
        }
    }

    @Override
    public void playSample(short[] chunk, int ini, int fin)
    {
        if (mSample.sampleData == null)
        {
            return;
        }

        for(int c=0;c<mChannels.length;c++) {
            if (mPlayingChannels[c]==false)
                continue;

            Channel channel = mChannels[c];

            float t = channel.mTimeInSamples * channel.mSpeed;

            for (int i = ini; i < fin; i += 2) {
                int idx = (int) (t);
                if (idx >= mSample.getLengthInFrames()) {
                    StopChannel(c);
                    break;
                }

                mSample.copyFrame(i, chunk, idx, channel.mVolume);

                t += channel.mSpeed;
                channel.mTimeInSamples++;
            }
        }
    }

    @Override
    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);
        JSONObject jsonObj2 = new JSONObject();
        jsonObj2.put("fileName", instrumentFilename);
        jsonObj2.put("baseNote", baseNote);
        jsonObj.put("sample", jsonObj2);
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);
        JSONObject jsonObj2 = jsonObj.getJSONObject("sample");
        instrumentFilename = jsonObj2.getString("fileName");
        baseNote = jsonObj2.getInt("baseNote");
    }
}
