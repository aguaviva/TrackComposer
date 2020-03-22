package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class GeneratorSynth extends Generator {
    float TwoPi = 2.0f*3.1415926f;
    int mSampleRate = 44100;

    float mTremoloFreq=0, mTremoloAmplitude = 0.0f;
    float mVibratoFreq=0, mVibratoAmplitude = 0.0f;

    public void setTremoloFreq(float freq)
    {
        mTremoloFreq = freq;
    }

    public void setTremoloAmplitude(float amplitude)    { mTremoloAmplitude = amplitude; }

    public void setVibratoFreq(float freq)
    {
        mVibratoFreq = freq;
    }

    public void setVibratoAmplitude(float amplitude)
    {
        mVibratoAmplitude = amplitude;
    }


    @Override
    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin)
    {
        float t = channel.timeInSamples * channel.speed;
        float freqN = (baseNoteFreq * TwoPi)  / (float)mSampleRate;

        float effectsTime = channel.timeInSamples;

        float mTremoloFreqN = (mTremoloFreq * TwoPi)  / (float)mSampleRate;
        float mVibratoFreqN = (mVibratoFreq * TwoPi)  / (float)mSampleRate;

        for (int i = ini; i < fin; i += 2)
        {
            // tremolo
            float tremolo = (1.0f + mTremoloAmplitude * (float)Math.abs(Math.sin(mTremoloFreqN * effectsTime)));
            float vibrato = mVibratoAmplitude * (float)(Math.sin(mVibratoFreqN * effectsTime)/TwoPi);

            short v = (short) (tremolo * channel.volume * 6000 * (Math.sin(freqN * t + vibrato)));

            channel.volume += channel.volumeSpeed;
            if (channel.volume<=0) {
                channel.mPlaying = false;
                return;
            }

            chunk[i + 0] += v;
            chunk[i + 1] += v;

            t+=channel.speed;
            effectsTime+=1;
            channel.timeInSamples++;
        }
    }

    @Override
    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        //save tremolo data
        {
            JSONObject jsonObj2 = new JSONObject();
            jsonObj2.put("freq", mTremoloFreq);
            jsonObj2.put("amplitude", mTremoloAmplitude);
            jsonObj.put("tremolo", jsonObj2);
        }

        //save vibrato data
        {
            JSONObject jsonObj2 = new JSONObject();
            jsonObj2.put("freq", mVibratoFreq);
            jsonObj2.put("amplitude", mVibratoAmplitude);
            jsonObj.put("vibrato", jsonObj2);
        }
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        if (jsonObj.has("tremolo"))
        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("tremolo");
            mTremoloFreq = (float)jsonObj2.getDouble("freq");
            mTremoloAmplitude = (float)jsonObj2.getDouble("amplitude");
        }

        if (jsonObj.has("vibrato"))
        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("vibrato");
            mVibratoFreq = (float)jsonObj2.getDouble("freq");
            mVibratoAmplitude = (float)jsonObj2.getDouble("amplitude");
        }

    }
}