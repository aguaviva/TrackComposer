package com.example.trackcomposer;

import android.app.Activity;
import android.os.Environment;

import java.io.File;

public class InstrumentChooser
{
    // file selection event handling
    //
    public interface InstrumentChooserListener {
        void GetSelectedInstrumentId(Generator generator);
    }

    public InstrumentChooser(Activity activity, final InstrumentList instruments, final int currentId, final InstrumentChooserListener instrumentChooserListener)
    {
        String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
        File directory = new File(extStore);

        final FileChooser filesChooser = new FileChooser(activity, directory);
        filesChooser.setExtension("ogg");

        filesChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final File file) {
                GeneratorSample sample = new GeneratorSample();
                sample.load(file.getPath());
                int sampleId = instruments.register(sample, currentId);
                instrumentChooserListener.GetSelectedInstrumentId(sample);
            }
        });

        filesChooser.setFileTouchedListener(new FileChooser.FileTouchedListener() {
            @Override public void fileTouched(final File file) {
                GeneratorSample sample = new GeneratorSample();
                sample.load(file.getPath());
                int sampleId = instruments.register(sample, currentId);
                instrumentChooserListener.GetSelectedInstrumentId(sample);
            }
        });

        filesChooser.showDialog();
    }
}
