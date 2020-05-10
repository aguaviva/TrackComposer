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

        master = new Mixer();
        master.mMixerListener = new Mixer.MixerListener() {
            @Override
            public void AddNote(float noteTime, Event event){
                PatternBase p = mPatternDataBase.get(event.id);
                Mixer.MixerListener listener = p.GetMixerListener();

                mTracks[event.channel].SetState(p.getIter());
                mTracks[event.channel].mMixerListener = listener;
                mTracks[event.channel].setTime(noteTime);//ch.timeInSamples/master.mTempoInSamples);
            }

            @Override
            public void PlayBeat(short[] chunk, int ini, int fin, float volume) {
                for (int c = 0; c < mTracks.length; c++) {
                    mTracks[c].renderChunk(chunk, ini, fin, 1);
                }
            }
        };

        mTracks = new Mixer[channels];
        mChannel = new Channel[channels];
        for (int c = 0; c < mTracks.length; c++) {
            mTracks[c] = new Mixer();
            mChannel[c] = new Channel();
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
        master.renderChunk(chunk, ini, fin, volume);
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

            }
        }
        jsonObj.put("percussion", jsonPercussion);
        jsonObj.put("pianoRoll", jsonPianoRoll);
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

        master.SetState(getIter());
        master.setTime(0);
    }
};
