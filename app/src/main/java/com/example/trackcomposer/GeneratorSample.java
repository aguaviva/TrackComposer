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

public class GeneratorSample extends Generator {
    private short[] sampleData = null;

    public String instrumentFilename = "none";

    public GeneratorSample()
    {
        super();
    }

    @Override
    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin)
    {
        if (sampleData == null)
        {
            channel.mPlaying = false;
            return;
        }

        float t = channel.timeInSamples * channel.speed;

        for (int i = ini; i < fin; i += 2)
        {
            int idx = (int)(t);
            if (idx>=sampleData.length/2)
            {
                channel.mPlaying = false;
                return;
            }

            chunk[i + 0] += (short) (channel.volume * sampleData[2*idx+0]);
            chunk[i + 1] += (short) (channel.volume * sampleData[2*idx+1]);
            t+=channel.speed;
            channel.timeInSamples++;
        }
    }

    @Override
    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);
        JSONObject jsonObj2 = new JSONObject();
        jsonObj2.put("fileName", instrumentFilename);
        jsonObj2.put("baseNote", baseNote);
        jsonObj.put("sample", jsonObj2);
    }

    @Override
    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);
        JSONObject jsonObj2 = jsonObj.getJSONObject("sample");
        instrumentFilename = jsonObj2.getString("fileName");
        baseNote = jsonObj2.getInt("baseNote");
    }

    public void load(String path) {
        File f = new File(path);
        String fileName = f.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos != -1) {
            fileName = fileName.substring(0, pos);
        }

        instrumentFilename = path;
        instrumentName = fileName;

        if (loadAndDecode(path)==false)
        {
            instrumentName = "BAD " + instrumentName;
            sampleData = null;
        }
    }

    public boolean loadAndDecode(String filename)
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
                return false;
            }

            Log.d(LOG_TAG, String.format("TRACKS #: %d", extractor.getTrackCount()));
            if (extractor.getTrackCount()==0)
            {
                extractor.release();
                return false;
            }
            else {
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
                    if (inIndex < 0) {
                        Log.d("DecodeActivity", "dequeueInputBuffer returnning " + inIndex);
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
            sampleData = new short[sampleSize];
            {
                for (int i = 0; i < blocks.size(); i++) {
                    short[] chunk = blocks.get(i);
                    for (int b = 0; b < chunk.length; b++) {
                        sampleData[index++] = chunk[b];
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


}
