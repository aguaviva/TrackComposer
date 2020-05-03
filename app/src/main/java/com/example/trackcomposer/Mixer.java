package com.example.trackcomposer;

import android.util.Log;

class Mixer {
    static class Channel {
        Event mEvent;
        int timeInSamples=0;
        int durationInSamples=0;
        boolean mPlaying = false;
        float speed;
        float volume;
    };
    Mixer mParent;

    Channel[] mChannel = new Channel[100];
    int mTempoInSamples = 44100/4;
    public int mTime = 0, mNextTime = 0;

    boolean mStillNotes = false;
    boolean mStillPlaying = false;

    private SortedListOfNotes.State iter = null;

    InstrumentList mInstrumentList;

    Mixer(Mixer parent, InstrumentList instrumentList)
    {
        mParent = parent;
        mInstrumentList = instrumentList;
        for(int i = 0; i< mChannel.length; i++)
            mChannel[i] = new Channel();

        mTime = 0;
        mNextTime = 0;
    }

    public void SetState(SortedListOfNotes.State state)
    {
        iter = state;
    }

    public void play(Event event, int channel, float speed, float volume)
    {
        mChannel[channel].mEvent = event;
        mChannel[channel].speed = speed;
        mChannel[channel].timeInSamples = 0;
        mChannel[channel].durationInSamples = 0;
        mChannel[channel].volume = volume;
        mChannel[channel].mPlaying = true;

        //mParent.play(int sampleId, int channel, 1, 1);
    }

    private boolean sequencer()
    {
        if (iter==null)
            return false;

        if (mNextTime <= mTime) {

            int notes = iter.getNotesCount();
            if (notes<=0) {
                mNextTime = mTime;
                return false;
            }

            Event event = null;
            for (int i = 0; i < notes; i++) {
                event = iter.GetNote();
                Channel ch = mChannel[event.channel];

                ch.mEvent = event;
                ch.timeInSamples = mTime - (int)(event.time * mTempoInSamples);
                ch.durationInSamples = (int)(event.durantion * mTempoInSamples);
                ch.volume = 1;
                ch.mPlaying = true;

                if (mParent==null)
                {
                    Log.i("TC", String.format("time %f %d",((float)mTime/(float)mTempoInSamples), event.channel));
                }
                else
                {
                    Log.i("TC", String.format("  - time %f %d",(float)mTime/(float)mTempoInSamples, event.channel));
                }

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

            // render until next event, if no more events render the full chunk
            int deltaTime = (mNextTime - mTime);
            if (deltaTime>0) {
                mid = Math.min(ini + 2 * deltaTime, fin);
            }

            //mStillPlaying = render(chunk, ini, mid, volume);
            if (mMixerListener!=null){

                mMixerListener.PlayBeat(null, chunk, ini, fin, volume);
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

        Event event = iter.GetNote();
        if (event!=null) {
            mNextTime = (int) (event.time * mTempoInSamples);
        }
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
