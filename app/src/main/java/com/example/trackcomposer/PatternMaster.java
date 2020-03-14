package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

class PatternMaster extends Pattern
{
    Pattern  [] mChannels;

    public PatternMaster(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
        mChannels = new Pattern[channels];
    }

    @Override
    void PlayBeat(Mixer sp, int beat)
    {
        int pattern = beat / length;
        pattern = pattern % length;

        CallBeatListener(pattern);

        for (int c = 0; c < channels; c++) {
            int note = hits[c][pattern].hit;
            if (note>0) {
                Pattern p =mChannels[c];
                if (p!=null)
                    p.PlayBeat(sp, beat);
            }
        }
    }

    public void NewPatterns(Pattern pattern, int channel)
    {
        mChannels[channel] = pattern;
    }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeToJson(jsonObj);

        JSONObject jsonInstruments = new JSONObject();

        JSONObject jsonPatterns = new JSONObject();
        JSONObject jsonNotes = new JSONObject();
        JSONObject jsonChords = new JSONObject();
        int i=0;
        for (Pattern channel : mChannels) {
            if (channel!=null) {
                JSONObject jsonObj3 = new JSONObject();

                channel.serializeToJson(jsonObj3);

                if (channel instanceof PatternPercussion) {
                    jsonPatterns.put(String.valueOf(i), jsonObj3);
                }
                else if (channel instanceof PatternNote) {
                    jsonNotes.put(String.valueOf(i), jsonObj3);
                }
                else if (channel instanceof PatternChord) {
                    jsonChords.put(String.valueOf(i), jsonObj3);
                }
            }
            i++;
        }
        jsonObj.put("patterns", jsonPatterns);
        jsonObj.put("notes", jsonNotes);
        jsonObj.put("chords", jsonChords);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        super.serializeFromJson(jsonObj);

        mChannels = new Pattern[channels];
        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("patterns");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternPercussion("", "", 16, 16);
                mChannels[i].serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("notes");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternNote("", "", 16, 16);
                mChannels[i].serializeFromJson(jsonObj3);
            }
        }

        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("chords");
            Iterator<String> iter = jsonObj2.keys(); //This should be the iterator you want.
            while (iter.hasNext()) {
                String key = iter.next();
                int i = Integer.parseInt(key);
                JSONObject jsonObj3 = jsonObj2.getJSONObject(key);
                mChannels[i] = new PatternChord("", "", 16, 16);
                mChannels[i].serializeFromJson(jsonObj3);
            }
        }
    }

};
