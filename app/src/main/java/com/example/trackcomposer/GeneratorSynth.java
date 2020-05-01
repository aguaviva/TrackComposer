package com.example.trackcomposer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class GeneratorSynth extends Generator {
    float TwoPi = 2.0f * 3.1415926f;
    float mAmplitude = (float)((Short.MAX_VALUE-1)/4.0f);
    float mTremoloFreq = 0, mTremoloAmplitude = 0.0f;
    float mVibratoFreq = 0, mVibratoAmplitude = 0.0f;

    EnvelopeADSR adsr = new EnvelopeADSR();

    public GeneratorSynth()
    {
        super();
        SetSampleRate(44100);
        adsr.setTimings(.1f,1.0f,.1f,.8f,0.3f);
        adsr.setSustainTime(.5f);
    }

    @Override
    public int getLengthInFrames() {
        return (int)(adsr.getEnvelopeDurationInSeconds()*GetSampleRate());
    }

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
        float timeInSeconds = channel.timeInSamples * GetInvSampleRate();

        float freqN = (baseNoteFreq * TwoPi)  * GetInvSampleRate();
        float mTremoloFreqN = (mTremoloFreq * TwoPi)  * GetInvSampleRate();
        float mVibratoFreqN = (mVibratoFreq * TwoPi)  * GetInvSampleRate();

        for (int i = ini; i < fin; i += 2)
        {
            // tremolo
            float tremolo = (1.0f + mTremoloAmplitude * (float)Math.abs(Math.sin(mTremoloFreqN * channel.timeInSamples)));
            float vibrato = mVibratoAmplitude * (float)(Math.sin(mVibratoFreqN * channel.timeInSamples)/TwoPi);

            float envelope = adsr.getEnvelope(timeInSeconds);

            float generator = mAmplitude * (float)Math.sin(freqN * t + vibrato);

            short v = (short) (tremolo * channel.volume * envelope * generator);

            if (timeInSeconds>=1.0f) {
                channel.mPlaying = false;
                break;
            }

            chunk[i + 0] += v;
            chunk[i + 1] += v;

            t += channel.speed;
            channel.timeInSamples++;
            timeInSeconds += GetInvSampleRate();
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