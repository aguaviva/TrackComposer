package com.example.trackcomposer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {

    Generator mGenerator;

    Paint black;
    Paint box;
    Paint gray;
    Paint blue;

    public WaveformView(Context context) {
        super(context);
        init(null, 0);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        black = new Paint();
        black.setColor(Color.BLACK);

        box = new Paint();
        box.setColor(Color.BLACK);
        box.setStyle(Paint.Style.FILL);

        gray = new Paint();
        gray.setColor(Color.LTGRAY);
        gray.setStyle(Paint.Style.FILL);

        blue = new Paint();
        blue.setColor(Color.rgb(200, 191, 231));
        blue.setStyle(Paint.Style.FILL);
    }

    short[] mWaveformMax = null;
    short[] mWaveformMin = null;

    void SetGeneratorSample(Generator generatorSample)
    {
        mGenerator = generatorSample;
    }

    void UpdateWave()
    {
        int reduction = 10;
        mWaveformMax = new short[getWidth()/reduction];
        mWaveformMin = new short[getWidth()/reduction];

        for(int i=0;i<mWaveformMax.length;i++) {
            mWaveformMax[i]=Short.MIN_VALUE;
            mWaveformMin[i]=Short.MAX_VALUE;
        }

        if (mGenerator==null)
        {
            return;
        }

        Mixer.Channel channel = new Mixer.Channel();
        channel.sampleId = mGenerator.sampleId;
        channel.mPlaying = true;
        channel.volume = 1.0f;
        channel.speed = 1.0f;

        short[] chunk = new short[1024];

        int t=0;
        for(int c=0;;c++) {

            for (int i = 0; i < chunk.length; i++)
            {
                chunk[i]=0;
            }

            mGenerator.playSample(channel, chunk, 0, chunk.length);

            for (int i = 0; i < chunk.length; i+=2) {

                int x = (t * mWaveformMax.length) / mGenerator.getLengthInFrames();
                t++;

                if (x>=mWaveformMax.length)
                    break;

                short sample = (short) chunk[i];
                mWaveformMax[x] = (short) Math.max(mWaveformMax[x], sample);
                mWaveformMin[x] = (short) Math.min(mWaveformMin[x], sample);
            }

            if (channel.mPlaying == false)
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mGenerator.getLengthInFrames()<0)
        {
            canvas.drawText("cannot found", getWidth()/2, getHeight()/2, black);
            return;
        }

        UpdateWave();

        int yCenter = (getHeight()/2);

        int prevX = 0;
        for(int i=0;i<mWaveformMax.length;i++)
        {
            int x = (getWidth()*i)/mWaveformMax.length;
            int yMax = mWaveformMax[i];
            int yMin = mWaveformMin[i];

            yMax =  (getHeight()*yMax)/(32000*2);
            yMin =  (getHeight()*yMin)/(32000*2);

            canvas.drawRect(prevX, yCenter-yMax, x, yCenter-yMin, black);
            prevX = x;
        }

        canvas.drawLine(0, yCenter, getWidth(), yCenter, gray);

        // i in ms

        for(int i=0;i<10;i++)
        {
            int x  = i * 100;
            canvas.drawLine(x, 0, x, getHeight(), gray);
        }
    }
}
