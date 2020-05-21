package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternBase {
    String type, mName, mFileName;
    int mChannels;
    private SortedListOfEvents mNotes = new SortedListOfEvents();

    public PatternBase(String name, String fileName, int channels, float length)
    {
        mName =name;
        mFileName = fileName;
        mChannels = channels;
        SetLength(length);
    }

    String GetName() { return mName;}
    float GetLength() { return mNotes.mLength; }
    void SetLength(float length) { mNotes.mLength = length; }
    int GetChannelCount() { return mChannels; }
    Event GetNoteByIndex(int index) { return mNotes.GetEventByIndex(index); }
    Mixer.MixerListener GetMixerListener() { return null; }

    public Event get(int row, float time) {
        return mNotes.GetEventBy(row, time);
    }

    public SortedListOfEvents.State getIter() {
        return mNotes.getIter();
    }
    public void sortEvents() { mNotes.sortEvents(); }

    void Set(Event note) {
        mNotes.Set(note);
    }

    void Clear(int channel, float time) {
        mNotes.Clear(channel, time);
    }
    void Clear(Event event) {
        mNotes.Clear(event);
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
        jsonObj.put("name", mName);
        jsonObj.put("channels", mChannels);

        mNotes.serializeToJson(jsonObj);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        mName = jsonObj.getString("name");
        mChannels = jsonObj.getInt("channels");

        mNotes = new SortedListOfEvents();
        mNotes.serializeFromJson(jsonObj);
        //ScaleTime(16);
    }

    void ScaleTime(int multiplier)
    {
        mNotes.ScaleTime(multiplier);
        //length *=multiplier;
    }
}
