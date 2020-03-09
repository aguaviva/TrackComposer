package com.example.trackcomposer;

import android.media.SoundPool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

class PatternMaster extends Pattern
{
    Pattern  [] mChannels;

    public PatternMaster(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
        mChannels = new Pattern[channels];
    }

    @Override
    String getChannelName(int channel)
    {
        Pattern ch = mChannels[channel];

        if (ch!=null)
            return ch.GetName();
        return null;
    }

    @Override
    void PlayBeat(MySoundPool sp, int beat)
    {
        int pattern = beat / length;
        pattern = pattern % length;

        for (int c = 0; c < channels; c++) {
            int note = hits[c][pattern];
            if (note>0) {
                Pattern p =mChannels[c];
                if (p!=null)
                    p.PlayBeat(sp, beat);
            }
        }
    }

    public void NewPatterns(Pattern pattern, int channel)
    {
        mChannels[channel] = pattern;
    }

    @Override
    void PatternToJson(JSONObject jsonObj) throws JSONException
    {
        super.PatternToJson(jsonObj);

        JSONObject jsonPatterns = new JSONObject();
        JSONObject jsonNotes = new JSONObject();
        int i=0;
        for (Pattern channel : mChannels) {
            if (channel!=null) {
                JSONObject jsonObj3 = new JSONObject();

                channel.PatternToJson(jsonObj3);

                if (channel instanceof PatternPercussion) {
                    jsonPatterns.put(String.valueOf(i), jsonObj3);
                }
                else if (channel instanceof PatternNote) {
                    jsonNotes.put(String.valueOf(i), jsonObj3);
                }
            }
            i++;
        }
        jsonObj.put("patterns", jsonPatterns);
        jsonObj.put("notes", jsonNotes);
    }

    @Override
    void PatternFromJson(MySoundPool sp, JSONObject jsonObj) throws JSONException
    {
        super.PatternFromJson(sp, jsonObj);

        mChannels = new Pattern[channels];
        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("patterns");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternPercussion(name, fileName, 16, 16);
                mChannels[i].PatternFromJson(sp, jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("notes");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternNote(name, fileName, 16, 16);
                mChannels[i].PatternFromJson(sp, jsonObj3);
            }
        }
    }

    @Override
    public void Load(MySoundPool sp) {
        super.Load(sp);
    }

    @Override
    public void Save() {
        super.Save();
    }
};
