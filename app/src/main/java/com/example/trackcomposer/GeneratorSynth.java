package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class GeneratorSynth extends Generator {
    float TwoPi = 2.0f*3.1415926f;
    int mSampleRate = 44100;

    @Override
    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin)
    {
        float t = channel.timeInSamples * channel.speed;
        float freqN = TwoPi * 440 / (float)mSampleRate;

        for (int i = ini; i < fin; i += 2)
        {
            short v = (short) (channel.volume * 6000 * Math.sin(freqN * t));

            channel.volume += channel.volumeSpeed;
            if (channel.volume<=0) {
                channel.mPlaying = false;
                return;
            }

            chunk[i + 0] += v;
            chunk[i + 1] += v;

            t+=channel.speed;
            channel.timeInSamples++;
        }
    }

    @Override
    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);
    }
}