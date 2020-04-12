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
    private int mTime = 0;
    private int mTimeIni = 0, mTimeFin = 0;

    MySoundPool soundPool;
    InstrumentList instrumentList;
    Mixer mixer;
    void Init()
    {
        if (mPatternMaster==null) {
            instrumentList = new InstrumentList();
            mixer = new Mixer(instrumentList);
            soundPool = new MySoundPool();
            soundPool.init(44100, new MySoundPool.NextBeatListener() {
                @Override
                public void renderChunk(short[] chunk, int ini, int fin) {
                    mixer.renderChunk(chunk, ini, fin);
                }

                @Override
                public void beat() {
                    if (mPlaying) {
                        mPatternMaster.PlayBeat(mixer, mTime, 1);
                        mTime++;

                        if (mTime>=mTimeFin)
                            mTime = mTimeIni;
                    }
                }
            });

            extStoreDir = new File(Environment.getExternalStorageDirectory() + "/TrackComposer");

            mPatternMaster = new PatternMaster("songy", extStoreDir + "/songy", 8, 16);
        }
    }

    public int getTime()
    {
        return mTime;
    }
    public void setTime(int mTime)
    {
        this.mTime = mTime;
    }

    void setLoop(int timeIni, int timeFin)
    {
        mTimeIni = timeIni;
        mTimeFin = timeFin;

        setTime(mTimeIni);
    }


    boolean PlayPause()
    {
        mPlaying=!mPlaying;
        return mPlaying;
    }

    public void Load(String filename)
    {
        try {
            FileInputStream fileInputStream = new FileInputStream (new File(filename));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String lines=bufferedReader.readLine();

            JSONObject jsonObj = new JSONObject(lines);

            instrumentList = new InstrumentList();
            mixer = new Mixer(instrumentList);
            instrumentList.serializeFromJson(jsonObj);

            mPatternMaster = new PatternMaster("caca", filename, 16,16);
            mPatternMaster.serializeFromJson(jsonObj);

            soundPool.setBmp(mPatternMaster.mBPM);
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