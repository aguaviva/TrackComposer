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
        mInstrumentName = "InstrumentSampler";

        for(int c=0;c<mChannels.length;c++) {
            mChannels[c] = new Channel();
        }
    }

    float freqOne = Misc.GetFrequency(40);

    @Override
    ChannelStateBase getNewChannelState() { return new Channel(); }

    @Override
    public void playSample(int note, float frequency, float duration, float volume) {
        int channelId = GetAvailableChannel();
        if (channelId>=0) {
            Channel channel = mChannels[channelId];
            channel.mNote = note;
            channel.mTimeInSamples = 0;
            channel.mFreq = frequency;
            channel.mVolume = volume;
            channel.mSpeed = frequency/freqOne;
        }
    }

    @Override
    public void playSample(short[] chunk, int ini, int fin)
    {
        if (mSample.isSampleValid() == false)
        {
            return;
        }

        for(int c=0;c<mChannels.length;c++) {
            if (isChannelPlaying(c)==false)
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
        mSample.serializeToJson(jsonObj);
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);
        mSample.serializeFromJson(jsonObj);
    }

    public static String GetInstrumentType() { return "InstrumentSampler"; };
}
