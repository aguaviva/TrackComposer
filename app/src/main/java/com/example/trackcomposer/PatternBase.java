package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatternBase {
    String type, name, fileName;
    int channels;
    int length;
    GeneratorInfo[][] hits;

    public PatternBase(String name, String fileName, int channels, int length)
    {
        this.name=name;
        this.fileName = fileName;
        this.channels = channels;
        this.length = length;
        hits = new GeneratorInfo[channels][length];

        for (int c = 0; c < GetChannelCount(); c++)
        {
            for (int l = 0; l < GetLength(); l++)
            {
                Set(c, l, new GeneratorInfo());
            }
        }
    }

    String GetName() { return name;}
    int GetLength() { return length; }
    int GetChannelCount() { return channels; }
    GeneratorInfo Get(int channel, int pos) { return hits[channel][pos]; }
    void Set(int channel, int pos, GeneratorInfo hit)
    {
        hits[channel][pos] = hit;
    }

    public interface BeatListener { public void beat(int currentBeat); }
    BeatListener mBeatListener;
    public void SetBeatListener(BeatListener beatListener) { mBeatListener = beatListener;}
    public void CallBeatListener(int currentBeat)
    {
        if (mBeatListener!=null)
            mBeatListener.beat(currentBeat);
    }

    void PlayBeat(Mixer sp, int beat, float volume)
    {
        beat = beat % length;
        CallBeatListener(beat);
        for (int c = 0; c < channels; c++) {
            GeneratorInfo note = hits[c][beat];
            if (note.hit>0) {
                Play(sp, c, volume);
            }
        }
    }

    public void Play(Mixer sp, int note, float volume)
    {

    }

    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("name", name);
        jsonObj.put("length", length);
        jsonObj.put("channels", channels);

        JSONArray jsonArrC = new JSONArray();
        for (int c = 0; c < GetChannelCount(); c++)
        {
            JSONArray jsonArrL = new JSONArray();
            for (int l = 0; l < GetLength(); l++)
            {
                JSONObject json = new JSONObject();
                Get(c, l).serializeToJson(json);
                jsonArrL.put(json);
            }
            jsonArrC.put(jsonArrL);
        }
        jsonObj.put("pattern", jsonArrC);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        name = jsonObj.getString("name");
        length = jsonObj.getInt("length");
        channels = jsonObj.getInt("channels");
        hits = new GeneratorInfo[channels][length];
        JSONArray jArrC = jsonObj.getJSONArray("pattern");
        for (int c = 0; c < jArrC.length(); c++)
        {
            JSONArray jArrL = jArrC.getJSONArray(c);
            for (int l = 0; l < jArrL.length(); l++)
            {
                Set(c, l, new GeneratorInfo());
                Get(c, l).serializeFromJson(jArrL.getJSONObject(l));
            }
        }
    }
}
