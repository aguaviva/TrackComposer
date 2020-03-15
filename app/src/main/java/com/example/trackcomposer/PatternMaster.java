package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

class PatternMaster extends PatternBase
{
    PatternBase[] mChannels;
    float[] mVolume;

    public PatternMaster(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
        mChannels = new PatternBase[channels];
        mVolume = new float[channels];
    }

    @Override
    void PlayBeat(Mixer sp, int beat, float volume)
    {
        int pattern = beat / length;
        pattern = pattern % length;

        CallBeatListener(pattern);

        for (int c = 0; c < channels; c++) {
            int note = hits[c][pattern].hit;
            if (note>0) {
                PatternBase p =mChannels[c];
                if (p!=null)
                    p.PlayBeat(sp, beat, mVolume[c]);
            }
        }
    }

    public void NewPatterns(PatternBase pattern, int channel)
    {
        mChannels[channel] = pattern;
    }

    public void setVolume(int channel, float volume) { mVolume[channel]=volume;}
    public float getVolume(int channel) { return mVolume[channel];}

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONObject jsonPatterns = new JSONObject();
        JSONObject jsonNotes = new JSONObject();
        JSONObject jsonChords = new JSONObject();
        for (int i=0;i<mChannels.length;i++) {
            PatternBase channel = mChannels[i];
            if (channel!=null) {
                JSONObject jsonObj3 = new JSONObject();

                channel.serializeToJson(jsonObj3);

                if (channel instanceof PatternPercussion) {
                    jsonPatterns.put(String.valueOf(i), jsonObj3);
                }
                else if (channel instanceof PatternNote) {
                    jsonNotes.put(String.valueOf(i), jsonObj3);
                }
                else if (channel instanceof PatternChord) {
                    jsonChords.put(String.valueOf(i), jsonObj3);
                }
            }
        }
        jsonObj.put("patterns", jsonPatterns);
        jsonObj.put("notes", jsonNotes);
        jsonObj.put("chords", jsonChords);

        // put volumes
        {
            JSONArray jsonVolumes = new JSONArray();
            for (int i = 0; i < mVolume.length; i++) {
                jsonVolumes.put(mVolume[i]);
            }
            jsonObj.put("volumes", jsonVolumes);
        }
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        mChannels = new PatternBase[channels];
        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("patterns");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternPercussion("", "", 16, 16);
                mChannels[i].serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("notes");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternNote("", "", 16, 16);
                mChannels[i].serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("chords");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternChord("", "", 16, 16);
                mChannels[i].serializeFromJson(jsonObj3);
            }
        }

        if (jsonObj.has("volumes")) {
            JSONArray jsonVolumes = jsonObj.getJSONArray("volumes");
            if (jsonVolumes != null) {
                for (int i = 0; i < jsonVolumes.length(); i++) {
                    mVolume[i] = (float)jsonVolumes.getDouble(i);
                }
            }
        }

    }

};
