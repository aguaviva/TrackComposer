package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class InstrumentSynthBasic extends InstrumentBase {
    float TwoPi = 2.0f * 3.1415926f;
    float mAmplitude = (float)((Short.MAX_VALUE-1)/32.0f);
    float mTremoloFreq = 0, mTremoloAmplitude = 0.0f;
    float mVibratoFreq = 0, mVibratoAmplitude = 0.0f;

    class Channel extends ChannelStateBase {

    }

    Channel[] mChannels = new Channel[10];
    EnvelopeADSR adsr = new EnvelopeADSR();

    public InstrumentSynthBasic()
    {
        super();
        SetSampleRate(44100);
        adsr.setTimings(.1f,1.0f,.1f,.8f,0.3f);
        mInstrumentName = "GeneratorSynth";

        for(int c=0;c<mChannels.length;c++) {
            mChannels[c] = new Channel();
        }
    }

    @Override
    ChannelStateBase getNewChannelState() { return new Channel(); }

    @Override
    public int getLengthInFrames() {
        return 0;//(int)(adsr.getEnvelopeDurationInSeconds()*GetSampleRate());
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
    public void playSample(int note, float frequency, float duration, float volume) {
        int channelId = GetAvailableChannel();
        if (channelId>=0) {
            Channel channel = mChannels[channelId];
            channel.mNote = note;
            channel.mTimeInSamples = 0;
            channel.mDuration = duration;
            channel.mFreq = frequency;
            channel.mVolume = volume;
        }
    }

    @Override
    public void playSample(short[] chunk, int ini, int fin)
    {
        float mTremoloFreqN = (mTremoloFreq * TwoPi)  * GetInvSampleRate();
        float mVibratoFreqN = (mVibratoFreq * TwoPi)  * GetInvSampleRate();

        for(int c=0;c<mChannels.length;c++) {
            if (isChannelPlaying(c)==false)
                continue;

            Channel channel = mChannels[c];

            float freqN = (channel.mFreq * TwoPi)  * GetInvSampleRate();
            float timeInSeconds = channel.mTimeInSamples * GetInvSampleRate();

            float duration = adsr.getEnvelopeDurationInSeconds(channel.mDuration);

            for (int i = ini; i < fin; i += 2) {
                // tremolo
                float tremolo = (1.0f + mTremoloAmplitude * (float) Math.abs(Math.sin(mTremoloFreqN * channel.mTimeInSamples)));
                float vibrato = mVibratoAmplitude * (float) (Math.sin(mVibratoFreqN * channel.mTimeInSamples) / TwoPi);

                float envelope = adsr.getEnvelope(timeInSeconds, channel.mDuration);

                float generator = (float) Math.sin(freqN * channel.mTimeInSamples + vibrato);

                generator = Math.signum(generator); //square

                short v = (short) (tremolo * channel.mVolume * envelope * mAmplitude * generator);

                if (timeInSeconds >= duration) {
                    StopChannel(c);
                    break;
                }

                chunk[i + 0] += v;
                chunk[i + 1] += v;

                channel.mTimeInSamples++;
                timeInSeconds += GetInvSampleRate();
            }
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

    public static String GetInstrumentType() { return "InstrumentSynthBasic"; };
}