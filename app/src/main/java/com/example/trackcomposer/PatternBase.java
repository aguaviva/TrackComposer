package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternBase {
    String type, name, fileName;
    int channels;
    private SortedListOfNotes mNotes = new SortedListOfNotes();

    public SortedListOfNotes.State iter;

    public PatternBase(String name, String fileName, int channels, float length)
    {
        iter = getIter();
        this.name=name;
        this.fileName = fileName;
        this.channels = channels;
        SetLength(length);
    }

    String GetName() { return name;}
    float GetLength() { return mNotes.mLength; }
    void SetLength(float length) { mNotes.mLength = length; }
    int GetChannelCount() { return channels; }
    Event GetNoteByIndex(int index) { return mNotes.GetNoteByIndex(index); }
    Event Get(int channel, float time)
    {
        return mNotes.GetNoteBy(channel, time);
    }

    void Set(Event note) {
        mNotes.Set(note);
    }

    public Event get(int row, float time) {
        return mNotes.GetNoteBy(row, time);
    }

    public SortedListOfNotes.State getIter() {
        return mNotes.getIter();
    }

    void Set(int channel, float time, GeneratorInfo hit)
    {
        if (hit!=null)
        {
            Event note = new Event();
            note.time = time;
            note.channel = channel;
            note.mGen = hit;
            mNotes.Set(note);
        }
        else
        {
            //mNotes.SetTime(time);
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

    void PlayBeat(short[] chunk, int ini, int fin, float volume)
    {
        ////
    }
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("name", name);
        jsonObj.put("channels", channels);

        mNotes.serializeToJson(jsonObj);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        name = jsonObj.getString("name");
        channels = jsonObj.getInt("channels");

        mNotes = new SortedListOfNotes();
        mNotes.serializeFromJson(jsonObj);
        //ScaleTime(16);

        iter = getIter();
    }

    void ScaleTime(int multiplier)
    {
        mNotes.ScaleTime(multiplier);
        //length *=multiplier;
    }
}
