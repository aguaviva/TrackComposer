package com.example.trackcomposer;

public class Mixer {
    static class Channel {
        Event mEvent;
        int timeInSamples=0;
        boolean mPlaying = false;
        float speed;
        float volume;
    };
    Channel[] mChannel = new Channel[100];
    int mTempoInSamples = 44100/4;

    public int mTime = 0, mNextTime = 0;

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

    public void play(int sampleId, int channel, float freq, float volume)
    {
        float freqBase = Misc.GetFrequency(mInstrumentList.get(sampleId).baseNote);

        mChannel[channel].speed = freq / freqBase;
        mChannel[channel].timeInSamples = 0;
        mChannel[channel].volume = volume;
        mChannel[channel].mPlaying = true;
    }

    private boolean sequencer(MixerListener mixerListener)
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
                ch.volume = 1;
                ch.mPlaying = true;

                if (mixerListener!=null)
                {
                    mixerListener.AddNote(ch);
                }

                iter.nextNote();
            }

            float time = iter.GetTimeOfNextNote();
            mNextTime = (int)(time * mTempoInSamples);
        }

        return true;
    }

    void renderChunk(short[] chunk, int ini, int fin, float volume)
    {
        while(ini<fin) {

            // hit notes
            if (sequencer(mMixerListener)==false)
                return;

            int deltaTime = (mNextTime - mTime);
            int mid = Math.min(ini + 2*deltaTime, fin);

            // play channels
            for (int c = 0; c < mChannel.length; c++) {
                Channel ch = mChannel[c];
                if (ch.mEvent != null) {
                    if (mMixerListener!=null)
                    {
                        mMixerListener.PlayBeat(ch, chunk, ini, mid, ch.volume);
                        if (ch.mPlaying==false)
                        {
                            ch.mEvent = null;
                        }
                    }
                }
            }

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
        sequencer(mMixerListener);
        //iter.setTime(time);
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
