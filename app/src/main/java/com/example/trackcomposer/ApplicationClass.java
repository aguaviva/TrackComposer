package com.example.trackcomposer;

import android.app.Application;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Environment;

import org.json.JSONException;

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

    void Init()
    {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new MySoundPool();
        soundPool.init(44100, new MySoundPool.NextBeatListener()
        {
            @Override
            public void beat() {
                if (mPlaying) {
                    mPatternMaster.PlayBeat(soundPool, count);
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
}