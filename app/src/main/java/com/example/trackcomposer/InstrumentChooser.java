package com.example.trackcomposer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class InstrumentChooser
{
    // file selection event handling
    //
    public interface InstrumentChooserListener {
        void GetSelectedInstrumentId(Generator generator);
    }

    public InstrumentChooser(final Activity activity, final InstrumentList instruments, final int currentId, final InstrumentChooserListener instrumentChooserListener)
    {
        final Dialog dialogChooseGenerator = new Dialog(activity);
        dialogChooseGenerator.setContentView(R.layout.choose_generator);
        dialogChooseGenerator.setTitle("Name your pattern");

        // Create Synth
        Button btnNewShynth = (Button) dialogChooseGenerator.findViewById(R.id.buttonNewShynth);
        btnNewShynth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChooseGenerator.dismiss();
                GeneratorSynth sample = new GeneratorSynth();
                int sampleId = instruments.register(sample, currentId);
                instrumentChooserListener.GetSelectedInstrumentId(sample);
            }
        });

        // Create Synth
        Button btnNewSample = (Button) dialogChooseGenerator.findViewById(R.id.buttonNewSample);
        btnNewSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChooseGenerator.dismiss();
                String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
                File directory = new File(extStore);

                final FileChooser filesChooser = new FileChooser(activity, directory, "Load Sample");
                filesChooser.setExtension("ogg");

                filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(final String file) { }  // we are loading the instrument on touch

                    @Override
                    public void fileTouched(final String file) {
                        GeneratorSample sample = new GeneratorSample();
                        sample.load(file);
                        int sampleId = instruments.register(sample, currentId);
                        instrumentChooserListener.GetSelectedInstrumentId(sample);
                    }
                });

                filesChooser.showDialog();
            }
        });

        dialogChooseGenerator.show();
    }
}
