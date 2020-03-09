package com.example.trackcomposer;

import android.media.SoundPool;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class Sample {
    public int sampleId = -1;
    public String instrumentName = "none";
    public String instrumentFilename = "none";

    int baseNote = 40;
    float baseNoteFreq = 0.0f;

    public Sample()
    {
        baseNoteFreq = GetFrequency(baseNote);
    }

    public void LoadSample(MySoundPool sp, String path) {
        File f = new File(path);
        String fileName = f.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos != -1) {
            fileName = fileName.substring(0, pos);
        }

        instrumentFilename = path;
        instrumentName = fileName;
        sampleId = sp.load(path, 0);
    }

    public static String getNoteName(int n)
    {
        String [] notes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
        int octave = (n+8)/12;
        int noteIdx = (n-1)%12;
        return notes[noteIdx] + String.valueOf(octave);
    }

    public static float GetFrequency(int n)
    {
        float exponent = ((float)n-49.0f)/12.0f;
        return (float)Math.pow(2.0f,  exponent) * 440.0f;
    }

    public void Play(MySoundPool sp, int note)
    {
        float freq = GetFrequency(note);
        float speed = freq/GetFrequency(baseNote);
        sp.play(sampleId, 1, speed);
    }

    //@Override
    public void PatternToJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObj2 = new JSONObject();
        jsonObj2.put("fileName", instrumentFilename);
        jsonObj2.put("baseNote", baseNote);
        jsonObj.put("sample", jsonObj2);
    }

    //@Override
    public void PatternFromJson(MySoundPool sp, JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObj2 = jsonObj.getJSONObject("sample");
        instrumentFilename = jsonObj2.getString("fileName");
        baseNote = jsonObj2.getInt("baseNote");

        if (instrumentFilename.contentEquals("none")==false)
            LoadSample(sp, instrumentFilename);
    }
}
