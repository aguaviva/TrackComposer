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
    public class State {
        private int mIndex = 0;
        public int  mTime = 0, mNextTime = 0;
        private SortedListOfNotes iter;

        public State(SortedListOfNotes sln)
        {
            iter=sln;
        }

        public void reset()
        {
            mTime = 0;
            mNextTime = 0;
            setTime(0);
        }

        public boolean setTime(float time) {
            mIndex = iter.GetNextNoteIndexByTime(time);
            if (mIndex<0) {
                mIndex = 0;
                return false;
            }
            else {
                return true;
            }
        }

        public int getNotesCount() {
            return iter.getNotesCount(mIndex);
        }

        public Event GetNote()
        {
            return iter.GetNoteByIndex(mIndex);
        }

        public float GetTimeOfNextNote()
        {
            Event event = GetNote();
            if (event!=null)
                return event.time;
            else
                return iter.GetNoteByIndex(mIndex-1).time+iter.GetNoteByIndex(mIndex-1).durantion;
        }

        public void nextNote()
        {
            mIndex++;
        }
    };


    private int mIndex = 0;
    private int mNotesCount = 0;

    private List<Event> myList = new ArrayList<>();

    public SortedListOfNotes.State getIter() {
        return new SortedListOfNotes.State(this);
    }


    public Event GetNoteByIndex(int index)
    {
        if (index<myList.size())
            return myList.get(index);
        return null;
    }


    int GetNextNoteIndexByTime(float time)
    {
        for(int i=0;i<myList.size();i++)
        {
            Event n = myList.get(i);
            if (time<=n.time)
                return i;
        }
        return -1;
    }


    public Event GetNoteBy(int row, float time)
    {
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

    private int getNotesCount(int index)
    {
        if (myList==null)
            return 0;

        if (index>=myList.size())
            return 0;

        mNotesCount = 0;
        float time = myList.get(index).time;
        for(int i=index;i<myList.size();i++)
        {
            if (myList.get(i).time!=time)
                return mNotesCount;
            mNotesCount++;
        }
        return mNotesCount;
    }

    public void ScaleTime(int multiplier)
    {
        for(int i=0;i<myList.size();i++)
        {
            //myList.get(i).time*=multiplier;
            //myList.get(i).durantion*=multiplier;
        }
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
