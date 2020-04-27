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

class MySoundPool {

    int buffSizeInBytes, buffSizeInShorts, buffSizeInFrames;
    int buffcount;
    int mSampleRate;
    AudioTrack audioTrack;
    short[] mChunk;
    int index = 0;
    int queue = 0;
    int mTick = 0;



    String TAG = "RenderAudio";

    MySoundPool()
    {
    }

    public interface NextBeatListener
    {
        public void renderChunk(short[] chunk, int ini, int fin);
    }

    NextBeatListener mNextBeat;


    public int getSampleRate()
    {
        return mSampleRate;
    }

    public void init(int sampleRate, final NextBeatListener nextBeatListener) {
        mSampleRate= sampleRate;
        mNextBeat = nextBeatListener;

        buffSizeInBytes = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        buffSizeInShorts = buffSizeInBytes /2; //because 16bits
        buffSizeInFrames = buffSizeInShorts/2; //because stereo
        Log.i(TAG, "AudioTrack.minBufferSize = " + buffSizeInBytes);
        buffcount = 4;
        mChunk = new short[buffcount * buffSizeInShorts];


        /*
        String framesPerBuffer = AudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        String sampleRate = AudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String audioSourceUnprocessed = AudioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED);
        */

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

        audioTrack.setPositionNotificationPeriod(buffSizeInFrames);
    }

    void play()
    {
        while (queue<buffcount)
            nextChunk(audioTrack);
        audioTrack.play();
    }

    void pause()
    {
        queue = 0;
        audioTrack.pause();
    }


    void nextChunk(AudioTrack track)
    {
        int ini = index * buffSizeInShorts;
        int fin = (index+1) * buffSizeInShorts;

        // clear buffer
        for (int i = ini; i < fin; i ++)
            mChunk[i] = 0;

        if (mNextBeat != null) {
            mNextBeat.renderChunk(mChunk, ini, fin);
        }

        track.write(mChunk, index * buffSizeInShorts, buffSizeInShorts);
        index = (index+1) % buffcount;
        queue++;
    }
}
