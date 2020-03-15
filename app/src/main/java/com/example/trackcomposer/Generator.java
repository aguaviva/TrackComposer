package com.example.trackcomposer;

import android.app.Activity;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Generator {
    public int sampleId = -1;
    public String instrumentName = "none";

    int baseNote = 40;
    float baseNoteFreq = 0.0f;

    public Generator() {
        baseNoteFreq = Misc.GetFrequency(baseNote);
    }

    public void Play(Mixer sp, int note) {
        float freq = Misc.GetFrequency(note);
        float speed = freq / Misc.GetFrequency(baseNote);
        sp.play(sampleId, 1, speed);
    }

    public void playSample(Mixer.Channel channel, short[] chunk, int ini, int fin) {
    }

    public void serializeToJson(JSONObject jsonObj) throws JSONException {
        jsonObj.put("sampleId", sampleId);
    }

    public void serializeFromJson(JSONObject jsonObj) throws JSONException {
        sampleId = jsonObj.getInt("sampleId");
    }

    public void load(String path)
    {
    }

    private void instrumentChooser(Activity activity, final int channel)
    {
        File directory = new File(Environment.getExternalStorageDirectory() + "/TrackComposer");
        FileChooser filesChooser = new FileChooser(activity, directory, "Load");
        filesChooser.setExtension("ogg");
        filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final String file) { load(file); }
            @Override public void fileTouched(final File file) { load(file.getPath()); }
        });

        filesChooser.showDialog();
    }
}
