package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternChord extends PatternBase
{
    int sampleId = -1;
    int baseNote = 40;

    public PatternChord(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    public int KeyToNote(int n)
    {
        int f = n / 3;

        //get root of progression
        int root = Misc.getFifthsProgression(baseNote, f);

        //get choord's note
        int ch = Misc.getTriadChord(root, n % 3, (f!=3));

        return ch;
    }

    @Override
    public void Play(Mixer sp, int note, float volume)
    {
        if (sampleId>=0)
        {
            float speed = Misc.GetFrequency(KeyToNote(note)) / Misc.GetFrequency(baseNote);
            sp.play(sampleId, 0, speed, volume);
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