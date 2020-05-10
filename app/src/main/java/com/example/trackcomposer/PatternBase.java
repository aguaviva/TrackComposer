package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternBase {
    String type, name, fileName;
    int channels;
    private SortedListOfNotes mNotes = new SortedListOfNotes();

    public PatternBase(String name, String fileName, int channels, float length)
    {
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
    Mixer.MixerListener GetMixerListener() { return null; }

    public Event get(int row, float time) {
        return mNotes.GetNoteBy(row, time);
    }

    public SortedListOfNotes.State getIter() {
        return mNotes.getIter();
    }
    public void sortEvents() { mNotes.sortNotes(); }

    void Set(Event note) {
        mNotes.Set(note);
    }

    void Clear(int channel, float time) {
        mNotes.Clear(channel, time);
    }

    public interface BeatListener { public void beat(float currentBeat); }
    BeatListener mBeatListener;
    public void SetBeatListener(BeatListener beatListener) { mBeatListener = beatListener;}
    public void CallBeatListener(float currentBeat)
    {
        if (mBeatListener!=null)
            mBeatListener.beat(currentBeat);
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
    }

    void ScaleTime(int multiplier)
    {
        mNotes.ScaleTime(multiplier);
        //length *=multiplier;
    }
}
