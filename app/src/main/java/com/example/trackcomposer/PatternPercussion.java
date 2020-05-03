package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class PatternPercussion extends PatternBase
{
    public int[] mChannels;

    Mixer.MixerListener mMixerListener = new Mixer.MixerListener() {
        @Override
        public void AddNote(Mixer.Channel ch){

            //float freq = Misc.GetFrequency(ch.mEvent.channel);
            //float freqBase = Misc.GetFrequency(InstrumentList.getInstance().get(ch.mEvent.mGen.sampleId).baseNote);

            ch.speed = 1;
            ch.timeInSamples = 0;
            ch.volume = 0.5f;
            ch.mPlaying = true;
        }

        @Override
        public void PlayBeat(Mixer.Channel ch, short[] chunk, int ini, int fin, float volume) {
            //Generator g = InstrumentList.getInstance().get(ch.mEvent.mGen.sampleId);
            InstrumentBase g = InstrumentList.getInstance().get(mChannels[ch.mEvent.channel]);

            g.playSample(ch, chunk, ini, fin);
        }
    };

    public PatternPercussion(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);

        mChannels = new int[channels];
        for (int i = 0; i < mChannels.length; i++) {
            mChannels[i] = -1;
        }
    }
/*
    @Override
    public void Play(Mixer sp, int note, float volume)
    {
        if (mChannels[note]>=0)
            sp.play(mChannels[note], 0, Misc.GetFrequency(40), volume);
    }
*/

    @Override
    Mixer.MixerListener GetMixerListener() { return mMixerListener; }


    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONArray jsonObj2 = new JSONArray();
        for (int sampleId : mChannels) {
            jsonObj2.put(sampleId);
        }
        jsonObj.put("sampleId", jsonObj2);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        JSONArray jsonObj2 = jsonObj.getJSONArray("sampleId");
        for (int i=0;i<mChannels.length;i++) {
            mChannels[i] = jsonObj2.getInt(i);
        }
    }
};
