package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class PatternPianoRoll extends PatternBase
{
    int sampleId = -1;
    int baseNote = 40; //c4 - 261.6256

    Mixer.MixerListener mMixerListener = new Mixer.MixerListener() {
        @Override
        public void AddNote(Mixer.Channel ch){

            float freq = Misc.GetFrequency(ch.mEvent.channel);
            float freqBase = Misc.GetFrequency(InstrumentList.getInstance().get(ch.mEvent.mGen.sampleId).baseNote);

            ch.speed = freq / freqBase;
            ch.timeInSamples = 0;
            ch.volume = 0.5f;
            ch.mPlaying = true;
        }

        @Override
        public void PlayBeat(Mixer.Channel ch, short[] chunk, int ini, int fin, float volume) {
            Generator g = InstrumentList.getInstance().get(ch.mEvent.mGen.sampleId);
            g.playSample(ch, chunk, ini, fin);
        }
    };

    public PatternPianoRoll(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    @Override
    Mixer.MixerListener GetMixerListener() { return mMixerListener; }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", sampleId);
        jsonObj.put("baseNote", baseNote);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        sampleId = jsonObj.getInt("sampleId");
        baseNote = jsonObj.getInt("baseNote");

        //mixer.iter = getIter();
    }
};
