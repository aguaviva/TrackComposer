package com.example.trackcomposer;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;

class WidgetVcrControl {

    CheckBox mPlay;
    TimeLine mTimeLine;
    TimeLineView mTimeLineView;
    ApplicationClass mAppState;

    public WidgetVcrControl(Toolbar toolbar, ApplicationClass appState)
    {
        mAppState = appState;

        final ImageButton fab = (ImageButton)toolbar.findViewById(R.id.previous);
        fab.setImageResource(android.R.drawable.ic_media_previous);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mAppState.setLoop((int) 0, (int) (1 * 16 * 16));
                mTimeLine.setTime(0);
                mAppState.mPatternMaster.setTime(0);
                mTimeLineView.invalidate();
            }
        });

        mPlay = (CheckBox)toolbar.findViewById(R.id.play);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppState.playing(mPlay.isChecked());
            }
        });

        final ImageButton fab3 = (ImageButton)toolbar.findViewById(R.id.add);
        fab3.setImageResource(android.R.drawable.ic_menu_revert);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //float ini = mTimeLineView.getSelection();
                //addPattern(mRowSelected, mTimeLine.getTime());
            }
        });
    }

    public void onResume(TimeLine timeLine, TimeLineView timeLineView){
        mPlay.setChecked(mAppState.isPlaying());
        mTimeLine = timeLine;
        mTimeLineView = timeLineView;
    }
}
