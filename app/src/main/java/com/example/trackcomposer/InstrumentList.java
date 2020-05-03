package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class InstrumentList
{
    HashMap<Integer, InstrumentBase> instruments = new HashMap<Integer, InstrumentBase>();

    private static InstrumentList singleton = new InstrumentList( );

    private InstrumentList()    {    }

    public static InstrumentList getInstance( ) {
        return singleton;
    }

    public InstrumentBase get(int i)
    {
        return instruments.get(i);
    }

    public void reset()
    {
        instruments.clear();
    }


    public void add(int trackId, InstrumentBase sample) {
            instruments.put(trackId, sample);
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObjInstruments = new JSONObject();

        JSONArray jsonObjSamples = new JSONArray();
        for(int i = 0; i< instruments.size(); i++)
        {
            if (instruments.get(i) instanceof InstrumentSampler) {
                JSONObject jsonObj2 = new JSONObject();
                instruments.get(i).serializeToJson(jsonObj2);
                jsonObjSamples.put(jsonObj2);
            }
        }

        JSONArray jsonObjSynths = new JSONArray();
        for(int i = 0; i< instruments.size(); i++)
        {
            if (instruments.get(i) instanceof InstrumentSynthBasic) {
                JSONObject jsonObj2 = new JSONObject();
                instruments.get(i).serializeToJson(jsonObj2);
                jsonObjSynths.put(jsonObj2);
            }
        }

        jsonObjInstruments.put("samples", jsonObjSamples);
        jsonObjInstruments.put("synths", jsonObjSynths);
        jsonObj.put("instruments", jsonObjInstruments);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObjInstruments = jsonObj.getJSONObject("instruments");

        JSONArray jsonObjSynths = jsonObjInstruments.getJSONArray("synths");
        JSONArray jsonObjSamples = jsonObjInstruments.getJSONArray("samples");

        int count = jsonObjSynths.length()+ jsonObjSamples.length();

        reset();

        for(int i=0;i<jsonObjSynths.length();i++)
        {
            InstrumentBase gen = new InstrumentSynthBasic();
            gen.serializeFromJson(jsonObjSynths.getJSONObject(i));
            instruments.put(gen.sampleId, gen);
        }

        for(int i=0;i<jsonObjSamples.length();i++)
        {
            InstrumentSampler gen = new InstrumentSampler();
            gen.serializeFromJson(jsonObjSamples.getJSONObject(i));
            //gen.mSample.instrumentFilename
            gen.mSample.load(gen.instrumentFilename);
            instruments.put(gen.sampleId, gen);
        }
    }
}
