package com.example.trackcomposer;

import android.media.SoundPool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

class PatternNote extends Pattern
{
    Sample sample = new Sample();
    int baseNote = 40;
    public PatternNote(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    String getChannelName(int n)
    {
        return sample.getNoteName(n+baseNote);
    }

    @Override
    public void Play(MySoundPool sp, int note)
    {
        sample.Play(sp, baseNote+ note);
    }


    @Override
    void PatternToJson(JSONObject jsonObj) throws JSONException {
        super.PatternToJson(jsonObj);
        jsonObj.put("baseNote", baseNote);
        sample.PatternToJson(jsonObj);
    }

    @Override
    void PatternFromJson(MySoundPool sp, JSONObject jsonObj) throws JSONException {
        super.PatternFromJson(sp, jsonObj);
        //baseNote = jsonObj.getInt("baseNote");
        sample.PatternFromJson(sp, jsonObj);

    }

};
