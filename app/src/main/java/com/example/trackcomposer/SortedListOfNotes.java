package com.example.trackcomposer;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedListOfNotes
{
    public static class Note
    {
        int time;
        int durantion;
        int channel;
        GeneratorInfo mGen;
    };

    int mTime = -1;
    int mIndex = 0;
    int mNotesCount = 0;

    List<Note> myList = new ArrayList<>();

    Note GetNoteByIndex(int index)
    {
        if (index<myList.size())
            return myList.get(index);
        return null;
    }


    private static int findFirstOccurrence(List<Note> a, int start, int end, int key){

        if (a.size()==0)
            return -1;

        while(start < end){
            int mid = start + (end - start) / 2;

            if(a.get(mid).time >= key){
                end = mid;
            }
            else{
                start = mid + 1;
            }
        }
        return (a.get(start).time == key) ? start : -1;


    }

    private void sortNotes()
    {
        Collections.sort(myList, new Comparator<Note>() {
            @Override
            public int compare(Note lhs, Note rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.time < rhs.time ? -1 : (lhs.time > rhs.time) ? 1 : 0;
            }
        });
    }

    private int getNotesCount()
    {
        mNotesCount = 0;
        for(int i=mIndex;i<myList.size();i++)
        {
            if (myList.get(i).time==mTime)
                mNotesCount++;
        }
        return mNotesCount;
    }

    private int SetTimeNext()
    {
        mTime++;

        mIndex += mNotesCount;
        if (mIndex>=myList.size()) {
            mNotesCount = 0;
            return -1;
        }
        else if (myList.get(mIndex).time == mTime)
        {
            return getNotesCount();
        }
        else
        {
            mNotesCount = 0;
            return 0;
        }
    }

    private int SetTimeRandom(int time)
    {
        mTime = time;

        int index = findFirstOccurrence(myList, 0, myList.size()-1, time);
        if (index == -1) {
            mNotesCount = 0;
            return 0;
        }
        else {
            mIndex = index;
            return getNotesCount();
        }
    }

    public void ScaleTime(int multiplier)
    {
        for(int i=0;i<myList.size();i++)
        {
            myList.get(i).time*=multiplier;
        }
    }

    public int SetTime(int time)
    {
        return SetTimeRandom(time);
        /*
        if (time == mTime)
        {
            return mNotesCount;
        }
        else if (time == mTime+1)
        {
            return SetTimeNext();
        }
        else
        {
            return SetTimeRandom(time);
        }
         */
    }

    public Note Get(int index)
    {
        if (index>=mNotesCount)
            return null;

        return myList.get(mIndex+index);
    }

    public void Set(Note gen)
    {
        gen.time = mTime;
        Clear(gen.channel);
        myList.add(gen);
        sortNotes();

        SetTimeRandom(mTime);
    }

    public boolean Clear(int channel)
    {
        if (mNotesCount<=0)
            return false;

        for(int i=mIndex;i<mIndex + mNotesCount;i++)
        {
            if (myList.get(i).channel==channel) {
                myList.remove(i);
                break;
            }
        }

        SetTimeRandom(mTime);

        return true;
    }

    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        JSONArray jsonArr = new JSONArray();
        for (int c = 0; c < myList.size(); c++)
        {
            JSONObject json = new JSONObject();
            Note note = myList.get(c);
            json.put("time", note.time);
            json.put("channel", note.channel);
            json.put("durantion", note.durantion);
            note.mGen.serializeToJson(json);
            jsonArr.put(json);
        }
        jsonObj.put("pattern", jsonArr);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        JSONArray jArr = jsonObj.getJSONArray("pattern");
        for (int c = 0; c < jArr.length(); c++)
        {
            JSONObject json = jArr.getJSONObject(c);
            Note note = new Note();
            note.time = json.getInt("time");
            note.channel = json.getInt("channel");
            note.durantion = json.getInt("durantion");
            note.mGen = new GeneratorInfo();

            note.mGen.serializeFromJson(json);
            myList.add(note);
        }
    }
}
