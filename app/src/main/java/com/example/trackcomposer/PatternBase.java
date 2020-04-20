package com.example.trackcomposer;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PatternBase {
    String type, name, fileName;
    int channels;
    float length;
    private SortedListOfNotes mNotes = new SortedListOfNotes();

    public PatternBase(String name, String fileName, int channels, float length)
    {
        this.name=name;
        this.fileName = fileName;
        this.channels = channels;
        this.length = length;
    }

    String GetName() { return name;}
    float GetLength() { return length; }
    int GetChannelCount() { return channels; }
    SortedListOfNotes.Note GetNoteByIndex(int index) { return mNotes.GetNoteByIndex(index); }
    GeneratorInfo Get(int channel, int time)
    {
        int numNotes = mNotes.SetTime(time);
        for(int i=0;i<numNotes;i++)
        {
            SortedListOfNotes.Note note = mNotes.Get(i);
            if (note.channel == channel)
                return note.mGen;
        }

        return null;
    }

    void Set(int channel, int pos, GeneratorInfo hit)
    {
        mNotes.SetTime(pos);

        if (hit!=null)
        {
            SortedListOfNotes.Note note = new SortedListOfNotes.Note();
            note.time = pos;
            note.channel = channel;
            note.mGen = hit;
            mNotes.Set(note);
        }
        else
        {
            mNotes.Clear(channel);
        };
    }

    public interface BeatListener { public void beat(float currentBeat); }
    BeatListener mBeatListener;
    public void SetBeatListener(BeatListener beatListener) { mBeatListener = beatListener;}
    public void CallBeatListener(float currentBeat)
    {
        if (mBeatListener!=null)
            mBeatListener.beat(currentBeat);
    }

    void PlayBeat(Mixer sp, float beat, float volume)
    {
        beat = beat;// % length;

        int noteCount = mNotes.SetTime(beat);

        CallBeatListener(beat);

        for (int c = 0; c < noteCount; c++) {
            SortedListOfNotes.Note note = mNotes.Get(c);
            if (note != null) {
                Play(sp, note.channel, volume);
            }
        }
    }

    public void Play(Mixer sp, int channel, float volume)
    {

    }

    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("name", name);
        jsonObj.put("length", length);
        jsonObj.put("channels", channels);

        mNotes.serializeToJson(jsonObj);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        name = jsonObj.getString("name");
        length = jsonObj.getInt("length");
        channels = jsonObj.getInt("channels");

        mNotes = new SortedListOfNotes();
        mNotes.serializeFromJson(jsonObj);
        //ScaleTime(16);
    }

    void ScaleTime(int multiplier)
    {
        mNotes.ScaleTime(multiplier);
        length *=multiplier;
    }
}
