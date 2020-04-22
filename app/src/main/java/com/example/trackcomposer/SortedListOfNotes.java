package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedListOfNotes
{
    private float mTime = -1;
    private int mIndex = 0;
    private int mNotesCount = 0;

    private List<Event> myList = new ArrayList<>();

    Event GetNoteByIndex(int index)
    {
        if (index<myList.size())
            return myList.get(index);
        return null;
    }

    public Event GetNoteBy(int row, float time)
    {
        int index = SetTimeRandom(time);
        for(int i=0;i<myList.size();i++)
        {
            Event n = myList.get(i);
            if (time>=n.time && time<(n.time + n.durantion) && row == n.channel)
                return n;
        }
        return null;
    }


    private static int findFirstOccurrence(List<Event> a, int start, int end, float key){

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
        Collections.sort(myList, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
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

    private int SetTimeRandom(float time)
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
            //myList.get(i).time*=multiplier;
            //myList.get(i).durantion*=multiplier;
        }
    }

    public int SetTime(float time)
    {
        return SetTimeRandom(time);
    }

    public Event Get(int index)
    {
        if (index>=mNotesCount)
            return null;

        return myList.get(mIndex+index);
    }

    public void Set(Event gen)
    {
        Clear(gen.channel);
        myList.add(gen);
        sortNotes();
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
            Event note = myList.get(c);
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
            Event note = new Event();
            note.time = json.getInt("time");
            note.channel = json.getInt("channel");
            note.durantion = json.getInt("durantion");
            note.mGen = new GeneratorInfo();

            note.mGen.serializeFromJson(json);
            myList.add(note);
        }
    }
}
