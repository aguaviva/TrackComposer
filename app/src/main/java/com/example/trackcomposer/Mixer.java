package com.example.trackcomposer;

public class Mixer {
    class Channel {
        int sampleId = -1;
        int timeInSamples=0;
        int timeDurationInSamples=0;
        boolean mPlaying = false;
        float speed;
        float volume;
        float volumeSpeed;
    };
    Channel[] mChannel = new Channel[20];
    int mCurrentChannel = 0;

    InstrumentList mInstrumentList = null;

    Mixer(InstrumentList instrumentList)
    {
        mInstrumentList = instrumentList;
        for(int i = 0; i< mChannel.length; i++)
            mChannel[i] = new Channel();
    }


    public void renderChunk(short[] chunk, int ini, int fin)
    {
        for(int c = 0; c< mChannel.length; c++)
        {
            Channel ch =mChannel[c];
            if (ch.mPlaying && ch.sampleId>=0)
            {
                Generator g = mInstrumentList.get(ch.sampleId);
                g.playSample(ch, chunk, ini, fin);
            }
        }
    }

    public void play(int sampleId, int channel, float freq, float volume)
    {
        float freqBase = Misc.GetFrequency(mInstrumentList.get(sampleId).baseNote);

        mChannel[mCurrentChannel].sampleId = sampleId;
        mChannel[mCurrentChannel].speed = freq / freqBase;
        mChannel[mCurrentChannel].timeInSamples = 0;
        mChannel[mCurrentChannel].timeDurationInSamples=11000;
        mChannel[mCurrentChannel].volume = volume;
        mChannel[mCurrentChannel].volumeSpeed = -0.00005f;
        mChannel[mCurrentChannel].mPlaying = true;

        mCurrentChannel++;
        if (mCurrentChannel>=mChannel.length)
            mCurrentChannel = 0;
    }

}
