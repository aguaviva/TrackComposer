package com.example.trackcomposer;

public class GeneratorKarplusStrong extends Generator {

    float [] delayLine;

    int startDelay, endDelay;

    GeneratorKarplusStrong()
    {
        SetSampleRate(44100);
        delayLine = new float[44100]; //TODO: compute a better size
        instrumentName = "GeneratorKarplusStrong";
    }

    void pluck(int length)
    {
        timeInSamples = 0;
        mPlaying = true;
        length = Math.min(length, delayLine.length);

        for(int i=0;i<length;i++)
        {
            delayLine[i] = (float)(Math.random()-0.5f);
        }

        startDelay = 0;
        endDelay = length-1;
    }

    float SynthStep()
    {
        float v1 = delayLine[startDelay];
        startDelay++;
        if (startDelay>=delayLine.length)
            startDelay = 0;

        float v2 = delayLine[startDelay];

        delayLine[endDelay] = (v1+v2) * 0.5f * 0.994f;
        endDelay++;
        if (endDelay>=delayLine.length)
            endDelay = 0;

        return v1;
    }

    @Override
    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin) {

        if (channel.timeInSamples==0)
        {
             pluck((int)(44100.0f / (channel.speed*110.0f)));
            channel.timeInSamples++;
        }

        float timeInSeconds = timeInSamples * GetInvSampleRate();

        for (int i = ini; i < fin; i += 2) {

            short v = (short)(10000 * SynthStep());

            chunk[i + 0] += v;
            chunk[i + 1] += v;

            timeInSamples++;

            if (timeInSamples>44100.0f/1.0f) {
                mPlaying = false;
                channel.mPlaying = false;
                break;
            }

            timeInSeconds += GetInvSampleRate();
        }
    }
}
