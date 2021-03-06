package com.example.trackcomposer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Sample {
    private short[] mSampleData = null;
    private String mInstrumentName = "none";
    private String mInstrumentFilename = "none";
    private int mTracks = 0;
    private int mSampleRate = 0;

    public int getLengthInFrames() {
        if (mSampleData ==null)
            return -1;
        return mSampleData.length/mTracks;
    }

    public float getLengthInSeconds() {
        return (float)getLengthInFrames() / mSampleRate;
    }

    public String getName() { return mInstrumentName; }
    public boolean isSampleValid() { return mSampleData != null; }

    public boolean load(String path) {
        File f = new File(path);
        String name = f.getName();
        int pos = name.lastIndexOf(".");
        if (pos != -1) {
            name = name.substring(0, pos);
        }
        mInstrumentName = name;

        mInstrumentFilename = path;

        if (loadAndDecode(path)==false)
        {
            mSampleData = null;
            return false;
        }

        return true;
    }

    void copyFrame(int bufferOffset, short []buffer, int sampleIndex, float factor)
    {
        if (mTracks == 2) {
            buffer[bufferOffset + 0] += (short) (factor * mSampleData[2 * sampleIndex + 0]);
            buffer[bufferOffset + 1] += (short) (factor * mSampleData[2 * sampleIndex + 1]);
        } else if (mTracks == 1) {
            buffer[bufferOffset + 0] += (short) (factor * mSampleData[sampleIndex]);
            buffer[bufferOffset + 1] += (short) (factor * mSampleData[sampleIndex]);
        }
    }

    public boolean loadAndDecode(String filename)
    {
        Log.d("DecodeActivity", "Loading " + filename);

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
                return false;
            }

            Log.d(LOG_TAG, String.format("TRACKS #: %d", extractor.getTrackCount()));
            mTracks = extractor.getTrackCount();
            if (extractor.getTrackCount()==0)
            {
                extractor.release();
                return false;
            }
            else
            {
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
                while (!eosReceived)
                {
                    int inIndex = codec.dequeueInputBuffer(1000);
                    if (inIndex < 0)
                    {
                        Log.d("DecodeActivity", "dequeueInputBuffer returnning " + inIndex);
                        break;
                    }
                    else if (inIndex >= 0)
                    {
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
                                mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                                mTracks = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                                Log.d("DecodeActivity", "New format: " + format + "  Sample rate: " + mSampleRate + " channels: " + mTracks);
                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                                break;

                            default:
                                ByteBuffer outBuffer = codecOutputBuffers[outIndex];
                                //Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + outBuffer);

                                final short[] chunk = new short[info.size / 2];
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
            }

            int sampleSize = 0;
            {
                for (int i = 0; i < blocks.size(); i++)
                    sampleSize += blocks.get(i).length;
            }

            int index = 0;
            mSampleData = new short[sampleSize];
            {
                for (int i = 0; i < blocks.size(); i++) {
                    short[] chunk = blocks.get(i);
                    for (int b = 0; b < chunk.length; b++) {
                        mSampleData[index++] = chunk[b];
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("instrumentFilename", mInstrumentFilename);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        mInstrumentFilename = jsonObj.getString("instrumentFilename");
        load(mInstrumentFilename);
    }

}
