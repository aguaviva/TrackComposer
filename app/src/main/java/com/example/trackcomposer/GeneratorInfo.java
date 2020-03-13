package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeneratorInfo {
    int hit;
    int sampleId;
    float speed;
    float volume = 0.5f;
    float volumeSpeed = -0.00005f;

    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        jsonObj.put("hit", hit);
        jsonObj.put("sampleId", sampleId);
        jsonObj.put("speed", speed);
        jsonObj.put("volume", volume);
        jsonObj.put("volumeSpeed", volumeSpeed);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        hit = jsonObj.getInt("hit");
        sampleId = jsonObj.getInt("sampleId");
        speed = (float)jsonObj.getDouble("speed");
        volume = (float)jsonObj.getDouble("volume");
        volumeSpeed = (float)jsonObj.getDouble("volumeSpeed");
    }
}
