package com.example.trackcomposer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {

    GeneratorSample mGeneratorSample;

    Paint black;
    Paint box;
    Paint gray;
    Paint blue;


    public WaveformView(Context context) {
        super(context);
        init(null, 0);
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

    void SetGeneratorSample(GeneratorSample generatorSample)
    {
        mGeneratorSample = generatorSample;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mGeneratorSample.GetSampleSize()<0)
        {
            canvas.drawText("cannot found", getWidth()/2, getHeight()/2, black);
            return;
        }

        if (mWaveformMax==null)
        {
            int reduction = 10;
            mWaveformMax = new short[getWidth()/reduction];
            mWaveformMin = new short[getWidth()/reduction];

            for(int i=0;i<mWaveformMax.length;i++) {
                mWaveformMax[i]=Short.MIN_VALUE;
                mWaveformMin[i]=Short.MAX_VALUE;
            }

            for(int i=0;i<mGeneratorSample.GetSampleSize();i++) {

                int x = (i * mWaveformMax.length)/mGeneratorSample.GetSampleSize();
                short sample = mGeneratorSample.GetSample(i);
                mWaveformMax[x]=(short)Math.max(mWaveformMax[x],sample);
                mWaveformMin[x]=(short)Math.min(mWaveformMin[x],sample);
            }
        }

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
