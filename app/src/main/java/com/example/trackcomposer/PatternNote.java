package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class PatternNote extends Pattern
{
    int sampleId = -1;
    int baseNote = 40;
    public PatternNote(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    @Override
    public void Play(Mixer sp, int note)
    {
        if (sampleId>=0)
        {
            float speed = Misc.GetFrequency(baseNote + note) / Misc.GetFrequency(baseNote);
            sp.play(sampleId, 0, speed);
        }
    }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", sampleId);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        sampleId = jsonObj.getInt("sampleId");
    }

};
