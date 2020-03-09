package com.example.trackcomposer;

import android.media.SoundPool;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Pattern {
    String type, name, fileName;
    int channels;
    int length;
    int[][] hits;

    public Pattern(String name, String fileName, int channels, int length)
    {
        this.name=name;
        this.fileName = fileName;
        this.channels = channels;
        this.length = length;
        hits = new int[channels][length];
    }

    String GetName() { return name;}
    int GetLength() { return length; }
    int GetChannelCount() { return channels; }
    String getChannelName(int channel) { return "none";}
    int Get(int channel, int pos) { return hits[channel][pos]; }
    void Set(int channel, int pos, int hit)
    {
        hits[channel][pos] = hit;
    }

    void PlayBeat(MySoundPool sp, int beat)
    {
        beat = beat % length;
        for (int c = 0; c < channels; c++) {
            int note = hits[c][beat];
            if (note>0) {
                Play(sp, c);
            }
        }
    }

    public void Play(MySoundPool sp, int note)
    {

    }

    void PatternToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("name", name);
        jsonObj.put("length", length);
        jsonObj.put("channels", channels);

        JSONArray jsonArrC = new JSONArray();
        for (int c = 0; c < GetChannelCount(); c++) {
            JSONArray jsonArrL = new JSONArray();
            for (int l = 0; l < GetLength(); l++) {
                jsonArrL.put(Get(c, l));
            }
            jsonArrC.put(jsonArrL);
        }
        jsonObj.put("pattern", jsonArrC);
    }

    void PatternFromJson(MySoundPool sp, JSONObject jsonObj) throws JSONException
    {
        name = jsonObj.getString("name");
        length = jsonObj.getInt("length");
        channels = jsonObj.getInt("channels");
        hits = new int[channels][length];
        JSONArray jArrC = jsonObj.getJSONArray("pattern");
        for (int c = 0; c < jArrC.length(); c++) {
            JSONArray jArrL = jArrC.getJSONArray(c);
            for (int l = 0; l < jArrL.length(); l++) {
                Set(c, l, jArrL.getInt(l));
            }
        }
    }

    public void Load(MySoundPool sp)
    {
        try {
            FileInputStream fileInputStream = new FileInputStream (new File(fileName+".json"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String lines=bufferedReader.readLine();

            JSONObject jsonObj = new JSONObject(lines);
            PatternFromJson(sp, jsonObj);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Save()
    {
        try {
            JSONObject jsonObj = new JSONObject();
            PatternToJson(jsonObj);
            String str = jsonObj.toString();

            FileOutputStream fileOutputStream = new FileOutputStream (new File(fileName+".json"), false);
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
