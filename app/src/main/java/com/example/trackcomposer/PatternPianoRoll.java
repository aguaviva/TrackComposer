package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class PatternPianoRoll extends PatternBase
{
    int mInstrumentId = -1;
    int mBaseNote = 40; //c4 - 261.6256

    Mixer.MixerListener mMixerListener = new Mixer.MixerListener() {
        @Override
        public void AddNote(Mixer mixer, float noteTime, Event event){
            play(event, 1.0f);
        }

        @Override
        public void PlayBeat(short[] chunk, int ini, int fin) {
            InstrumentBase g = InstrumentList.getInstance().get(mInstrumentId);
            g.playSample(chunk, ini, fin);
        }
    };

    public PatternPianoRoll(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    public void play(Event event, float volume)
    {
        InstrumentBase g = InstrumentList.getInstance().get(mInstrumentId);
        g.playSample(event.mChannel, Misc.GetFrequency(event.mChannel), event.mDuration/4, volume);
    }

    @Override
    Mixer.MixerListener GetMixerListener() { return mMixerListener; }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", mInstrumentId);
        jsonObj.put("baseNote", mBaseNote);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        mInstrumentId = jsonObj.getInt("sampleId");
        mBaseNote = jsonObj.getInt("baseNote");
    }
};
