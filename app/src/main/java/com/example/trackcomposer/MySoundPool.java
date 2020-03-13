package com.example.trackcomposer;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MySoundPool {

    int buffSizeInBytes, buffSizeInShorts, buffSizeInFrames;
    int buffcount;
    int mSampleRate;
    AudioTrack audioTrack;
    short[] mChunk;
    int index = 0;
    int queue = 0;
    int mTick = 0;
    int mTempoInSamples = 0;


    String TAG = "RenderAudio";

    MySoundPool()
    {
    }

    public interface NextBeatListener
    {
        public void renderChunk(short[] chunk, int ini, int fin);
        public void beat();
    }

    NextBeatListener mNextBeat;


    void init(int sampleRate, final NextBeatListener nextBeatListener) {
        mSampleRate= sampleRate;
        mNextBeat = nextBeatListener;

        float delaySecs = (160.0f*0.250f/120.0f);
        mTempoInSamples = (int)(delaySecs * mSampleRate);

        buffSizeInBytes = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        buffSizeInShorts = buffSizeInBytes /2; //because 16bits
        buffSizeInFrames = buffSizeInShorts/2; //because stereo
        Log.i(TAG, "AudioTrack.minBufferSize = " + buffSizeInBytes);
        buffcount = 4;
        mChunk = new short[buffcount * buffSizeInShorts];

        // create an audiotrack object
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, buffSizeInBytes*buffcount, AudioTrack.MODE_STREAM);

        audioTrack.setPlaybackPositionUpdateListener( new OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                queue--;
                if (queue<buffcount-1)
                    Log.d(TAG, "onPeriodicNotification..." + queue);
                // nothing to do
                while (queue<buffcount)
                    nextChunk(track);
            }

            @Override
            public void onMarkerReached(AudioTrack track) {}
        });

        while (queue<buffcount)
            nextChunk(audioTrack);

        audioTrack.setPositionNotificationPeriod(buffSizeInFrames);

        audioTrack.play();
    }

    void nextChunk(AudioTrack track)
    {
        int ini = index * buffSizeInShorts;
        int fin = (index+1) * buffSizeInShorts;

        // clear buffer
        for (int i = ini; i < fin; i ++)
            mChunk[i] = 0;

        while(ini<fin)
        {
            int ticksUntilNextBeat = 2*mTempoInSamples - mTick;
            int length = (fin-ini);
            if (ticksUntilNextBeat > length)
            {
                if (mNextBeat != null) {
                    mNextBeat.renderChunk(mChunk, ini, fin);
                }
                mTick += length;
                break;
            }
            else
            {
                int ff = ini + ticksUntilNextBeat;
                ff = Math.min(ff,fin);
                if (mNextBeat != null) {
                    mNextBeat.renderChunk(mChunk, ini, ff);
                }
                mTick += (ff-ini);  //here mTick should be 2*mTempoInSamples
                if (2*mTempoInSamples != mTick)
                {
                    Log.e(TAG, "beat:" + mTick + "   expected "+ (2*mTempoInSamples)) ;
                }

                if (mNextBeat != null) {
                    mNextBeat.beat();
                }

                mTick = 0;

                ini=ff;
            }
        }

        track.write(mChunk, index * buffSizeInShorts, buffSizeInShorts);
        index = (index+1) % buffcount;
        queue++;
    }
}
