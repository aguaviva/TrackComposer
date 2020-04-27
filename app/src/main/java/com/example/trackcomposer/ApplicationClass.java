package com.example.trackcomposer;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ApplicationClass extends Application {
    public HashMap<Integer, Bitmap> mPatternImgDataBase = new HashMap<Integer, Bitmap>();
    public PatternMaster mPatternMaster;
    public PatternBase mLastPatternAdded;
    public File extStoreDir;
    private boolean mPlaying = false;
    int mSampleRate = 44100;

    MySoundPool soundPool;
    InstrumentList instrumentList = InstrumentList.getInstance( );
    Mixer mixer;
    void Init()
    {
        if (mPatternMaster==null) {
            instrumentList.reset();
            mixer = new Mixer(instrumentList);
            soundPool = new MySoundPool();
            soundPool.init(mSampleRate, new MySoundPool.NextBeatListener() {
                @Override
                public void renderChunk(short[] chunk, int ini, int fin) {
                    if (mPatternMaster!=null) {
                        mPatternMaster.PlayBeat(chunk, ini, fin, 1);
                    }
                }
            });

            extStoreDir = new File(Environment.getExternalStorageDirectory() + "/TrackComposer");

            mPatternMaster = new PatternMaster("songy", extStoreDir + "/songy", 8, 256);
        }
    }

    boolean PlayPause()
    {
        mPlaying=!mPlaying;

        if (mPlaying)
            soundPool.play();
        else
            soundPool.pause();

        return mPlaying;
    }

    public void Load(String filename)
    {
        try {
            FileInputStream fileInputStream = new FileInputStream (new File(filename));
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            fileInputStream.close();
            String lines = new String(buffer, "UTF-8");

            JSONObject jsonObj = new JSONObject(lines);

            instrumentList.reset();
            mixer = new Mixer(instrumentList);
            instrumentList.serializeFromJson(jsonObj);

            mPatternMaster = new PatternMaster("caca", filename, 16,16);
            mPatternMaster.serializeFromJson(jsonObj);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Save(String filename)
    {
        try {
            JSONObject jsonObj = new JSONObject();
            instrumentList.serializeToJson(jsonObj);
            mPatternMaster.serializeToJson(jsonObj);
            String str = jsonObj.toString();

            FileOutputStream fileOutputStream = new FileOutputStream (new File(filename), false);
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}