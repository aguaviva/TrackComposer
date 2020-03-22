package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class PatternNote extends PatternBase
{
    int sampleId = -1;
    int baseNote = 40;

    public PatternNote(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    @Override
    public void Play(Mixer sp, int note, float volume)
    {
        if (sampleId>=0)
        {
            sp.play(sampleId, 0, Misc.GetFrequency(baseNote + note), volume);
        }
    }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", sampleId);
        jsonObj.put("baseNote", baseNote);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        sampleId = jsonObj.getInt("sampleId");
        baseNote = jsonObj.getInt("baseNote");
    }

};
