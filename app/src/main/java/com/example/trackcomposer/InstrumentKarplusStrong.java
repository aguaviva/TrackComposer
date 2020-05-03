package com.example.trackcomposer;

public class InstrumentKarplusStrong extends InstrumentBase {

    class Channel
    {
        int mNote = 0;
        int mTimeInSamples = 0;
        int mVolume = 0;
        float mSpeed = 0;

        float [] delayLine;

        int startDelay, endDelay;

        Channel()
        {
            delayLine = new float[44100]; //TODO: compute a better size
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
    };

    Channel[] mChannels = new Channel[10];

    InstrumentKarplusStrong()
    {
        SetSampleRate(44100);
        instrumentName = "GeneratorKarplusStrong";

        for(int c=0;c<mChannels.length;c++) {
            mChannels[c] = new Channel();
        }
    }

    @Override
    public void playSample(int note, float speed) {

        int channelId = GetAvailableChannel();

        Channel channels = mChannels[channelId];
        channels.mNote = note;
        channels.mTimeInSamples = 0;
        channels.mSpeed = speed;
        channels.pluck((int)(44100.0f / (speed*110.0f)));
    }

    @Override
    public void playSample(Mixer.Channel foo, short[] chunk, int ini, int fin) {

        for(int c=0;c<mChannels.length;c++) {

            Channel channel = mChannels[c];
            if (mPlayingChannels[c] == false)
                continue;

            for (int i = ini; i < fin; i += 2) {

                short v = (short) (10000 * channel.SynthStep());

                chunk[i + 0] += v;
                chunk[i + 1] += v;

                if (timeInSamples > 44100.0f / 1.0f) {
                    StopChannel(i);
                    break;
                }

                channel.mTimeInSamples++;
            }
        }
    }
}
