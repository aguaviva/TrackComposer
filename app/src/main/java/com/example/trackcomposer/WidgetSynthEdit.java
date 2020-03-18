package com.example.trackcomposer;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class WidgetSynthEdit {

    static View SynthEditor(Activity act, final GeneratorSynth synth)
    {
        View synthControls = act.getLayoutInflater().inflate(R.layout.synth_controls, null);

        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()) {
                    case R.id.tremoloFreq:
                        synth.setTremoloFreq(progress);
                        break;
                    case R.id.tremoloAmp:
                        synth.setTremoloAmplitude(((float) progress) / 100.0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        SeekBar tremoloFreq = (SeekBar) synthControls.findViewById(R.id.tremoloFreq);
        tremoloFreq.setProgress((int)synth.mTremoloFreq);
        tremoloFreq.setOnSeekBarChangeListener(seekBarListener);

        SeekBar tremoloAmplitude = (SeekBar) synthControls.findViewById(R.id.tremoloAmp);
        tremoloAmplitude.setProgress((int)(synth.mTremoloAmplitude*100.0f));
        tremoloAmplitude.setOnSeekBarChangeListener(seekBarListener);

        return synthControls;
    }
}

