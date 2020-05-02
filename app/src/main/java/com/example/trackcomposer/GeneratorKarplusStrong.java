package com.example.trackcomposer;

public class GeneratorKarplusStrong extends Generator {

    float [] delayLine;

    int startDelay, endDelay;

    GeneratorKarplusStrong()
    {
        SetSampleRate(44100);
        delayLine = new float[1024];
        instrumentName = "GeneratorKarplusStrong";
    }

    void pluck(int length)
    {
        length = Math.max(length, delayLine.length);

        for(int i=0;i<length;i++)
        {
            delayLine[i] = (float)(2.0f*Math.random()-0.5f);
        }

        startDelay = 0;
        endDelay = length;
    }

    float SynthStep()
    {
        float v1 = delayLine[startDelay];

        startDelay++;
        endDelay++;

        if (startDelay>=delayLine.length)
            startDelay = 0;

        if (endDelay>=delayLine.length)
            endDelay = 0;

        float v2 = delayLine[startDelay];

        delayLine[endDelay] = (v1+v2) * 0.5f * 0.994f;

        return v1;
    }
    
    @Override
    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin) {

        if (channel.timeInSamples==0)
        {
             pluck((int)(channel.speed*delayLine.length/4.0f));
        }

        float t = channel.timeInSamples * channel.speed;
        float timeInSeconds = channel.timeInSamples * GetInvSampleRate();

        for (int i = ini; i < fin; i += 2) {

            short v = (short)(10000 * SynthStep());

            chunk[i + 0] += v;
            chunk[i + 1] += v;

            t += channel.speed;
            channel.timeInSamples++;
            timeInSeconds += GetInvSampleRate();
        }
    }
}
