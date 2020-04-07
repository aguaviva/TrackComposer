package com.example.trackcomposer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

public class InstrumentChooser {
    ApplicationClass mAppState;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    ListView mListInstruments;

    // file selection event handling
    //
    public interface InstrumentChooserListener {
        void GetSelectedInstrumentId(Generator generator);
    }

    public InstrumentChooser(final Activity activity, final InstrumentList instruments, final int currentId, final InstrumentChooserListener instrumentChooserListener)
    {
        final Dialog dialogChooseGenerator = new Dialog(activity);
        dialogChooseGenerator.setContentView(R.layout.activity_instrument);
        dialogChooseGenerator.setTitle("Instrument Editor");

        mAppState = ((ApplicationClass)activity.getApplication());

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(dialogChooseGenerator.getContext(), android.R.layout.simple_list_item_1, arrayList);

        final ListView mListInstruments = (ListView) dialogChooseGenerator.findViewById(R.id.listInstruments);
        mListInstruments.setAdapter(adapter);
        for(int i=0;i<mAppState.instrumentList.mSamples.size();i++)
        {
            arrayList.add(mAppState.instrumentList.get(i).instrumentName);
        }

        mListInstruments.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (currentId>=0)
        {
            mListInstruments.setSelection(currentId);
            mListInstruments.setItemChecked(currentId, true);
        }

        adapter.notifyDataSetChanged();

        mListInstruments.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView  parent, View view, int position, long id)
            {
                LinearLayout editInstrument = (LinearLayout) dialogChooseGenerator.findViewById(R.id.editInstrument);
                editInstrument.removeAllViews();

                Generator gen = mAppState.instrumentList.get((int)id);

                instrumentChooserListener.GetSelectedInstrumentId(gen);

                if (gen instanceof GeneratorSynth)
                {
                    GeneratorSynth genSynth = (GeneratorSynth) gen;
                    View synthControls = WidgetSynthEdit.SynthEditor(activity, (GeneratorSynth) gen);
                    editInstrument.addView(synthControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                    view.setSelected(true);
                }
                else if (gen instanceof GeneratorSample)
                {
                    GeneratorSample genSample = (GeneratorSample) gen;
                    WaveformView viewSample = new WaveformView(dialogChooseGenerator.getContext());
                    viewSample.SetGeneratorSample(genSample);
                    editInstrument.addView(viewSample, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                }
            }
        });

        Button newSample = (Button) dialogChooseGenerator.findViewById(R.id.newSample);
        newSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String extStore = Environment.getExternalStorageDirectory() + "/TrackComposer";
                File directory = new File(extStore);

                final FileChooser filesChooser = new FileChooser((Activity) activity, directory, "Load Sample");
                filesChooser.setExtension("ogg");

                filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(final String file) { }  // we are loading the instrument on touch

                    @Override
                    public void fileTouched(final String file) {
                        GeneratorSample sample = new GeneratorSample();
                        sample.load(file);
                        int sampleId = mAppState.instrumentList.register(sample, currentId);

                        arrayList.add(mAppState.instrumentList.get(sampleId).instrumentName);
                        adapter.notifyDataSetChanged();
                        instrumentChooserListener.GetSelectedInstrumentId(sample);
                        mListInstruments.setItemChecked(arrayList.size()-1,true);
                    }
                });

                filesChooser.showDialog();
            }
        });

        Button newSynth = (Button) dialogChooseGenerator.findViewById(R.id.newSynth);
        newSynth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneratorSynth sample = new GeneratorSynth();
                int sampleId = instruments.register(sample, currentId);

                arrayList.add(mAppState.instrumentList.get(sampleId).instrumentName);
                adapter.notifyDataSetChanged();
                instrumentChooserListener.GetSelectedInstrumentId(sample);
                mListInstruments.setItemChecked(arrayList.size()-1,true);
            }
        });

        dialogChooseGenerator.show();
    }
}
