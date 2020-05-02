package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Event {
    float time;
    float durantion;
    int channel;
    int id;

    void serializeToJson(JSONObject json) throws JSONException
    {
        json.put("time", time);
        json.put("channel", channel);
        json.put("durantion", durantion);
        json.put("sampleId", id);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        time = jsonObj.getInt("time");
        channel = jsonObj.getInt("channel");
        durantion = jsonObj.getInt("durantion");
        id = jsonObj.getInt("sampleId");
    }
}
