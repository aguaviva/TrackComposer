package com.example.trackcomposer;

import android.util.Log;

class Mixer {
    int mTempoInSamples = 44100/4;
    public int mTime = 0, mNextTime = 0;

    boolean mStillNotes = false;
    boolean mStillPlaying = false;

    private SortedListOfNotes.State iter = null;

    Mixer()
    {
        mTime = 0;
        mNextTime = 0;
    }

    public void SetState(SortedListOfNotes.State state)
    {
        iter = state;
    }

    public void play(Event event, int channel, float speed, float volume)
    {
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

                Log.i("TC", String.format("  - time %f %d",(float)mTime/(float)mTempoInSamples, event.channel));

                if (mMixerListener!=null)
                {
                    mMixerListener.AddNote(event);
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
        public void AddNote(Event event);
        public void PlayBeat(short[] chunk, int ini, int fin, float volume);
    }

    MixerListener mMixerListener;

}
