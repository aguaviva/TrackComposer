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

    public String instrumentFilename = "none";

    Sample mSample;

    public GeneratorSample()
    {
        super();
    }

    short GetSample(int i) { return mSample.sampleData[i]; }

    @Override
    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin)
    {
        if (mSample.sampleData == null)
        {
            channel.mPlaying = false;
            return;
        }

        float t = channel.timeInSamples * channel.speed;

        for (int i = ini; i < fin; i += 2)
        {
            int idx = (int)(t);
            if (mTracks*idx>=mSample.sampleData.length)
            {
                channel.mPlaying = false;
                return;
            }

            if (mTracks==2) {
                chunk[i + 0] += (short) (channel.volume * mSample.sampleData[2 * idx + 0]);
                chunk[i + 1] += (short) (channel.volume * mSample.sampleData[2 * idx + 1]);
            } else if (mTracks==1) {
                chunk[i + 0] += (short) (channel.volume * mSample.sampleData[idx]);
                chunk[i + 1] += (short) (channel.volume * mSample.sampleData[idx]);
            }

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
}
