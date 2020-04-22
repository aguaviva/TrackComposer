package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

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
            mNotes.SetTime(time);
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

    void PlayBeat(Mixer sp, float time, float volume)
    {
        int noteCount = mNotes.SetTime(time);

        CallBeatListener(time);

        for (int c = 0; c < noteCount; c++) {
            Event note = mNotes.Get(c);
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
        //length *=multiplier;
    }
}
