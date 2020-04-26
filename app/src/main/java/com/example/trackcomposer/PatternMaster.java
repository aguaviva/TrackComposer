package com.example.trackcomposer;

import android.util.Log;

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

        master = new Mixer(InstrumentList.getInstance());
        master.mMixerListener = new Mixer.MixerListener() {
            @Override
            public void AddNote(Mixer.Channel ch){

                PatternBase p = mPatternDataBase.get(ch.mEvent.mGen.sampleId);
                Mixer.MixerListener listener = p.GetMixerListener();

                mTracks[ch.mEvent.channel].iter = p.getIter();
                mTracks[ch.mEvent.channel].mMixerListener = listener;
                ch.volume = mChannel[ch.mEvent.channel].mVolume;
            }

            @Override
            public void PlayBeat(Mixer.Channel ch, short[] chunk, int ini, int fin, float volume) {
            }
        };

        mTracks = new Mixer[channels];
        mChannel = new Channel[channels];
        for (int c = 0; c < mTracks.length; c++) {
            mTracks[c] = new Mixer(InstrumentList.getInstance());
            mChannel[c] = new Channel();
        }

        master.iter = getIter();
        master.iter.setTime(0);
        master.iter.mTime = 0;
        master.iter.mNextTime = 0;
    }

    public void setTime(float time) {
        master.iter.setTime((int)(time * 9800));
    }

    public float getTime() {
        return (float)master.iter.mTime/9800;
    }

    void PlayBeat(short[] chunk, int ini, int fin, float volume)
    {
        CallBeatListener(master.iter.mTime/9800);

        master.renderChunk(chunk, ini, fin, volume);

        for (int c = 0; c < mTracks.length; c++) {
            if (mTracks[c].iter!=null) {
                if (mChannel[c].mVolume>0) {
                    mTracks[c].renderChunk(chunk, ini, fin, mChannel[c].mVolume);
                }
            }
        }
    }

    public void setVolume(int channel, float volume) {
        mChannel[channel].mVolume=volume;
    }

    public float getVolume(int channel)
    {
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
            setBmp(jsonObj2.getInt("BPM"));
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("patterns");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                PatternPercussion pattern = new PatternPercussion("", "", 16, 16);
                mPatternDataBase.put(i, pattern);
                pattern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("notes");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                PatternPianoRoll pattern = new PatternPianoRoll("", "", 16, 16);
                mPatternDataBase.put(i, pattern);
                pattern.serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("chords");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
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

        master.iter = getIter();
        master.iter.setTime(0);
        master.iter.mTime = 0;
        master.iter.mNextTime = 0;
    }
};
