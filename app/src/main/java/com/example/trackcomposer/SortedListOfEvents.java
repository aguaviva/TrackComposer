package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class SortedListOfEvents
{
    public class State {
        private int mIndex = 0;
        private SortedListOfEvents mEvents;
        private boolean mEOS = true;

        public State(SortedListOfEvents sln)
        {
            mEvents =sln;
        }

        public void setTime(float time) {
            mEOS = false;
            for(int i=0;i< mEvents.size();) {
                int notes = mEvents.getEventsCount(i);
                for(int n=0;n<notes;n++) {
                    Event note = myList.get(i+n);
                    if (time >= note.mTime && time< note.mTime + note.mDuration) {
                        mIndex = i;
                        return;
                    }
                    if (time <= note.mTime) {
                        mIndex = i;
                        return;
                    }
                }
                i+= notes;
            }

            // no events, flag EndOfStream
            mEOS = true;
        }

        public int getEventCount() { return mEvents.getEventsCount(mIndex); }

        public Event GetEvent()
        {
            return mEvents.GetEventByIndex(mIndex);
        }

        public float getStartTimeOfCurrentEvent()
        {
            Event event = GetEvent();
            if (event!=null)
                return event.mTime;
            else
                return mEvents.mLength;
        }

        public boolean reachedEndOfStream()
        {
            return mEOS;
        }

        public void nextEvent()
        {
            mIndex++;
        }
    };


    protected List<Event> myList = new ArrayList<>();
    public float mLength = 0;

    public SortedListOfEvents.State getIter() {
        return new SortedListOfEvents.State(this);
    }

    public Event GetEventByIndex(int index)
    {
        if (index<myList.size())
            return myList.get(index);
        return null;
    }

    int GetNextEventIndexByTime(float time)
    {
        for(int i=0;i<myList.size();i++)
        {
            Event n = myList.get(i);
            if (time<=n.mTime)
                return i;
        }
        return -1;
    }

    public Event GetEventBy(int row, float time)
    {
        for(int i=0;i<myList.size();i++)
        {
            Event n = myList.get(i);
            if (time>=n.mTime && time<(n.mTime + n.mDuration) && row == n.mChannel)
                return n;
        }
        return null;
    }

    private static int findFirstOccurrence(List<Event> a, int start, int end, float key){

        if (a.size()==0)
            return -1;

        while(start < end){
            int mid = start + (end - start) / 2;

            if(a.get(mid).mTime >= key){
                end = mid;
            }
            else{
                start = mid + 1;
            }
        }
        return (a.get(start).mTime == key) ? start : -1;
    }

    public void sortEvents()
    {
        Collections.sort(myList, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.mTime < rhs.mTime ? -1 : (lhs.mTime > rhs.mTime) ? 1 : 0;
            }
        });
    }

    private int size()
    {
        return myList.size();
    }

    private int getEventsCount(int index)
    {
        if (myList==null)
            return 0;

        if (index>=myList.size())
            return 0;

        int notesCount = 0;
        float time = myList.get(index).mTime;
        for(int i=index;i<myList.size();i++)
        {
            if (myList.get(i).mTime !=time)
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
        Clear(gen.mChannel, gen.mTime);
        myList.add(gen);
        sortEvents();
    }

    public boolean Clear(Event event)
    {
        for(int i=0;i<myList.size();i++)
        {
            if (myList.get(i) == event) {
                myList.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean Clear(int channel, float time)
    {
        for(int i=0;i<myList.size();i++)
        {
            if (myList.get(i).mChannel ==channel && myList.get(i).mTime == time) {
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
