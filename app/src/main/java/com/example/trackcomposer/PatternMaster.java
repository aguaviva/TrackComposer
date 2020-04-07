package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

class PatternMaster extends PatternBase
{
    int mBPM = 120;

    public HashMap<Integer, PatternBase> mPatternDataBase = new HashMap<Integer, PatternBase>();
    float[] mVolume;

    public PatternMaster(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
        mVolume = new float[channels];
    }

    @Override
    void PlayBeat(Mixer sp, int beat, float volume)
    {
        int pattern = beat / length;
        pattern = pattern % length;

        CallBeatListener(pattern);

        for (int c = 0; c < channels; c++) {
            GeneratorInfo genI = Get(c, pattern);
            if (genI!=null) {
                PatternBase p =mPatternDataBase.get(genI.sampleId);
                if (p!=null)
                    if (mVolume[c]>0)
                        p.PlayBeat(sp, beat, mVolume[c]);
            }
        }
    }


    public void setVolume(int channel, float volume) { mVolume[channel]=volume;}
    public float getVolume(int channel) { return mVolume[channel];}

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONObject jsonInfo = new JSONObject();
        jsonInfo.put("BPM", mBPM);
        jsonObj.put("info", jsonInfo);


        JSONObject jsonPatterns = new JSONObject();
        JSONObject jsonNotes = new JSONObject();
        JSONObject jsonChords = new JSONObject();

        for (Integer key : mPatternDataBase.keySet()) {
            PatternBase channel = mPatternDataBase.get(key);
            if (channel!=null) {
                JSONObject jsonObj3 = new JSONObject();

                channel.serializeToJson(jsonObj3);

                if (channel instanceof PatternPercussion) {
                    jsonPatterns.put(String.valueOf(key), jsonObj3);
                }
                else if (channel instanceof PatternPianoRoll) {
                    jsonNotes.put(String.valueOf(key), jsonObj3);
                }
                else if (channel instanceof PatternChord) {
                    jsonChords.put(String.valueOf(key), jsonObj3);
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

        if (jsonObj.has("info")) {
            JSONObject jsonObj2 = jsonObj.getJSONObject("info");
            mBPM = jsonObj2.getInt("BPM");
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("patterns");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                PatternPercussion patern = new PatternPercussion("", "", 16, 16);
                mPatternDataBase.put(i, patern);
                patern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("notes");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                PatternPianoRoll patern = new PatternPianoRoll("", "", 16, 16);
                mPatternDataBase.put(i, patern);
                patern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("chords");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                PatternChord patern = new PatternChord("", "", 16, 16);
                mPatternDataBase.put(i, patern);
                patern.serializeFromJson(jsonObj3);
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
