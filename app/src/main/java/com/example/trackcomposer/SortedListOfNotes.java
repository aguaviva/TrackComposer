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
        private SortedListOfNotes events;

        public State(SortedListOfNotes sln)
        {
            events =sln;
        }

        public void setTime(float time) {
            for(int i=0;i<events.size();) {
                int notes = events.getNotesCount(i);
                for(int n=0;n<notes;n++) {
                    Event note = myList.get(i+n);
                    if (time >= note.time  && time< note.time + note.durantion) {
                        mIndex = i;
                        return;
                    }
                    if (time <= note.time) {
                        mIndex = i;
                        return;
                    }
                }
                i+= notes;
            }

            // set index at the end
            mIndex = events.size();
        }

        public int getNotesCount() {
            return events.getNotesCount(mIndex);
        }

        public Event GetNote()
        {
            return events.GetNoteByIndex(mIndex);
        }

        public float getStartTimeOfCurrentNote()
        {
            Event event = GetNote();
            if (event!=null)
                return event.time;
            else
                return events.mLength;
        }

        public boolean AreNotesLeft()
        {
            return mIndex<events.size();
        }

        public void nextNote()
        {
            mIndex++;
        }
    };

    protected List<Event> myList = new ArrayList<>();
    public float mLength = 0;

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

    public void sortNotes()
    {
        Collections.sort(myList, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.time < rhs.time ? -1 : (lhs.time > rhs.time) ? 1 : 0;
            }
        });
    }

    private int size()
    {
        return myList.size();
    }

    private int getNotesCount(int index)
    {
        if (myList==null)
            return 0;

        if (index>=myList.size())
            return 0;

        int notesCount = 0;
        float time = myList.get(index).time;
        for(int i=index;i<myList.size();i++)
        {
            if (myList.get(i).time!=time)
                return notesCount;
            notesCount++;
        }
        return notesCount;
    }

    public void ScaleTime(int multiplier)
    {
        for(int i=0;i<myList.size();i++)
        {
            //myList.get(i).time*=multiplier;
            //myList.get(i).durantion*=multiplier;
        }
    }

    public void Set(Event gen)
    {
        Clear(gen.channel, gen.time);
        myList.add(gen);
        sortNotes();
    }

    public boolean Clear(int channel, float time)
    {
        for(int i=0;i<myList.size();i++)
        {
            if (myList.get(i).channel==channel && myList.get(i).time == time) {
                myList.remove(i);
                return true;
            }
        }
        return false;
    }

    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("length", mLength);

        JSONArray jsonArr = new JSONArray();
        for (int c = 0; c < myList.size(); c++)
        {
            JSONObject json = new JSONObject();
            Event note = myList.get(c);
            note.serializeToJson(json);
            jsonArr.put(json);
        }
        jsonObj.put("pattern", jsonArr);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        mLength = jsonObj.getInt("length");

        JSONArray jArr = jsonObj.getJSONArray("pattern");
        for (int c = 0; c < jArr.length(); c++)
        {
            JSONObject json = jArr.getJSONObject(c);
            Event note = new Event();
            note.serializeFromJson(json);
            myList.add(note);
        }
    }
}
