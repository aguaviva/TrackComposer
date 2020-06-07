package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class PatternVocals extends PatternBase
{
    int mInstrumentId = -1;

    Mixer.MixerListener mMixerListener = new Mixer.MixerListener() {
        @Override
        public void AddNote(Mixer mixer, float noteTime, Event event){

            int time = mixer.getTimeInSamples(event);
            play(time, event);
        }

        @Override
        public void PlayBeat(short[] chunk, int ini, int fin, float volume) {
            InstrumentBase g = InstrumentList.getInstance().get(mInstrumentId);
            g.playSample(chunk, ini, fin);
        }
    };

    public PatternVocals(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    public void play(int noteTime, Event event)
    {
        InstrumentVocals g = (InstrumentVocals)InstrumentList.getInstance().get(mInstrumentId);
        g.playSample(event.mChannel, Misc.GetFrequency(event.mChannel), event.mDuration/4, noteTime);
    }

    @Override
    Mixer.MixerListener GetMixerListener() { return mMixerListener; }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", mInstrumentId);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        mInstrumentId = jsonObj.getInt("sampleId");
    }
};