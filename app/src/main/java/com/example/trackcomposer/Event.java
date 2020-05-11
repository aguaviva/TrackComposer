package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
    float mTime;
    float mDuration;
    int mChannel;
    int mId;

    void serializeToJson(JSONObject json) throws JSONException
    {
        json.put("time", mTime);
        json.put("channel", mChannel);
        json.put("durantion", mDuration);
        json.put("sampleId", mId);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        mTime = jsonObj.getInt("time");
        mChannel = jsonObj.getInt("channel");
        mDuration = jsonObj.getInt("durantion");
        mId = jsonObj.getInt("sampleId");
    }
}
