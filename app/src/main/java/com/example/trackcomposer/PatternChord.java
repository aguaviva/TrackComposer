package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternChord extends PatternBase
{
    int sampleId = -1;
    int baseNote = 40;

    public PatternChord(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);
    }

    public int TrackToNote(int track)
    {
        int f = track / 3;

        int progression =  Misc.getFifthsProgression(f);

        //get root of progression
        int root = baseNote + Misc.getFifthsProgressionRootNumber(progression);

        //get choord's note
        int ch = root + Misc.getTriadChord(track % 3, Math.signum(progression)>0);

        return ch;
    }

    public String TrackToChord(int track) {
        int progression =  Misc.getFifthsProgression(track);
        return Misc.getFifthsProgressionName(progression);
    }

    @Override
    public void Play(Mixer sp, int channel, float volume)
    {
        if (sampleId>=0)
        {
            sp.play(sampleId, 0, Misc.GetFrequency(TrackToNote(channel)), volume);
        }
    }

    @Override
    void serializeToJson(JSONObject jsonObj) throws JSONException {
        super.serializeToJson(jsonObj);
        jsonObj.put("sampleId", sampleId);
        jsonObj.put("baseNote", baseNote);
    }

    @Override
    void serializeFromJson(JSONObject jsonObj) throws JSONException {
        super.serializeFromJson(jsonObj);
        sampleId = jsonObj.getInt("sampleId");
        baseNote = jsonObj.getInt("baseNote");
    }

};