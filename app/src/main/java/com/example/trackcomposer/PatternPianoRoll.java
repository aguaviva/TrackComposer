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
            ch.speed = ComputeSpeed(ch.mEvent);
            ch.timeInSamples = 0;
            ch.volume = 0.5f;
            ch.mPlaying = true;
        }

        @Override
        public void PlayBeat(Mixer.Channel ch, short[] chunk, int ini, int fin, float volume) {
            Generator g = InstrumentList.getInstance().get(sampleId);
            g.playSample(ch, chunk, ini, fin);
        }
    };

    public PatternPianoRoll(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    public float ComputeSpeed(Event ev)
    {
        float freq = ChannelToFreq(ev.channel);
        float freqBase = Misc.GetFrequency(InstrumentList.getInstance().get(sampleId).baseNote);
        return freq / freqBase;
    }

    public float ChannelToFreq(int channel)
    {
        return Misc.GetFrequency(channel);
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
    }
};
