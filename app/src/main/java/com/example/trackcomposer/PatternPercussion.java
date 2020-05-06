package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class PatternPercussion extends PatternBase
{
    int sampleId = -1;

    Mixer.MixerListener mMixerListener = new Mixer.MixerListener() {
        @Override
        public void AddNote(Event event){
            play(event);
        }

        @Override
        public void PlayBeat(short[] chunk, int ini, int fin, float volume) {
            InstrumentBase g = InstrumentList.getInstance().get(sampleId);
            g.playSample(chunk, ini, fin);
        }
    };

    public PatternPercussion(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    public void play(Event event)
    {
        InstrumentBase g = InstrumentList.getInstance().get(sampleId);
        g.playSample(event.channel, 0);
    }

    @Override
    Mixer.MixerListener GetMixerListener() { return mMixerListener; }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", sampleId);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        sampleId = jsonObj.getInt("sampleId");
    }
};
