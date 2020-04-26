package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class PatternPercussion extends PatternBase
{
    public int[] mChannels;

    public PatternPercussion(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);

        mChannels = new int[channels];
        for (int i = 0; i < mChannels.length; i++) {
            mChannels[i] = -1;
        }
    }
/*
    @Override
    public void Play(Mixer sp, int note, float volume)
    {
        if (mChannels[note]>=0)
            sp.play(mChannels[note], 0, Misc.GetFrequency(40), volume);
    }
*/
    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONArray jsonObj2 = new JSONArray();
        for (int sampleId : mChannels) {
            jsonObj2.put(sampleId);
        }
        jsonObj.put("sampleId", jsonObj2);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        JSONArray jsonObj2 = jsonObj.getJSONArray("sampleId");
        for (int i=0;i<mChannels.length;i++) {
            mChannels[i] = jsonObj2.getInt(i);
        }
    }
};
