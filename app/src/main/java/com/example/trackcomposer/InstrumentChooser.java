package com.example.trackcomposer;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class InstrumentChooser {
    ApplicationClass mAppState;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private ListView mListInstruments;

    public interface InstrumentChooserListener {
        void GetSelectedInstrumentId(InstrumentBase generator);
    }

    public InstrumentChooser(final Activity activity, final InstrumentList instruments, final int currentId, final InstrumentChooserListener instrumentChooserListener) {
        final Dialog dialogChooseGenerator = new Dialog(activity);
        dialogChooseGenerator.setContentView(R.layout.activity_instrument);
        dialogChooseGenerator.setTitle("Instrument Editor");

        mAppState = ((ApplicationClass) activity.getApplication());

        // fill list view with instruments
        //
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(dialogChooseGenerator.getContext(), android.R.layout.simple_list_item_1, arrayList);
        mListInstruments = (ListView) dialogChooseGenerator.findViewById(R.id.listInstruments);
        mListInstruments.setAdapter(adapter);
        FillList(currentId);

        //
        //
        mListInstruments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                LinearLayout editInstrument = (LinearLayout) dialogChooseGenerator.findViewById(R.id.editInstrument);
                editInstrument.removeAllViews();

                InstrumentBase gen = mAppState.instrumentList.get((int) id);
                if (gen == null) {
                    Toast.makeText(activity, "cannot find instrument " + String.valueOf(id), Toast.LENGTH_LONG).show();
                    return;
                }

                instrumentChooserListener.GetSelectedInstrumentId(gen);

                if (gen instanceof InstrumentSynthBasic) {
                    InstrumentSynthBasic genSynth = (InstrumentSynthBasic) gen;
                    View synthControls = WidgetSynthEdit.SynthEditor(activity, (InstrumentSynthBasic) gen);
                    editInstrument.addView(synthControls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                    view.setSelected(true);
                } else if (gen instanceof InstrumentSampler) {
                    InstrumentSampler genSample = (InstrumentSampler) gen;
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
                final FileChooser filesChooser = new FileChooser((Activity) activity, mAppState.extStoreDir, "Load Sample");
                filesChooser.setExtension("ogg");
                filesChooser.setFileChooserListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(final String file) {
                    }  // we are loading the instrument on touch

                    @Override
                    public void fileTouched(final String file) {
                        InstrumentSampler sample = new InstrumentSampler();
                        sample.mSample.load(file);
                        Register(sample, currentId, instrumentChooserListener);
                    }
                });

                filesChooser.showDialog();
            }
        });

        Button newSynth = (Button) dialogChooseGenerator.findViewById(R.id.newSynth);
        newSynth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GeneratorSynth sample = new GeneratorSynth();
                InstrumentKarplusStrong sample = new InstrumentKarplusStrong();
                Register(sample, currentId, instrumentChooserListener);
            }
        });

        dialogChooseGenerator.show();
    }

    void FillList(int currentId)
    {
        arrayList.clear();
        for(int i = 0; i<mAppState.instrumentList.mInstruments.size(); i++)
        {
            arrayList.add(mAppState.instrumentList.get(i).mInstrumentName);
            //adapter.add(mAppState.instrumentList.get(i).instrumentName);
        }
        adapter.notifyDataSetChanged();

        mListInstruments.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (currentId>=0)
        {
            mListInstruments.setSelection(currentId);
            mListInstruments.setItemChecked(currentId, true);
        }
    }


    void Register(InstrumentBase gen, int currentId, InstrumentChooserListener instrumentChooserListener)
    {
        //int sampleId = InstrumentList.getInstance().register(gen, -1);
        instrumentChooserListener.GetSelectedInstrumentId(gen);
        //FillList(sampleId);
    }
}
