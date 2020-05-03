package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class InstrumentSampler extends InstrumentBase {

    public String instrumentFilename = "none";

    class Channel
    {
        int mNote = 0;
        int mTimeInSamples = 0;
        int mVolume = 0;
        float mSpeed = 0;
    };

    Channel [] mChannels = new Channel[10];

    Sample mSample;

    public InstrumentSampler()
    {
        super();
    }

    short GetSample(int i) { return mSample.sampleData[i]; }

    @Override
    public void playSample(int note, float speed) {

        int channelId = GetAvailableChannel();

        Channel channels = mChannels[channelId];
        channels.mNote = note;
        channels.mTimeInSamples = 0;
        channels.mSpeed = speed;
    }

    @Override
    public void playSample(Mixer.Channel foo, short[] chunk, int ini, int fin)
    {
        if (mSample.sampleData == null)
        {
            return;
        }

        for(int c=0;c<mChannels.length;c++) {

            Channel channel = mChannels[c];
            if (mPlayingChannels[c]==false)
                continue;

            float t = channel.mTimeInSamples * channel.mSpeed;

            for (int i = ini; i < fin; i += 2) {
                int idx = (int) (t);
                if (mTracks * idx >= mSample.sampleData.length) {
                    StopChannel(i);
                    break;
                }

                if (mTracks == 2) {
                    chunk[i + 0] += (short) (channel.mVolume * mSample.sampleData[2 * idx + 0]);
                    chunk[i + 1] += (short) (channel.mVolume * mSample.sampleData[2 * idx + 1]);
                } else if (mTracks == 1) {
                    chunk[i + 0] += (short) (channel.mVolume * mSample.sampleData[idx]);
                    chunk[i + 1] += (short) (channel.mVolume * mSample.sampleData[idx]);
                }

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
