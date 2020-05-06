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
        sample.sampleId = trackId;
        instruments.put(trackId, sample);
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObjInstruments = new JSONObject();

        JSONArray jsonObjSamples = new JSONArray();
        for(int i = 0; i< instruments.size(); i++)
        {
            JSONObject jsonObj2 = new JSONObject();

            InstrumentBase instBase = instruments.get(i);
            String type = "";
            if (instBase instanceof InstrumentPercussion) {
                InstrumentPercussion inst = new InstrumentPercussion();
                type = inst.GetInstrumentType();
            } else if (instBase instanceof InstrumentSynthBasic) {
                InstrumentSynthBasic inst = new InstrumentSynthBasic();
                type = inst.GetInstrumentType();
            } else if (instBase instanceof InstrumentSampler) {
                InstrumentSampler inst = new InstrumentSampler();
                type = inst.GetInstrumentType();
            } else if (instBase instanceof InstrumentKarplusStrong) {
                InstrumentKarplusStrong inst = new InstrumentKarplusStrong();
                type = inst.GetInstrumentType();
            }
            jsonObj2.put("type", type);
            instBase.serializeToJson(jsonObj2);

            jsonObjSamples.put(jsonObj2);
        }
        jsonObjInstruments.put("list", jsonObjSamples);

        jsonObj.put("instruments", jsonObjInstruments);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject jsonObjInstruments = jsonObj.getJSONObject("instruments");

        reset();

        JSONArray list = jsonObjInstruments.getJSONArray("list");
        for(int i=0;i<list.length();i++) {
            JSONObject jsonObj2 = list.getJSONObject(i);
            String type = jsonObj2.getString("type");
            InstrumentBase inst = null;
            if (type.equals(InstrumentPercussion.GetInstrumentType())) {
                inst = new InstrumentPercussion();
            } else if (type.equals(InstrumentSynthBasic.GetInstrumentType())) {
                inst = new InstrumentSynthBasic();
            } else if (type.equals(InstrumentSampler.GetInstrumentType())) {
                inst = new InstrumentSampler();
            } else if (type.equals(InstrumentKarplusStrong.GetInstrumentType())) {
                inst = new InstrumentKarplusStrong();
            }
            inst.serializeFromJson(jsonObj2);
            instruments.put(inst.sampleId, inst);
        }
    }
}
