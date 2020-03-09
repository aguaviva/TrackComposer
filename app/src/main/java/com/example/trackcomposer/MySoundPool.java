package com.example.trackcomposer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MySoundPool {

    int buffSizeInBytes, buffSizeInShorts, buffSizeInFrames;
    int buffcount;
    int mSampleRate;
    float TwoPi = 2.0f*3.1415926f;
    AudioTrack audioTrack;
    short[] mChunk;
    int index = 0;
    int queue = 0;
    int mTick = 0;
    int mTempoInSamples = 0;

    ArrayList<short[]> samplesData = new ArrayList<short[]>();

    String TAG = "RenderAudio";

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

    MySoundPool()
    {
        for(int i = 0; i< mChannel.length; i++)
            mChannel[i] = new Channel();
    }

    public interface NextBeatListener {
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
        audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            mSampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffSizeInBytes*buffcount,
            AudioTrack.MODE_STREAM);

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
            public void onMarkerReached(AudioTrack track) {
            }

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
                renderChunk(mChunk, ini, fin);
                mTick += length;
                break;
            }
            else
            {
                int ff = ini + ticksUntilNextBeat;
                ff = Math.min(ff,fin);
                renderChunk(mChunk, ini, ff);
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

    private void renderChunk(short[] chunk, int ini, int fin)
    {
        //Log.d(TAG, "chunk " + (fin-ini));

        for(int c = 0; c< mChannel.length; c++)
        {
            if (mChannel[c].mPlaying)
            {
                if (mChannel[c].sampleId>=0)
                    playSample(mChannel[c], mChunk, ini, fin);
            }
        }
    }

    private void playSineWave(Channel channel, short[] chunk, int ini, int fin)
    {
        float t = (float) (channel.timeInSamples);
        float freqN = TwoPi * 440* channel.speed / (float)mSampleRate;

        for (int i = ini; i < fin; i += 2)
        {
            short v = (short) (channel.volume * 400 * Math.sin(freqN * t));

            channel.volume += channel.volumeSpeed;
            if (channel.volume<=0) {
                channel.mPlaying = false;
                return;
            }

            mChunk[i + 0] += v;
            mChunk[i + 1] += v;
            t++;
        }

        channel.timeInSamples =(int)t;
    }

    private void playSample(Channel channel, short[] chunk, int ini, int fin)
    {
        float t = (float) (channel.timeInSamples);
        float freqN = channel.speed;

        short data[] = samplesData.get(channel.sampleId);

        for (int i = ini; i < fin; i += 2)
        {
            int idx = (int)(freqN * t);
            if (idx>=data.length/2)
            {
                channel.mPlaying = false;
                return;
            }

            mChunk[i + 0] += (short) (channel.volume * data[2*idx+0]);
            mChunk[i + 1] += (short) (channel.volume * data[2*idx+1]);
            t++;
        }

        channel.timeInSamples =(int)t;
    }


    public void play(int sampleId, int channel, float speed)
    {
        mChannel[mCurrentChannel].sampleId = sampleId;
        mChannel[mCurrentChannel].speed = speed;
        mChannel[mCurrentChannel].timeInSamples = 0;
        mChannel[mCurrentChannel].timeDurationInSamples=11000;
        mChannel[mCurrentChannel].volume = 0.8f;
        mChannel[mCurrentChannel].volumeSpeed = -0.5f;
        mChannel[mCurrentChannel].mPlaying = true;

        mCurrentChannel++;
        if (mCurrentChannel>=mChannel.length)
            mCurrentChannel = 0;
    }

    public int load(String filename, int id)
    {
        Log.v("DecodeActivity", "Loading " + filename);

        try
        {
            ArrayList<short[]> blocks = new ArrayList<short[]>();

            String LOG_TAG="JB";
            int TIMEOUT_US = 1000;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            MediaExtractor extractor = new MediaExtractor();
            try {
                extractor.setDataSource(filename);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }

            Log.d(LOG_TAG, String.format("TRACKS #: %d", extractor.getTrackCount()));
            MediaFormat format1 = extractor.getTrackFormat(0);
            String mime = format1.getString(MediaFormat.KEY_MIME);
            Log.d(LOG_TAG, String.format("MIME TYPE: %s", mime));

            MediaCodec codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format1, null /* surface */, null /* crypto */, 0 /* flags */);
            codec.start();
            ByteBuffer[] codecInputBuffers = codec.getInputBuffers();
            ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();

            int trackIndex = 0;
            extractor.selectTrack(trackIndex); // <= You must select a track. You will read samples from the media from this track!

            boolean eosReceived = false;
            while (!eosReceived) {
                int inIndex = codec.dequeueInputBuffer(1000);
                if (inIndex < 0)
                {
                    Log.d("DecodeActivity", "dequeueInputBuffer returnning "+ inIndex);
                   break;
                }
                if (inIndex >= 0) {
                    ByteBuffer buffer = codecInputBuffers[inIndex];
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        // We shouldn't stop the playback at this point, just pass the EOS
                        // flag to mDecoder, we will get it again from the
                        // dequeueOutputBuffer
                        Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                    } else {
                        codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }

                    int outIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US);
                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                            codecOutputBuffers = codec.getOutputBuffers();
                            break;

                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            MediaFormat format = codec.getOutputFormat();
                            Log.d("DecodeActivity", "New format: " + format + "  Sample rate: " + format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                            break;

                        default:
                            ByteBuffer outBuffer = codecOutputBuffers[outIndex];
                            Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + outBuffer);

                            final short[] chunk = new short[info.size/2];
                            outBuffer.asShortBuffer().get(chunk); // Read the buffer all at once
                            outBuffer.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

                            blocks.add(chunk);

                            codec.releaseOutputBuffer(outIndex, false);
                            break;
                    }

                    // All decoded frames have been rendered, we can stop playing now
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        break;
                    }
                }
            }

            codec.stop();
            codec.release();
            extractor.release();

            int sampleSize = 0;
            {
                for (int i = 0; i < blocks.size(); i++)
                    sampleSize += blocks.get(i).length;
            }

            int index = 0;
            short[] sample = new short[sampleSize];
            samplesData.add(sample);
            {
                for (int i = 0; i < blocks.size(); i++) {
                    short[] chunk = blocks.get(i);
                    for (int b = 0; b < chunk.length; b++) {
                        sample[index++] = chunk[b];
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return samplesData.size()-1;
    }
}
