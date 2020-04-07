package com.example.trackcomposer;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WidgetNoteTransposer {

    interface Listener
    {
        public String update(int inc);
    };

    static View AddUpAndDownKey(Activity act, String initialText, final Listener widgetListener)
    {
        View noteControls = act.getLayoutInflater().inflate(R.layout.note_controls, null);

        final TextView textView = (TextView) noteControls.findViewById(R.id.textView);

        textView.setText(initialText);

        Button.OnClickListener buttonListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.buttonUP:
                        textView.setText(widgetListener.update(1));
                        break;
                    case R.id.buttonDown:
                        textView.setText(widgetListener.update(-1));
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
