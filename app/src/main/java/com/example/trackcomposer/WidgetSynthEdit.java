package com.example.trackcomposer;

import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;

public class WidgetSynthEdit {

    static View SynthEditor(Activity act, final InstrumentSynthBasic synth)
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
                    case R.id.vibratoFreq:
                        synth.setVibratoFreq(progress);
                        break;
                    case R.id.vibratoAmp:
                        synth.setVibratoAmplitude(((float) progress));
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
        tremoloAmplitude.setProgress((int)(synth.mTremoloAmplitude*100));
        tremoloAmplitude.setOnSeekBarChangeListener(seekBarListener);

        SeekBar vibratoFreq = (SeekBar) synthControls.findViewById(R.id.vibratoFreq);
        vibratoFreq.setProgress((int)synth.mVibratoFreq);
        vibratoFreq.setOnSeekBarChangeListener(seekBarListener);

        SeekBar vibratoAmplitude = (SeekBar) synthControls.findViewById(R.id.vibratoAmp);
        vibratoAmplitude.setProgress((int)(synth.mVibratoAmplitude));
        vibratoAmplitude.setOnSeekBarChangeListener(seekBarListener);

        WaveformView wave = (WaveformView) synthControls.findViewById(R.id.waveformView);
        wave.SetGeneratorSample(synth);

        return synthControls;
    }
}

