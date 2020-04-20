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

    class Channel
    {
        int id=-1;
        int time;
        float mVolume;
    };

    Channel[] mChannel;


    public PatternMaster(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
        mChannel = new Channel[channels];
        for (int c = 0; c < mChannel.length; c++)
            mChannel[c] = new Channel();
    }

    @Override
    void PlayBeat(Mixer sp, float time, float volume)
    {
        for (int c = 0; c < GetChannelCount(); c++) {
            Channel ch = mChannel[c];
            if (ch.id==-1) {
                GeneratorInfo gi =  Get(c, (int)time);
                if (gi!=null) {
                    ch.id = gi.sampleId;
                    ch.time = 0;
                }
            }
        }

        CallBeatListener(time/256);

        // play channels
        for (int c = 0; c < mChannel.length; c++) {
            Channel ch = mChannel[c];
            if (ch.id>=0) {
                PatternBase p = mPatternDataBase.get(ch.id);
                if (p != null) {
                    if (ch.mVolume > 0) {
                        p.PlayBeat(sp, ch.time, ch.mVolume);
                    }
                }

                ch.time++;
                if (ch.time >= p.length)
                    ch.id = -1;
            }
        }

    }

    public void setVolume(int channel, float volume) { mChannel[channel].mVolume=volume;}
    public float getVolume(int channel) { return mChannel[channel].mVolume;}

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
            for (int i = 0; i < mChannel.length; i++) {
                jsonVolumes.put(mChannel[i].mVolume);
            }
            jsonObj.put("volumes", jsonVolumes);
        }
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        //ScaleTime(16);

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
                mChannel = new Channel[jsonVolumes.length()];
                for (int c = 0; c < mChannel.length; c++)
                    mChannel[c] = new Channel();
                for (int i = 0; i < jsonVolumes.length(); i++) {
                    mChannel[i].mVolume = (float)jsonVolumes.getDouble(i);
                }
            }
        }

    }

};
