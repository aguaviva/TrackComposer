package com.example.trackcomposer;

public class Mixer {
    static class Channel {
        Event mEvent;
        int timeInSamples=0;
        int durationInSamples=0;
        boolean mPlaying = false;
        float speed;
        float volume;
    };
    Channel[] mChannel = new Channel[100];
    int mTempoInSamples = 44100/4;
    public int mTime = 0, mNextTime = 0;

    boolean mStillNotes = false;
    boolean mStillPlaying = false;

    SortedListOfNotes.State iter;

    InstrumentList mInstrumentList;

    Mixer(InstrumentList instrumentList)
    {
        mInstrumentList = instrumentList;
        for(int i = 0; i< mChannel.length; i++)
            mChannel[i] = new Channel();

        mTime = 0;
        mNextTime = 0;
    }

    public void play(SortedListOfNotes.State state)
    {
        iter = state;
    }

    public void play(int sampleId, int channel, float freq, float volume)
    {
        float freqBase = Misc.GetFrequency(mInstrumentList.get(sampleId).baseNote);

        mChannel[channel].speed = freq / freqBase;
        mChannel[channel].timeInSamples = 0;
        mChannel[channel].durationInSamples = 0;
        mChannel[channel].volume = volume;
        mChannel[channel].mPlaying = true;
    }

    boolean render(short[] chunk, int ini, int fin, float volume)
    {
        boolean bStillPlaying = false;
        // play channels
        for (int c = 0; c < mChannel.length; c++) {
            Channel ch = mChannel[c];
            if (ch.mEvent != null) {
                if (mMixerListener!=null)
                {
                    mMixerListener.PlayBeat(ch, chunk, ini, fin, ch.volume);
                    if (ch.mPlaying==false)
                    {
                        ch.mEvent = null;
                    }
                    else
                    {
                        bStillPlaying = true;
                    }
                }
            }
        }

        return bStillPlaying;
    }

    private boolean sequencer()
    {
        if (mNextTime <= mTime) {

            int notes = iter.getNotesCount();
            if (notes<=0) {

                return false;
            }

            for (int i = 0; i < notes; i++) {
                Event event = iter.GetNote();
                Channel ch = mChannel[event.channel];

                ch.mEvent = event;
                ch.timeInSamples = mTime - (int)(event.time * mTempoInSamples);
                ch.durationInSamples = (int)(event.durantion * mTempoInSamples);
                ch.volume = 1;
                ch.mPlaying = true;

                if (mMixerListener!=null)
                {
                    mMixerListener.AddNote(ch);
                }

                iter.nextNote();
            }

            float time = iter.GetTimeOfNextNote();
            mNextTime = (int)(time * mTempoInSamples);
        }

        return true;
    }

    public boolean IsStillPlaying()
    {
        return mStillNotes==false && mStillPlaying==false;
    }

    void renderChunk(short[] chunk, int ini, int fin, float volume)
    {
        //if (IsStillPlaying() == false)
        //    return;

        while(ini<fin) {

            int mid = fin;

            // hit notes
            mStillNotes = sequencer();

            if (mStillNotes)
            {
                // render until next event
                int deltaTime = (mNextTime - mTime);
                mid = Math.min(ini + 2*deltaTime, fin);
            }

            mStillPlaying = render(chunk, ini, mid, volume);

            mTime += (mid-ini)/2;
            ini = mid;
        }
    }

    public void setTime(float time) {

        for (int c = 0; c < mChannel.length; c++) {
            Channel ch = mChannel[c];
            ch.mEvent = null;
            ch.mPlaying=false;
        }

        iter.setTime(time);
        mTime = (int)(time * mTempoInSamples);
        mNextTime = mTime;
        sequencer();
    }

    public float getTime()
    {
        return ((float)mTime)/((float)mTempoInSamples);
    }


    public interface MixerListener
    {
        public void AddNote(Channel ch);
        public void PlayBeat(Channel ch, short[] chunk, int ini, int fin, float volume);
    }

    MixerListener mMixerListener;

}
