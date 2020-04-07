package com.example.trackcomposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class InstrumentList
{
    ArrayList<Generator> mSamples = new ArrayList<Generator>();

    InstrumentList()
    {
    }

    public Generator get(int i)
    {
        return mSamples.get(i);
    }

    public int register(Generator sample, int sampleId) {
        if (sampleId == -1) {
            mSamples.add(sample);
            sample.sampleId = mSamples.size() - 1;
            return sample.sampleId;
        } else {
            sample.sampleId = sampleId;
            mSamples.set(sampleId, sample);
            return sampleId;
        }
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObjInstruments = new JSONObject();

        JSONArray jsonObjSamples = new JSONArray();
        for(int i=0;i<mSamples.size();i++)
        {
            if (mSamples.get(i) instanceof GeneratorSample) {
                JSONObject jsonObj2 = new JSONObject();
                mSamples.get(i).serializeToJson(jsonObj2);
                jsonObjSamples.put(jsonObj2);
            }
        }

        JSONArray jsonObjSynths = new JSONArray();
        for(int i=0;i<mSamples.size();i++)
        {
            if (mSamples.get(i) instanceof GeneratorSynth) {
                JSONObject jsonObj2 = new JSONObject();
                mSamples.get(i).serializeToJson(jsonObj2);
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


        HashMap<Integer, Generator> mSampleMap = new HashMap<Integer, Generator>();


        for(int i=0;i<jsonObjSynths.length();i++)
        {
            Generator gen = new GeneratorSynth();
            gen.serializeFromJson(jsonObjSynths.getJSONObject(i));
            mSampleMap.put(gen.sampleId, gen);
        }

        for(int i=0;i<jsonObjSamples.length();i++)
        {
            GeneratorSample gen = new GeneratorSample();
            gen.serializeFromJson(jsonObjSamples.getJSONObject(i));
            gen.load(gen.instrumentFilename);
            mSampleMap.put(gen.sampleId, gen);
        }

        mSamples = new ArrayList<Generator>(count);
        for(int i=0;i<mSampleMap.size();i++) {
            mSamples.add(mSampleMap.get(i));
        }
    }
}
