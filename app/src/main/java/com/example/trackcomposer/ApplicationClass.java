package com.example.trackcomposer;

import android.app.Application;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ApplicationClass extends Application {
    public PatternMaster mPatternMaster;
    public Pattern mLastPatternAdded;
    public File extStoreDir;
    private boolean mPlaying = false;
    private int count = 0;

    MySoundPool soundPool;
    InstrumentList instrumentList;
    Mixer mixer;
    void Init()
    {
        instrumentList = new InstrumentList();
        mixer = new Mixer(instrumentList);
        soundPool = new MySoundPool();
        soundPool.init(44100, new MySoundPool.NextBeatListener()
        {
            @Override
            public void renderChunk(short[] chunk, int ini, int fin)
            {
                mixer.renderChunk(chunk, ini, fin);
            }

            @Override
            public void beat() {
                if (mPlaying) {
                    mPatternMaster.PlayBeat(mixer, count);
                    count++;
                }
            }
        });

        extStoreDir = new File(Environment.getExternalStorageDirectory() + "/TrackComposer");

        mPatternMaster = new PatternMaster("songy",extStoreDir+"/songy",16,16) ;
    }

    int time = 0;
    public int GetTime()
    {
        return time;
    }

    void PlayPause()
    {
        mPlaying=!mPlaying;
    }

    public void Load(String filename)
    {
        try {
            FileInputStream fileInputStream = new FileInputStream (new File(extStoreDir+filename+".json"));
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

            FileOutputStream fileOutputStream = new FileOutputStream (new File(extStoreDir+filename+".json"), false);
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