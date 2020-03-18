package com.example.trackcomposer;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

public class WidgetNoteTransposer {
    static View AddUpAndDownKey(Activity act, final View patternView, final PatternNote patternNote)
    {
        View noteControls = act.getLayoutInflater().inflate(R.layout.note_controls, null);

        Button.OnClickListener buttonListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.buttonUP:
                        patternNote.baseNote++;
                        patternView.invalidate();
                        break;
                    case R.id.buttonDown:
                        if (patternNote.baseNote > 0)
                            patternNote.baseNote--;
                        patternView.invalidate();
                        break;
                }
            }
        };

        // buttonUP
        Button buttonUP = (Button) noteControls.findViewById(R.id.buttonUP);
        buttonUP.setOnClickListener(buttonListener);

        // buttonDown
        Button buttonDown = (Button) noteControls.findViewById(R.id.buttonDown);
        buttonDown.setOnClickListener(buttonListener);

        return noteControls;
    }
}
