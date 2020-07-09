package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

class PatternMaster extends PatternBase
{
    int mBPM = 120;
    public int mSampleRate = 44100;
    public int mTempoInSamples;

    public HashMap<Integer, PatternBase> mPatternDataBase = new HashMap<Integer, PatternBase>();

    class Channel
    {
        float mVolume;
    };

    Channel[] mChannel;

    Mixer master;
    Mixer[] mTracks;

    public PatternMaster(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);

        master = new Mixer(100000);
        master.mMixerListener = new Mixer.MixerListener() {
            @Override
            public void AddNote(Mixer mixer, float noteTime, Event event){
                PatternBase p = mPatternDataBase.get(event.mId);
                Mixer.MixerListener listener = p.GetMixerListener();

                mTracks[event.mChannel].SetState(p.getIter());
                mTracks[event.mChannel].mMixerListener = listener;
                mTracks[event.mChannel].setTime(noteTime);
            }

            @Override
            public void PlayBeat(short[] chunk, int ini, int fin) {

                // do the mixing
                for (int c = 0; c < mTracks.length; c++) {
                    for (int i = ini; i < fin; i++)
                        mTracks[c].getChunk()[i]=0;
                }

                //render tracks
                for (int c = 0; c < mTracks.length; c++) {
                    mTracks[c].renderChunk(ini, fin);
                }

                // do the mixing
                for (int i = ini; i < fin; i++) {
                    float v = 0;
                    for (int c = 0; c < mTracks.length; c++) {
                        v +=  (mTracks[c].getChunk()[i] * mChannel[c].mVolume);
                    }
                    chunk[i] = (short)v;
                }
            }
        };


        mChannel = new Channel[channels];
        for (int c = 0; c < mChannel.length; c++) {
            mChannel[c] = new Channel();
        }

        mTracks = new Mixer[channels];
        for (int c = 0; c < mTracks.length; c++) {
            mTracks[c] = new Mixer(100000);
        }

        master.SetState(getIter());
        master.setTime(0);
    }

    public void setTime(float time) {
        master.setTime(time);
    }

    public float getTime() {
        return master.getTime();
    }

    void PlayBeat(short[] chunk, int ini, int fin, float volume)
    {
        CallBeatListener(master.getTime());

        master.renderChunk(ini, fin);

        for (int i = ini; i < fin; i++) {
            chunk[i] =  master.getChunk()[i];
        }
    }

    public void setVolume(int channel, float volume) {
        mChannel[channel].mVolume=volume;
    }
    public float getVolume(int channel) {
        return mChannel[channel].mVolume;
    }

    public int getBmp() { return mBPM; }
    public void setBmp(int beatsPerMinute)
    {
        mBPM = beatsPerMinute;
        float delaySecs = (60.0f/((float)beatsPerMinute)) / 2.0f;
        mTempoInSamples = (int)(delaySecs * mSampleRate);

        master.mTempoInSamples = mTempoInSamples;
        for (int c = 0; c < mTracks.length; c++)
            mTracks[c].mTempoInSamples = mTempoInSamples;
    }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONObject jsonInfo = new JSONObject();
        jsonInfo.put("BPM", mBPM);
        jsonObj.put("info", jsonInfo);

        JSONObject jsonPercussion = new JSONObject();
        JSONObject jsonPianoRoll = new JSONObject();
        JSONObject jsonChords = new JSONObject();
        JSONObject jsonVocals = new JSONObject();

        for (Integer key : mPatternDataBase.keySet()) {
            PatternBase channel = mPatternDataBase.get(key);
            if (channel!=null) {
                JSONObject jsonObj3 = new JSONObject();

                channel.serializeToJson(jsonObj3);

                if (channel instanceof PatternPercussion) {
                    jsonPercussion.put(String.valueOf(key), jsonObj3);
                }
                else if (channel instanceof PatternPianoRoll) {
                    jsonPianoRoll.put(String.valueOf(key), jsonObj3);
                }
                else if (channel instanceof PatternChord) {
                    jsonChords.put(String.valueOf(key), jsonObj3);
                }
                else if (channel instanceof PatternVocals) {
                    jsonVocals.put(String.valueOf(key), jsonObj3);
                }
            }
        }
        jsonObj.put("percussion", jsonPercussion);
        jsonObj.put("pianoRoll", jsonPianoRoll);
        jsonObj.put("chords", jsonChords);
        jsonObj.put("vocals", jsonVocals);
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

        if (jsonObj.has("info")) {
            JSONObject jsonObj2 = jsonObj.getJSONObject("info");
            setBmp(jsonObj2.getInt("BPM"));
        }

        {
            JSONObject jsonPercussion = jsonObj.getJSONObject("percussion");
            Iterator<String> iter = jsonPercussion.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonPercussion.getJSONObject(key);
                PatternPercussion pattern = new PatternPercussion("", "", 16, 16);
                mPatternDataBase.put(i, pattern);
                pattern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonPianoRoll = jsonObj.getJSONObject("pianoRoll");
            Iterator<String> iter = jsonPianoRoll.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonPianoRoll.getJSONObject(key);
                PatternPianoRoll pattern = new PatternPianoRoll("", "", 16, 16);
                mPatternDataBase.put(i, pattern);
                pattern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonChords = jsonObj.getJSONObject("chords");
            Iterator<String> iter = jsonChords.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonChords.getJSONObject(key);
                PatternChord pattern = new PatternChord("", "", 16, 16);
                mPatternDataBase.put(i, pattern);
                pattern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonChords = jsonObj.getJSONObject("vocals");
            Iterator<String> iter = jsonChords.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonChords.getJSONObject(key);
                PatternVocals pattern = new PatternVocals("", "", 16, 16);
                mPatternDataBase.put(i, pattern);
                pattern.serializeFromJson(jsonObj3);
            }
        }

        if (jsonObj.has("volumes")) {
            JSONArray jsonVolumes = jsonObj.getJSONArray("volumes");
            if (jsonVolumes != null) {
                mChannel = new Channel[2*jsonVolumes.length()];
                for (int c = 0; c < mChannel.length; c++)
                    mChannel[c] = new Channel();
                for (int i = 0; i < jsonVolumes.length(); i++) {
                    mChannel[i].mVolume = (float)jsonVolumes.getDouble(i);
                }
            }
        }

        master.SetState(getIter());
        master.setTime(0);
    }
};
