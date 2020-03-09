package com.example.trackcomposer;

import android.media.SoundPool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

class PatternPercussion extends Pattern
{
    public Sample [] mChannels;
    int baseNote = 40;

    public PatternPercussion(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);

        mChannels = new Sample[channels];
        for(int i=0;i<channels;i++)
        {
            mChannels[i] = new Sample();
        }
    }

    @Override
    String getChannelName(int channel)
    {
        Sample ch = mChannels[channel];
        if (ch!=null)
            return mChannels[channel].instrumentName;
        return null;
    }

    @Override
    public void Play(MySoundPool sp, int note)
    {
        if (mChannels[note]!=null)
            mChannels[note].Play(sp, baseNote);
    }

    @Override
    void PatternToJson(JSONObject jsonObj) throws JSONException
    {
        super.PatternToJson(jsonObj);

        JSONObject jsonObj2 = new JSONObject();
        int i=0;
        for (Sample channel : mChannels) {
            if (channel!=null) {
                JSONObject jsonObj3 = new JSONObject();
                channel.PatternToJson(jsonObj3);
                jsonObj2.put(String.valueOf(i), jsonObj3);
            }
            i++;
        }
        jsonObj.put("instruments", jsonObj2);
    }

    @Override
    void PatternFromJson(MySoundPool sp, JSONObject jsonObj) throws JSONException
    {
        super.PatternFromJson(sp, jsonObj);

        JSONObject jsonObj2 = jsonObj.getJSONObject("instruments");
        mChannels = new Sample[channels];
        Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
        while(iter.hasNext()){
            String key = iter.next();
            int i = Integer.parseInt(key);
            mChannels[i] = new Sample();
            mChannels[i].PatternFromJson(sp,  jsonObj2.getJSONObject(key));
        }
    }
};
