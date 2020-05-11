package com.example.trackcomposer;

public class InstrumentKarplusStrong extends InstrumentBase {

    class Channel extends ChannelStateBase
    {
        float [] delayLine;
        int startDelay, endDelay;

        Channel()
        {
            super();
            delayLine = new float[44100]; //TODO: compute a better size
        }

        void pluck(int length)
        {
            mTimeInSamples = 0;
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
        mInstrumentName = "GeneratorKarplusStrong";

        for(int c=0;c<mChannels.length;c++) {
            mChannels[c] = new Channel();
        }
    }

    @Override
    ChannelStateBase getNewChannelState() { return new Channel(); }

    @Override
    public void playSample(int note, float freq) {

        int channelId = GetAvailableChannel();
        if (channelId>=0) {
            Channel channel = mChannels[channelId];
            channel.mNote = note;
            channel.mVolume = 1.0f;
            channel.mFreq = freq;
            channel.mTimeInSamples = 0;
            channel.pluck((int) (44100.0f / freq));
        }
    }

    @Override
    public void playSample(short[] chunk, int ini, int fin) {

        for(int c=0;c<mChannels.length;c++) {
            if (isChannelPlaying(c)==false)
                continue;

            Channel channel = mChannels[c];

            for (int i = ini; i < fin; i += 2) {

                short v = (short) (10000 * channel.SynthStep());

                chunk[i + 0] += v;
                chunk[i + 1] += v;

                if (channel.mTimeInSamples > 44100.0f / 1.0f) {
                    StopChannel(c);
                    break;
                }

                channel.mTimeInSamples++;
            }
        }
    }

    public static String GetInstrumentType() { return "InstrumentKarplusStrong"; };
}
