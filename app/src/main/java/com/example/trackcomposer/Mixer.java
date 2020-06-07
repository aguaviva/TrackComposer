package com.example.trackcomposer;

import android.util.Log;

class Mixer {
    int mTempoInSamples = 44100/4;
    private int mTime = 0, mNextTime = 0;

    boolean mStillNotes = false;
    boolean mStillPlaying = false;

    private SortedListOfEvents.State iter = null;

    Mixer()
    {
        mTime = 0;
        mNextTime = 0;
    }

    public void SetState(SortedListOfEvents.State state)
    {
        iter = state;
    }

    public void play(Event event, int channel, float speed, float volume)
    {
        //mParent.play(int sampleId, int channel, 1, 1);
    }

    public float getTime(Event event) {
        return ((float)mTime/(float)mTempoInSamples) - event.mTime;
    }

    public int getTimeInSamples(Event event) {
        return (int)(mTime - event.mTime*mTempoInSamples);
    }

    private boolean sequencer()
    {
        if (iter==null)
            return false;

        if (mTime >= mNextTime) {

            int notes = iter.getEventCount();
            if (notes<=0) {
                mNextTime = mTime;
                return false;
            }

            Event event = null;
            for (int i = 0; i < notes; i++) {
                event = iter.GetEvent();

                Log.i("TC", String.format("  - time %f %d",(float)mTime/(float)mTempoInSamples, event.mChannel));

                if (mMixerListener!=null)
                {
                    mMixerListener.AddNote(this, ((float)mTime/(float)mTempoInSamples) - event.mTime, event);
                }

                iter.nextEvent();
            }

            float time = iter.getStartTimeOfCurrentEvent();
            mNextTime = (int)(time * mTempoInSamples);
        }

        return true;
    }

    void renderChunk(short[] chunk, int ini, int fin, float volume)
    {
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

                mMixerListener.PlayBeat(chunk, ini, fin, volume);
            }

            mTime += (mid-ini)/2;
            ini = mid;
        }
    }

    public void setTime(float time) {

        iter.setTime(time);
        mTime = (int)(time * mTempoInSamples);
        mNextTime = mTime;

        Event event = iter.GetEvent();
        if (event!=null) {
            int timeEvent = (int)(event.mTime * mTempoInSamples);
            if (timeEvent>=mTime)
            {
                //the event happens in the future, wait for it
                mNextTime = timeEvent;
            }
        }
    }

    public float getTime()
    {
        return ((float)mTime)/((float)mTempoInSamples);
    }


    public interface MixerListener
    {
        public void AddNote(Mixer mixer, float time, Event event);
        public void PlayBeat(short[] chunk, int ini, int fin, float volume);
    }

    MixerListener mMixerListener;

}
