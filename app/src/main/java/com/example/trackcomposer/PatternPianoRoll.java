package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

class PatternPianoRoll extends PatternBase
{
    int sampleId = -1;
    int baseNote = 40; //c4 - 261.6256

    Mixer mixer = new Mixer(InstrumentList.getInstance( ));

    public PatternPianoRoll(String name, String filename, int channels, int length)
    {
        super(name, filename, channels, length);

    }

    @Override
    void PlayBeat(short[] chunk, int ini, int fin, float volume)
    {
        CallBeatListener(iter.mTime);

        while(ini<fin) {

            // hit notes
            if (iter.mNextTime <= iter.mTime) {

                int notes = iter.getNotesCount();
                if (notes<=0) {
                    return;
                }

                for (int i = 0; i < notes; i++) {
                    Event event = iter.GetNote();

                    mixer.play(event.mGen.sampleId, event.channel, Misc.GetFrequency(event.channel), volume);

                    iter.nextNote();
                }

                float time = iter.GetTimeOfNextNote();
                iter.mNextTime = (int)(time * (44100/4));
            }

            int deltaTime = (iter.mNextTime - iter.mTime);
            int mid = Math.min(ini + 2*deltaTime, fin);

            mixer.renderChunk(chunk, ini, mid);

            iter.mTime += (mid-ini)/2;
            ini = mid;
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
