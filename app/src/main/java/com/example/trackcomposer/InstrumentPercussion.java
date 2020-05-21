package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InstrumentPercussion extends InstrumentBase {

    class Channel extends ChannelStateBase
    {
        Sample mSample = new Sample();
        boolean playSample(short[] chunk, int ini, int fin)
        {
            for (int i = ini; i < fin; i += 2) {
                if (mTimeInSamples >= mSample.getLengthInFrames()) {
                    return true;
                }

                mSample.copyFrame(i, chunk, mTimeInSamples, mVolume);

                mTimeInSamples++;
            }
            return false;
        }
    };

    Channel[] mChannels;

    public InstrumentPercussion()
    {
        super();
        SetSampleRate(44100);
        mInstrumentName = "InstrumentPercussion";

        mChannels = new Channel[10];
        for(int c=0;c<mChannels.length;c++) {
            mChannels[c] = new Channel();
        }
    }

    String GetChannelName(int channel){
        return mChannels[channel].mSample.getName();
    }

    @Override
    public void playSample(int note, float frequency, float duration) {
        Channel channel = mChannels[note];
        channel.mNote = note;
        channel.mTimeInSamples = 0;
        channel.mFreq = 0;
        channel.mVolume = 1.0f;
        //mPlayingChannels[note]=true;
    }

    @Override
    public void playSample(short[] chunk, int ini, int fin)
    {
        for(int c=0;c<mChannels.length;c++) {
            //if (isChannelPlaying(c)==false)
            //    continue;

            if (mChannels[c].playSample(chunk, ini, fin)) {
                StopChannel(c);
            }
        }
    }

    public boolean loadSample(int channel, String filename)
    {
        return mChannels[channel].mSample.load(filename);
    }

    @Override
    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONArray jsonArr = new JSONArray();
        for(int c=0;c<mChannels.length;c++) {
            if (mChannels[c].mSample!=null) {
                JSONObject jsonObj2 = new JSONObject();
                jsonObj2.put("index", c);
                mChannels[c].mSample.serializeToJson(jsonObj2);
                jsonArr.put(jsonObj2);
            }
        }
        jsonObj.put("sampleArray", jsonArr);
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        JSONArray jArr = jsonObj.getJSONArray("sampleArray");
        for(int c=0;c<jArr.length();c++) {
            JSONObject json = jArr.getJSONObject(c);
            int index = json.getInt("index");
            mChannels[c].mSample.serializeFromJson(json);
        }
    }

    public static String GetInstrumentType() { return "InstrumentPercussion"; };
}
